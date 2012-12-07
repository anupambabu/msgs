//
//  ContactStore.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "ContactStore.h"
#import "Atlas.h"
#import "StringUtil.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

static ContactStore* instance = nil;

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation ContactStore

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(fmdb);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setupDB {	
	NSString* accountDBPath = [StringUtil filePathInDocumentsDirectoryForFileName:kContactStoreDB];
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
	
	[fmdb executeUpdate:@"CREATE TABLE IF NOT EXISTS contact (pk INTEGER PRIMARY KEY, "
						 "id INTEGER, "
						 "user INTEGER, "
						 "state VARCHAR(50))"];
	
	[fmdb executeUpdate:@"CREATE UNIQUE INDEX IF NOT EXISTS contact_id ON contact(id);"];
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
- (bool)setContact:(NSNumber*)contact user:(NSNumber*)user state:(NSString*)state {
	return [fmdb executeUpdate:@"INSERT OR REPLACE INTO contact(id, user, state) VALUES(?, ?, ?)", 
			contact, user, state];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setContact:(NSDictionary*)payload {
	return [self setContact:[NSNumber numberWithLongLong:[[payload objectForKey:@"contact"] longLongValue]]
					   user:[NSNumber numberWithLongLong:[[payload objectForKey:@"user"] longLongValue]]
					  state:[payload objectForKey:@"state"]];
}

@end
