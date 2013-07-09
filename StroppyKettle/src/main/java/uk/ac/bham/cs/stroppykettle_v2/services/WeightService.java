package uk.ac.bham.cs.stroppykettle_v2.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

	// Send alive test message every 3 seconds.
	private static final int TIMEOUT = 3 * 1000;

	//public final float[] mWeightTab = {0, 212.2f, 250, 280, 320, 370, 405, 450};
	//public final float ERROR = 0.1f;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private Messenger mMessenger;

	private AmarinoReceiver mReceiver;

	private Timer mAliveTimer;

	private float mLastWeight;

	@Override
	public void onCreate() {
		super.onCreate();

		mMessenger = new Messenger(new IncomingHandler());

		mReceiver = new AmarinoReceiver();

		AmarinoHelper.registerListener(this, this);

		mLastWeight = 0;
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
		AmarinoHelper.unregisterListener(this, this);
	}

	private void startAliveTask() {
		stopAliveTask();
		final Handler handler = new Handler();
		mAliveTimer = new Timer();
		TimerTask aliveCheck = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						interactWithArduino(MSG_ALIVE);
					}
				});
			}
		};
		mAliveTimer.schedule(aliveCheck, TIMEOUT, TIMEOUT);
	}

	private void stopAliveTask() {
		if (mAliveTimer != null) {
			mAliveTimer.cancel();
			mAliveTimer.purge();
			mAliveTimer = null;
		}
	}

	private void startReconnectTask() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				interactWithArduino(MSG_CONNECT);
			}
		}, TIMEOUT);
	}

	private float getNbCups(float weight) {

		/*
		int lowBound = -1;
        int upBound = -1;

        for(int i = 0; i < mWeightTab.length; i++) {
            if(weight < mWeightTab[i]) {
                upBound = i;
                lowBound = i-1;
                break;
            }
        }

        if(lowBound != -1 && upBound != -1) {
            float measure = (weight - mWeightTab[lowBound]) / (float)(mWeightTab[upBound] - mWeightTab[lowBound]);

            if(DEBUG_MODE) {
                Log.d(TAG, "Weight : " + weight + ", Low : " + lowBound + ", Up : " + upBound + ", Measure : " + measure);
            }

            if(measure < ERROR) {
                return lowBound;
            } else if(measure > 1 - ERROR) {
                return upBound;
            } else {
                return lowBound + measure;
            }
        // Too much water.
        } else if(lowBound == -1 && upBound == -1) {
                return mWeightTab.length - 1;
        // Not enough 1 cup/empty/nothing
        } else {
            return -1;
        }
		*/
		return weight;
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
				AmarinoHelper.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS,
						BluetoothSerial.POWER_EVENT, arg);
				break;
			case MSG_GET_CURRENT:
				AmarinoHelper.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS,
						BluetoothSerial.WEIGHT_INFO, arg);
				break;
			case MSG_CONNECT:
				AmarinoHelper.connect(this, StroppyKettleApplication.DEVICE_ADDRESS);
				break;
			case MSG_DISCONNECT:
				AmarinoHelper.disconnect(this, StroppyKettleApplication.DEVICE_ADDRESS);
				break;
			case MSG_ALIVE:
				AmarinoHelper.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS,
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
		startReconnectTask();
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

			mLastWeight = getNbCups(weight);
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
