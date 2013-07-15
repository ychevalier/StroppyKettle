package uk.ac.bham.cs.stroppykettle_v2;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import uk.ac.bham.cs.stroppykettle_v2.services.WeightService;

public class StroppyKettleApplication extends Application {

	public static final boolean DEBUG_MODE = true;
	public static final String TAG = StroppyKettleApplication.class.getSimpleName();

	public static final String PREFS_NAME = "StroppyPrefs";

	public static final String PREF_CONDITION = "Condition";
	public static final String PREF_STROPPINESS = "Stroppiness";
	public static final String PREF_ADDRESS = "Address";
	public static final String PREF_PRECISION = "Precision";
	public static final String PREF_MAX_CUPS = "MaxCups";
	public static final String PREF_ALIVE_INTERVAL = "AliveInterval";
	public static final String PREF_DATA_INTERVAL = "DataInterval";
	public static final String PREF_CUPS_TIMEOUT = "CupsTimeout";
	public static final String PREF_GAME_TIMEOUT = "GameTimeout";
	public static final String PREF_BOILING_TIMEOUT = "BoilingTimeout";
	public static final String PREF_PROGRESS_TIMEOUT = "ProgressTimeout";
	public static final String PREF_GAME_MAX_SPEED = "GameMaxSpeed";

	public static final int CONDITION_LOGIN = 0;
	public static final int CONDITION_MONITORING = 1;
	public static final int CONDITION_STROPPY = 2;

	public static final int DEFAULT_CONDITION = CONDITION_STROPPY;
	public static final int DEFAULT_STROPPINESS = 100;
	public static final String DEFAULT_ADDRESS = "00:06:66:08:E7:09";
	public static final int DEFAULT_PRECISION = 10;
	public static final int DEFAULT_MAX_CUPS = 5;
	public static final int DEFAULT_ALIVE_INTERVAL = 3;
	public static final int DEFAULT_DATA_INTERVAL = 24 * 60 * 60;
	public static final int DEFAULT_CUPS_TIMEOUT = 10;
	public static final int DEFAULT_GAME_TIMEOUT = 10;
	public static final int DEFAULT_BOILING_TIMEOUT = 10;
	public static final int DEFAULT_PROGRESS_TIMEOUT = 1;
	public static final int DEFAULT_GAME_MAX_SPEED = 40;

	@Override
	public void onCreate() {
		super.onCreate();

		// TODO Temporary default values.
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREF_CONDITION, DEFAULT_CONDITION);
		editor.putInt(PREF_STROPPINESS, DEFAULT_STROPPINESS);
		editor.putString(PREF_ADDRESS, DEFAULT_ADDRESS);
		editor.putInt(PREF_PRECISION, DEFAULT_PRECISION);
		editor.putInt(PREF_MAX_CUPS, DEFAULT_MAX_CUPS);
		editor.putInt(PREF_ALIVE_INTERVAL, DEFAULT_ALIVE_INTERVAL);
		editor.putInt(PREF_DATA_INTERVAL, DEFAULT_DATA_INTERVAL);
		editor.putInt(PREF_CUPS_TIMEOUT, DEFAULT_CUPS_TIMEOUT);
		editor.putInt(PREF_GAME_TIMEOUT, DEFAULT_GAME_TIMEOUT);
		editor.putInt(PREF_BOILING_TIMEOUT, DEFAULT_BOILING_TIMEOUT);
		editor.putInt(PREF_PROGRESS_TIMEOUT, DEFAULT_PROGRESS_TIMEOUT);
		editor.putInt(PREF_GAME_MAX_SPEED, DEFAULT_GAME_MAX_SPEED);

		editor.commit();

		Intent i = new Intent(this, WeightService.class);
		startService(i);
	}

	public static int computeNbSpins(float weight, float supposedWeight, int stroppiness, int precision) {

		if (weight > supposedWeight + precision) {
			return (stroppiness / 100) * ((int) weight - ((int) supposedWeight + precision));
		} else {
			return 0;
		}
	}
}
