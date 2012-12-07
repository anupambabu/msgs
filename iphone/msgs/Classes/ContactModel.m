//
//  ContactModel.m
//  msgs
//
//  Created by Lewis Zimmerman on 10-12-26.
//  Copyright 2010 Qorporation. All rights reserved.
//

#import "Atlas.h"

#import "ContactModel.h"
#import "Contact.h"

#import <Three20Core/Three20Core.h>
#import <Three20Core/TTCorePreprocessorMacros.h>
#import <Three20Network/TTGlobalNetwork.h>
#import <Three20Network/TTURLRequest.h>
#import <extThree20JSON/extThree20JSON.h>

@implementation ContactModel

@synthesize searchQuery = _searchQuery;
@synthesize contacts	= _contacts;

///////////////////////////////////////////////////////////////////////////////////////////////////
- (id)initWithSearchQuery:(NSString*)searchQuery {
	if (self = [super init]) {
		self.searchQuery = searchQuery;
	}
	
	return self;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)dealloc {
	TT_RELEASE_SAFELY(_searchQuery);
	TT_RELEASE_SAFELY(_contacts);
	[super dealloc];
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)load:(TTURLRequestCachePolicy)cachePolicy more:(BOOL)more {
	if (!self.isLoading && TTIsStringWithAnyText(_searchQuery)) {
		NSString* url = [NSString stringWithFormat:kMsgsContactSearchFormat, _searchQuery];
		
		TTURLRequest* request = [TTURLRequest
								 requestWithURL: url
								 delegate: self];
		
		request.cachePolicy = cachePolicy | TTURLRequestCachePolicyEtag;
		request.cacheExpirationAge = TT_CACHE_EXPIRATION_AGE_NEVER;
		
		TTURLJSONResponse* response = [[TTURLJSONResponse alloc] init];
		request.response = response;
		TT_RELEASE_SAFELY(response);
		
		[request send];
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (void)requestDidFinishLoad:(TTURLRequest*)request {
	TTURLJSONResponse* response = request.response;
	TTDASSERT([response.rootObject isKindOfClass:[NSDictionary class]]);
	
	NSDictionary* feed = response.rootObject;
	TTDASSERT([[feed objectForKey:@"contacts"] isKindOfClass:[NSArray class]]);
	
	NSArray* entries = [feed objectForKey:@"contacts"];
	
	TT_RELEASE_SAFELY(_contacts);
	NSMutableArray* contacts = [[NSMutableArray alloc] initWithCapacity:[entries count]];
	
	for (NSDictionary* entry in entries) {
		Contact* contact = [[Contact alloc] init];

		contact.username = [entry objectForKey:@"username"];
		contact.status = [NSNumber numberWithLongLong:[[entry objectForKey:@"status"] longLongValue]];
		
		[contacts addObject:contact];
		TT_RELEASE_SAFELY(contact);
	}
	
	_contacts = contacts;
	
	[super requestDidFinishLoad:request];
}


@end
