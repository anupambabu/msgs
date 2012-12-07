//
//  ConversationTableItemCell.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-19.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Three20UI/TTTableMessageItemCell.h>

@interface ConversationTableItemCell : UITableViewCell {
	UIView *contentView;
	UILabel *label;
}

- (void)drawContentView:(CGRect)r;

@end
