//
//  ConversationDataSource.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-27.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "ConversationDataSource.h"
#import "ConversationModel.h"
#import "Conversation.h"

#import "ConversationTableItem.h"
#import "ConversationTableItemCell.h"

#import <Three20Core/TTCorePreprocessorMacros.h>
#import <Three20UI/TTTableMessageItem.h>

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation ConversationDataSource

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)init {
	if (self = [super init]) {
		model = [[ConversationModel alloc] init];
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
	
	for (Conversation* conversation in model.conversations) {
		[modelItems addObject:[TTTableMessageItem itemWithTitle: conversation.label
														caption: nil
														   text: conversation.text
													  timestamp: conversation.time
															URL: [NSString stringWithFormat:@"msgs://conversations/%@", conversation.ident]]];
	}
	
	self.items = modelItems;
	TT_RELEASE_SAFELY(modelItems);
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)titleForLoading:(BOOL)reloading {
	if (reloading) {
		return NSLocalizedString(@"Updating conversations...", @"Conversation list update text");
	} else {
		return NSLocalizedString(@"Loading conversations...", @"Conversation list loading text");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)titleForEmpty {
	return NSLocalizedString(@"No conversations found.", @"Conversation list no results");
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSString*)subtitleForError:(NSError*)error {
	return NSLocalizedString(@"Sorry, there was an error loading your conversations.", @"");
}

@end