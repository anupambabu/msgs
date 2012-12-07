//
//  MessageSocket.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-06.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CFNetwork/CFSocketStream.h>
#import "MessageSocketDelegate.h"

@interface MessageSocket : NSObject <NSNetServiceDelegate> {
	bool connected;
	
	id<MessageSocketDelegate> delegate;
	NSString* host;
	int port;
	
	CFSocketNativeHandle connectedSocketHandle;
	NSNetService* netService;
	
	CFReadStreamRef readStream;
	bool readStreamOpen;
	NSMutableData* incomingDataBuffer;
	int packetBodySize;
	
	CFWriteStreamRef writeStream;
	bool writeStreamOpen;
	NSMutableData* outgoingDataBuffer;
}

@property(nonatomic,retain) id<MessageSocketDelegate> delegate;

- (id)initWithHostAddress:(NSString*)host andPort:(int)port;
- (id)initWithNativeSocketHandle:(CFSocketNativeHandle)nativeSocketHandle;
- (id)initWithNetService:(NSNetService*)netService;

- (BOOL)isConnected;
- (BOOL)connect;
- (void)close;
- (void)sendNetworkPacket:(NSDictionary*)packet;
- (void)sendNetworkJSON:(NSDictionary*)packet;

- (void)keepAlive;

@end
