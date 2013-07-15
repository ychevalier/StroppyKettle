package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import uk.ac.bham.cs.stroppykettle_v2.R;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getString(R.string.action_settings));

		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//noinspection deprecation
		addPreferencesFromResource(R.xml.fragmented_preferences);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
			case R.id.action_calibration:
				i = new Intent(this, CalibrationActivity.class);
				break;
			case R.id.action_monitor:
				i = new Intent(this, MonitorActivity.class);
				break;
			case R.id.action_settings:
				// DO Nothing...
				break;
		}
		if (i != null) {
			startActivity(i);
			finish();
		}
		return true;
	}
}