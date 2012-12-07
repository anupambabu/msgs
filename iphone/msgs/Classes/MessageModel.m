//
//  MessageModel.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "MessageModel.h"
#import "Message.h"

#import "ConversationStore.h"
#import "MessageStore.h"
#import "UserStore.h"

#import <Three20Core/Three20Core.h>
#import <Three20Core/TTCorePreprocessorMacros.h>
#import <extThree20JSON/extThree20JSON.h>

#import <Three20Network/TTGlobalNetwork.h>
#import <Three20Network/TTURLRequest.h>
#import <Three20Network/TTURLCache.h>

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation MessageModel

@synthesize messages;

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithConversation:(NSNumber*)conversationIdent {
	if (self = [super init]) {
		loadedTime = nil;
		updatedTime = nil;
		conversation = conversationIdent;
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(messages);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (BOOL)isOutdated {
	if (loadedTime == nil) {
		return YES;
	} else {
		updatedTime = [NSDate date];
		return (loadedTime - updatedTime) > 0;
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)load:(TTURLRequestCachePolicy)cachePolicy more:(BOOL)more {
	if (!self.isLoading) {
		loadedTime = [NSDate date];
		
		[super didStartLoad];
		
		[[MessageStore get] setDelegate:self];
		NSArray* rawList = [[MessageStore get] messagesForConversation:conversation];
		
		TT_RELEASE_SAFELY(messages);
		NSMutableArray* newMessages = [[NSMutableArray alloc] initWithCapacity:[rawList count]];
		
		for (NSDictionary* entry in rawList) {
			NSDictionary* sender = [[UserStore get] getByID:[entry objectForKey:@"sender"]];
			
			Message* message = [[Message alloc] init];
			message.ident = [entry objectForKey:@"id"];
			message.label = [sender objectForKey:@"name"];
			message.text = [entry objectForKey:@"body"];
			message.time = [NSDate dateWithTimeIntervalSince1970:[(NSNumber*)[entry objectForKey:@"time"] doubleValue]];
			
			[newMessages addObject:message];
			
			TT_RELEASE_SAFELY(message);
			TT_RELEASE_SAFELY(sender);
		}
		
		messages = newMessages;
		
		TT_RELEASE_SAFELY(rawList);
		
		[super didFinishLoad];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSNumber*)listeningConversation {
	return conversation;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)receivedMessage:(NSDictionary*)message {
	loadedTime = [NSDate date];
	[super didChange];
}

@end