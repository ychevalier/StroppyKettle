package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CupsStroppyActivity extends GenericStroppyActivity implements OnClickListener {
	
	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = CupsStroppyActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cups_stroppy);
		
		Button stroppyButton = (Button) findViewById(R.id.cups_button);
		stroppyButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent i = null;
		switch (v.getId()) {
		case R.id.cups_button:
			i = new Intent(this, GameStroppyActivity.class);
			break;
		}
		if(i != null) {
			startActivity(i);
			//finish();
		}
	}
}