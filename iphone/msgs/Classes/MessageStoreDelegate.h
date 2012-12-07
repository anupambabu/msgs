//
//  MessageStoreDelegate.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol MessageStoreDelegate <NSObject> 

- (NSNumber*)listeningConversation;
- (void)receivedMessage:(NSDictionary*)message;

@end
