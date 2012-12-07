//
//  AccountStore.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FMDatabase.h"

@interface AccountStore : NSObject {
	FMDatabase* fmdb;
}

+ (id)get;

- (NSString*)getDeviceIdent;
- (NSString*)getDeviceVerifier;

- (NSString*)getUserID;
- (bool)setUserID:(NSString*)val;

- (NSString*)getUserEmail;
- (bool)setUserEmail:(NSString*)val;

- (NSString*)getAuthToken;
- (bool)setAuthToken:(NSString*)val;

- (NSString*)getAuthSecret;
- (bool)setAuthSecret:(NSString*)val;

- (long long)getLastSync;
- (bool)setLastSync:(long long)val;

@end
