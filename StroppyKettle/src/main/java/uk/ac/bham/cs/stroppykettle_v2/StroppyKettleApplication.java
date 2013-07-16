package uk.ac.bham.cs.stroppykettle_v2;

import android.app.Application;
import android.content.Intent;

import uk.ac.bham.cs.stroppykettle_v2.services.WeightService;

public class StroppyKettleApplication extends Application {

	public static final boolean DEBUG_MODE = true;
	public static final String TAG = StroppyKettleApplication.class.getSimpleName();

	public static final String PREFS_NAME = "StroppyPrefs";

	@Override
	public void onCreate() {
		super.onCreate();

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
