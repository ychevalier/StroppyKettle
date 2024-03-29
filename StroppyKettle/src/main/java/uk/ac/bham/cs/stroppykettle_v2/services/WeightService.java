package uk.ac.bham.cs.stroppykettle_v2.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.util.Calendar;
import java.util.Map;

import at.abraxas.amarino.AmarinoHelper;
import at.abraxas.amarino.AmarinoListener;
import at.abraxas.amarino.AmarinoReceiver;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.protocols.BluetoothSerial;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract;
import uk.ac.bham.cs.stroppykettle_v2.receivers.ReceiverList;

public class WeightService extends Service implements AmarinoListener {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = WeightService.class.getSimpleName();

	public static final String EXTRA_ADDRESS = "uk.ac.bham.cs.stroppykettle_v2.services.WeightService.EXTRA_ADDRESS";
	public static final String EXTRA_ALIVE = "uk.ac.bham.cs.stroppykettle_v2.services.WeightService.EXTRA_ALIVE";

	public static final int MSG_TOGGLE_POWER = 0;
	public static final int MSG_GET_CURRENT = 1;
	public static final int MSG_CONNECT = 2;
	public static final int MSG_DISCONNECT = 3;
	public static final int MSG_ALIVE = 4;
	public static final int MSG_GET_CONNECTION_STATE = 5;
	public static final int MSG_CONNECT_NEW = 6;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private Messenger mMessenger;

	private AmarinoReceiver mReceiver;

	private float mLastWeight;

	private Handler mHandler;

	private boolean mHasAskedDisconnection;

	private String mAddress;

	private int mAliveInterval;

	private boolean mIsConnected;

	Runnable mAliveTask = new Runnable() {
		public void run() {
			interactWithArduino(MSG_ALIVE);
			mHandler.postDelayed(mAliveTask, mAliveInterval * 1000);
		}
	};

	Runnable mReconnectTask = new Runnable() {
		@Override
		public void run() {
			interactWithArduino(MSG_CONNECT);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		mMessenger = new Messenger(new IncomingHandler());

		mReceiver = new AmarinoReceiver();

		AmarinoHelper.registerListener(this, this);

		mLastWeight = 0;

		mHandler = new Handler();

		mIsConnected = false;

		mHasAskedDisconnection = false;

		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		//mAddress = settings.getString(getString(R.string.address_key), getString(R.string.address_default));
		//mAliveInterval = settings.getInt(getString(R.string.alive_key), getResources().getInteger(R.integer.alive_default));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			mAddress = intent.getStringExtra(EXTRA_ADDRESS);
			mAliveInterval = intent.getIntExtra(EXTRA_ALIVE, 0);
		}
		if (mAddress != null) {
			interactWithArduino(MSG_CONNECT);
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		broadcastConnection(mIsConnected ? 1 : 0);
		return mMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopAliveTask();
		stopReconnectTask();
		AmarinoHelper.unregisterListener(this, this);
	}

	private void startAliveTask() {
		stopAliveTask();
		if (mHandler != null) {
			mHandler.postDelayed(mAliveTask, mAliveInterval * 1000);
		}
	}

	private void stopAliveTask() {
		if (mHandler != null) {
			mHandler.removeCallbacks(mAliveTask);
		}
	}

	private void startReconnectTask() {
		if (mHandler != null) {
			mHandler.postDelayed(mReconnectTask, mAliveInterval * 1000);
		}
	}

	private void stopReconnectTask() {
		if (mHandler != null) {
			mHandler.removeCallbacks(mReconnectTask);
		}
	}

	private void broadcastConnection(int state) {
		Intent i = new Intent();
		i.setAction(ReceiverList.CONNECTION_RECEIVER);
		i.putExtra(ReceiverList.EXTRA_STATE, state);
		sendBroadcast(i);
	}

	private void broadcastWeight(float weight) {
		Intent i = new Intent();
		i.setAction(ReceiverList.WEIGHT_RECEIVER);
		i.putExtra(ReceiverList.EXTRA_WEIGHT, weight);
		sendBroadcast(i);
	}

	private void interactWithArduino(int type) {
		interactWithArduino(type, 0, null);
	}

	private void interactWithArduino(int type, int arg, String address) {
		stopAliveTask();
		switch (type) {
			case MSG_TOGGLE_POWER:
				AmarinoHelper.sendDataToArduino(this, mAddress,
						BluetoothSerial.POWER_EVENT, arg);
				break;
			case MSG_GET_CURRENT:
				AmarinoHelper.sendDataToArduino(this, mAddress,
						BluetoothSerial.WEIGHT_INFO, arg);
				break;
			case MSG_CONNECT:
				AmarinoHelper.connect(this, mAddress);
				break;
			case MSG_CONNECT_NEW:
				// We in fact disconnect && reconnect automatically.
				AmarinoHelper.disconnect(this, mAddress);
				mAddress = address;
				mAliveInterval = arg;
				break;
			case MSG_DISCONNECT:
				mHasAskedDisconnection = true;
				AmarinoHelper.disconnect(this, mAddress);
				break;
			case MSG_ALIVE:
				AmarinoHelper.sendDataToArduino(this, mAddress,
						BluetoothSerial.ALIVE, arg);
				break;
			case MSG_GET_CONNECTION_STATE:
				broadcastConnection(mIsConnected ? 1 : 0);
				break;
		}
	}

	public void onConnectResult(int result, String from) {
		if (result == CONNECT_SUCCEDED) {
			startAliveTask();

			mIsConnected = true;
			broadcastConnection(1);

			Calendar cal = Calendar.getInstance();
			long time = cal.getTimeInMillis();

			ContentValues cv = new ContentValues();
			cv.put(StroppyKettleContract.Connections.CONNECTION_TIME, time);
			cv.put(StroppyKettleContract.Connections.CONNECTION_STATE, 1);
			getContentResolver().insert(StroppyKettleContract.Connections.CONTENT_URI, cv);
		} else {
			startReconnectTask();
		}
	}

	public void onDisconnectResult(String from) {
		if (!mHasAskedDisconnection) {
			startReconnectTask();
		}
		mHasAskedDisconnection = false;

		mIsConnected = false;
		broadcastConnection(0);

		Calendar cal = Calendar.getInstance();
		long time = cal.getTimeInMillis();

		ContentValues cv = new ContentValues();
		cv.put(StroppyKettleContract.Connections.CONNECTION_TIME, time);
		cv.put(StroppyKettleContract.Connections.CONNECTION_STATE, 0);
		getContentResolver().insert(StroppyKettleContract.Connections.CONTENT_URI, cv);
	}

	public void onReceiveData(String data, String from) {
		startAliveTask();

		try {
			float weight = Float.valueOf(data);

			if (weight == BluetoothSerial.ALIVE_REPLY) return;

			ContentValues cv = new ContentValues();
			Calendar cal = Calendar.getInstance();
			cv.put(StroppyKettleContract.Logs.LOG_PREVIOUS_WEIGHT, mLastWeight);
			cv.put(StroppyKettleContract.Logs.LOG_WEIGHT, weight);
			cv.put(StroppyKettleContract.Logs.LOG_DATETIME, cal.getTimeInMillis());
			getContentResolver().insert(StroppyKettleContract.Logs.CONTENT_URI, cv);

			mLastWeight = weight;
			broadcastWeight(mLastWeight);
		} catch (NumberFormatException e) {
			if (DEBUG_MODE) {
				e.printStackTrace();
			}
		}
	}

	public void onReceiveNearbyDevices(Map<String, String> nearbyDevs) {
	}

	public void onReceiveConnectedDevices(String[] connectedDevs) {
	}

	public AmarinoReceiver getAmarinoReceiver() {
		return mReceiver;
	}

	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			interactWithArduino(msg.what, msg.arg1, (String) msg.obj);
		}
	}
}
