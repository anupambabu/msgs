//
//  ContactDataSource.h
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <Three20UI/TTListDataSource.h>

@class ContactModel;

@interface ContactDataSource : TTListDataSource {
	ContactModel* _searchFeedModel;
}

- (id)initWithSearchQuery:(NSString*)searchQuery;
- (Class)tableView:(UITableView*)tableView cellClassForObject:(id)object;

@end