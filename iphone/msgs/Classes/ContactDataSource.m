//
//  ContactDataSource.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "ContactDataSource.h"
#import "ContactModel.h"
#import "Contact.h"

#import "ContactTableItem.h"
#import "ContactTableItemCell.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

@implementation ContactDataSource

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithSearchQuery:(NSString*)searchQuery {
	if (self = [super init]) {
		_searchFeedModel = [[ContactModel alloc] initWithSearchQuery:searchQuery];
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(_searchFeedModel);
	
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id<TTModel>)model {
	return _searchFeedModel;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)tableViewDidLoadModel:(UITableView*)tableView {
	NSMutableArray* items = [[NSMutableArray alloc] init];
	
	for (Contact* contact in _searchFeedModel.contacts) {
		[items addObject:[ContactTableItem itemWithText: contact.username
											URL:@"msgs://tableItemTest"]];
	}
	
	self.items = items;
	TT_RELEASE_SAFELY(items);
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)titleForLoading:(BOOL)reloading {
	if (reloading) {
		return NSLocalizedString(@"Updating Contact List...", @"Contact list updating text");
	} else {
		return NSLocalizedString(@"Loading Contact List...", @"Contact list loading text");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)titleForEmpty {
	return NSLocalizedString(@"No contacts found.", @"Contact list no results");
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)subtitleForError:(NSError*)error {
	return NSLocalizedString(@"Sorry, there was an error loading your contacts.", @"");
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (Class)tableView:(UITableView*)tableView cellClassForObject:(id)object {
	return [ContactTableItemCell class];
}

@end