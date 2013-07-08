package uk.ac.bham.cs.stroppykettle_v2.ui.fragments;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsFragment extends Fragment {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = SettingsFragment.class.getSimpleName();

	public static final String EXTRA_MESSAGE = "uk.ac.bham.cs.stroppykettle_v2.ui.fragments.SettingsFragment.EXTRA_MESSAGE";

	private int mNbCups;

	public static final SettingsFragment newInstance(int nbCups) {
		SettingsFragment f = new SettingsFragment();
		Bundle bdl = new Bundle(1);
		bdl.putInt(EXTRA_MESSAGE, nbCups);
		f.setArguments(bdl);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mNbCups = getArguments().getInt(EXTRA_MESSAGE);
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
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
		} else if (mNbCups > StroppyKettleApplication.NUMBER_OF_CUPS) {
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
