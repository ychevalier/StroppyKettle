package uk.ac.bham.cs.stroppykettle_v2.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import uk.ac.bham.cs.stroppykettle_v2.R;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.fragment_settings);
	}
}