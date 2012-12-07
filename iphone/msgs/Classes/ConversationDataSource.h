//
//  ConversationDataSource.h
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-27.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <Three20UI/TTListDataSource.h>

@class ConversationModel;

@interface ConversationDataSource : TTListDataSource {
	ConversationModel* model;
}

- (id)init;

@end