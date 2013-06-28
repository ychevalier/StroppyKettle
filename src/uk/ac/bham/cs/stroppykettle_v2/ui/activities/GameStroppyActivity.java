package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import java.util.Timer;
import java.util.TimerTask;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class GameStroppyActivity extends GenericStroppyActivity implements OnTouchListener,
		OnGlobalLayoutListener {
	
	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = GameStroppyActivity.class.getSimpleName();
	
	public static final String EXTRA_NB_CUPS = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.GameStroppyActivity.EXTRA_NB_CUPS";
	public static final String EXTRA_USER_ID = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.GameStroppyActivity.EXTRA_USER_ID";
	
	class mTask extends TimerTask {
	        @Override
	        public void run() {
	        	if(mProgress != null && mRevCounter > 0) {
	        		mProgress.setProgress(--mRevCounter);
	        	}
	        }
	   };
	
	private static final int TRESHOLD = 40;
	private static final int NB_REVOLUTION = 5;
	
	private static final int SEC_GO_DOWN = 1000;

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
	
	private Timer mTimer;
	
	private int mNbCups;
	private int mUserId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_stroppy);
		
		mWheelView = (ImageView) findViewById(R.id.teaView);
		mWheelView.setOnTouchListener(this);
		
		mProgress = (ProgressBar) findViewById(R.id.progress_bar);
		mProgress.setMax(NB_REVOLUTION);

		Bitmap imageOriginal = BitmapFactory.decodeResource(getResources(),
				R.drawable.tea_o);
		
		mOriginalWidth = imageOriginal.getWidth();
		mOriginalHeight = imageOriginal.getHeight();

		mMatrix = new Matrix();

		mWheelView.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(getIntent() != null) {
			mNbCups = getIntent().getIntExtra(EXTRA_NB_CUPS, 0);
			mUserId = getIntent().getIntExtra(EXTRA_USER_ID, 0);
		} else {
			mNbCups = 0;
			mUserId = 0;
		}
		
		mRevCounter = 0;
		mRotCounter = 0;
		mProgress.setProgress(0);
		
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new mTask(), SEC_GO_DOWN, SEC_GO_DOWN);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if(mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}
	
	private void revOver() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("You can now enjoy your tea.").setTitle("Congratulations!");

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent i = new Intent(GameStroppyActivity.this, LoginStroppyActivity.class);
						startActivity(i);
						GameStroppyActivity.this.finish();
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	
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
				if(mTimer != null) {
					mTimer.cancel();
					mTimer.purge();
					mTimer = null;
				}
				
				rotate(rotation);
				mRotCounter += rotation;
				
				if(mRotCounter > 360f) {
					mRevCounter++;
					mRotCounter = 0;
					mProgress.setProgress(mRevCounter);
					if(mRevCounter >= NB_REVOLUTION) {
						revOver();
					}
				}
				
				if(mRevCounter < NB_REVOLUTION) {
					mTimer = new Timer();
					mTimer.scheduleAtFixedRate(new mTask(), SEC_GO_DOWN, SEC_GO_DOWN);
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
}