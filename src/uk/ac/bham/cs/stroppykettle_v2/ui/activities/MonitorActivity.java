package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.receivers.ReceiverList;

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

        Button clearBt = (Button) findViewById(R.id.clearButton);
        clearBt.setOnClickListener(this);

        Button connect = (Button) findViewById(R.id.connectButton);
        connect.setOnClickListener(this);

        Button disconnect = (Button) findViewById(R.id.disconnectButton);
        disconnect.setOnClickListener(this);

        mWeightReceiver = new WeightReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mWeightReceiver, new IntentFilter(
                ReceiverList.WEIGHT_RECEIVER));

        getLastWeight();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mWeightReceiver);
    }

    private void setMonitorScreen(float newWeight) {
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
                //mWeightText.setText("");
                getLastWeight();
                break;
            case R.id.connectButton:
                connect();
                break;
            case R.id.disconnectButton:
                disconnect();
                break;
        }
    }

    public class WeightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float weight = intent.getFloatExtra(ReceiverList.EXTRA_WEIGHT, 0f);

            setMonitorScreen(weight);
        }
    }
}
