//
//  ContactTableItemCell.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-05.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "ContactTableItemCell.h"

// UI
#import <Three20UI/UIViewAdditions.h>
#import <Three20UI/UITableViewAdditions.h>

// - Table items
#import <Three20UI/TTTableTextItem.h>
#import <Three20UI/TTTableLongTextItem.h>
#import <Three20UI/TTTableGrayTextItem.h>
#import <Three20UI/TTTableButton.h>
#import <Three20UI/TTTableLink.h>
#import <Three20UI/TTTableSummaryItem.h>

// Style
#import <Three20Style/TTDefaultStyleSheet.h>
#import <Three20Style/TTGlobalStyle.h>

static const CGFloat kMaxLabelHeight = 2000;

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation ContactTableItemCell


///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString*)identifier {
	if (self = [super initWithStyle:style reuseIdentifier:identifier]) {
		self.textLabel.highlightedTextColor = TTSTYLEVAR(highlightedTextColor);
		self.textLabel.lineBreakMode = UILineBreakModeWordWrap;
		self.textLabel.numberOfLines = 0;
	}
	
	return self;
}


///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark -
#pragma mark Class private


///////////////////////////////////////////////////////////////////////////////////////////////////
+ (UIFont*)textFontForItem:(TTTableTextItem*)item {
	if ([item isKindOfClass:[TTTableLongTextItem class]]) {
		return TTSTYLEVAR(font);
	} else if ([item isKindOfClass:[TTTableGrayTextItem class]]) {
		return TTSTYLEVAR(font);
	} else {
		return TTSTYLEVAR(tableFont);
	}
}


///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark -
#pragma mark TTTableViewCell class public


///////////////////////////////////////////////////////////////////////////////////////////////////
+ (CGFloat)tableView:(UITableView*)tableView rowHeightForObject:(id)object {
	TTTableTextItem* item = object;
	
	CGFloat width = tableView.width - (kTableCellHPadding*2 + [tableView tableCellMargin]*2);
	UIFont* font = [self textFontForItem:item];
	CGSize size = [item.text sizeWithFont:font
						constrainedToSize:CGSizeMake(width, CGFLOAT_MAX)
							lineBreakMode:UILineBreakModeTailTruncation];

	if (size.height > kMaxLabelHeight) {
		size.height = kMaxLabelHeight;
	}
	
	return size.height + kTableCellVPadding*2;
}


///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark -
#pragma mark UIView


///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)layoutSubviews {
	[super layoutSubviews];
	
	self.textLabel.frame = CGRectInset(self.contentView.bounds,
									   kTableCellHPadding, kTableCellVPadding);
}


///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark -
#pragma mark TTTableViewCell


///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setObject:(id)object {
	if (_item != object) {
		[super setObject:object];
		
		TTTableTextItem* item = object;
		self.textLabel.text = item.text;
		
		self.textLabel.font = TTSTYLEVAR(tableFont);
		self.textLabel.textColor = TTSTYLEVAR(textColor);
		self.textLabel.textAlignment = UITextAlignmentRight;
	}
}


@end