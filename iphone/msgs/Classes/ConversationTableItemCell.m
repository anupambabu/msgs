//
//  ConversationTableItemCell.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-19.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "ConversationTableItemCell.h"
#import <Three20Core/TTCorePreprocessorMacros.h>

@interface ConversationTableItemCellView : UIView
@end

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation ConversationTableItemCellView

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)drawRect:(CGRect)r {
	[(ConversationTableItemCell*)[self superview] drawContentView:r];
}

@end

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation ConversationTableItemCell

static UIFont *firstTextFont = nil;

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)drawContentView:(CGRect)r {
	firstTextFont = [[UIFont systemFontOfSize:16] retain];
		
	CGContextRef context = UIGraphicsGetCurrentContext();
	
	UIColor *backgroundColor = [UIColor whiteColor];
	UIColor *textColor = [UIColor blackColor];
	
	[backgroundColor set];
	CGContextFillRect(context, r);
	
	NSString* testString = @"Omg! Lewis is SOO fuckin Rogue, this shit aint even funny!";
	CGSize s = [testString sizeWithFont:firstTextFont constrainedToSize:CGSizeMake(200, 500) lineBreakMode:UILineBreakModeWordWrap];
	
	UIImage* balloon = [[UIImage imageNamed:@"balloonLeft.png"] stretchableImageWithLeftCapWidth:25 topCapHeight:14];
	[balloon drawInRect:CGRectMake(0, 0, s.width + 24, s.height + 15)];
	
	[textColor set];
	[testString drawInRect:CGRectMake(16, 4, s.width, s.height) withFont:firstTextFont lineBreakMode:UILineBreakModeWordWrap];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if(self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
		contentView = [[ConversationTableItemCellView alloc] initWithFrame:CGRectZero];
		contentView.opaque = YES;
		[self addSubview:contentView];
		[contentView release];
		
		label = [[UILabel alloc] initWithFrame:CGRectZero];
		label.textAlignment =  UITextAlignmentCenter;
		label.textColor = [UIColor whiteColor];
		label.backgroundColor = [UIColor blackColor];
		label.font = [UIFont fontWithName:@"Arial Rounded MT Bold" size:(36.0)];
		label.text = @"ASLDKJALSKDJ";
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

@end

