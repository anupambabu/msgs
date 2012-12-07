//
//  ContactTableItem.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-05.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Three20UI/TTTableLinkedItem.h>

@interface ContactTableItem : TTTableLinkedItem {
	NSString* _text;
}

@property (nonatomic, copy) NSString* text;

+ (id)itemWithText:(NSString*)text;
+ (id)itemWithText:(NSString*)text URL:(NSString*)URL;
+ (id)itemWithText:(NSString*)text URL:(NSString*)URL accessoryURL:(NSString*)accessoryURL;
+ (id)itemWithText:(NSString*)text delegate:(id)delegate selector:(SEL)selector;

@end