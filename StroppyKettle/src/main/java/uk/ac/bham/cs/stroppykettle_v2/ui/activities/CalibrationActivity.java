package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Button;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract;
import uk.ac.bham.cs.stroppykettle_v2.ui.adapters.SettingsPagerAdapter;
import uk.ac.bham.cs.stroppykettle_v2.ui.fragments.CalibrationFragment;
import uk.ac.bham.cs.stroppykettle_v2.ui.views.CustomViewPager;

public class CalibrationActivity extends GenericActivity implements
		OnPageChangeListener, View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = CalibrationActivity.class.getSimpleName();

	private int mCurrentPosition;
	private CustomViewPager mPager;
	private boolean mDoNotUpdate;
	private Button mWeightIndicator;
	private float mLastWeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Views
		setTitle(getString(R.string.action_calibration));
		setContentView(R.layout.activity_calibration);

		mWeightIndicator = (Button) findViewById(R.id.weight);
		mWeightIndicator.setText(String.valueOf(mLastWeight));
		mWeightIndicator.setOnClickListener(this);

		List<Fragment> fragments = getFragments();

		SettingsPagerAdapter pageAdapter = new SettingsPagerAdapter(getSupportFragmentManager(),
				fragments);
		mPager = (CustomViewPager) findViewById(R.id.viewpager);
		mPager.setAdapter(pageAdapter);

		// Bind the title indicator to the adapter
		CirclePageIndicator pageIndicator = (CirclePageIndicator) findViewById(R.id.circles);
		pageIndicator.setViewPager(mPager);

		pageIndicator.setOnPageChangeListener(this);

		// Inits
		mPager.setCurrentItem(0);
		mCurrentPosition = 0;

		// TODO : test if we need to update or not.
		mDoNotUpdate = false;
		mLastWeight = 0;

		getSupportLoaderManager().restartLoader(0, null, this);
		setRefreshing(true);
	}

	@Override
	protected void onServiceBound() {
		getCurrentWeight();
	}

	@Override
	protected void onReceiveNewWeight(float weight) {
		mLastWeight = weight;
		mWeightIndicator.setText(String.valueOf(mLastWeight));
	}

	@Override
	public void onClick(View view) {
		if (view == mWeightIndicator) {
			getCurrentWeight();
		}
	}

	private List<Fragment> getFragments() {
		List<Fragment> fList = new ArrayList<Fragment>();

		for (int i = -1; i <= mMaxCups + 1; i++) {
			fList.add(CalibrationFragment.newInstance(i, mMaxCups));
		}
		return fList;
	}

	@Override
	public void onPageSelected(final int position) {
		if (position < mCurrentPosition) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(getString(R.string.dialog_confirm_message)).setTitle(getString(R.string.dialog_confirm_title));

			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mCurrentPosition = position;
						}
					});
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// If we are going to the same page again,
							// we dont want to update.
							mDoNotUpdate = true;
							mPager.setCurrentItem(mCurrentPosition);
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();

		} else {
			mCurrentPosition = position;
			if (!mDoNotUpdate) {
				// We are using position -2 because it starts at -1 and
				// we are at the next position that the one we want to update.
				ContentValues cv = new ContentValues();
				cv.put(StroppyKettleContract.Scale.SCALE_NB_CUPS, mCurrentPosition - 2);
				cv.put(StroppyKettleContract.Scale.SCALE_WEIGHT, mLastWeight);
				getContentResolver().insert(StroppyKettleContract.Scale.CONTENT_URI, cv);

				// Position start from 0, and there is nbCups + 3 fragments.
				// (nothing, empty and done).
				if (mCurrentPosition == mMaxCups + 2) {
					mPager.setPagingEnabled(false);
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							finish();
						}
					}, 800);
				}
			}
			mDoNotUpdate = false;
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
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		String[] projection = {StroppyKettleContract.Scale.SCALE_ID, StroppyKettleContract.Scale.SCALE_NB_CUPS, StroppyKettleContract.Scale.SCALE_WEIGHT};

		return new CursorLoader(this, StroppyKettleContract.Scale.CONTENT_URI,
				projection, null, null, null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> cursorLoader, final Cursor cursor) {
		setRefreshing(false);

		if (cursor == null) return;

		if (cursor.getCount() >= mMaxCups + 2) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(R.string.dialog_redo_message).setTitle(R.string.dialog_redo_title);

			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Nothing
						}
					});
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							CalibrationActivity.this.finish();
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
		}
		// We stop this the first time we go here.
		cursor.close();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
	}
}
