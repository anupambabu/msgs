//
//  AccountInitializer.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "AccountInitializer.h"
#import "Atlas.h"

#import <Three20Core/Three20Core.h>
#import <Three20Core/TTCorePreprocessorMacros.h>
#import <Three20Network/TTGlobalNetwork.h>
#import <Three20Network/TTURLRequest.h>
#import <extThree20JSON/extThree20JSON.h>

@interface AccountInitializer ()

- (void)load;

@end

@implementation AccountInitializer

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(_delegate);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithDelegate:(id<AccountInitializerDelegate>)delegate {
	_delegate = delegate;
	_store = [AccountStore get];	
	[self load];
	
	return self;	
}

///////////////////////////////////////////////////////////////////////////////////////////////////
+ (id)start:(id<AccountInitializerDelegate>)delegate {
	return [[AccountInitializer alloc] initWithDelegate:delegate];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)load {
	NSString* deviceIdent = [_store getDeviceIdent];
	NSString* deviceVerifier = [_store getDeviceVerifier];
	NSString* authToken = [_store getAuthToken];
	
	NSString* url = [NSString stringWithFormat:kAuthRequestURLFormat, deviceIdent, deviceVerifier, authToken ? authToken : @""];
	
	TTURLRequest* request = [TTURLRequest requestWithURL:url delegate:self];
	TTURLJSONResponse* response = [[TTURLJSONResponse alloc] init];
	request.response = response;
	TT_RELEASE_SAFELY(response);
	
	[request send];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)requestDidFinishLoad:(TTURLRequest*)request {
	TTURLJSONResponse* response = request.response;
	TTDASSERT([response.rootObject isKindOfClass:[NSDictionary class]]);
	
	NSDictionary* info = response.rootObject;
	TTDASSERT([[info objectForKey:@"status"] isKindOfClass:[NSString class]]);
	
	NSString* status = [info objectForKey:@"status"];
	if ([status isEqualToString:@"ok"]) {
		TTDASSERT([[info objectForKey:@"user_id"] isKindOfClass:[NSString class]]);
		TTDASSERT([[info objectForKey:@"user_email"] isKindOfClass:[NSString class]]);
		TTDASSERT([[info objectForKey:@"auth_token"] isKindOfClass:[NSString class]]);
		TTDASSERT([[info objectForKey:@"auth_secret"] isKindOfClass:[NSString class]]);
		
		NSString* userID = [info objectForKey:@"user_id"];
		NSString* userEmail = [info objectForKey:@"user_email"];
		NSString* authToken = [info objectForKey:@"auth_token"];
		NSString* authSecret = [info objectForKey:@"auth_secret"];
		
		[_store setUserID:userID];
		[_store setUserEmail:userEmail];
		[_store setAuthToken:authToken];
		[_store setAuthSecret:authSecret];
		
		[_delegate accountInitialized:userID userEmail:userEmail authToken:authToken authSecret:authSecret];
	} else {
		[_delegate accountInitializationFailed];
	}
}

@end
