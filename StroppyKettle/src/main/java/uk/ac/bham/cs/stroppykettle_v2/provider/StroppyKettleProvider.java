package uk.ac.bham.cs.stroppykettle_v2.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.Interactions;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.Logs;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.Scale;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.Users;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleDatabase.Tables;

public class StroppyKettleProvider extends ContentProvider {

	public static final Boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	protected static final String TAG = StroppyKettleProvider.class
			.getSimpleName();

	private static final int LOGS = 10;
	private static final int LOGS_ID = 11;

	private static final int USERS = 20;
	private static final int USERS_ID = 21;
	private static final int USERS_ID_INTERACTIONS = 22;

	private static final int INTERACTIONS = 30;
	private static final int INTERACTIONS_ID = 31;

	private static final int SCALE = 40;
	private static final int SCALE_ID = 41;

	private static final int CONNECTIONS = 50;
	private static final int CONNECTIONS_ID = 51;

	static final String UNDERSCORE = "_";
	static final String SLASH = "/";
	static final String STAR = "*";
	static final String ALL = "all";

	private StroppyKettleDatabase dbHelper;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = StroppyKettleContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, StroppyKettleContract.PATH_CONNECTIONS, CONNECTIONS);
		matcher.addURI(authority, StroppyKettleContract.PATH_CONNECTIONS + SLASH
				+ STAR, CONNECTIONS_ID);

		matcher.addURI(authority, StroppyKettleContract.PATH_SCALE, SCALE);
		matcher.addURI(authority, StroppyKettleContract.PATH_SCALE + SLASH
				+ STAR, SCALE_ID);

		matcher.addURI(authority, StroppyKettleContract.PATH_LOGS, LOGS);
		matcher.addURI(authority, StroppyKettleContract.PATH_LOGS + SLASH
				+ STAR, LOGS_ID);

		matcher.addURI(authority, StroppyKettleContract.PATH_USERS, USERS);
		matcher.addURI(authority, StroppyKettleContract.PATH_USERS + SLASH
				+ STAR, USERS_ID);
		matcher.addURI(authority, StroppyKettleContract.PATH_USERS + SLASH
				+ STAR + SLASH + StroppyKettleContract.PATH_INTERACTIONS,
				USERS_ID_INTERACTIONS);

		matcher.addURI(authority, StroppyKettleContract.PATH_INTERACTIONS,
				INTERACTIONS);
		matcher.addURI(authority, StroppyKettleContract.PATH_INTERACTIONS + SLASH
				+ STAR, INTERACTIONS_ID);

		return matcher;
	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case CONNECTIONS:
				return StroppyKettleContract.Connections.CONTENT_TYPE;
			case CONNECTIONS_ID:
				return StroppyKettleContract.Connections.CONTENT_ITEM_TYPE;
			case SCALE:
				return StroppyKettleContract.Scale.CONTENT_TYPE;
			case SCALE_ID:
				return StroppyKettleContract.Scale.CONTENT_ITEM_TYPE;
			case LOGS:
				return StroppyKettleContract.Logs.CONTENT_TYPE;
			case LOGS_ID:
				return StroppyKettleContract.Logs.CONTENT_ITEM_TYPE;
			case USERS:
				return StroppyKettleContract.Users.CONTENT_TYPE;
			case USERS_ID:
				return StroppyKettleContract.Users.CONTENT_ITEM_TYPE;
			case USERS_ID_INTERACTIONS:
				return StroppyKettleContract.Interactions.CONTENT_TYPE;
			case INTERACTIONS:
				return StroppyKettleContract.Interactions.CONTENT_TYPE;
			case INTERACTIONS_ID:
				return StroppyKettleContract.Interactions.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		dbHelper = new StroppyKettleDatabase(getContext());
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		long id = -1;

		switch (match) {
			case CONNECTIONS:
			case CONNECTIONS_ID:
				id = db.insertOrThrow(Tables.CONNECTIONS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(uri, id);
			case SCALE:
			case SCALE_ID:
				// Insert if not in db, otherwise update nbCups.
				try {
					id = db.insertOrThrow(Tables.SCALE, null, values);
				} catch (SQLiteConstraintException e) {
					db.update(Tables.SCALE, values, Scale.SCALE_NB_CUPS + "=" + values.get(Scale.SCALE_NB_CUPS), null);
				}
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(uri, id);
			case LOGS:
			case LOGS_ID:
				id = db.insertOrThrow(Tables.LOGS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(uri, id);
			case USERS:
			case USERS_ID:
				id = db.insertOrThrow(Tables.USERS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(uri, id);
			case INTERACTIONS:
			case INTERACTIONS_ID:
				id = db.insertOrThrow(Tables.INTERACTIONS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return ContentUris.withAppendedId(uri, id);
			case USERS_ID_INTERACTIONS:
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {

		String seg = uri.getLastPathSegment();
		Integer id = Integer.valueOf(seg);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		switch (match) {
			case CONNECTIONS:
			case CONNECTIONS_ID:
				try {
					return db.update(Tables.CONNECTIONS, values,
							StroppyKettleContract.Connections.CONNECTION_ID + "=" + id, null);
				} catch (Exception e) {
					return db.update(Tables.CONNECTIONS, values, selection,
							selectionArgs);
				}
			case SCALE:
			case SCALE_ID:
				try {
					return db.update(Tables.SCALE, values,
							Scale.SCALE_ID + "=" + id, null);
				} catch (Exception e) {
					return db.update(Tables.SCALE, values, selection,
							selectionArgs);
				}
			case LOGS:
			case LOGS_ID:
				try {
					return db.update(Tables.LOGS, values,
							Logs.LOG_ID + "=" + id, null);
				} catch (Exception e) {
					return db.update(Tables.LOGS, values, selection,
							selectionArgs);
				}
			case USERS:
			case USERS_ID:
				try {
					return db.update(Tables.USERS, values,
							Users.USER_ID + "=" + id, null);
				} catch (Exception e) {
					return db.update(Tables.USERS, values, selection,
							selectionArgs);
				}
			case INTERACTIONS:
			case INTERACTIONS_ID:
				try {
					return db.update(Tables.INTERACTIONS, values, Interactions.INTERACTION_ID
							+ "=" + id, null);
				} catch (Exception e) {
					return db.update(Tables.INTERACTIONS, values, selection,
							selectionArgs);
				}
			case USERS_ID_INTERACTIONS:
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String seg = uri.getLastPathSegment();
		Integer id = Integer.valueOf(seg);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		switch (match) {
			case CONNECTIONS:
			case CONNECTIONS_ID:
				try {
					return db.delete(Tables.CONNECTIONS, StroppyKettleContract.Connections.CONNECTION_ID + "=" + id, null);
				} catch (Exception e) {
					return db.delete(Tables.CONNECTIONS, selection, selectionArgs);
				}
			case SCALE:
			case SCALE_ID:
				try {
					return db.delete(Tables.SCALE, Scale.SCALE_ID + "=" + id, null);
				} catch (Exception e) {
					return db.delete(Tables.SCALE, selection, selectionArgs);
				}
			case LOGS:
			case LOGS_ID:
				try {
					return db.delete(Tables.LOGS, Logs.LOG_ID + "=" + id, null);
				} catch (Exception e) {
					return db.delete(Tables.LOGS, selection, selectionArgs);
				}
			case USERS:
			case USERS_ID:
				try {
					return db.delete(Tables.USERS, Users.USER_ID + "=" + id, null);
				} catch (Exception e) {
					return db.delete(Tables.USERS, selection, selectionArgs);
				}
			case INTERACTIONS:
			case INTERACTIONS_ID:
				try {
					return db.delete(Tables.INTERACTIONS, Interactions.INTERACTION_ID + "="
							+ id, null);
				} catch (Exception e) {
					return db.delete(Tables.INTERACTIONS, selection, selectionArgs);
				}
			case USERS_ID_INTERACTIONS:
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		String groupBy = null;
		String having = null;

		int uriType = sUriMatcher.match(uri);
		switch (uriType) {
			case CONNECTIONS_ID:
				queryBuilder.setTables(Tables.CONNECTIONS);
				queryBuilder.appendWhere(StroppyKettleContract.Connections.CONNECTION_ID + "="
						+ uri.getLastPathSegment());
				break;
			case CONNECTIONS:
				queryBuilder.setTables(Tables.CONNECTIONS);
				break;
			case SCALE_ID:
				queryBuilder.setTables(Tables.SCALE);
				queryBuilder.appendWhere(Scale.SCALE_ID + "="
						+ uri.getLastPathSegment());
				break;
			case SCALE:
				queryBuilder.setTables(Tables.SCALE);
				break;
			case LOGS_ID:
				queryBuilder.setTables(Tables.LOGS);
				queryBuilder.appendWhere(Logs.LOG_ID + "="
						+ uri.getLastPathSegment());
				break;
			case LOGS:
				queryBuilder.setTables(Tables.LOGS);
				break;
			case USERS_ID:
				queryBuilder.setTables(Tables.USERS);
				queryBuilder.appendWhere(Users.USER_ID + "="
						+ uri.getLastPathSegment());
				break;
			case USERS:
				queryBuilder.setTables(Tables.USERS);
				break;
			case INTERACTIONS_ID:
				queryBuilder.appendWhere(Interactions.INTERACTION_ID + "="
						+ uri.getLastPathSegment());
				break;
			case INTERACTIONS:
				queryBuilder.setTables(Tables.INTERACTIONS);
				break;
			case USERS_ID_INTERACTIONS:
				queryBuilder.setTables(Tables.INTERACTIONS);
				queryBuilder.appendWhere(Interactions.INTERACTION_USER_ID + "="
						+ StroppyKettleContract.Users.getUserIdFromUserInteractions(uri));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI : " + uri);
		}

		Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
				projection, selection, selectionArgs, groupBy, having, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
}
