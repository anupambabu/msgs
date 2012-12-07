//
//  EventStore.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-16.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FMDatabase.h"

@interface EventStore : NSObject {
	FMDatabase* fmdb;
}

+ (id)get;

- (bool)setEvent:(NSNumber*)event time:(NSNumber*)time type:(NSString*)type entity:(NSNumber*)entity action:(NSString*)action;

@end
