package uk.ac.bham.cs.stroppykettle_v2.provider;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.InteractionsColumns;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.UsersColumns;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StroppyKettleDatabase extends SQLiteOpenHelper {
	
	public static final Boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	protected static final String TAG = StroppyKettleDatabase.class.getSimpleName();
	
	private static final String DATABASE_NAME = "stroppykettle.db";
	private static final int DATABASE_VERSION = 1;

	interface Tables {
		String USERS = "users";
		String INTERACTIONS = "login";
	}

	public StroppyKettleDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	private interface References {
		String USER_ID = "REFERENCES " + Tables.INTERACTIONS + "(" + StroppyKettleContract.Users.USER_ID + ")";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.USERS + " ("
				+ UsersColumns.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ UsersColumns.USER_NAME + " TEXT)");
		
		db.execSQL("CREATE TABLE " + Tables.INTERACTIONS + " ("
				+ InteractionsColumns.INTERACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ InteractionsColumns.INTERACTION_DATETIME + " LONG,"
				+ InteractionsColumns.INTERACTION_CONDITION + " INTEGER,"
				+ InteractionsColumns.INTERACTION_NB_CUPS + " INTEGER,"
				+ InteractionsColumns.INTERACTION_WEIGHT + " INTEGER,"
				+ InteractionsColumns.INTERACTION_USER_ID + " INTEGER " + References.USER_ID + ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (DEBUG_MODE)
			Log.d(TAG,
					"Upgrading database from version " + oldVersion
							+ " to " + newVersion
							+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.USERS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.INTERACTIONS);
		onCreate(db);
	}
}