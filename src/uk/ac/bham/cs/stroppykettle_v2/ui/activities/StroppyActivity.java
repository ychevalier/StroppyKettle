package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

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
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class StroppyActivity extends GenericActivity implements
		OnLongClickListener, OnTouchListener,
		OnGlobalLayoutListener {
	
	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = StroppyActivity.class.getSimpleName();

	private static final int TRESHOLD = 40;
	private static final int NB_REVOLUTION = 5;

	private ImageView mWheelView;
	private ProgressBar mProgress;

	private Bitmap mImageOriginal;
	private Matrix mMatrix;
	private int mWheelHeight;
	private int mWheelWidth;
	
	private float mRotCounter;
	private int mRevCounter;

	private double mStartAngle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stroppy);

		Button exit = (Button) findViewById(R.id.exit_button);
		exit.setOnLongClickListener(this);
		
		mWheelView = (ImageView) findViewById(R.id.teaView);
		mWheelView.setOnTouchListener(this);
		
		mProgress = (ProgressBar) findViewById(R.id.progress_bar);
		mProgress.setMax(NB_REVOLUTION);
		

		mImageOriginal = BitmapFactory.decodeResource(getResources(),
				R.drawable.tea_o);

		mMatrix = new Matrix();

		mWheelView.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mRevCounter = 0;
		mRotCounter = 0;
		
		mProgress.setProgress(0);
	}
	
	private void revOver() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("You can now enjoy your tea.").setTitle("Congratulations!");

		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						StroppyActivity.this.finish();
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
			float translateX = mWheelWidth / 2 - mImageOriginal.getWidth() / 2;
			float translateY = mWheelHeight / 2 - mImageOriginal.getHeight() / 2;
			
			mMatrix.postTranslate(translateX, translateY);
			mWheelView.setImageMatrix(mMatrix);
		}
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.exit_button:
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			finish();
			return true;
		}
		return false;
	}
}
