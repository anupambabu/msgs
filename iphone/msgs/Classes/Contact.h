//
//  Contact.h
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface Contact : NSObject {
	NSString* _username;
	NSNumber* _status;
}

@property (nonatomic, copy)   NSString* username;
@property (nonatomic, copy)   NSNumber* status;

@end
