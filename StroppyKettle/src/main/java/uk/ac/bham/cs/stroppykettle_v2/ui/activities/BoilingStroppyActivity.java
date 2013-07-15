package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.os.Bundle;
import android.os.Handler;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;

public class BoilingStroppyActivity extends GenericStroppyActivity {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = BoilingStroppyActivity.class.getSimpleName();

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
		setContentView(R.layout.activity_boiling_stroppy);
		mHandler = new Handler();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mHandler != null) {
			mHandler.postDelayed(mBoilingRunnable, mBoilingTimeout * 1000);
		}
	}

	@Override
	protected void onStop() {
		// This first otherwise we already unbound...
		sendPowerMessage(false);

		super.onStop();
		if (mHandler != null) {
			mHandler.removeCallbacks(mBoilingRunnable);
		}
	}

	@Override
	protected void receivedNewWeight(float weight) {
		BoilingStroppyActivity.this.finish();
	}

	@Override
	public void onBackPressed() {
	}
}