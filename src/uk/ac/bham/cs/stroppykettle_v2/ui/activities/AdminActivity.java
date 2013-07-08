package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import uk.ac.bham.cs.stroppykettle_v2.R;

public class AdminActivity extends GenericActivity implements OnClickListener {

	private Button mStroppyButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_admin);

		mStroppyButton = (Button) findViewById(R.id.stroppy_button);
		mStroppyButton.setOnClickListener(this);
	}

	@Override
	protected void receivedNewWeight(float weight) {

	}

	@Override
	public void onClick(View v) {
		Intent i = null;
		switch (v.getId()) {
			case R.id.stroppy_button:
				i = new Intent(this, LoginStroppyActivity.class);
				break;
		}
		if (i != null) {
			startActivity(i);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
	}
}
