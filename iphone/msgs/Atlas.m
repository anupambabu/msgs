//
//  Atlas.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#include "Atlas.h"

NSString* kAnyURLPath = @"*";
NSString* kAppHomeURLPath = @"msgs://home";
NSString* kAppConversationURLPath = @"msgs://conversations/([0-9]*)";
NSString* kAppContactListURLPath = @"msgs://contacts";
NSString* kAppContactAddURLPath = @"msgs://contacts/add";
NSString* kAppContactViewURLPath = @"msgs://contacts/([0-9]*)";

NSString* kAccountStoreDB = @"account.db";
NSString* kMessageStoreDB = @"message.db";
NSString* kContactStoreDB = @"contact.db";
NSString* kConversationStoreDB = @"conversation.db";
NSString* kUserStoreDB = @"user.db";
NSString* kEventStoreDB = @"event.db";

int kHostPort = 50000;

/*
NSString* kHostAddress = @"127.0.0.1";
NSString* kAuthRequestURLFormat = @"http://127.0.0.1:8080/api/auth/device/iphone/%@/%@/%@";
NSString* kMsgsContactSearchFormat = @"http://127.0.0.1:8080/api/%@/contacts/list";
//*/

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