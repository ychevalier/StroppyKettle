package uk.ac.bham.cs.stroppykettle_v2.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class StroppyKettleContract {

	public static final String CONTENT_AUTHORITY = "uk.ac.bham.cs.stroppykettle_v2";

	private static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	public static final String PATH_USERS = "users";
	public static final String PATH_INTERACTIONS = "interactions";

	interface UsersColumns {
		String USER_ID = "_id";
		String USER_NAME = "name";
	}

	interface InteractionsColumns {
		String INTERACTION_ID = "_id";
		String INTERACTION_DATETIME = "datetime";
		String INTERACTION_CONDITION = "condition";
		String INTERACTION_NB_CUPS = "nb_cups";
		String INTERACTION_WEIGHT = "weight";
		String INTERACTION_USER_ID = "user_id";
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
