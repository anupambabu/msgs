//
//  ContactViewController.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "ContactViewController.h"
#import "ContactDataSource.h"

#import <Three20UI/TTTableViewDragRefreshDelegate.h>

@implementation ContactViewController

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
		self.title = @"Contacts";
		self.variableHeightRows = YES;
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)createModel {
	self.dataSource = [[[ContactDataSource alloc]
						initWithSearchQuery:@"lzimm"] autorelease];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id<UITableViewDelegate>)createDelegate {
	return [[[TTTableViewDragRefreshDelegate alloc] initWithController:self] autorelease];
}

@end
