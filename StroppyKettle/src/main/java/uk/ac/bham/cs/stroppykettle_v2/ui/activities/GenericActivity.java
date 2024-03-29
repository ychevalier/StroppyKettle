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
import android.preference.PreferenceManager;
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

	private ConnectionReceiver mConnectionReceiver;
	private WeightReceiver mWeightReceiver;

	// Service.
	protected Messenger mService = null;
	protected boolean mBound;

	// Loading UI.
	protected boolean mIsRefreshing;
	private ProgressDialog mProgressDialog;

	// Parameters that are shared.
	protected int mStroppiness;
	protected int mCondition;
	protected String mAddress;
	protected int mPrecision;
	protected int mMaxCups;
	protected int mAliveInterval;
	protected int mDataInterval;
	protected int mCupsTimeout;
	protected int mGameTimeout;
	protected int mBoilingTimeout;
	protected int mProgressTimeout;
	protected int mGameMaxSpeed;

	protected boolean mIsConnected;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			mBound = true;
			onServiceBound();
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			mBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mConnectionReceiver = new ConnectionReceiver();
		mWeightReceiver = new WeightReceiver();
		mIsRefreshing = false;
		mIsConnected = false;

		// Restore preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		mStroppiness = settings.getInt(getString(R.string.stroppiness_key), getResources().getInteger(R.integer.stroppiness_default));
		mCondition = settings.getInt(getString(R.string.condition_key), getResources().getInteger(R.integer.condition_default));
		mAddress = settings.getString(getString(R.string.address_key), getString(R.string.address_default));
		mPrecision = settings.getInt(getString(R.string.precision_key), getResources().getInteger(R.integer.precision_default));
		mMaxCups = settings.getInt(getString(R.string.max_cups_key), getResources().getInteger(R.integer.max_cups_default));
		mAliveInterval = settings.getInt(getString(R.string.alive_key), getResources().getInteger(R.integer.alive_default));
		mDataInterval = settings.getInt(getString(R.string.data_key), getResources().getInteger(R.integer.data_default));
		mCupsTimeout = settings.getInt(getString(R.string.cups_timeout_key), getResources().getInteger(R.integer.cups_timeout_default));
		mGameTimeout = settings.getInt(getString(R.string.game_timeout_key), getResources().getInteger(R.integer.game_timeout_default));
		mBoilingTimeout = settings.getInt(getString(R.string.boiling_timeout_key), getResources().getInteger(R.integer.boiling_timeout_default));
		mProgressTimeout = settings.getInt(getString(R.string.progress_timeout_key), getResources().getInteger(R.integer.progress_timeout_default));
		mGameMaxSpeed = settings.getInt(getString(R.string.game_max_speed_key), getResources().getInteger(R.integer.game_max_speed_default));
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

		registerReceiver(mConnectionReceiver, new IntentFilter(ReceiverList.CONNECTION_RECEIVER));
		registerReceiver(mWeightReceiver, new IntentFilter(ReceiverList.WEIGHT_RECEIVER));

		bindService(new Intent(this, WeightService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	// This is the soonest time
	// we can send something to the service.
	protected void onServiceBound() {
		getConnectionState();
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterReceiver(mConnectionReceiver);
		unregisterReceiver(mWeightReceiver);

		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	protected void setRefreshing(boolean enable) {
		if (enable && !mIsRefreshing) {
			mProgressDialog = ProgressDialog.show(this, "",
					"Loading...", true);
		} else if (!enable && mIsRefreshing) {
			mProgressDialog.dismiss();
		}
		mIsRefreshing = enable;
	}

	protected void getConnectionState() {
		sendMessage(WeightService.MSG_GET_CONNECTION_STATE);
	}

	protected void sendPowerMessage(boolean onoff) {
		sendMessage(WeightService.MSG_TOGGLE_POWER, onoff ? 1 : 0);
	}

	protected void getCurrentWeight() {
		sendMessage(WeightService.MSG_GET_CURRENT);
	}

	protected void connectNewAddress() {
		sendMessage(WeightService.MSG_CONNECT_NEW);
	}

	protected void connect() {
		sendMessage(WeightService.MSG_CONNECT);
	}

	protected void disconnect() {
		sendMessage(WeightService.MSG_DISCONNECT);
	}

	private void sendMessage(int msgType) {
		sendMessage(msgType, 0, null);
	}

	private void sendMessage(int msgType, int arg) {
		sendMessage(msgType, arg, null);
	}

	private void sendMessage(int msgType, int arg, String address) {
		if (!mBound) return;
		// Create and send a message to the service, using a supported 'what' value
		Message msg = Message.obtain(null, msgType, arg, 0, address);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected void onReceiveNewWeight(float weight) {
	}

	protected void onConnect() {
	}

	protected void onDisconnect() {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
			//case R.id.action_calibration:
			//	if (!(this instanceof CalibrationActivity)) {
			//		i = new Intent(this, CalibrationActivity.class);
			//	}
			//	break;
			case R.id.action_monitor:
				if (!(this instanceof MonitorActivity)) {
					i = new Intent(this, MonitorActivity.class);
				}
				break;
			case R.id.action_settings:
				if (!(this instanceof SettingsActivity)) {
					i = new Intent(this, SettingsActivity.class);
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

	public class ConnectionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(ReceiverList.EXTRA_STATE, 0);
			mIsConnected = state == 1;

			if (mIsConnected) {
				onConnect();
			} else {
				onDisconnect();
			}
			//Toast.makeText(GenericActivity.this, mIsConnected ? "Connected" : "Disconnected", Toast.LENGTH_SHORT).show();
		}
	}

	public class WeightReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			float weight = intent.getFloatExtra(ReceiverList.EXTRA_WEIGHT, 0f);
			onReceiveNewWeight(weight);
		}
	}
}
