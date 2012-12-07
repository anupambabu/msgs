//
//  MessageViewController.h
//  msgs
//
//  Created by Lewis Zimmerman on 11-01-23.
//  Copyright 2011 Qorporation. All rights reserved.
//

#import <Three20UI/TTTableViewController.h>
#import <Three20UI/TTTextEditor.h>
#import <Three20UI/TTTextEditorDelegate.h>

@interface MessageViewController : TTTableViewController <TTTextEditorDelegate> {
	TTTextEditor *textEditor;
}

@end