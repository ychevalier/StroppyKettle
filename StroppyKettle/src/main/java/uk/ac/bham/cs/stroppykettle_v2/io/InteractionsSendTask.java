package uk.ac.bham.cs.stroppykettle_v2.io;

import android.content.Context;
import android.database.Cursor;
import android.provider.Settings.Secure;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.protocols.JSONParams;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract;
import uk.ac.bham.cs.stroppykettle_v2.utils.Utils;

public class InteractionsSendTask extends GenericSendTask {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = InteractionsSendTask.class.getSimpleName();

	public InteractionsSendTask(int id, Context context, SentListener listener) {
		super(id, context, listener);
	}

	@Override
	protected Boolean doInBackground(Long... args) {
		if (mContext == null || args == null || args[0] == null) return false;

		// Check the internet connection.
		if (!checkConnectivity()) {
			return false;
		}

		// Query the database
		Long lastSending = args[0];
		String[] projection = {
				StroppyKettleContract.Interactions.INTERACTION_ID,
				StroppyKettleContract.Interactions.INTERACTION_START_DATETIME,
				StroppyKettleContract.Interactions.INTERACTION_STOP_DATETIME,
				StroppyKettleContract.Interactions.INTERACTION_NB_FAILURES,
				StroppyKettleContract.Interactions.INTERACTION_CONDITION,
				StroppyKettleContract.Interactions.INTERACTION_IS_STROPPY,
				StroppyKettleContract.Interactions.INTERACTION_NB_CUPS,
				StroppyKettleContract.Interactions.INTERACTION_NB_SPINS,
				StroppyKettleContract.Interactions.INTERACTION_STROPPINESS,
				StroppyKettleContract.Interactions.INTERACTION_USER_ID,
				StroppyKettleContract.Interactions.INTERACTION_WEIGHT
		};
		String selection = StroppyKettleContract.Interactions.INTERACTION_START_DATETIME + ">=?";
		String[] selectionArgs = {lastSending.toString()};
		String order = StroppyKettleContract.Interactions.INTERACTION_START_DATETIME;

		Cursor cursor = mContext.getContentResolver().query(StroppyKettleContract.Interactions.CONTENT_URI, projection, selection, selectionArgs, order);
		if (cursor == null) return false;
		if (cursor.getCount() == 0) return true;

		JSONObject toSend = new JSONObject();
		JSONArray interactions = new JSONArray();

		// Create the Json
		while (cursor.moveToNext()) {
			JSONObject interaction = new JSONObject();
			try {
				interaction.put(JSONParams.INTERACTION_START_DATETIME, cursor.getLong(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_START_DATETIME)));
				interaction.put(JSONParams.INTERACTION_STOP_DATETIME, cursor.getLong(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_STOP_DATETIME)));
				interaction.put(JSONParams.INTERACTION_NB_FAILURES, cursor.getInt(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_NB_FAILURES)));
				interaction.put(JSONParams.INTERACTION_CONDITION, cursor.getInt(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_CONDITION)));
				interaction.put(JSONParams.INTERACTION_IS_STROPPY, cursor.getInt(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_IS_STROPPY)));
				interaction.put(JSONParams.INTERACTION_NB_CUPS, cursor.getInt(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_NB_CUPS)));
				interaction.put(JSONParams.INTERACTION_NB_SPINS, cursor.getInt(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_NB_SPINS)));
				interaction.put(JSONParams.INTERACTION_STROPPINESS, cursor.getInt(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_STROPPINESS)));
				interaction.put(JSONParams.INTERACTION_USER_ID, cursor.getLong(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_USER_ID)));
				interaction.put(JSONParams.INTERACTION_WEIGHT, cursor.getFloat(cursor.getColumnIndex(StroppyKettleContract.Interactions.INTERACTION_WEIGHT)));
			} catch (JSONException e) {
				if (DEBUG_MODE) {
					e.printStackTrace();
				}
				return false;
			}
			if (DEBUG_MODE) {
				Log.d(TAG, "Added : " + interaction.toString());
			}
			interactions.put(interaction);
		}

		try {
			toSend.put(JSONParams.DEVICE_ID, Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID));
			toSend.put(JSONParams.INTERACTION_LIST, interactions);
		} catch (JSONException e) {
			if (DEBUG_MODE) {
				e.printStackTrace();
			}
			return false;
		}

		// Send the JSON
		return sendJSON(toSend, Utils.INTERACTIONS_PATH);
	}
}