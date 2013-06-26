package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import java.util.ArrayList;
import java.util.List;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.ui.adapters.SettingsPageAdapter;
import uk.ac.bham.cs.stroppykettle_v2.ui.fragments.SettingsFragment;
import uk.ac.bham.cs.stroppykettle_v2.ui.views.CustomViewPager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.viewpagerindicator.CirclePageIndicator;

public class SettingsActivity extends GenericActivity implements
		OnPageChangeListener {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = SettingsActivity.class.getSimpleName();

	private SettingsPageAdapter pageAdapter;
	private int mCurrentPosition;
	private CustomViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		List<Fragment> fragments = getFragments();
		pageAdapter = new SettingsPageAdapter(getSupportFragmentManager(),
				fragments);
		mPager = (CustomViewPager) findViewById(R.id.viewpager);
		mPager.setAdapter(pageAdapter);

		// Bind the title indicator to the adapter
		CirclePageIndicator pageIndicator = (CirclePageIndicator) findViewById(R.id.circles);
		pageIndicator.setViewPager(mPager);

		pageIndicator.setOnPageChangeListener(this);

		mPager.setCurrentItem(0);
		mCurrentPosition = mPager.getCurrentItem();
	}

	private List<Fragment> getFragments() {
		List<Fragment> fList = new ArrayList<Fragment>();

		for (int i = -1; i <= StroppyKettleApplication.NUMBER_OF_CUPS + 1; i++) {
			fList.add(SettingsFragment.newInstance(i));
		}
		return fList;
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
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
							mPager.setCurrentItem(mCurrentPosition);
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();

		} else {
			mCurrentPosition = position;
			
			// Position start from 0, and there is nbCups + 3 fragments.
			// (nothing, empty and done).
			if(position == StroppyKettleApplication.NUMBER_OF_CUPS + 2) {
				mPager.setPagingEnabled(false);
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						finish();
					}
				}, 1000);
			}
		}
	}
}
