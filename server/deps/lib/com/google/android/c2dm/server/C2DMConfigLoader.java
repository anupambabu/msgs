/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.c2dm.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Stores config information related to Android Cloud to Device Messaging.
 * 
 */
class C2DMConfigLoader {

    C2DMConfig dmConfig;
    String currentToken;

    public C2DMConfigLoader() {
    	try {
	    	dmConfig = new C2DMConfig();
	        InputStream is = this.getClass().getClassLoader().getResourceAsStream("/dataMessagingToken.txt");
	        if (is != null) {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	            String token = reader.readLine();
	            dmConfig.setAuthToken(token);
	        }
    	} catch (Exception e) {
    		
    	}
    }
    
    /**
     * Update the token. 
     * 
     * Called on "Update-Client-Auth" or when admins set a new token.
     * @param token
     */
    public void updateToken(String token) {
        if (token != null) {
            currentToken = token;
            getC2DMConfig().setAuthToken(token);
        }
    }
    
    /** 
     * Token expired
     */
    public void invalidateCachedToken() {
        currentToken = null;
    }

    /**
     * Return the auth token.
     *
     * It'll first memcache, if not found will use the database. 
     * 
     * @return
     */
    public String getToken() {
        if (currentToken == null) {
            currentToken = getC2DMConfig().getAuthToken();
        } 
        return currentToken;
    }

    private C2DMConfig getC2DMConfig() {
        return dmConfig;
    }
}
