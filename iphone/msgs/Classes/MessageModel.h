//
//  MessageModel.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <Three20Network/TTModel.h>

#import "MessageStoreDelegate.h"

@interface MessageModel : TTModel <MessageStoreDelegate> {
	NSDate* loadedTime;
	NSDate* updatedTime;
	NSNumber* conversation;
	NSArray* messages;
}

@property (nonatomic, readonly) NSArray* messages;

- (id)initWithConversation:(NSNumber*)conversationIdent;

@end