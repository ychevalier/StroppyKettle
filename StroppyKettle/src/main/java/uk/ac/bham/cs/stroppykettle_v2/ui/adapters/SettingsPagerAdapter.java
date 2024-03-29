package uk.ac.bham.cs.stroppykettle_v2.ui.adapters;

import java.util.List;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SettingsPagerAdapter extends FragmentPagerAdapter {
	private List<Fragment> fragments;
	
	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = SettingsPagerAdapter.class.getSimpleName();

	public SettingsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}
}
