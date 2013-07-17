package uk.ac.bham.cs.stroppykettle_v2.io;

import android.content.Context;
import android.database.Cursor;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.protocols.JSONParams;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract;
import uk.ac.bham.cs.stroppykettle_v2.utils.Utils;

public class LogsSendTask extends GenericSendTask {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = LogsSendTask.class.getSimpleName();

	public LogsSendTask(int id, Context context, SentListener listener) {
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
		String[] projection = {StroppyKettleContract.Logs.LOG_ID, StroppyKettleContract.Logs.LOG_DATETIME, StroppyKettleContract.Logs.LOG_PREVIOUS_WEIGHT, StroppyKettleContract.Logs.LOG_WEIGHT};
		String selection = StroppyKettleContract.Logs.LOG_DATETIME + ">=?";
		String[] selectionArgs = {lastSending.toString()};
		String order = StroppyKettleContract.Logs.LOG_DATETIME;

		Cursor cursor = mContext.getContentResolver().query(StroppyKettleContract.Logs.CONTENT_URI, projection, selection, selectionArgs, order);
		if (cursor == null) return false;
		if (cursor.getCount() == 0) return true;

		JSONObject toSend = new JSONObject();
		JSONArray logs = new JSONArray();

		// Create the Json
		while (cursor.moveToNext()) {
			JSONObject log = new JSONObject();
			try {
				log.put(JSONParams.LOG_DATETIME, cursor.getLong(cursor.getColumnIndex(StroppyKettleContract.Logs.LOG_DATETIME)));
				log.put(JSONParams.LOG_PREVIOUS_WEIGHT, cursor.getFloat(cursor.getColumnIndex(StroppyKettleContract.Logs.LOG_PREVIOUS_WEIGHT)));
				log.put(JSONParams.LOG_WEIGHT, cursor.getFloat(cursor.getColumnIndex(StroppyKettleContract.Logs.LOG_WEIGHT)));
			} catch (JSONException e) {
				if (DEBUG_MODE) {
					e.printStackTrace();
				}
				return false;
			}
			if (DEBUG_MODE) {
				Log.d(TAG, "Added : " + log.toString());
			}
			logs.put(log);
		}

		try {
			toSend.put(JSONParams.DEVICE_ID, Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID));
			toSend.put(JSONParams.LOG_LIST, logs);
		} catch (JSONException e) {
			if (DEBUG_MODE) {
				e.printStackTrace();
			}
			return false;
		}

		// Send the JSON
		return sendJSON(toSend, Utils.LOGS_PATH);
	}
}