//
//  HomeViewController.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-18.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "HomeViewController.h"
#import "ConversationDataSource.h"

#import <Three20UI/TTTableViewDragRefreshDelegate.h>
#import <Three20UI/TTSectionedDataSource.h>
#import <Three20UI/TTTableMessageItem.h>

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation HomeViewController

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {		
		self.variableHeightRows = YES;
		self.title = @"msgs";
		
		UIView* titleView = [[UIImageView alloc] initWithFrame:self.navigationController.navigationBar.frame];
		self.navigationItem.titleView = titleView;
		
		UIImage* titleImage = [UIImage imageNamed:@"logo.png"];
		UIImageView* titleImageView = [[UIImageView alloc] initWithImage:titleImage];
		titleImageView.frame = CGRectMake(0, 0, titleImage.size.width, titleImage.size.height);
		titleImageView.center = titleView.center;
		titleImageView.transform = CGAffineTransformMakeTranslation(0.0, 0.0);
		[titleView insertSubview:titleImageView atIndex:0];
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)createModel {
	self.dataSource = [[[ConversationDataSource alloc] init] autorelease];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id<UITableViewDelegate>)createDelegate {
	return [[[TTTableViewDragRefreshDelegate alloc] initWithController:self] autorelease];
}

@end
