package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import uk.ac.bham.cs.stroppykettle_v2.ui.fragments.SettingsFragment;
import uk.ac.bham.cs.stroppykettle_v2.ui.views.CustomViewPager;

public class SettingsActivity extends GenericActivity implements
		OnPageChangeListener, View.OnClickListener {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = SettingsActivity.class.getSimpleName();

	private int mCurrentPosition;
	private CustomViewPager mPager;
	private boolean mDoNotUpdateOnCancel;
	private Button mWeightIndicator;
	private float mLastWeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		mLastWeight = 0;

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

		mPager.setCurrentItem(0);
		mCurrentPosition = mPager.getCurrentItem();
		mDoNotUpdateOnCancel = false;
	}

	@Override
	protected void receivedNewWeight(float weight) {
		mLastWeight = weight;
		mWeightIndicator.setText(String.valueOf(mLastWeight));
	}

	@Override
	public void onClick(View view) {
		if(view == mWeightIndicator) {
			getCurrentWeight();
		}
	}

	private List<Fragment> getFragments() {
		List<Fragment> fList = new ArrayList<Fragment>();

		for (int i = -1; i <= StroppyKettleApplication.NUMBER_OF_CUPS + 1; i++) {
			fList.add(SettingsFragment.newInstance(i));
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
							mDoNotUpdateOnCancel = true;
							mPager.setCurrentItem(mCurrentPosition);
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();

		} else {
			mCurrentPosition = position;
			if(!mDoNotUpdateOnCancel) {
				// We are using position -2 because it starts at -1 and
				// we are at the next position that the one we want to update.
				ContentValues cv = new ContentValues();
				cv.put(StroppyKettleContract.Scale.SCALE_NB_CUPS, mCurrentPosition - 2);
				cv.put(StroppyKettleContract.Scale.SCALE_WEIGHT, mLastWeight);
				getContentResolver().insert(StroppyKettleContract.Scale.CONTENT_URI, cv);

				// Position start from 0, and there is nbCups + 3 fragments.
				// (nothing, empty and done).
				if (mCurrentPosition == StroppyKettleApplication.NUMBER_OF_CUPS + 2) {
					mPager.setPagingEnabled(false);
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							finish();
						}
					}, 1000);
				}
			}
			mDoNotUpdateOnCancel = false;
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
