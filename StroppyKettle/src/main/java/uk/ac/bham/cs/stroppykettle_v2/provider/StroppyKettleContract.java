package uk.ac.bham.cs.stroppykettle_v2.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class StroppyKettleContract {

	public static final String CONTENT_AUTHORITY = "uk.ac.bham.cs.stroppykettle_v2";

	private static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	public static final String PATH_CONNECTIONS = "connections";
	public static final String PATH_SCALE = "scale";
	public static final String PATH_LOGS = "logs";
	public static final String PATH_USERS = "users";
	public static final String PATH_INTERACTIONS = "interactions";

	interface ConnectionsColumns {
		String CONNECTION_ID = BaseColumns._ID;
		String CONNECTION_TIME = "datetime";
		String CONNECTION_STATE = "state";
	}

	interface ScaleColumns {
		String SCALE_ID = BaseColumns._ID;
		String SCALE_NB_CUPS = "datetime";
		String SCALE_WEIGHT = "prev_weight";
	}

	interface LogsColumns {
		String LOG_ID = BaseColumns._ID;
		String LOG_DATETIME = "datetime";
		String LOG_PREVIOUS_WEIGHT = "prev_weight";
		String LOG_WEIGHT = "weight";
	}

	interface UsersColumns {
		String USER_ID = BaseColumns._ID;
		String USER_NAME = "name";
	}

	interface InteractionsColumns {
		String INTERACTION_ID = BaseColumns._ID;
		String INTERACTION_START_DATETIME = "start";
		String INTERACTION_STOP_DATETIME = "stop";
		String INTERACTION_CONDITION = "condition";
		String INTERACTION_NB_CUPS = "nb_cups";
		String INTERACTION_WEIGHT = "weight";
		String INTERACTION_STROPPINESS = "stroppiness";
		String INTERACTION_IS_STROPPY = "is_stroppy";
		String INTERACTION_NB_SPINS = "nb_spins";
		String INTERACTION_NB_FAILURES = "nb_failures";
		String INTERACTION_USER_ID = "user_id";
	}

	public static class Connections implements ConnectionsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_CONNECTIONS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.android.stroppykettle.connection";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.android.stroppykettle.connection";

		// Get One Connection --> Useless.
		public static Uri buildConnectionsUri(String connectionId) {
			return CONTENT_URI.buildUpon().appendPath(connectionId).build();
		}
	}

	public static class Scale implements ScaleColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_SCALE).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.android.stroppykettle.scale";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.android.stroppykettle.scale";

		// Get One Weight.
		public static Uri buildScaleUri(String scaleId) {
			return CONTENT_URI.buildUpon().appendPath(scaleId).build();
		}
	}

	public static class Logs implements LogsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_LOGS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.android.stroppykettle.log";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.android.stroppykettle.log";

		// Get One Log.
		public static Uri buildLogsUri(String logId) {
			return CONTENT_URI.buildUpon().appendPath(logId).build();
		}
	}

	public static class Users implements UsersColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_USERS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.android.stroppykettle.user";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.android.stroppykettle.user";

		// Get One User.
		public static Uri buildUsersUri(String userId) {
			return CONTENT_URI.buildUpon().appendPath(userId).build();
		}

		// Get all interactions for one user.
		public static Uri buildInteractionDirUri(String userId) {
			return CONTENT_URI.buildUpon().appendPath(userId)
					.appendPath(PATH_INTERACTIONS).build();
		}

		public static String getUserIdFromUserInteractions(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	public static class Interactions implements InteractionsColumns,
			BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_INTERACTIONS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.android.stroppykettle.interaction";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.android.stroppykettle.interaction";

		// Get one interaction.
		public static Uri buildInteractionsUri(String interactionId) {
			return CONTENT_URI.buildUpon().appendPath(interactionId).build();
		}
	}
}
