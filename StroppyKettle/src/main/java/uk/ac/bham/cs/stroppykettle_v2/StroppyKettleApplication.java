package uk.ac.bham.cs.stroppykettle_v2;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import uk.ac.bham.cs.stroppykettle_v2.services.DataService;
import uk.ac.bham.cs.stroppykettle_v2.services.WeightService;

public class StroppyKettleApplication extends Application {

	public static final boolean DEBUG_MODE = true;
	public static final String TAG = StroppyKettleApplication.class.getSimpleName();

	// TODO
	public static final String SERVER_URL = "http://example.com";

	@Override
	public void onCreate() {
		super.onCreate();

		// == Arduino Service == //
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String address = settings.getString(getString(R.string.address_key), getString(R.string.address_default));
		int alive = settings.getInt(getString(R.string.alive_key), getResources().getInteger(R.integer.alive_default));

		Intent weightService = new Intent(this, WeightService.class);
		weightService.putExtra(WeightService.EXTRA_ADDRESS, address);
		weightService.putExtra(WeightService.EXTRA_ALIVE, alive);
		startService(weightService);

		// == Data Sending Service == //
		int dataInterval = settings.getInt(getString(R.string.data_key), getResources().getInteger(R.integer.data_default));
		Calendar cal = Calendar.getInstance();
		long time = cal.getTimeInMillis();

		PendingIntent dataIntent = PendingIntent.getService(this, 0, new Intent(this, DataService.class), 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time + dataInterval * 60 * 60 * 1000, dataInterval * 60 * 60 * 1000, dataIntent);
	}

	public static int computeNbSpins(float weight, float supposedWeight, int stroppiness, int precision) {
		if (weight > supposedWeight + precision) {
			return (stroppiness / 100) * ((int) weight - ((int) supposedWeight + precision));
		} else {
			return 0;
		}
	}
}
