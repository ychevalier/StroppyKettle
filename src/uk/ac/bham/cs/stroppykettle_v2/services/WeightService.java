package uk.ac.bham.cs.stroppykettle_v2.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.protocols.BluetoothSerial;
import uk.ac.bham.cs.stroppykettle_v2.receivers.ReceiverList;

public class WeightService extends Service {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = WeightService.class.getSimpleName();

    public static final int MSG_TOGGLE_POWER = 0;
    public static final int MSG_GET_LAST = 1;
    public static final int MSG_CONNECT = 2;
    public static final int MSG_DISCONNECT = 3;

    // Timeout is after 1000 milliseconds
    // of having sent a message and receiving nothing
    private static final int TIMEOUT = 1000;

    private Runnable mTimeoutTrigger = new Runnable() {
        public void run() {
            if (mIsConnected) {
                interactWithArduino(MSG_DISCONNECT, 0, true);
            } else {
                interactWithArduino(MSG_CONNECT, 0, true);
            }
        }
    };

    public final int[] mWeightTab = {215, 252, 288, 331, 375, 413, 460};
    public final float ERROR = 0.2f;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private Messenger mMessenger;

	private boolean mIsConnected;
	private AmarinoReceiver mAmarinoReceiver;

	private float mLastWeight;

    private Handler mHandler;

    @Override
	public void onCreate() {
		super.onCreate();

		mMessenger = new Messenger(new IncomingHandler());

        mLastWeight = 0f;

        mHandler = new Handler();

        mIsConnected = false;
		mAmarinoReceiver = new AmarinoReceiver();

		registerReceiver(mAmarinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_CONNECTED));
		registerReceiver(mAmarinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_CONNECTION_FAILED));
		registerReceiver(mAmarinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_DISCONNECTED));
		registerReceiver(mAmarinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_PAIRING_REQUESTED));
		registerReceiver(mAmarinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_RECEIVED));
        registerReceiver(mAmarinoReceiver, new IntentFilter(
                AmarinoIntent.ACTION_CONNECTED_DEVICES));
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        interactWithArduino(MSG_CONNECT, 0, true);
        return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        interactWithArduino(MSG_DISCONNECT, 0, false);
        unregisterReceiver(mAmarinoReceiver);
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
            float measure = (float)(weight - mWeightTab[lowBound]) / (float)(mWeightTab[upBound] - mWeightTab[lowBound]);

            if(DEBUG_MODE) {
                Log.d(TAG, "Weight : " + weight +  ", Low : " + lowBound + ", Up : " + upBound + ", Measure : " + measure);
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
        i.putExtra(ReceiverList.EXTRA_WEIGHT, mLastWeight);
        sendBroadcast(i);
    }

    private void interactWithArduino(int type, int arg, boolean setTimeout) {
        switch (type) {
            case MSG_TOGGLE_POWER:
                Amarino.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS,
                        BluetoothSerial.POWER_EVENT, arg);
                break;
            case MSG_GET_LAST:
                Amarino.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS,
                        BluetoothSerial.WEIGHT_INFO, 0);
                break;
            case MSG_CONNECT:
                Amarino.connect(WeightService.this, StroppyKettleApplication.DEVICE_ADDRESS);
                break;
            case MSG_DISCONNECT:
                Amarino.disconnect(WeightService.this, StroppyKettleApplication.DEVICE_ADDRESS);
                break;
        }

        if (setTimeout && mHandler != null) {
            mHandler.postDelayed(mTimeoutTrigger, TIMEOUT);
        }
    }

    private class AmarinoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

            // If here, that means that bluetooth is running.
            if (mHandler != null) {
                mHandler.removeCallbacks(mTimeoutTrigger);
            }

            String action = intent.getAction() == null ? "" : intent.getAction();
            if (action.equals(AmarinoIntent.ACTION_CONNECTED)) {
                if (DEBUG_MODE) {
					Log.d(TAG, "I am connected!");
				}
				mIsConnected = true;
            } else if (action.equals(AmarinoIntent.ACTION_RECEIVED)) {
                try {
                    float weight = Float.parseFloat(intent.getStringExtra(AmarinoIntent.EXTRA_DATA));

                    if (DEBUG_MODE) {
                        Log.d(TAG, "I received a new weight : " + weight);
                    }

                    mLastWeight = getNbCups(weight);

                    broadcastWeight(mLastWeight);
                } catch (NumberFormatException e) {
                    if (DEBUG_MODE) {
                        e.printStackTrace();
                    }
                }
            } else if (action.equals(AmarinoIntent.ACTION_DISCONNECTED)) {
                if (DEBUG_MODE) {
                    Log.d(TAG, "Disconnected!");
                }
                mIsConnected = false;
            } else {
                if (DEBUG_MODE) {
                    Log.d(TAG, "Trouble!!");
                }
			}
		}
	}

	private class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) 
		{
            interactWithArduino(msg.what, msg.arg1, true);
        }
	}
}
