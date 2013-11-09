package com.glevel.wwii.database;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.glevel.wwii.database.dao.BattleDao;
import com.glevel.wwii.database.dao.CampaignDao;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final static int DATABASE_VERSION = 5;

	public static final String DB_NAME = "wwii";

	private Context mContext;
	private BattleDao mBattleDao;
	private CampaignDao mCampaignDao;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		this.mContext = context;
		mBattleDao = new BattleDao(this);
		mCampaignDao = new CampaignDao(this);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		executeSQLFile(db, "db.sql");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		executeSQLFile(db, "db.sql");
	}

	private void executeSQLFile(SQLiteDatabase db, String fileName) {
		try {
			InputStream is = mContext.getResources().getAssets().open(fileName);
			String[] statements = FileHelper.parseSqlFile(is);
			for (String statement : statements) {
				db.execSQL(statement);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called everytime the database is opened by getReadableDatabase or
	 * getWritableDatabase. This is called after onCreate or onUpgrade is
	 * called.
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	public BattleDao getBattleDao() {
		return mBattleDao;
	}

	public CampaignDao getCampaignDao() {
		return mCampaignDao;
	}

}
