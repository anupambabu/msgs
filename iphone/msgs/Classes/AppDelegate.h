//
//  msgsAppDelegate.h
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-27.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "MessageSocket.h"
#import "MessageSocketDelegate.h"
#import "AccountStore.h"
#import "MessageStore.h"
#import "ContactStore.h"
#import "ConversationStore.h"
#import "UserStore.h"
#import "EventStore.h"
#import "ConversationStore.h"
#import "AccountInitializer.h"
#import "AccountInitializerDelegate.h"

@interface AppDelegate : NSObject <UIApplicationDelegate, MessageSocketDelegate, NSStreamDelegate, AccountInitializerDelegate> {
	AccountStore* _accountStore;
	MessageStore* _messageStore;
	ContactStore* _contactStore;
	ConversationStore* _conversationStore;
	UserStore* _userStore;
	EventStore* _eventStore;
	
	bool _requiresAccountInitialization;
	bool _accountInitialiationCompleted;
	
	AccountInitializer* _initializer;
	MessageSocket* _socket;
}

@end

