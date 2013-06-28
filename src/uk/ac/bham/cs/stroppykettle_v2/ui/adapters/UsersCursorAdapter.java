package uk.ac.bham.cs.stroppykettle_v2.ui.adapters;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleContract.Users;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UsersCursorAdapter extends CursorAdapter {

	class UserHolder {
		TextView name;
	}

	public UsersCursorAdapter(Context context) {
		super(context, null, 0);
	}
	
	public String getItemName(int position) {
		if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
            	return ((Cursor) getItem(position)).getString(mCursor
        				.getColumnIndex(Users.USER_NAME));
            } else {
                return null;
            }
        } else {
            return null;
        }
	}

	@Override
	public long getItemId(int position) {
		if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
            	return ((Cursor) getItem(position)).getLong(mCursor
        				.getColumnIndex(Users.USER_ID));
            } else {
                return 0;
            }
        } else {
            return 0;
        }
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.row_user, parent, false);

		UserHolder holder = new UserHolder();
		holder.name = (TextView) v.findViewById(R.id.row_user_name);

		v.setTag(holder);

		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		UserHolder holder = (UserHolder) view.getTag();

		holder.name.setText(cursor.getString(cursor
				.getColumnIndex(Users.USER_NAME)));
	}
}
