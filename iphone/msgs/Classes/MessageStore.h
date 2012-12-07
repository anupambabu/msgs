//
//  MessageStore.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "MessageStoreDelegate.h"

#import <Foundation/Foundation.h>
#import "FMDatabase.h"

@interface MessageStore : NSObject {
	FMDatabase* fmdb;
	id<MessageStoreDelegate> delegate;
}

+ (id)get;

- (bool)setMessage:(NSNumber*)message sender:(NSNumber*)sender conversation:(NSNumber*)conversation time:(NSNumber*)time body:(NSString*)body;
- (bool)setMessage:(NSDictionary*)payload;

- (void)setDelegate:(id<MessageStoreDelegate>)storeDelegate;

- (NSDictionary*)getByID:(NSNumber*)ident;
- (NSDictionary*)getMostRecentMessageForConversation:(NSNumber*)ident;
- (NSArray*)messagesForConversation:(NSNumber*)ident;

@end
