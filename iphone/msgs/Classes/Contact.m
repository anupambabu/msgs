//
//  Contact.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "Contact.h"

#import <Three20Core/TTCorePreprocessorMacros.h>

@implementation Contact

@synthesize username = _username;
@synthesize status = _status;

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(_username);
	TT_RELEASE_SAFELY(_status);
	
	[super dealloc];
}


@end
