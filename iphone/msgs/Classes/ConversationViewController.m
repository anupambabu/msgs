//
//  ConversationViewController.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "ConversationViewController.h"
#import "ConversationDataSource.h"
#import "ConversationTableItem.h"
#import "ConversationTableItemCell.h"

#import <Three20UI/TTTableViewDragRefreshDelegate.h>

@implementation ConversationViewController

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 5;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {		
		//self.variableHeightRows = YES;
		
		self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
		
		UIView* titleView = [[UIImageView alloc] initWithFrame:self.navigationController.navigationBar.frame];
		self.navigationItem.titleView = titleView;
		
		UIImage* titleImage = [UIImage imageNamed:@"logo.png"];
		UIImageView* titleImageView = [[UIImageView alloc] initWithImage:titleImage];
		titleImageView.frame = CGRectMake(0, 0, titleImage.size.width, titleImage.size.height);
		titleImageView.center = titleView.center;
		titleImageView.transform = CGAffineTransformMakeTranslation(-4.0, 0.0);
		[titleView insertSubview:titleImageView atIndex:0];
	}
	
	return self;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	return 150;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	static NSString *CellIdentifier = @"Cell";
	
	ConversationTableItemCell *cell = (ConversationTableItemCell *)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	if(cell == nil) {
		cell = [[[ConversationTableItemCell alloc] initWithStyle:UITableViewStylePlain reuseIdentifier:CellIdentifier] autorelease];
	}
	
	return cell;
}

@end
