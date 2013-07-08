package uk.ac.bham.cs.stroppykettle_v2;

import android.app.Application;
import android.content.Intent;

import uk.ac.bham.cs.stroppykettle_v2.services.WeightService;

public class StroppyKettleApplication extends Application {

	public static final boolean DEBUG_MODE = true;
	public static final String TAG = StroppyKettleApplication.class.getSimpleName();

	public static final String DEVICE_ADDRESS = "00:06:66:08:E7:09";

	public static final int NUMBER_OF_CUPS = 5;

	@Override
	public void onCreate() {
		super.onCreate();

		Intent i = new Intent(this, WeightService.class);
		startService(i);
	}
}
