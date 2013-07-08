package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.Users;
import uk.ac.bham.cs.stroppykettle_v2.ui.adapters.UsersCursorAdapter;

public class LoginStroppyActivity extends GenericStroppyActivity implements
		OnItemClickListener, LoaderCallbacks<Cursor>, OnEditorActionListener {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = LoginStroppyActivity.class
			.getSimpleName();

	private UsersCursorAdapter mAdapter;
	private EditText mAddName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_stroppy);

		ListView userList = (ListView) findViewById(R.id.user_list);

		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.row_add_user,
				userList, false);
		userList.addFooterView(header, null, true);

		mAddName = (EditText) findViewById(R.id.row_add_user);
		mAddName.setOnEditorActionListener(this);

		userList.setOnItemClickListener(this);
		mAdapter = new UsersCursorAdapter(this);
		userList.setAdapter(mAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		getSupportLoaderManager().restartLoader(0, null, this);
	}

	@Override
	protected void receivedNewWeight(float weight) {
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {Users.USER_ID, Users.USER_NAME};

		CursorLoader cursorLoader = new CursorLoader(this, Users.CONTENT_URI,
				projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	private void startCupsActivity(long id, String name) {
		Intent i = new Intent(this, CupsStroppyActivity.class);
		i.putExtra(CupsStroppyActivity.EXTRA_USER_NAME, name);
		i.putExtra(CupsStroppyActivity.EXTRA_USER_ID, id);
		startActivity(i);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		String name = (String) ((TextView) v.findViewById(R.id.row_user_name))
				.getText();
		startCupsActivity(id, name);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH
				|| actionId == EditorInfo.IME_ACTION_DONE
				|| event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

			if (!TextUtils.isEmpty(v.getText())) {
				String name = v.getText().toString();

				boolean isPresent = false;
				for (int i = 0; i < mAdapter.getCount(); i++) {
					String cName = mAdapter.getItemName(i);
					if (name.equalsIgnoreCase(cName)) {
						isPresent = true;
						break;
					}
				}
				if (!isPresent && getContentResolver() != null) {
					ContentValues cv = new ContentValues();
					cv.put(Users.USER_NAME, name);
					Uri uri = getContentResolver().insert(Users.CONTENT_URI, cv);

					try {
						long id = Long.parseLong(uri.getLastPathSegment());
						startCupsActivity(id, name);
					} catch (NumberFormatException e) {
						// Nothing to do here.
					}
				} else if (isPresent) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);

					builder.setMessage("You are already in the list").setTitle("Error");

					builder.setPositiveButton(android.R.string.ok, null);

					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}

			mAddName.clearFocus();
			mAddName.setText("");
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mAddName.getWindowToken(), 0);
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
	}
}