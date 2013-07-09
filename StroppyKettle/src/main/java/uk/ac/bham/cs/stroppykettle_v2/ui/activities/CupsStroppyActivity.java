package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

	private ViewPager mPager;
	private int mNbCups;

	private long mUserId;

	private TextView mTitle;

	private Map<Integer, Float> mCupWeightRef;

	private boolean mIsWaitingForWeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cups_stroppy);

		mTitle = (TextView) findViewById(R.id.cups_title);

		Button stroppyButton = (Button) findViewById(R.id.cups_button);
		stroppyButton.setOnClickListener(this);

		CupsPagerAdapter pageAdapter = new CupsPagerAdapter(getViews());

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		mPager = (ViewPager) findViewById(R.id.cups_selector);
		mPager.setPageMargin(-(int) (size.x / 2));

		mPager.setOffscreenPageLimit(StroppyKettleApplication.NUMBER_OF_CUPS);
		mPager.setAdapter(pageAdapter);

		mPager.setOnPageChangeListener(this);

		mCupWeightRef = new HashMap<Integer, Float>();

		mIsWaitingForWeight = false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		String name = null;
		if (getIntent() != null) {
			name = getIntent().getStringExtra(EXTRA_USER_NAME);
			mUserId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
		}
		if (name == null) {
			name = "";
		}

		mTitle.setText(String.format(getString(R.string.cups_title), name));

		mNbCups = 1;
		if (mPager != null) {
			mPager.setCurrentItem(mNbCups, false);
		}

		getSupportLoaderManager().restartLoader(0, null, this);
		setRefreshing(true);
	}

	@Override
	protected void receivedNewWeight(float weight) {
		if (mIsWaitingForWeight) {
			mIsWaitingForWeight = false;
			setRefreshing(false);
			sendPowerMessage(true);

			// TODO Compute if we need stroppyness & discrepency.
			Random random = new Random();

			Intent i = new Intent(this, GameStroppyActivity.class);
			i.putExtra(GameStroppyActivity.EXTRA_USER_ID, mUserId);
			i.putExtra(GameStroppyActivity.EXTRA_DISCREPANCY, random.nextInt(100));
			i.putExtra(GameStroppyActivity.EXTRA_NB_CUPS, mNbCups);
			i.putExtra(GameStroppyActivity.EXTRA_WEIGHT, weight);
			startActivity(i);

			finish();
		}
	}

	private List<View> getViews() {
		List<View> vList = new ArrayList<View>();

		for (int i = 0; i <= StroppyKettleApplication.NUMBER_OF_CUPS + 1; i++) {
			if (i == 0) {
				View v = new HalfScreenView(this);
				vList.add(v);
			} else if (i == StroppyKettleApplication.NUMBER_OF_CUPS + 1) {
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
				mIsWaitingForWeight = true;
				getCurrentWeight();
				break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
							   int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		if (position == 0) {
			mPager.setCurrentItem(1);
			mNbCups = 1;
		} else if (position == StroppyKettleApplication.NUMBER_OF_CUPS + 1) {
			mPager.setCurrentItem(StroppyKettleApplication.NUMBER_OF_CUPS);
			mNbCups = StroppyKettleApplication.NUMBER_OF_CUPS;
		} else {
			mNbCups = position;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		String[] projection = {StroppyKettleContract.Scale.SCALE_ID, StroppyKettleContract.Scale.SCALE_NB_CUPS, StroppyKettleContract.Scale.SCALE_WEIGHT};

		CursorLoader cursorLoader = new CursorLoader(this, StroppyKettleContract.Scale.CONTENT_URI,
				projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		setRefreshing(false);

		if(cursor == null) return;

		while(cursor.moveToNext()) {
			mCupWeightRef.put(cursor.getInt(cursor
					.getColumnIndex(StroppyKettleContract.Scale.SCALE_NB_CUPS)), cursor.getFloat(cursor
					.getColumnIndex(StroppyKettleContract.Scale.SCALE_WEIGHT)));

			if(DEBUG_MODE) {
				Log.d(TAG, cursor.getInt(cursor
						.getColumnIndex(StroppyKettleContract.Scale.SCALE_NB_CUPS)) + " -  " + cursor.getFloat(cursor
						.getColumnIndex(StroppyKettleContract.Scale.SCALE_WEIGHT)));
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}
}