package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.os.Bundle;
import android.os.Handler;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;

public class BoilingStroppyActivity extends GenericStroppyActivity  {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = BoilingStroppyActivity.class.getSimpleName();

	// TODO 10 seconds boiling for now.
	private static final int BOILING_TIME = 10 * 1000;

	private Runnable mBoilingRunnable = new Runnable() {
		@Override
		public void run() {
			BoilingStroppyActivity.this.finish();
		}
	};

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setRefreshing(true);
		if(mHandler != null) {
			mHandler.postDelayed(mBoilingRunnable, BOILING_TIME);
		}
	}

	@Override
	protected void onStop() {
		// This first otherwise we already unbound...
		sendPowerMessage(false);

		super.onStop();
		setRefreshing(false);
		if(mHandler != null) {
			mHandler.removeCallbacks(mBoilingRunnable);
		}
	}

	@Override
	protected void receivedNewWeight(float weight) {
		BoilingStroppyActivity.this.finish();
	}
}