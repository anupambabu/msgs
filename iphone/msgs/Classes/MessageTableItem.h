//
//  MessageTableItem.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Three20UI/TTTableMessageItem.h>

#import "Message.h"

@interface MessageTableItem : TTTableMessageItem {
	Message* message;
}

@property (nonatomic, retain) Message* message;

+ (id)initWithMessage:(Message*)message;

@end

