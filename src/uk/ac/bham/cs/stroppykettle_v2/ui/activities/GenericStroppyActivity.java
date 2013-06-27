package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import uk.ac.bham.cs.stroppykettle_v2.R;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

public class GenericStroppyActivity extends GenericActivity implements
		OnLongClickListener {

	@Override
	public void setContentView(int layoutResID) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.setContentView(R.layout.activity_generic_stroppy);

		Button exit = (Button) findViewById(R.id.exit_button);
		exit.setOnLongClickListener(this);

		RelativeLayout contentLayout = (RelativeLayout) findViewById(R.id.content_activity);

		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentLayout.addView(layoutInflater.inflate(layoutResID,
				contentLayout, false));
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.exit_button:
			Intent i = new Intent(this, AdminActivity.class);
			//i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i);
			//finish();
			return true;
		}
		return false;
	}
	
	// It doesnt matter if the user want to go back
	//@Override
	//public void onBackPressed() {}
}
