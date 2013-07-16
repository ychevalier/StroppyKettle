package uk.ac.bham.cs.stroppykettle_v2.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.ConnectionsColumns;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.InteractionsColumns;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.LogsColumns;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.ScaleColumns;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.UsersColumns;

public class StroppyKettleDatabase extends SQLiteOpenHelper {

	public static final Boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	protected static final String TAG = StroppyKettleDatabase.class.getSimpleName();

	public static final String DATABASE_NAME = "stroppykettle.db";
	private static final int DATABASE_VERSION = 1;

	interface Tables {
		String CONNECTIONS = "connections";
		String SCALE = "scale";
		String LOGS = "logs";
		String USERS = "users";
		String INTERACTIONS = "interactions";
	}

	public StroppyKettleDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private interface References {
		String USER_ID = "REFERENCES " + Tables.INTERACTIONS + "(" + StroppyKettleContract.Users.USER_ID + ")";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.CONNECTIONS + " ("
				+ ConnectionsColumns.CONNECTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ConnectionsColumns.CONNECTION_TIME + " LONG,"
				+ ConnectionsColumns.CONNECTION_STATE + " INTEGER)");

		db.execSQL("CREATE TABLE " + Tables.SCALE + " ("
				+ ScaleColumns.SCALE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ScaleColumns.SCALE_NB_CUPS + " INTEGER UNIQUE,"
				+ ScaleColumns.SCALE_WEIGHT + " REAL)");

		db.execSQL("CREATE TABLE " + Tables.LOGS + " ("
				+ LogsColumns.LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ LogsColumns.LOG_DATETIME + " LONG,"
				+ LogsColumns.LOG_PREVIOUS_WEIGHT + " REAL,"
				+ LogsColumns.LOG_WEIGHT + " REAL )");

		db.execSQL("CREATE TABLE " + Tables.USERS + " ("
				+ UsersColumns.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ UsersColumns.USER_NAME + " TEXT)");

		db.execSQL("CREATE TABLE " + Tables.INTERACTIONS + " ("
				+ InteractionsColumns.INTERACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ InteractionsColumns.INTERACTION_START_DATETIME + " LONG,"
				+ InteractionsColumns.INTERACTION_STOP_DATETIME + " LONG,"
				+ InteractionsColumns.INTERACTION_CONDITION + " INTEGER,"
				+ InteractionsColumns.INTERACTION_NB_CUPS + " INTEGER,"
				+ InteractionsColumns.INTERACTION_NB_SPINS + " INTEGER,"
				+ InteractionsColumns.INTERACTION_STROPPINESS + " INTEGER,"
				+ InteractionsColumns.INTERACTION_WEIGHT + " REAL,"
				+ InteractionsColumns.INTERACTION_IS_STROPPY + " INTEGER,"
				+ InteractionsColumns.INTERACTION_NB_FAILURES + " INTEGER,"
				+ InteractionsColumns.INTERACTION_USER_ID + " INTEGER " + References.USER_ID + ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (DEBUG_MODE)
			Log.d(TAG,
					"Upgrading database from version " + oldVersion
							+ " to " + newVersion
							+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.CONNECTIONS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.SCALE);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.LOGS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.USERS);
		db.execSQL("DROP TABLE IF EXISTS " + Tables.INTERACTIONS);
		onCreate(db);
	}
}