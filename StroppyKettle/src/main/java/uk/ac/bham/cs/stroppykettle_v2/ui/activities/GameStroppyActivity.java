package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.Calendar;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract;

public class GameStroppyActivity extends GenericStroppyActivity implements OnTouchListener,
		OnGlobalLayoutListener {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = GameStroppyActivity.class.getSimpleName();

	public static final String EXTRA_NB_SPINS = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.GameStroppyActivity.EXTRA_NB_SPINS";
	public static final String EXTRA_WEIGHT = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.GameStroppyActivity.EXTRA_WEIGHT";
	public static final String EXTRA_NB_CUPS = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.GameStroppyActivity.EXTRA_NB_CUPS";
	public static final String EXTRA_START_TIME = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.GameStroppyActivity.EXTRA_START_TIME";
	public static final String EXTRA_USER_ID = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.GameStroppyActivity.EXTRA_USER_ID";

	private static final int TIMEOUT = 10000;

	private Runnable mTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			timeIsOut();
		}
	};

	private Runnable mDecrementRunnable = new Runnable() {
		@Override
		public void run() {
			if (mProgress != null && mRevCounter > 0) {
				mProgress.setProgress(--mRevCounter);

				// If we are here, then the progress is down.
				if (mRevCounter == 0) {
					timeIsOut();
				} else {
					mHandler.postDelayed(mDecrementRunnable, SEC_GO_DOWN);
				}
			}
		}
	};

	private static final int TRESHOLD = 40;

	private static final int SEC_GO_DOWN = 1000;

	private boolean mIsSuccess;

	private long mUserId;
	private float mWeight;
	private int mNbCups;
	private long mStartTime;
	private int mNbSpins;

	private ImageView mWheelView;
	private ProgressBar mProgress;

	private Matrix mMatrix;
	private int mWheelHeight;
	private int mWheelWidth;

	private int mOriginalWidth;
	private int mOriginalHeight;

	private float mRotCounter;
	private int mRevCounter;

	private double mStartAngle;

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_stroppy);

		mWheelView = (ImageView) findViewById(R.id.teaView);
		mWheelView.setOnTouchListener(this);

		mProgress = (ProgressBar) findViewById(R.id.progress_bar);

		Bitmap imageOriginal = BitmapFactory.decodeResource(getResources(),
				R.drawable.wheel);

		mOriginalWidth = imageOriginal.getWidth();
		mOriginalHeight = imageOriginal.getHeight();
		mMatrix = new Matrix();
		mWheelView.getViewTreeObserver().addOnGlobalLayoutListener(this);

		mHandler = new Handler();
	}

	@Override
	protected void onStart() {
		super.onStart();

		int discrepancy = 0;
		if (getIntent() != null) {
			mNbCups = getIntent().getIntExtra(EXTRA_NB_CUPS, 0);
			mUserId = getIntent().getLongExtra(EXTRA_USER_ID, 0);
			mWeight = getIntent().getFloatExtra(EXTRA_WEIGHT, 0);
			mStartTime = getIntent().getLongExtra(EXTRA_START_TIME, 0);
			mNbSpins = getIntent().getIntExtra(EXTRA_NB_SPINS, 0);
		}

		mProgress.setMax(mNbSpins);

		resetAndRelaunch();
	}

	@Override
	protected void onStop() {
		// Just in case...
		if (!mIsSuccess) {
			sendPowerMessage(false);
		}

		super.onStop();

		if (mHandler != null) {
			mHandler.removeCallbacks(mDecrementRunnable);
			mHandler.removeCallbacks(mTimeoutRunnable);
		}

		logInteraction();
	}

	private void resetAndRelaunch() {
		mRevCounter = 0;
		mRotCounter = 0;
		mProgress.setProgress(0);

		mIsSuccess = false;

		if (mHandler != null) {
			mHandler.removeCallbacks(mDecrementRunnable);
			mHandler.removeCallbacks(mTimeoutRunnable);
		}

		sendPowerMessage(true);

		mHandler.postDelayed(mTimeoutRunnable, TIMEOUT);
	}

	private void logInteraction() {
		Calendar cal = Calendar.getInstance();
		ContentValues cv = new ContentValues();
		cv.put(StroppyKettleContract.Interactions.INTERACTION_NB_CUPS, mNbCups);
		cv.put(StroppyKettleContract.Interactions.INTERACTION_WEIGHT, mWeight);
		cv.put(StroppyKettleContract.Interactions.INTERACTION_START_DATETIME, mStartTime);
		cv.put(StroppyKettleContract.Interactions.INTERACTION_STOP_DATETIME, cal.getTimeInMillis() / 1000);
		cv.put(StroppyKettleContract.Interactions.INTERACTION_USER_ID, mUserId);
		cv.put(StroppyKettleContract.Interactions.INTERACTION_IS_SUCCESS, mIsSuccess);
		cv.put(StroppyKettleContract.Interactions.INTERACTION_IS_STROPPY, 1);
		cv.put(StroppyKettleContract.Interactions.INTERACTION_CONDITION, 0); // TODO

		getContentResolver().insert(StroppyKettleContract.Interactions.CONTENT_URI, cv);
	}

	@Override
	protected void receivedNewWeight(float weight) {
		finish();
	}

	private void revOver() {
		mIsSuccess = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("You can now enjoy your tea.").setTitle("Congratulations!");

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent i = new Intent(GameStroppyActivity.this, BoilingStroppyActivity.class);
						startActivity(i);
						GameStroppyActivity.this.finish();
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void timeIsOut() {
		// The user has failed.
		sendPowerMessage(false);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("Would you like to try again?").setTitle("You fail...");

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//logInteraction();
						resetAndRelaunch();
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						GameStroppyActivity.this.finish();
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// ********* Wheel Section ********** //

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		int evt = event.getAction();

		float touchedX = event.getX();
		float touchedY = event.getY();

		if (evt == MotionEvent.ACTION_DOWN) {
			mStartAngle = getAngle(touchedX, touchedY);
		} else if (evt == MotionEvent.ACTION_MOVE) {

			double currentAngle = getAngle(touchedX, touchedY);

			float rotation = (float) (mStartAngle - currentAngle);

			if (rotation > 0f && rotation < TRESHOLD) {
				if (mHandler != null) {
					mHandler.removeCallbacks(mDecrementRunnable);
				}

				rotate(rotation);
				mRotCounter += rotation;

				if (mRotCounter > 360f) {
					// Stop the initial timeout if there is at least a full spin.
					if (mHandler != null) {
						mHandler.removeCallbacks(mTimeoutRunnable);
					}
					mRevCounter++;
					mRotCounter = 0;
					mProgress.setProgress(mRevCounter);
					if (mRevCounter >= mNbSpins) {
						revOver();
					}
				}

				if (mRevCounter < mNbSpins) {
					if (mHandler != null) {
						mHandler.postDelayed(mDecrementRunnable, SEC_GO_DOWN);
					}
				}
			}
			mStartAngle = currentAngle;
		}
		return true;
	}

	private void rotate(float degrees) {
		mMatrix.postRotate(degrees, mWheelWidth / 2, mWheelHeight / 2);
		mWheelView.setImageMatrix(mMatrix);
	}

	/**
	 * @return The angle of the unit circle with the image view's center
	 */
	private double getAngle(double xTouch, double yTouch) {
		double x = xTouch - (mWheelView.getWidth() / 2d);
		double y = mWheelView.getHeight() - yTouch
				- (mWheelView.getHeight() / 2d);
		switch (getQuadrant(x, y)) {
			case 1:
				return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
			case 2:
				return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
			case 3:
				return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
			case 4:
				return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
			default:
				return 0;
		}
	}

	/**
	 * @return The selected quadrant.
	 */
	private static int getQuadrant(double x, double y) {
		if (x >= 0) {
			return y >= 0 ? 1 : 4;
		} else {
			return y >= 0 ? 2 : 3;
		}
	}

	@Override
	public void onGlobalLayout() {
		// method called more than once, but the values only need to be
		// initialized one time
		if (mWheelHeight == 0 || mWheelWidth == 0) {
			mWheelHeight = mWheelView.getHeight();
			mWheelWidth = mWheelView.getWidth();

			// translate to the image view's center
			float translateX = mWheelWidth / 2 - mOriginalWidth / 2;
			float translateY = mWheelHeight / 2 - mOriginalHeight / 2;

			mMatrix.postTranslate(translateX, translateY);
			mWheelView.setImageMatrix(mMatrix);
		}
	}

	@Override
	public void onBackPressed() {
	}
}