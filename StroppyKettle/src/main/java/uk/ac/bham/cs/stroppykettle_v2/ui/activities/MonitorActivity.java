package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;

public class MonitorActivity extends GenericActivity implements OnClickListener {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = MonitorActivity.class.getSimpleName();

	private ToggleButton mPowerButton;
	private TextView mWeightText;
	private ScrollView mScroll;
	private Button mConnect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getString(R.string.action_monitor));

		setContentView(R.layout.activity_monitor);

		mScroll = (ScrollView) findViewById(R.id.scroll_monitor);

		mPowerButton = (ToggleButton) findViewById(R.id.powerButton);
		mPowerButton.setOnClickListener(this);
		mPowerButton.setEnabled(false);
		mWeightText = (TextView) findViewById(R.id.monitor_text);

		Button clearBt = (Button) findViewById(R.id.clearButton);
		clearBt.setOnClickListener(this);

		mConnect = (Button) findViewById(R.id.connectButton);
		mConnect.setOnClickListener(this);
		mConnect.setText(getString(R.string.connect));
	}

	@Override
	protected void onConnect() {
		super.onConnect();
		mConnect.setText(getString(R.string.disconnect));
		mPowerButton.setEnabled(true);
	}

	@Override
	protected void onDisconnect() {
		super.onConnect();
		mConnect.setText(getString(R.string.connect));
		mPowerButton.setEnabled(false);
	}

	@Override
	protected void onReceiveNewWeight(float newWeight) {
		mWeightText.setText(mWeightText.getText() + "\n" + newWeight);
		mScroll.post(new Runnable() {
			@Override
			public void run() {
				mScroll.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.powerButton:
				sendPowerMessage(mPowerButton.isChecked());
				break;
			case R.id.clearButton:
				mWeightText.setText("");
				//getLastWeight();
				break;
			case R.id.connectButton:
				if (mIsConnected) {
					disconnect();
				} else {
					connect();
				}
				break;
		}
	}
}
