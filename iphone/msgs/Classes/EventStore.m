//
//  EventStore.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-16.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "EventStore.h"
#import "Atlas.h"
#import "StringUtil.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

static EventStore* instance = nil;

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation EventStore

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(fmdb);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setupDB {	
	NSString* accountDBPath = [StringUtil filePathInDocumentsDirectoryForFileName:kEventStoreDB];
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
	
	[fmdb executeUpdate:@"CREATE TABLE IF NOT EXISTS event (pk INTEGER PRIMARY KEY, "
						 "id INTEGER, "
						 "time INTEGER, "
						 "type VARCHAR(50), "
						 "entity INTEGER, "
						 "action VARCHAR(50))"];
	
	[fmdb executeUpdate:@"CREATE UNIQUE INDEX IF NOT EXISTS event_id ON event(id);"];
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
- (bool)setEvent:(NSNumber*)event time:(NSNumber*)time type:(NSString*)type entity:(NSNumber*)entity action:(NSString*)action {
	return [fmdb executeUpdate:@"INSERT OR REPLACE INTO event(id, time, type, entity, action) VALUES(?, ?, ?, ?, ?)", 
			event, time, type, entity, action];
}

@end
