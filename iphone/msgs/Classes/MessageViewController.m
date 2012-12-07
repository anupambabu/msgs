//
//  MessageViewController.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "MessageViewController.h"
#import "MessageDataSource.h"

#import <Three20UI/TTTableViewDragRefreshDelegate.h>
#import <Three20UI/TTSectionedDataSource.h>
#import <Three20UI/TTTableMessageItem.h>

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation MessageViewController

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {		
		self.variableHeightRows = YES;
		self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
		self.title = @"msgs";
		
		UIView* titleView = [[UIImageView alloc] initWithFrame:self.navigationController.navigationBar.frame];
		self.navigationItem.titleView = titleView;
		
		UIImage* titleImage = [UIImage imageNamed:@"logo.png"];
		UIImageView* titleImageView = [[UIImageView alloc] initWithImage:titleImage];
		titleImageView.frame = CGRectMake(0, 0, titleImage.size.width, titleImage.size.height);
		titleImageView.center = titleView.center;
		titleImageView.transform = CGAffineTransformMakeTranslation(-4.0, 0.0);
		[titleView insertSubview:titleImageView atIndex:0];
		
		textEditor = [[[TTTextEditor alloc] init] autorelease];
		textEditor.autoresizesToText = YES;
		textEditor.minNumberOfLines = 1;
		textEditor.maxNumberOfLines = 4;
		[self.tableOverlayView insertSubview:textEditor atIndex:0];
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)createModel {
	self.dataSource = [[[MessageDataSource alloc] initWithConversation:[NSNumber numberWithInt:1]] autorelease];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id<UITableViewDelegate>)createDelegate {
	return [[[TTTableViewDragRefreshDelegate alloc] initWithController:self] autorelease];
}

@end