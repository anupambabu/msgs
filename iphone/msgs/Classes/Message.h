//
//  Message.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Message : NSObject {
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