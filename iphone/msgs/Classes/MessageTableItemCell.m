//
//  MessageTableItemCell.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "MessageTableItem.h"
#import "MessageTableItemCell.h"

#import <Three20UI/UIViewAdditions.h>
#import <Three20UI/UITableViewAdditions.h>
#import <Three20Core/TTCorePreprocessorMacros.h>

@interface MessageTableItemViewCell : UIView
@end

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation MessageTableItemViewCell

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)drawRect:(CGRect)r {
	[(MessageTableItemCell*)[self superview] drawContentView:r];
}

@end

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation MessageTableItemCell

@synthesize message;

static UIFont *firstTextFont = nil;

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)drawContentView:(CGRect)r {
	firstTextFont = [[UIFont systemFontOfSize:16] retain];
	
	CGContextRef context = UIGraphicsGetCurrentContext();
	
	UIColor *backgroundColor = [UIColor whiteColor];
	UIColor *textColor = [UIColor blackColor];
	
	[backgroundColor set];	

	CGContextFillRect(context, r);
	
	NSString* testString = self.message.text;
	CGSize s = [testString sizeWithFont:firstTextFont constrainedToSize:CGSizeMake([self superview].width / 3 * 2, 500) lineBreakMode:UILineBreakModeWordWrap];
	
	UIImage* balloon = [[UIImage imageNamed:@"balloonLeft.png"] stretchableImageWithLeftCapWidth:25 topCapHeight:14];
	[balloon drawInRect:CGRectMake(0, kTableCellVPadding, s.width + 24, s.height + 15)];
	
	[textColor set];
	[testString drawInRect:CGRectMake(16, kTableCellVPadding + 4, s.width, s.height) withFont:firstTextFont lineBreakMode:UILineBreakModeWordWrap];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if(self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
		contentView = [[MessageTableItemViewCell alloc] initWithFrame:CGRectZero];
		contentView.opaque = YES;
		[self addSubview:contentView];
		[contentView release];
		
		label = [[UILabel alloc] initWithFrame:CGRectZero];
		label.textAlignment =  UITextAlignmentCenter;
		label.textColor = [UIColor whiteColor];
		label.backgroundColor = [UIColor blackColor];
		label.font = [UIFont fontWithName:@"Arial Rounded MT Bold" size:(36.0)];
		label.text = @"";
		[self addSubview:label];
    }
	
    return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)layoutSubviews {
	[super layoutSubviews];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setObject:(id)object {
	[super setObject:object];

	MessageTableItem* item = object;
	self.message = item.message;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setFrame:(CGRect)f {
	[super setFrame:f];
	[contentView setFrame:[self bounds]];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)setNeedsDisplay {
	[super setNeedsDisplay];
	[contentView setNeedsDisplay];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
+ (CGFloat)tableView:(UITableView*)tableView rowHeightForObject:(id)object {
	MessageTableItem* item = object;
	
	CGSize s = [item.message.text sizeWithFont:[UIFont systemFontOfSize:16] constrainedToSize:CGSizeMake(tableView.width / 3 * 2, 500) lineBreakMode:UILineBreakModeWordWrap];
	
	return s.height + 15 + kTableCellVPadding*2;
}

@end