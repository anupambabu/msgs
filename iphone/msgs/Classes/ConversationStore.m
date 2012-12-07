//
//  ConversationStore.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "ConversationStore.h"
#import "Atlas.h"
#import "StringUtil.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

static ConversationStore* instance = nil;

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation ConversationStore

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(fmdb);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setupDB {	
	NSString* accountDBPath = [StringUtil filePathInDocumentsDirectoryForFileName:kConversationStoreDB];
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
		
	[fmdb executeUpdate:@"CREATE TABLE IF NOT EXISTS conversation (pk INTEGER PRIMARY KEY, "
						 "id INTEGER, "
						 "lastmessage INTEGER)"];
	
	[fmdb executeUpdate:@"CREATE UNIQUE INDEX IF NOT EXISTS conversation_id ON conversation(id);"];
	
	[fmdb executeUpdate:@"CREATE TABLE IF NOT EXISTS participant (pk INTEGER PRIMARY KEY, "
						 "id INTEGER, "
						 "conversation INTEGER, "
						 "user INTEGER, "
						 "lastsync INTEGER, "
						 "jointime INTEGER)"];
	
	[fmdb executeUpdate:@"CREATE UNIQUE INDEX IF NOT EXISTS participant_id ON participant(id);"];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSDictionary*)resultToDict:(FMResultSet*)rs {
	return [[NSDictionary alloc] initWithObjectsAndKeys:
			[NSNumber numberWithInt:[rs intForColumn:@"id"]], @"id",
			[NSNumber numberWithInt:[rs intForColumn:@"lastmessage"]], @"lastmessage",
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
- (bool)setConversation:(NSNumber*)conversation lastMessage:(NSNumber*)lastMessage {
	return [fmdb executeUpdate:@"INSERT OR REPLACE INTO conversation(id, lastmessage) VALUES(?, ?)", 
			conversation, lastMessage];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setConversation:(NSDictionary*)payload {
	return [self setConversation:[NSNumber numberWithLongLong:[[payload objectForKey:@"conversation"] longLongValue]]
					 lastMessage:[NSNumber numberWithLongLong:[[payload objectForKey:@"lastmessage"] longLongValue]]];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setParticipant:(NSNumber*)participant conversation:(NSNumber*)conversation user:(NSNumber*)user lastSync:(NSNumber*)lastSync joinTime:(NSNumber*)joinTime {
	return [fmdb executeUpdate:@"INSERT OR REPLACE INTO participant(id, conversation, user, lastsync, jointime) VALUES(?, ?, ?, ?, ?)", 
			participant, conversation, user, lastSync, joinTime];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setParticipant:(NSDictionary*)payload {
	return [self setParticipant:[NSNumber numberWithLongLong:[[payload objectForKey:@"participant"] longLongValue]] 
				   conversation:[NSNumber numberWithLongLong:[[payload objectForKey:@"conversation"] longLongValue]] 
						   user:[NSNumber numberWithLongLong:[[payload objectForKey:@"user"] longLongValue]] 
					   lastSync:[NSNumber numberWithLongLong:[[payload objectForKey:@"lastsync"] longLongValue]]
					   joinTime:[NSNumber numberWithLongLong:[[payload objectForKey:@"jointime"] longLongValue]]];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSArray*)list {
	FMResultSet* rs = [fmdb executeQuery:@"SELECT * FROM conversation"];
	NSMutableArray* list = [[NSMutableArray alloc] init];
	
	while ([rs next]) {
		[list addObject:[self resultToDict:rs]];
	}
	
	[rs close];
	return list;
}

@end
