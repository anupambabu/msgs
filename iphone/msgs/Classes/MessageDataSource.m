//
//  MessageDataSource.m
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import "MessageDataSource.h"
#import "MessageModel.h"
#import "Message.h"

#import "MessageTableItem.h"
#import "MessageTableItemCell.h"

#import <Three20Core/TTCorePreprocessorMacros.h>
#import <Three20UI/TTTableMessageItem.h>

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation MessageDataSource

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithConversation:(NSNumber*)conversationIdent {
	if (self = [super init]) {
		model = [[MessageModel alloc] initWithConversation:conversationIdent];
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(model);
	
	[super dealloc];
}


///////////////////////////////////////////////////////////////////////////////////////////////////
- (id<TTModel>)model {
	return model;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)tableViewDidLoadModel:(UITableView*)tableView {
	NSMutableArray* modelItems = [[NSMutableArray alloc] init];
	
	for (Message* message in model.messages) {
		[modelItems addObject:[MessageTableItem initWithMessage:message]];
	}
	
	self.items = modelItems;
	TT_RELEASE_SAFELY(modelItems);
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)titleForLoading:(BOOL)reloading {
	if (reloading) {
		return NSLocalizedString(@"Updating conversation...", @"Conversation update text");
	} else {
		return NSLocalizedString(@"Loading conversation...", @"Conversation loading text");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)titleForEmpty {
	return NSLocalizedString(@"No messages found.", @"Conversation no results");
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)subtitleForError:(NSError*)error {
	return NSLocalizedString(@"Sorry, there was an error loading your conversation.", @"");
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (Class)tableView:(UITableView*)tableView cellClassForObject:(id)object {
	return [MessageTableItemCell class];
}

@end