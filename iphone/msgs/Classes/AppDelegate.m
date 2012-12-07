//
//  msgsAppDelegate.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "AppDelegate.h"
#import "Atlas.h"

// Controllers
#import "HomeViewController.h"
#import "ContactListViewController.h"
#import "ConversationViewController.h"
#import "MessageViewController.h"

#import <Three20/Three20+Additions.h>
#import <Three20Core/TTGlobalCorePaths.h>
#import <Three20UI/TTNavigator.h>
#import <Three20UI/TTWebController.h>
#import <Three20UINavigator/TTURLMap.h>
#import <Three20UINavigator/TTURLAction.h>
#import <Three20Style/TTStyleSheet.h>
#import <extThree20CSSStyle/TTDefaultCSSStyleSheet.h>

#import <Three20Core/TTCorePreprocessorMacros.h>

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@interface AppDelegate ()
- (void)initializeAccount;
- (void)sendSocketAuthentication;
- (void)setupSocket;
- (void)showAlert:(NSString *)title;
- (void)alertIfnecessary:(NSDictionary*)message;
@end

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation AppDelegate

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)applicationDidFinishLaunching:(UIApplication *)application {
	_accountStore = [AccountStore get];	
	_messageStore = [MessageStore get];
	_contactStore = [ContactStore get];
	_conversationStore = [ConversationStore get];
	_userStore = [UserStore get];
	_eventStore = [EventStore get];
	
	_requiresAccountInitialization = ![_accountStore getAuthToken] || ![_accountStore getAuthSecret];
	_accountInitialiationCompleted = NO;
	
	[self initializeAccount];
	[self setupSocket];

	TTNavigator* navigator = [TTNavigator navigator];
	navigator.persistenceMode = TTNavigatorPersistenceModeNone;
	navigator.window = [[[UIWindow alloc] initWithFrame:TTScreenBounds()] autorelease];
	
	TTURLMap* map = navigator.URLMap;
	[map from:kAnyURLPath toViewController:[HomeViewController class]];
	[map from:kAppHomeURLPath toViewController:[HomeViewController class]];
	[map from:kAppConversationURLPath toViewController:[MessageViewController class]];
	[map from:kAppContactListURLPath toViewController:[ContactListViewController class]];
	
	if (![navigator restoreViewControllers]) {
		[navigator openURLAction:[TTURLAction actionWithURLPath:kAppHomeURLPath]];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)applicationWillEnterForeground:(UIApplication *)application {
	UIApplication* app = [UIApplication sharedApplication];
	[app clearKeepAliveTimeout];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)applicationDidEnterBackground:(UIApplication *)application {
	UIApplication* app = [UIApplication sharedApplication];
	[app clearKeepAliveTimeout];
	[app setKeepAliveTimeout:600 handler: ^{
		[_socket keepAlive];
		if (![_socket isConnected]) {
			[_socket close];
			[self setupSocket];
		}
	}];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (BOOL)navigator:(TTNavigator*)navigator shouldOpenURL:(NSURL*)URL {
	return YES;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (BOOL)application:(UIApplication*)application handleOpenURL:(NSURL*)URL {
	[[TTNavigator navigator] openURLAction:[TTURLAction actionWithURLPath:URL.absoluteString]];
	return YES;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {	
	TT_RELEASE_SAFELY(_socket);
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setupSocket {
	TT_RELEASE_SAFELY(_socket);
	
	_socket = [MessageSocket new];
	[_socket initWithHostAddress:kHostAddress andPort:kHostPort];
	[_socket setDelegate:self];
	NSError *error = nil;
	
	if (_socket == nil || ![_socket connect]) {
		if (error == nil) {
			NSLog(@"Failed creating server: Server instance is nil");
		} else {
			NSLog(@"Failed creating server: %@", error);
		}
		
		[self showAlert:@"Failed creating server"];
		return;
	} else {
		if (!_requiresAccountInitialization) {
			[self sendSocketAuthentication];
		}
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)showAlert:(NSString *)title {
	UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:title message:@"Check your networking configuration." delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	[alertView show];
	TT_RELEASE_SAFELY(alertView);
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)connectionAttemptFailed:(MessageSocket*)socket {
	if (socket == _socket) {
		[socket close];
		[self setupSocket];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)connectionTerminated:(MessageSocket*)socket {
	if (socket == _socket) {
		[socket close];
		[self setupSocket];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)alertIfnecessary:(NSDictionary*)message {
	UIApplication* application = [UIApplication sharedApplication];
	if ([application applicationState] == UIApplicationStateBackground) {
		UILocalNotification *localNotif = [[UILocalNotification alloc] init];
		if (localNotif) {
			localNotif.alertBody = [NSString stringWithFormat:NSLocalizedString(@"%@: %@", nil), [message objectForKey:@"profile"], [message objectForKey:@"message"]];
			localNotif.alertAction = NSLocalizedString(@"Read Msg", nil);
			localNotif.soundName = @"alarmsound.caf";
			localNotif.applicationIconBadgeNumber = 0;
			[application presentLocalNotificationNow:localNotif];
			TT_RELEASE_SAFELY(localNotif);
		}
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)receivedEvent:(NSDictionary*)event {
	NSString* type = [event objectForKey:@"type"];
	
	if ([type isEqual:@"message"]) {
		[_messageStore setMessage:event];
		[_eventStore setEvent:[NSNumber numberWithLongLong:[[event objectForKey:@"id"] longLongValue]] 
						 time:[NSNumber numberWithLongLong:[[event objectForKey:@"time"] longLongValue]] 
						 type:[event objectForKey:@"type"] 
					   entity:[NSNumber numberWithLongLong:[[event objectForKey:@"message"] longLongValue]] 
					   action:@""];
	} else if ([type isEqual:@"contact"]) {
		[_contactStore setContact:event];
		[_eventStore setEvent:[NSNumber numberWithLongLong:[[event objectForKey:@"id"] longLongValue]] 
						 time:[NSNumber numberWithLongLong:[[event objectForKey:@"time"] longLongValue]] 
						 type:[event objectForKey:@"type"] 
					   entity:[NSNumber numberWithLongLong:[[event objectForKey:@"contact"] longLongValue]] 
					   action:[event objectForKey:@"action"]];
	} else if ([type isEqual:@"conversation"]) {
		[_conversationStore setConversation:event];
		[_eventStore setEvent:[NSNumber numberWithLongLong:[[event objectForKey:@"id"] longLongValue]] 
						 time:[NSNumber numberWithLongLong:[[event objectForKey:@"time"] longLongValue]] 
						 type:[event objectForKey:@"type"] 
					   entity:[NSNumber numberWithLongLong:[[event objectForKey:@"conversation"] longLongValue]] 
					   action:[event objectForKey:@"action"]];
	} else if ([type isEqual:@"participant"]) {
		[_conversationStore setParticipant:event];
		[_eventStore setEvent:[NSNumber numberWithLongLong:[[event objectForKey:@"id"] longLongValue]] 
						 time:[NSNumber numberWithLongLong:[[event objectForKey:@"time"] longLongValue]] 
						 type:[event objectForKey:@"type"] 
					   entity:[NSNumber numberWithLongLong:[[event objectForKey:@"participant"] longLongValue]] 
					   action:[event objectForKey:@"action"]];
	} else if ([type isEqual:@"avatar"]) {
		[_userStore setUser:event];
		[_eventStore setEvent:[NSNumber numberWithLongLong:[[event objectForKey:@"id"] longLongValue]] 
						 time:[NSNumber numberWithLongLong:[[event objectForKey:@"time"] longLongValue]] 
						 type:[event objectForKey:@"type"] 
					   entity:[NSNumber numberWithLongLong:[[event objectForKey:@"avatar"] longLongValue]] 
					   action:[event objectForKey:@"action"]];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)receivedNetworkPacket:(NSDictionary*)message viaSocket:(MessageSocket*)socket {
	NSString* type = [message objectForKey:@"type"];
	
	if ([type isEqual:@"events"]) {
		NSArray* events = [message objectForKey:@"events"];
		if ([events count]) {
			long long latestSync = 0ll;
			NSMutableArray* eventIDs = [[NSMutableArray alloc] initWithCapacity:[events count]];
			
			for (NSDictionary* e in events) {
				long long eventTime = [[e objectForKey:@"time"] longLongValue];
				if (eventTime > latestSync) {
					latestSync = eventTime;
				}
				
				[eventIDs addObject:[e objectForKey:@"id"]];
				
				[self receivedEvent:e];
			}
			
			NSDictionary* response = [[NSDictionary alloc] initWithObjectsAndKeys:
									  @"ack", @"cmd",
									  eventIDs, @"checksum",
									  [[NSNumber numberWithLongLong:latestSync] stringValue], @"checksync",
									  [[NSNumber numberWithLongLong:[_accountStore getLastSync]] stringValue], @"lastsync",
									  nil];
			
			[socket sendNetworkJSON:response];
			
			TT_RELEASE_SAFELY(response);
			TT_RELEASE_SAFELY(eventIDs);
		}
	} else if ([type isEqual:@"checksum"]) {
		long long lastSync = [[message objectForKey:@"lastsync"] longLongValue];
		[_accountStore setLastSync:lastSync];
		NSLog(@"updating successful sync time %d", lastSync);
	} else if ([type isEqual:@"pong"]) {
		NSLog(@"received reply from server, socket still active");
	}
}

- (void)didAcceptConnectionForServer:(MessageSocket *)socket inputStream:(NSInputStream *)inStr outputStream:(NSOutputStream *)outStr {
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)initializeAccount {
	_initializer = [AccountInitializer start:self];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)sendSocketAuthentication {
	NSDictionary* auth = [[NSDictionary alloc] initWithObjectsAndKeys:
							@"connect", @"cmd",
							@"iphone", @"devicetype",
							[_accountStore getDeviceIdent], @"deviceident",
							[_accountStore getAuthToken], @"authtoken",
							[[NSNumber numberWithLongLong:[_accountStore getLastSync]] stringValue], @"lastsync",
							nil];
	
	[_socket sendNetworkJSON:auth];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)accountInitialized:(NSString*)userID userEmail:(NSString*)userEmail authToken:(NSString*)authToken authSecret:(NSString*)authSecret {
	NSLog(@"userID: %@ userEmail: %@ authToken: %@ authSecret: %@", userID, userEmail, authToken, authSecret);
	if (_requiresAccountInitialization) {
		[self sendSocketAuthentication];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)accountInitializationFailed {
	[self showAlert:@"Account Initialization Failed"];
	NSLog(@"accountInitFailure");
	TT_RELEASE_SAFELY(_initializer);
}

@end