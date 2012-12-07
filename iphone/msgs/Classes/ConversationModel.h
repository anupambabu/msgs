//
//  ConversationModel.h
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-27.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <Three20Network/TTModel.h>

@interface ConversationModel : TTModel {
	NSDate* loadedTime;
	NSDate* updatedTime;
	NSArray* conversations;
}

@property (nonatomic, readonly) NSArray* conversations;

- (id)init;

@end