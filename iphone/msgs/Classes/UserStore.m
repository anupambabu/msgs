//
//  UserStore.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-16.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "UserStore.h"
#import "Atlas.h"
#import "StringUtil.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

static UserStore* instance = nil;

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation UserStore

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(fmdb);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setupDB {	
	NSString* accountDBPath = [StringUtil filePathInDocumentsDirectoryForFileName:kUserStoreDB];
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
	
	[fmdb executeUpdate:@"CREATE TABLE IF NOT EXISTS user (pk INTEGER PRIMARY KEY, "
						 "id INTEGER, "
						 "name VARCHAR(50), "
						 "status VARCHAR(255), "
						 "avatar VARCHAR(255), "
						 "lastsync INTEGER)"];
	
		[fmdb executeUpdate:@"CREATE UNIQUE INDEX IF NOT EXISTS user_id ON user(id);"];
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
- (bool)setUser:(NSNumber*)user name:(NSString*)name status:(NSString*)status avatar:(NSString*)avatar lastSync:(NSNumber*)lastSync {
	return [fmdb executeUpdate:@"INSERT OR REPLACE INTO user(id, name, status, avatar, lastsync) VALUES(?, ?, ?, ?, ?)", 
			user, name, status, avatar, lastSync];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setUser:(NSDictionary*)payload {
	return [self setUser:[NSNumber numberWithLongLong:[[payload objectForKey:@"user"] longLongValue]] 
					name:[payload objectForKey:@"name"]
				  status:[payload objectForKey:@"status"]
				  avatar:[payload objectForKey:@"avatar"]
				lastSync:[NSNumber numberWithLongLong:[[payload objectForKey:@"lastsync"] longLongValue]]];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSDictionary*)getByID:(NSNumber*)ident {
	FMResultSet* rs = [fmdb executeQuery:@"SELECT * FROM user WHERE id = ?", ident];
	if ([rs next]) {
		NSDictionary* user = [[NSDictionary alloc] initWithObjectsAndKeys:
							  [NSNumber numberWithInt:[rs intForColumn:@"id"]], @"id",
							  [rs stringForColumn:@"name"], @"name",
							  [rs stringForColumn:@"status"], @"status",
							  [rs stringForColumn:@"avatar"], @"avatar",
							  [NSNumber numberWithInt:[rs intForColumn:@"lastsync"]], @"lastsync",
							  nil];
		
		[rs close];
		return user;
	}
	
	[rs close];	
	return nil;
}

@end
