package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class GenericActivity extends FragmentActivity {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = GenericActivity.class.getSimpleName();

	// private ConnectionReceiver mAmarinoReceiver;
	// protected boolean mIsConnected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mAmarinoReceiver = new ConnectionReceiver();
		// mIsConnected = false;
	}

	@Override
	protected void onStart() {
		super.onStart();

		/*
		 * registerReceiver(mAmarinoReceiver, new IntentFilter(
		 * AmarinoIntent.ACTION_CONNECTED)); registerReceiver(mAmarinoReceiver,
		 * new IntentFilter( AmarinoIntent.ACTION_CONNECTION_FAILED));
		 * registerReceiver(mAmarinoReceiver, new IntentFilter(
		 * AmarinoIntent.ACTION_DISCONNECTED));
		 * registerReceiver(mAmarinoReceiver, new IntentFilter(
		 * AmarinoIntent.ACTION_PAIRING_REQUESTED));
		 */

		// TODO : No more bluetooth for now
		// Amarino.connect(this, StroppyKettleApplication.DEVICE_ADDRESS);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// unregisterReceiver(mAmarinoReceiver);
	}

	/*
	 * private class ConnectionReceiver extends BroadcastReceiver {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) { if
	 * (intent.getAction().equals(AmarinoIntent.ACTION_CONNECTED)) { if
	 * (DEBUG_MODE) { Log.d(TAG, "I am connected"); } mIsConnected = true; }
	 * else { if (DEBUG_MODE) { Log.d(TAG, "Trouble, I can't connect..."); }
	 * mIsConnected = false; } } }
	 */

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
		case R.id.action_settings:
			i = new Intent(this, SettingsActivity.class);
			break;
		case R.id.action_monitor:
			i = new Intent(this, MonitorActivity.class);
			break;
		}
		if (i != null) {
			startActivity(i);
			
			// We don't want to finish the main activity.
			// Not really classy...
			if(!(this instanceof MainActivity)) {
				finish();
			}
		}
		return true;
	}
}
