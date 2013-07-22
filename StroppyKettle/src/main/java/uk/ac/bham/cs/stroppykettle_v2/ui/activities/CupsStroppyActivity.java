package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract;
import uk.ac.bham.cs.stroppykettle_v2.ui.adapters.CupsPagerAdapter;
import uk.ac.bham.cs.stroppykettle_v2.ui.views.HalfScreenView;

public class CupsStroppyActivity extends GenericStroppyActivity implements
		OnClickListener, OnPageChangeListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = CupsStroppyActivity.class.getSimpleName();

	public static final String EXTRA_USER_ID = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.CupsStroppyActivity.EXTRA_USER_ID";
	public static final String EXTRA_USER_NAME = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.CupsStroppyActivity.EXTRA_USER_NAME";
	public static final String EXTRA_START_TIME = "uk.ac.bham.cs.stroppykettle_v2.ui.activities.CupsStroppyActivity.EXTRA_START_TIME";

	private static final int IS_WAITING_FOR_WEIGHT = 0;
	private static final int IS_TIMEOUT = 1;
	private static final int IS_NOTHING = 2;

	private Runnable mTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			setRefreshing(true);
			mState = IS_TIMEOUT;
			getCurrentWeight();
		}
	};

	private ViewPager mPager;

	private int mState;

	protected SparseArray<Float> mCupWeightRef;

	private long mStartTime;
	private int mNbCups;
	private long mUserId;

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Intents
		String name = null;
		if (getIntent() != null) {
			name = getIntent().getStringExtra(EXTRA_USER_NAME);
			mUserId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
			mStartTime = getIntent().getLongExtra(EXTRA_START_TIME, -1);
		}

		if (getIntent() == null
				|| name == null
				|| mUserId == -1
				|| mStartTime == -1) {
			if (DEBUG_MODE) {
				Log.d(TAG, "Intent not complete, aborting.");
			}
			finish();
			return;
		}

		// Views
		setContentView(R.layout.activity_cups_stroppy);

		TextView title = (TextView) findViewById(R.id.cups_title);
		title.setText(String.format(getString(R.string.cups_title), name));

		Button stroppyButton = (Button) findViewById(R.id.cups_button);
		stroppyButton.setOnClickListener(this);

		CupsPagerAdapter pageAdapter = new CupsPagerAdapter(getViews());

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		mPager = (ViewPager) findViewById(R.id.cups_selector);
		mPager.setPageMargin(-(size.x / 2));

		mPager.setOffscreenPageLimit(mMaxCups);
		mPager.setAdapter(pageAdapter);
		mPager.setCurrentItem(1, false);
		mPager.setOnPageChangeListener(this);

		// Initialisations

		mCupWeightRef = new SparseArray<Float>();

		mState = IS_NOTHING;
		mNbCups = 1;

		mHandler = new Handler();

		mHandler.postDelayed(mTimeoutRunnable, mCupsTimeout * 1000);

		getSupportLoaderManager().restartLoader(0, null, this);
		setRefreshing(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacks(mTimeoutRunnable);
		}
	}

	@Override
	protected void onReceiveNewWeight(float weight) {
		if (mState == IS_WAITING_FOR_WEIGHT) {
			setRefreshing(false);
			sendPowerMessage(true);

			int nbSpins = (mCondition == getResources().getInteger(R.integer.condition_stroppy) ?
					StroppyKettleApplication.computeNbSpins(
							weight, mCupWeightRef.get(mNbCups) == null ?
							0 : mCupWeightRef.get(mNbCups), mStroppiness, mPrecision) : 0);

			if (DEBUG_MODE) {
				Log.d(TAG, "Condition : " + mCondition + " - Spins " + nbSpins + " - Weight : " + weight + " - Expected Weight : " + mCupWeightRef.get(mNbCups));
			}

			Intent i;
			if (nbSpins == 0) {
				interactionLog(mUserId, mCondition, mStartTime, -1, weight, mNbCups, false, true, 0, mStroppiness, nbSpins);
				i = new Intent(this, BoilingStroppyActivity.class);
			} else {
				i = new Intent(this, GameStroppyActivity.class);
				i.putExtra(GameStroppyActivity.EXTRA_USER_ID, mUserId);
				i.putExtra(GameStroppyActivity.EXTRA_NB_SPINS, nbSpins);
				i.putExtra(GameStroppyActivity.EXTRA_NB_CUPS, mNbCups);
				i.putExtra(GameStroppyActivity.EXTRA_WEIGHT, weight);
				i.putExtra(GameStroppyActivity.EXTRA_START_TIME, mStartTime);
			}
			startActivity(i);
			finish();
		} else if (mState == IS_TIMEOUT) {
			setRefreshing(false);
			sendPowerMessage(false);
			interactionLog(mUserId, mCondition, mStartTime, -1, weight, mNbCups, false, false, 0, mStroppiness, -1);
			finish();
		}
		mState = IS_NOTHING;
	}

	private List<View> getViews() {
		List<View> vList = new ArrayList<View>();

		for (int i = 0; i <= mMaxCups + 1; i++) {
			if (i == 0) {
				View v = new HalfScreenView(this);
				vList.add(v);
			} else if (i == mMaxCups + 1) {
				View v = new HalfScreenView(this);
				vList.add(v);
			} else {
				TextView tv = new TextView(this);
				tv.setText(String.valueOf(i));
				tv.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, getResources()
						.getDimensionPixelSize(R.dimen.selector_height)));
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getResources()
						.getDimensionPixelSize(R.dimen.text_height)
						/ getResources().getDisplayMetrics().density);
				tv.setGravity(Gravity.CENTER);
				tv.setIncludeFontPadding(false);
				vList.add(tv);
			}
		}
		return vList;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.cups_button:
				setRefreshing(true);
				mState = IS_WAITING_FOR_WEIGHT;
				getCurrentWeight();
				break;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		String[] projection = {StroppyKettleContract.Scale.SCALE_ID, StroppyKettleContract.Scale.SCALE_NB_CUPS, StroppyKettleContract.Scale.SCALE_WEIGHT};

		return new CursorLoader(this, StroppyKettleContract.Scale.CONTENT_URI,
				projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		setRefreshing(false);

		if (cursor == null) return;

		while (cursor.moveToNext()) {
			mCupWeightRef.put(cursor.getInt(cursor
					.getColumnIndex(StroppyKettleContract.Scale.SCALE_NB_CUPS)), cursor.getFloat(cursor
					.getColumnIndex(StroppyKettleContract.Scale.SCALE_WEIGHT)));

			if (DEBUG_MODE) {
				Log.d(TAG, cursor.getInt(cursor
						.getColumnIndex(StroppyKettleContract.Scale.SCALE_NB_CUPS)) + " -  " + cursor.getFloat(cursor
						.getColumnIndex(StroppyKettleContract.Scale.SCALE_WEIGHT)));
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}

	@Override
	public void onPageSelected(int position) {
		if (position == 0) {
			mPager.setCurrentItem(1);
			mNbCups = 1;
		} else if (position == mMaxCups + 1) {
			mPager.setCurrentItem(mMaxCups);
			mNbCups = mMaxCups;
		} else {
			mNbCups = position;
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
							   int positionOffsetPixels) {
	}
}