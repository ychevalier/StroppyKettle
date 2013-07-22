package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.os.Bundle;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.ui.fragments.SettingsFragment;

public class SettingsActivity extends GenericActivity implements SettingsFragment.ServiceParamsChangedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getString(R.string.action_settings));

		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();
	}

	@Override
	public void onServiceParamsChanged() {
		connectNewAddress();
	}

	@Override
	protected void onConnect() {
		//Toast.makeText(this, mIsConnected ? "Connected" : "Disconnected", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDisconnect() {
		//Toast.makeText(this, mIsConnected ? "Connected" : "Disconnected", Toast.LENGTH_SHORT).show();
	}
}