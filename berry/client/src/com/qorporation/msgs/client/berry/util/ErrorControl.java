package com.qorporation.msgs.client.berry.util;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

public class ErrorControl {

	public static void logException(Throwable e) {
    	final String errorString = e.toString();
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                Dialog.alert(errorString);        
            } 
        });
	}
	
}
