package uk.ac.bham.cs.stroppykettle_v2.io;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.protocols.JSONParams;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract;

public class ConnectionsSendTask extends GenericSendTask {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = ConnectionsSendTask.class.getSimpleName();

	public ConnectionsSendTask(int id, Context context, SentListener listener) {
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
		String[] projection = {StroppyKettleContract.Connections.CONNECTION_ID, StroppyKettleContract.Connections.CONNECTION_STATE, StroppyKettleContract.Connections.CONNECTION_TIME};
		String selection = StroppyKettleContract.Connections.CONNECTION_TIME + ">=?";
		String[] selectionArgs = {lastSending.toString()};
		String order = StroppyKettleContract.Connections.CONNECTION_TIME;

		Cursor cursor = mContext.getContentResolver().query(StroppyKettleContract.Connections.CONTENT_URI, projection, selection, selectionArgs, order);
		if (cursor == null) return false;

		JSONObject toSend = new JSONObject();
		JSONArray connections = new JSONArray();

		// Create the Json
		while (cursor.moveToNext()) {
			JSONObject connection = new JSONObject();
			try {
				connection.put(JSONParams.CONNECTION_DATETIME, cursor.getLong(cursor.getColumnIndex(StroppyKettleContract.Connections.CONNECTION_TIME)));
				connection.put(JSONParams.CONNECTION_STATE, cursor.getInt(cursor.getColumnIndex(StroppyKettleContract.Connections.CONNECTION_STATE)));
			} catch (JSONException e) {
				if (DEBUG_MODE) {
					e.printStackTrace();
				}
				return false;
			}
			if (DEBUG_MODE) {
				Log.d(TAG, "Added : " + connection.toString());
			}
			connections.put(connection);
		}

		try {
			toSend.put(JSONParams.CONNECTION_LIST, connections);
		} catch (JSONException e) {
			if (DEBUG_MODE) {
				e.printStackTrace();
			}
			return false;
		}

		// Send the JSON
		return sendJSON(toSend);
	}
}