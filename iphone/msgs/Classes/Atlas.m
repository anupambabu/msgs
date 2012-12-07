//
//  Atlas.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "Atlas.h"

int kHostPort = 50000;

///*
NSString* kHostAddress = @"10.0.1.13";
NSString* kAuthRequestURLFormat = @"http://10.0.1.13:8080/api/auth/device/iphone/%@/%@/%@";
NSString* kMsgsContactSearchFormat = @"http://10.0.1.13:8080/api/%@/contacts/list";
//*/

/*
NSString* kHostAddress = @"msgs.io";
NSString* kAuthRequestURLFormat = @"http://msgs.io:8080/api/auth/device/iphone/%@/%@/%@";
NSString* kMsgsContactSearchFormat = @"http://msgs.io:8080/api/%@/contacts/list";
//*/

NSString* kAnyURLPath = @"*";
NSString* kAppRootURLPath = @"msgs://root";
NSString* kContactListURLPath = @"msgs://contacts";

NSString* kAccountStoreDB = @"account.db";