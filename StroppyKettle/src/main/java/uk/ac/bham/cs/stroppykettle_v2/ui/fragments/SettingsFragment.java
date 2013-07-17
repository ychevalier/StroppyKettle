package uk.ac.bham.cs.stroppykettle_v2.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import uk.ac.bham.cs.stroppykettle_v2.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	public interface ServiceParamsChangedListener {
		void onServiceParamsChanged();
	}

	private ServiceParamsChangedListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.fragment_settings);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListener = (ServiceParamsChangedListener) getActivity();
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key == null || mListener == null) return;

		if (key.equals(getString(R.string.alive_key)) || key.equals(getString(R.string.address_key))) {
			mListener.onServiceParamsChanged();
		}
	}
}