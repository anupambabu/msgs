//
//  ConversationListViewController.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-18.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "ConversationListViewController.h"

#import <Three20UI/TTTableViewDragRefreshDelegate.h>

@implementation ConversationListViewController

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
		self.title = @"msgs";
		self.variableHeightRows = YES;
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)createModel {
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id<UITableViewDelegate>)createDelegate {
	return [[[TTTableViewDragRefreshDelegate alloc] initWithController:self] autorelease];
}


@end
