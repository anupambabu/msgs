//
//  ContactModel.h
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <Three20Network/TTURLRequestModel.h>

@interface ContactModel : TTURLRequestModel {
	NSString* _searchQuery;
	NSArray*  _contacts;
}

@property (nonatomic, copy)     NSString* searchQuery;
@property (nonatomic, readonly) NSArray*  contacts;

- (id)initWithSearchQuery:(NSString*)searchQuery;

@end