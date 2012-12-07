//
//  AccountStore.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "AccountStore.h"
#import "Atlas.h"
#import "StringUtil.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

static AccountStore* instance = nil;
static NSString* deviceIdent = nil;

static NSString* kUserIDKey = @"userID";
static NSString* kUserEmailKey = @"userEmail";
static NSString* kAuthTokenKey = @"authToken";
static NSString* kAuthSecretKey = @"authSecret";
static NSString* kLastSyncKey = @"lastSync";

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation AccountStore;

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(fmdb);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setupDB {	
	NSString* accountDBPath = [StringUtil filePathInDocumentsDirectoryForFileName:kAccountStoreDB];
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
	
	[fmdb executeUpdate:@"CREATE TABLE IF NOT EXISTS account (pk INTEGER PRIMARY KEY, "
						 "ident VARCHAR(50), "
						 "key VARCHAR(50), "
						 "value VARCHAR(50))"];
	
	[fmdb executeUpdate:@"CREATE UNIQUE INDEX IF NOT EXISTS account_key_ident ON account(key, ident);"];
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
- (NSString*)getKey:(NSString*)key {
	FMResultSet* rs = [fmdb executeQuery:@"SELECT value FROM account WHERE key = ? AND ident = ?", key, [self getDeviceIdent]];
	if ([rs next]) {
		NSString* val = [rs stringForColumn:@"value"];
		
		[rs close];

		return val;
	}
	
	return nil;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setKey:(NSString*)key with:(NSString*)val {
	return [fmdb executeUpdate:@"INSERT OR REPLACE INTO account(key, value, ident) VALUES(?, ?, ?)", key, val, [self getDeviceIdent]];
}
	
///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)getDeviceIdent {
	if (!deviceIdent) {
		deviceIdent = [[UIDevice currentDevice] uniqueIdentifier];
	}
	
	return deviceIdent;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)getDeviceVerifier {
	return [self getDeviceIdent];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)getUserID { return [self getKey:kUserIDKey]; }
- (bool)setUserID:(NSString*)val { return [self setKey:kUserIDKey with:val]; }

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)getUserEmail { return [self getKey:kUserEmailKey]; }
- (bool)setUserEmail:(NSString*)val { return [self setKey:kUserEmailKey with:val]; }

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)getAuthToken { return [self getKey:kAuthTokenKey]; }
- (bool)setAuthToken:(NSString*)val { return [self setKey:kAuthTokenKey with:val]; }

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)getAuthSecret { return [self getKey:kAuthSecretKey]; }
- (bool)setAuthSecret:(NSString*)val { return [self setKey:kAuthSecretKey with:val]; }

///////////////////////////////////////////////////////////////////////////////////////////////////
- (long long)getLastSync {
	NSString* syncVal = [self getKey:kLastSyncKey];
	if (syncVal) {
		return [syncVal longLongValue];
	} else {
		return 0ll;
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (bool)setLastSync:(long long)val {
	NSString* syncVal = [[NSNumber numberWithLongLong:val] stringValue];
	return [self setKey:kLastSyncKey with:syncVal];
}

@end
