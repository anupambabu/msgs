//
//  AccountInitializer.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AccountStore.h"
#import "AccountInitializerDelegate.h"

#import <Three20Network/TTURLRequestDelegate.h>

@interface AccountInitializer : NSObject <TTURLRequestDelegate> {
	id<AccountInitializerDelegate> _delegate;
	AccountStore* _store;
}

+ (id)start:(id<AccountInitializerDelegate>)delegate;

@end
