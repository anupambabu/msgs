//
//  MessageTableItemCell.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Three20UI/TTTableMessageItemCell.h>

#import "Message.h"

@interface MessageTableItemCell : TTTableMessageItemCell {
	UIView *contentView;
	UILabel *label;
	Message* message;
}

@property (nonatomic, retain) Message* message;

- (void)drawContentView:(CGRect)r;

@end