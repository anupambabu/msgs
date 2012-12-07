//
//  StringUtil.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-15.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "StringUtil.h"


@implementation StringUtil

+ (NSString*)filePathInDocumentsDirectoryForFileName:(NSString*)filename {
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES); 
	NSString *documentsDirectory = [paths objectAtIndex: 0]; 
	NSString *pathName = [documentsDirectory stringByAppendingPathComponent:filename];
	return pathName;
}

@end
