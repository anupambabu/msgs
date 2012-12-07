//
//  MessageDataSource.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <Three20UI/TTListDataSource.h>

@class MessageModel;

@interface MessageDataSource : TTListDataSource {
	MessageModel* model;
}

- (id)initWithConversation:(NSNumber*)conversationIdent;

@end