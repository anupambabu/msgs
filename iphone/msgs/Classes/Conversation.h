//
//  Conversation.h
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Conversation : NSObject {
	NSNumber* ident;
	NSString* label;
	NSString* text;
	NSDate*   time;
}

@property (nonatomic, retain) NSNumber* ident;
@property (nonatomic, copy) NSString* label;
@property (nonatomic, copy) NSString* text;
@property (nonatomic, retain) NSDate* time;

@end
