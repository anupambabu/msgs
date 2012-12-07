//
//  MessageSocketDelegate.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-06.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

@class MessageSocket;

@protocol MessageSocketDelegate <NSObject>

- (void)connectionAttemptFailed:(MessageSocket*)socket;
- (void)connectionTerminated:(MessageSocket*)socket;
- (void)receivedNetworkPacket:(NSDictionary*)message viaSocket:(MessageSocket*)socket;

@end
