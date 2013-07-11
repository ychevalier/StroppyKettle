package uk.ac.bham.cs.stroppykettle_v2.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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

	public static final int MSG_TOGGLE_POWER = 0;
	public static final int MSG_GET_CURRENT = 1;
	public static final int MSG_CONNECT = 2;
	public static final int MSG_DISCONNECT = 3;
	public static final int MSG_ALIVE = 4;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private Messenger mMessenger;

	private AmarinoReceiver mReceiver;

	private float mLastWeight;

	private Handler mHandler;

	private boolean mHasAskedDisconnection;

	private String mAddress;

	private int mAliveInterval;

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

		mHasAskedDisconnection = false;

		SharedPreferences settings = getSharedPreferences(StroppyKettleApplication.PREFS_NAME, 0);
		mAddress = settings.getString(StroppyKettleApplication.PREF_ADDRESS, StroppyKettleApplication.DEFAULT_ADDRESS);
		mAliveInterval = settings.getInt(StroppyKettleApplication.PREF_ALIVE_INTERVAL, StroppyKettleApplication.DEFAULT_ALIVE_INTERVAL);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		interactWithArduino(MSG_CONNECT);
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
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
		if(mHandler != null) {
			mHandler.postDelayed(mAliveTask, mAliveInterval * 1000);
		}
	}

	private void stopAliveTask() {
		if (mHandler != null) {
			mHandler.removeCallbacks(mAliveTask);
		}
	}

	private void startReconnectTask() {
		if(mHandler != null) {
			mHandler.postDelayed(mReconnectTask, mAliveInterval * 1000);
		}
	}

	private void stopReconnectTask() {
		if(mHandler != null) {
			mHandler.removeCallbacks(mReconnectTask);
		}
	}

	private void broadcastWeight(float weight) {
		Intent i = new Intent(ReceiverList.WEIGHT_RECEIVER);
		i.putExtra(ReceiverList.EXTRA_WEIGHT, weight);
		sendBroadcast(i);
	}

	private void interactWithArduino(int type) {
		interactWithArduino(type, 0);
	}

	private void interactWithArduino(int type, int arg) {
		stopAliveTask();
		switch (type) {
			case MSG_TOGGLE_POWER:
				AmarinoHelper.sendDataToArduino(this, mAddress,
						BluetoothSerial.POWER_EVENT, arg);
				break;
			case MSG_GET_CURRENT:
				AmarinoHelper.sendDataToArduino(this,mAddress,
						BluetoothSerial.WEIGHT_INFO, arg);
				break;
			case MSG_CONNECT:
				AmarinoHelper.connect(this, mAddress);
				break;
			case MSG_DISCONNECT:
				mHasAskedDisconnection = true;
				AmarinoHelper.disconnect(this, mAddress);
				break;
			case MSG_ALIVE:
				AmarinoHelper.sendDataToArduino(this, mAddress,
						BluetoothSerial.ALIVE, arg);
				break;
		}
	}

	public void onConnectResult(int result, String from) {
		if (result == CONNECT_SUCCEDED) {
			startAliveTask();
		} else {
			startReconnectTask();
		}
	}

	public void onDisconnectResult(String from) {
		if(!mHasAskedDisconnection) {
			startReconnectTask();
		}
		mHasAskedDisconnection = false;
	}

	public void onReceiveData(String data, String from) {
		startAliveTask();

		try {
			float weight = new Float(data);

			if (weight == BluetoothSerial.ALIVE_REPLY) return;

			ContentResolver cr = getContentResolver();
			if (cr != null) {
				ContentValues cv = new ContentValues();
				Calendar cal = Calendar.getInstance();
				cv.put(StroppyKettleContract.Logs.LOG_PREVIOUS_WEIGHT, mLastWeight);
				cv.put(StroppyKettleContract.Logs.LOG_WEIGHT, weight);
				cv.put(StroppyKettleContract.Logs.LOG_DATETIME, cal.getTimeInMillis());
				cr.insert(StroppyKettleContract.Logs.CONTENT_URI, cv);
			}

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
			interactWithArduino(msg.what, msg.arg1);
		}
	}
}
