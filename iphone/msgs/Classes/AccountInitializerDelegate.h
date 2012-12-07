//
//  AccountInitializerDelegate.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "AccountInitializerDelegate.h"

@class AccountInitializer;

@protocol AccountInitializerDelegate <NSObject>

- (void)accountInitialized:(NSString*)userID userEmail:(NSString*)userEmail authToken:(NSString*)authToken authSecret:(NSString*)authSecret;
- (void)accountInitializationFailed;

@end
