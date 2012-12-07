//
//  ConversationStore.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FMDatabase.h"

@interface ConversationStore : NSObject {
	FMDatabase* fmdb;
}

+ (id)get;

- (bool)setConversation:(NSNumber*)conversation lastMessage:(NSNumber*)lastMessage;
- (bool)setConversation:(NSDictionary*)payload;

- (bool)setParticipant:(NSNumber*)participant conversation:(NSNumber*)conversation user:(NSNumber*)user lastSync:(NSNumber*)lastSync joinTime:(NSNumber*)jointime;
- (bool)setParticipant:(NSDictionary*)payload;

- (NSArray*)list;

@end
