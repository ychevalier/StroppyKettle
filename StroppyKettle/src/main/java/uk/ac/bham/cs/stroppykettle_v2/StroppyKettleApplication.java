package uk.ac.bham.cs.stroppykettle_v2;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import uk.ac.bham.cs.stroppykettle_v2.services.WeightService;

public class StroppyKettleApplication extends Application {

	public static final boolean DEBUG_MODE = true;
	public static final String TAG = StroppyKettleApplication.class.getSimpleName();

	public static final String PREFS_NAME = "StroppyPrefs";

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String address = settings.getString(getString(R.string.address_key), getString(R.string.address_default));
		int alive = settings.getInt(getString(R.string.alive_key), getResources().getInteger(R.integer.alive_default));

		Intent i = new Intent(this, WeightService.class);
		i.putExtra(WeightService.EXTRA_ADDRESS, address);
		i.putExtra(WeightService.EXTRA_ALIVE, alive);
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
