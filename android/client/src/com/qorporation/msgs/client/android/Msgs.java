package com.qorporation.msgs.client.android;

import android.app.Activity;
import android.os.Bundle;

public class Msgs extends Activity {
	public static Msgs INSTANCE = null;
	public static final String HOST = "127.0.0.1";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        INSTANCE = this;
        setContentView(R.layout.main);
    }
}