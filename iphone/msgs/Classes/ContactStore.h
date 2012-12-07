//
//  ContactStore.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FMDatabase.h"

@interface ContactStore : NSObject {
	FMDatabase* fmdb;
}

+ (id)get;

- (bool)setContact:(NSNumber*)contact user:(NSNumber*)user state:(NSString*)state;
- (bool)setContact:(NSDictionary*)payload;

@end
