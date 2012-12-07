//
//  MessageStore.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "MessageStore.h"
#import "Atlas.h"
#import "StringUtil.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

static MessageStore* instance = nil;

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation MessageStore

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(fmdb);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setupDB {	
	NSString* accountDBPath = [StringUtil filePathInDocumentsDirectoryForFileName:kMessageStoreDB];
	fmdb = [[FMDatabase alloc] initWithPath:accountDBPath];
	if(![fmdb open]) {
		NSLog(@"Could not open the DB for whatever reason about to remove and copy the base file...here was the error:");
		NSLog(@"DB Err %d: %@", [fmdb lastErrorCode], [fmdb lastErrorMessage]);
	}
	
	[fmdb setLogsErrors:YES];
	[fmdb executeUpdate:@"PRAGMA CACHE_SIZE=1000"];
	[fmdb executeUpdate:@"PRAGMA encoding = \"UTF-8\""];
	[fmdb executeUpdate:@"PRAGMA auto_vacuum=1"];
	[fmdb executeUpdate:@"PRAGMA synchronous=NORMAL"];
	
	[fmdb executeUpdate:@"CREATE TABLE IF NOT EXISTS message (pk INTEGER PRIMARY KEY, "
						 "id INTEGER, "
						 "sender INTEGER, "
						 "conversation INTEGER, "
						 "time INTEGER, "
						 "body VARCHAR(512))"];
	
	[fmdb executeUpdate:@"CREATE UNIQUE INDEX IF NOT EXISTS message_id ON message(id);"];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSDictionary*)resultToDict:(FMResultSet*)rs {
	return [[NSDictionary alloc] initWithObjectsAndKeys:
			[NSNumber numberWithInt:[rs intForColumn:@"id"]], @"id",
			[NSNumber numberWithInt:[rs intForColumn:@"sender"]], @"sender",
			[NSNumber numberWithInt:[rs intForColumn:@"conversation"]], @"conversation",
			[NSNumber numberWithInt:[rs intForColumn:@"time"]], @"time",
			[rs stringForColumn:@"body"], @"body",
			nil];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)init {
	if (self = [super init]) {	
		[self setupDB];
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
+ (id)get {
	@synchronized(self) {
		if (instance == nil) {
			instance = [[self alloc] init];
		}
	}
	
	return instance;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setDelegate:(id<MessageStoreDelegate>)storeDelegate {
	delegate = storeDelegate;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setMessage:(NSNumber*)message sender:(NSNumber*)sender conversation:(NSNumber*)conversation time:(NSNumber*)time body:(NSString*)body {
	bool res = [fmdb executeUpdate:@"INSERT OR REPLACE INTO message(id, sender, conversation, time, body) VALUES(?, ?, ?, ?, ?)", 
				message, sender, conversation, time, body];
	
	if (delegate != nil && [[delegate listeningConversation] isEqual:conversation]) {
		[delegate receivedMessage:[self getByID:message]];
	}
	
	return res;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setMessage:(NSDictionary*)payload {
	return [self setMessage:[NSNumber numberWithLongLong:[[payload objectForKey:@"message"] longLongValue]] 
					 sender:[NSNumber numberWithLongLong:[[payload objectForKey:@"sender"] longLongValue]] 
			   conversation:[NSNumber numberWithLongLong:[[payload objectForKey:@"conversation"] longLongValue]] 
					   time:[NSNumber numberWithLongLong:[[payload objectForKey:@"time"] longLongValue]]
					   body:[payload objectForKey:@"body"]];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSDictionary*)getByID:(NSNumber*)ident {
	FMResultSet* rs = [fmdb executeQuery:@"SELECT * FROM message WHERE id = ?", ident];
	if ([rs next]) {
		NSDictionary* message = [self resultToDict:rs];
		
		[rs close];	
		return message;
	}
	
	[rs close];	
	return nil;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSDictionary*)getMostRecentMessageForConversation:(NSNumber*)ident {
	FMResultSet* rs = [fmdb executeQuery:@"SELECT * FROM message WHERE conversation = ?", ident];
	if ([rs next]) {
		NSDictionary* message = [self resultToDict:rs];
		
		[rs close];	
		return message;
	}
	
	[rs close];	
	return nil;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSArray*)messagesForConversation:(NSNumber*)ident {
	FMResultSet* rs = [fmdb executeQuery:@"SELECT * FROM message WHERE conversation = ?", ident];
	NSMutableArray* list = [[NSMutableArray alloc] init];
	
	while ([rs next]) {
		[list addObject:[self resultToDict:rs]];
	}
	
	[rs close];	
	return list;
}

@end
