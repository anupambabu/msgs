//
//  ConversationModel.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-27.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "ConversationModel.h"
#import "Conversation.h"

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
@implementation ConversationModel

@synthesize conversations;

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)init {
	if (self = [super init]) {
		loadedTime = nil;
		updatedTime = nil;
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(conversations);
	
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
		
		NSArray* rawList = [[ConversationStore get] list];
		
		TT_RELEASE_SAFELY(conversations);
		NSMutableArray* newConversations = [[NSMutableArray alloc] initWithCapacity:[rawList count]];
		
		for (NSDictionary* entry in rawList) {
			NSDictionary* lastMessage = [[MessageStore get] getMostRecentMessageForConversation:[entry objectForKey:@"id"]];
			NSDictionary* lastMessageSender = [[UserStore get] getByID:[lastMessage objectForKey:@"sender"]];
			
			Conversation* conversation = [[Conversation alloc] init];
			conversation.ident = [entry objectForKey:@"id"];
			conversation.label = [lastMessageSender objectForKey:@"name"];
			conversation.text = [lastMessage objectForKey:@"body"];
			conversation.time = [NSDate dateWithTimeIntervalSince1970:[(NSNumber*)[entry objectForKey:@"lastmessage"] doubleValue]];
			
			[newConversations addObject:conversation];
			
			TT_RELEASE_SAFELY(conversation);
			TT_RELEASE_SAFELY(lastMessageSender);
			TT_RELEASE_SAFELY(lastMessage);
		}
		
		conversations = newConversations;

		TT_RELEASE_SAFELY(rawList);

		[super didFinishLoad];
	}
}

@end