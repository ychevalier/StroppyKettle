package uk.ac.bham.cs.stroppykettle_v2.ui.adapters;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class CupsPagerAdapter extends PagerAdapter {

	private List<View> mViews;

	public CupsPagerAdapter(List<View> views) {
		super();
		mViews = views;
	}

	@Override
	public int getCount() {
		if (mViews == null) {
			return 0;
		}
		return mViews.size();
	}

	@Override
	public float getPageWidth(int position) {
		return 1.f;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ViewPager pager = (ViewPager) container;
		View view = mViews.get(position);

		pager.addView(view);

		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object view) {
		((ViewPager) container).removeView((View) view);
	}
}
