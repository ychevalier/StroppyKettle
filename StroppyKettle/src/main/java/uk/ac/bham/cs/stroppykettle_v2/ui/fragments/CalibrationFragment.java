package uk.ac.bham.cs.stroppykettle_v2.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;

public class CalibrationFragment extends Fragment {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = CalibrationFragment.class.getSimpleName();

	public static final String EXTRA_MESSAGE = "uk.ac.bham.cs.stroppykettle_v2.ui.fragments.CalibrationFragment.EXTRA_MESSAGE";
	public static final String EXTRA_MAX_CUPS = "uk.ac.bham.cs.stroppykettle_v2.ui.fragments.CalibrationFragment.EXTRA_MAX_CUPS";

	private int mNbCups;
	private int mMaxCups;

	public static final CalibrationFragment newInstance(int nbCups, int maxCups) {
		CalibrationFragment f = new CalibrationFragment();
		Bundle bdl = new Bundle();
		bdl.putInt(EXTRA_MESSAGE, nbCups);
		bdl.putInt(EXTRA_MAX_CUPS, maxCups);
		f.setArguments(bdl);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mNbCups = getArguments().getInt(EXTRA_MESSAGE);
		mMaxCups = getArguments().getInt(EXTRA_MAX_CUPS);
		View v = inflater.inflate(R.layout.fragment_calibration, container, false);
		TextView beginning = (TextView) v.findViewById(R.id.beginning);
		TextView number = (TextView) v.findViewById(R.id.number);
		TextView end = (TextView) v.findViewById(R.id.end);

		if (mNbCups == -1) {
			number.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
					.getDimensionPixelSize(R.dimen.big_big_text)
					/ getResources().getDisplayMetrics().density);
			number.setText(getResources().getString(R.string.remove_kettle));
		} else if (mNbCups == 0) {
			number.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
					.getDimensionPixelSize(R.dimen.big_big_text)
					/ getResources().getDisplayMetrics().density);
			number.setText(getResources().getString(R.string.empty_kettle));
		} else if (mNbCups > mMaxCups) {
			number.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
					.getDimensionPixelSize(R.dimen.big_big_text)
					/ getResources().getDisplayMetrics().density);
			number.setText(getResources().getString(R.string.last_frame));
		} else {
			beginning.setText(getResources().getString(R.string.fill_kettle));
			number.setText(String.valueOf(mNbCups));
			number.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources()
					.getDimensionPixelSize(R.dimen.huge_text)
					/ getResources().getDisplayMetrics().density);
			if (mNbCups == 1) {
				end.setText(getResources().getString(R.string.cup));
			} else {
				end.setText(getResources().getString(R.string.cups));
			}
		}
		return v;
	}
}
