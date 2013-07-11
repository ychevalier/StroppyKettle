package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.receivers.ReceiverList;
import uk.ac.bham.cs.stroppykettle_v2.services.WeightService;

public abstract class GenericActivity extends FragmentActivity {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = GenericActivity.class.getSimpleName();

	private WeightReceiver mWeightReceiver;

	// Messenger for communicating with the service.
	protected Messenger mService = null;

	// Flag indicating whether we have called bind on the service.
	protected boolean mBound;

	protected boolean mIsRefreshing;
	private ProgressDialog mProgressDialog;

	protected int mStroppiness;
	protected int mCondition;
	protected String mAddress;
	protected int mPrecision;
	protected int mMaxCups;
	protected int mAliveInterval;
	protected int mDataInterval;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			mBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mWeightReceiver = new WeightReceiver();
		mIsRefreshing = false;

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(StroppyKettleApplication.PREFS_NAME, 0);
		mStroppiness = settings.getInt(StroppyKettleApplication.PREF_STROPPINESS, StroppyKettleApplication.DEFAULT_STROPPINESS);
		mCondition = settings.getInt(StroppyKettleApplication.PREF_CONDITION, StroppyKettleApplication.DEFAULT_CONDITION);
		mAddress = settings.getString(StroppyKettleApplication.PREF_ADDRESS, StroppyKettleApplication.DEFAULT_ADDRESS);
		mPrecision = settings.getInt(StroppyKettleApplication.PREF_PRECISION, StroppyKettleApplication.DEFAULT_PRECISION);
		mMaxCups = settings.getInt(StroppyKettleApplication.PREF_MAX_CUPS, StroppyKettleApplication.DEFAULT_MAX_CUPS);
		mAliveInterval = settings.getInt(StroppyKettleApplication.PREF_ALIVE_INTERVAL, StroppyKettleApplication.DEFAULT_ALIVE_INTERVAL);
		mDataInterval = settings.getInt(StroppyKettleApplication.PREF_DATA_INTERVAL, StroppyKettleApplication.DEFAULT_DATA_INTERVAL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();

		registerReceiver(mWeightReceiver, new IntentFilter(
				ReceiverList.WEIGHT_RECEIVER));

		bindService(new Intent(this, WeightService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterReceiver(mWeightReceiver);

		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	protected void setRefreshing(boolean enable) {
		if(enable && !mIsRefreshing) {
			mProgressDialog = ProgressDialog.show(this, "",
				"Loading...", true);
		} else if(!enable && mIsRefreshing){
			mProgressDialog.dismiss();
		}
		mIsRefreshing = enable;
	}

	protected void sendPowerMessage(boolean onoff) {
		sendMessage(WeightService.MSG_TOGGLE_POWER, onoff ? 1 : 0);
	}

	protected void getCurrentWeight() {
		sendMessage(WeightService.MSG_GET_CURRENT);
	}

	protected void connect() {
		sendMessage(WeightService.MSG_CONNECT);
	}

	protected void disconnect() {
		sendMessage(WeightService.MSG_DISCONNECT);
	}

	private void sendMessage(int msgType) {
		sendMessage(msgType, 0);
	}

	private void sendMessage(int msgType, int arg) {
		if (!mBound) return;
		// Create and send a message to the service, using a supported 'what' value
		Message msg = Message.obtain(null, msgType, arg, 0);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected abstract void receivedNewWeight(float weight);

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
			case R.id.action_calibration:
				if (!(this instanceof CalibrationActivity)) {
					i = new Intent(this, CalibrationActivity.class);
				}
				break;
			case R.id.action_monitor:
				if (!(this instanceof MonitorActivity)) {
					i = new Intent(this, MonitorActivity.class);
				}
				break;
		}
		if (i != null) {
			startActivity(i);

			// We don't want to finish the main activity.
			// Not really classy...
			if (!(this instanceof AdminActivity)) {
				finish();
			}
		}
		return true;
	}

	public class WeightReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			float weight = intent.getFloatExtra(ReceiverList.EXTRA_WEIGHT, 0f);
			receivedNewWeight(weight);
		}
	}
}
