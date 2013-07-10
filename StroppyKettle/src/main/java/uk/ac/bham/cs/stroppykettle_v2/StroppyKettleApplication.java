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

	public static final String DEVICE_ADDRESS = "00:06:66:08:E7:09";

	public static final int NUMBER_OF_CUPS = 5;

	private static final int STEP = 10;

	public static final int CONDITION_LOGIN = 0;
	public static final int CONDITION_MONITORING = 1;
	public static final int CONDITION_STROPPY = 2;

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREF_CONDITION, CONDITION_STROPPY);
		editor.putInt(PREF_STROPPINESS, 10);

		editor.commit();

		Intent i = new Intent(this, WeightService.class);
		startService(i);
	}

	public static int computeNbSpins(float weight, float supposedWeight, int stroppiness) {

		if(weight > supposedWeight + STEP) {
			return stroppiness + (int)weight - ((int)supposedWeight + STEP);
		} else {
			return 0;
		}

		// TODO Compute if we need stroppyness & discrepency.
		//Random random = new Random();
		//return random.nextInt(100);
	}
}
