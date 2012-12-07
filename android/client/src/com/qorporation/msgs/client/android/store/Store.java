package com.qorporation.msgs.client.android.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.qorporation.msgs.client.android.Msgs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Store {
	protected static final Context DEFAULT_CONTEXT = Msgs.INSTANCE;
	protected static final String DB_LOCATION = "/SDCard/databases/msgs/";
	
	protected int version = 1;
	protected String dbName = null;
	protected Context context = null;
	protected SQLiteDatabase db = null;
	
	public Store(Context context, String dbName, Collection<String> initStatements) {
		this.dbName = dbName;
		
		try {
			this.context = context;
			this.db = this.init(initStatements);
		} catch (Exception e) {

		}
	}

	private SQLiteDatabase init(final Collection<String> initStatements) throws Exception {
		SQLiteOpenHelper helper = new SQLiteOpenHelper(this.context, this.dbName, null, this.version) {

			private void runInitStatements(SQLiteDatabase db) {
				ArrayList<String> statements = new ArrayList<String>(Arrays.asList(
						"PRAGMA CACHE_SIZE=1000",
						"PRAGMA encoding = \"UTF-8\"",
						"PRAGMA auto_vacuum=1",
						"PRAGMA synchronous=NORMAL"));
				
				statements.addAll(initStatements);
				
				for (String statement: statements) {
					db.execSQL(statement);
				}
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				this.runInitStatements(db);
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			}
			
		};
		
		return helper.getWritableDatabase();
	}
	
}
