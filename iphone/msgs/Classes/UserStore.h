//
//  UserStore.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-16.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FMDatabase.h"

@interface UserStore : NSObject {
	FMDatabase* fmdb;
}

+ (id)get;

- (bool)setUser:(NSNumber*)user name:(NSString*)name status:(NSString*)status avatar:(NSString*)avatar lastSync:(NSNumber*)lastSync;
- (bool)setUser:(NSDictionary*)payload;

- (NSDictionary*)getByID:(NSNumber*)ident;

@end
