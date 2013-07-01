package uk.ac.bham.cs.stroppykettle_v2.services;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.protocols.BluetoothSerial;
import uk.ac.bham.cs.stroppykettle_v2.receivers.ReceiverList;
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

public class WeightService extends Service {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = WeightService.class.getSimpleName();

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private Messenger mMessenger;

	private boolean mIsConnected;
	private AmarinoReceiver mAmarinoReceiver;

	private int mLastWeight = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		mMessenger = new Messenger(new IncomingHandler());

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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!mIsConnected) {
			Amarino.connect(this, StroppyKettleApplication.DEVICE_ADDRESS);
		}

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Amarino.disconnect(this, StroppyKettleApplication.DEVICE_ADDRESS);
		unregisterReceiver(mAmarinoReceiver);
	}

	private void sendPowerMessage(boolean onoff) {
		Amarino.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS,
				BluetoothSerial.POWER_EVENT, onoff ? 1 : 0);
	}
	
	private int getLastWeight() {
		return mLastWeight;
	}

	private class AmarinoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(AmarinoIntent.ACTION_CONNECTED)) {
				if (DEBUG_MODE) {
					Log.d(TAG, "I am connected!");
				}
				mIsConnected = true;
			} else if (intent.getAction().equals(AmarinoIntent.ACTION_RECEIVED)) {
				try {
					mLastWeight = Integer.parseInt(intent
							.getStringExtra(AmarinoIntent.EXTRA_DATA));
					Intent i = new Intent(ReceiverList.WEIGHT_RECEIVER);
					i.putExtra(ReceiverList.EXTRA_WEIGHT, mLastWeight);
					sendBroadcast(i);
				} catch (NumberFormatException e) {
					if (DEBUG_MODE) {
						e.printStackTrace();
					}
				}
			} else {
				if (DEBUG_MODE) {
					Log.d(TAG, "Trouble, I can't connect...");
				}
				mIsConnected = false;
			}
		}
	}

	private class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			// we will only have one client here
			//case MSG_REGISTER_CLIENT:
				//mClient = msg.replyTo;
				//if(!wifiConnected) onNetworkDisconnect();
				//break;

			default:
				super.handleMessage(msg);
			}
		}
	}
}
