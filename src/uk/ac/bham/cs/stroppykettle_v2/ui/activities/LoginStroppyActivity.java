package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginStroppyActivity extends GenericStroppyActivity implements OnClickListener {
	
	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = LoginStroppyActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_stroppy);
		
		Button stroppyButton = (Button) findViewById(R.id.login_button);
		stroppyButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent i = null;
		switch (v.getId()) {
		case R.id.login_button:
			i = new Intent(this, CupsStroppyActivity.class);
			break;
		}
		if(i != null) {
			startActivity(i);
			//finish();
		}
	}
	
	@Override
	public void onBackPressed() {}
}