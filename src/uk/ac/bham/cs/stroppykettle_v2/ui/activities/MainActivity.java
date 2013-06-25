package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import uk.ac.bham.cs.stroppykettle_v2.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends GenericActivity implements OnClickListener {

	private Button mStroppyButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mStroppyButton = (Button) findViewById(R.id.stroppy_button);
		mStroppyButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent i = null;
		switch (v.getId()) {
		case R.id.stroppy_button:
			i = new Intent(this, StroppyActivity.class);
			break;
		}
		if(i != null) {
			startActivity(i);
		}
	}

}
