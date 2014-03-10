package com.beatsportable.beats;

import java.util.Arrays;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Scoreboard extends SQLiteOpenHelper implements Runnable {
	
	private static final String KEY_MD5 = "md5";
	private static final String KEY_SCORE = "score";

	private static final String DATABASE_NAME = "Beats";
	private static final String TABLE_NAME = "LocalHighScore";
	private static final int DATABASE_VERSION = 1;
 
	private static final String CMD_TABLE_CREATE = "create table if not exists " + TABLE_NAME +  
		  "(_id integer  primary key autoincrement, " + KEY_MD5 + " text not null, " + KEY_SCORE + " integer not null);";
	
	public String md5;
	
	public Scoreboard(String md5) {
		super(Tools.c.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
		this.md5 = md5;
	}
	
	@Override
	public void onCreate(SQLiteDatabase d) {
		d.execSQL(CMD_TABLE_CREATE);
	}
	
	@Override
	public void onOpen(SQLiteDatabase d) {
		super.onOpen(d);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// Unimplemented
	}
	
	public int getScore() {
		Cursor query = getWritableDatabase().query(
				TABLE_NAME,
				new String [] {KEY_MD5, KEY_SCORE},
				KEY_MD5 + " = '" + md5 + "'",
				null, null,null,null,null
				);
		if (query.moveToFirst()) {
			String [] columnsRet = query.getColumnNames();
			int scoreIndex = Arrays.binarySearch(columnsRet, KEY_SCORE);
			if (scoreIndex > -1) {
				return query.getInt(scoreIndex);
			}
		}
		return 0;
	}
	
	private int newScore;
	public void setScore(int score) {
		newScore = score;
		new Thread(this).start();
	}
	
	public static void clearScores() {
		Tools.c.deleteDatabase(DATABASE_NAME);
	}

	public void run() {
		try {
			Thread.sleep(500);
		} catch (Exception e) {};
		ContentValues inputs = new ContentValues();
		inputs.put(KEY_MD5, md5);
		inputs.put(KEY_SCORE, newScore);
		getWritableDatabase().delete(
				TABLE_NAME,
				KEY_MD5 + " = '" + md5 + "'",
				null
				);
		getWritableDatabase().insert(TABLE_NAME, null, inputs);
		
	}
	
}
