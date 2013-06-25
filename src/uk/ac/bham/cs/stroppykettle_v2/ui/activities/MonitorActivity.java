package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.protocols.BluetoothSerial;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class MonitorActivity extends GenericActivity implements OnClickListener {

	private WeightReceiver mWeightReceiver = new WeightReceiver();
	
	private ToggleButton mPowerButton;
	private TextView mWeightText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);

		mPowerButton = (ToggleButton) findViewById(R.id.powerButton);
		mPowerButton.setOnClickListener(this);
		mWeightText = (TextView) findViewById(R.id.monitor_text);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(mWeightReceiver, new IntentFilter(
				AmarinoIntent.ACTION_RECEIVED));
		
		Amarino.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS, BluetoothSerial.WEIGHT_INFO, 42);		
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mWeightReceiver);
	}

	public class WeightReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;
			data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
			if (data != null)
				mWeightText.setText(mWeightText.getText() + "\n"  + data);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.powerButton:
			if (mPowerButton.isChecked()) {
				Amarino.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS, BluetoothSerial.POWER_EVENT, 1);
			} else {
				Amarino.sendDataToArduino(this, StroppyKettleApplication.DEVICE_ADDRESS, BluetoothSerial.POWER_EVENT, 0);
			}
			break;
		}
	}

}

