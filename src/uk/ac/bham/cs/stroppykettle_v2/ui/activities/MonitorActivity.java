package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.receivers.ReceiverList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MonitorActivity extends GenericActivity implements OnClickListener {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = MonitorActivity.class.getSimpleName();

	private WeightReceiver mWeightReceiver;

	private ToggleButton mPowerButton;
	private TextView mWeightText;
	private ScrollView mScroll;

	protected boolean mIsConnected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);

		mScroll = (ScrollView) findViewById(R.id.scroll_monitor);

		mPowerButton = (ToggleButton) findViewById(R.id.powerButton);
		mPowerButton.setOnClickListener(this);
		mWeightText = (TextView) findViewById(R.id.monitor_text);
		
		mWeightReceiver = new WeightReceiver();
	}

	@Override
	protected void onStart() {
		super.onStart();

		registerReceiver(mWeightReceiver, new IntentFilter(
				ReceiverList.WEIGHT_RECEIVER));
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterReceiver(mWeightReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.powerButton:
			//WeightService.sendPowerMessage(this, mPowerButton.isChecked());
			break;
		}
	}

	public class WeightReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int weight = intent.getIntExtra(ReceiverList.EXTRA_WEIGHT, 0);
			mWeightText.setText(mWeightText.getText() + "\n" + weight);
			
			mScroll.post(new Runnable() {
				@Override
				public void run() {
					mScroll.fullScroll(View.FOCUS_DOWN);
				}
			});
		}
	}
}
