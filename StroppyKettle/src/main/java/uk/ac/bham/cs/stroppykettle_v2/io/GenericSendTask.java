package uk.ac.bham.cs.stroppykettle_v2.io;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.util.Calendar;

import uk.ac.bham.cs.stroppykettle_v2.StroppyKettleApplication;
import uk.ac.bham.cs.stroppykettle_v2.utils.Utils;

public abstract class GenericSendTask extends AsyncTask<Long, Void, Boolean> {

	private static final boolean DEBUG_MODE = StroppyKettleApplication.DEBUG_MODE;
	private static final String TAG = GenericSendTask.class.getSimpleName();

	public interface SentListener {
		void onSent(int id, long startDate, boolean success);
	}

	protected int mId;
	protected Context mContext;
	protected SentListener mListener;
	protected long mStartDate;

	public GenericSendTask(int id, Context context, SentListener listener) {
		mId = id;
		mContext = context;
		mListener = listener;
		mStartDate = 0;
	}

	@Override
	protected void onPreExecute() {
		Calendar cal = Calendar.getInstance();
		mStartDate = cal.getTimeInMillis();
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		if (mListener != null) {
			mListener.onSent(mId, mStartDate, success);
		}
	}

	protected boolean checkConnectivity() {
		if (mContext == null) return false;

		ConnectivityManager conMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() == null
				|| !conMgr.getActiveNetworkInfo().isAvailable()
				|| !conMgr.getActiveNetworkInfo().isConnected()) {
			return false;
		}
		return true;
	}

	protected boolean sendJSON(JSONObject content, String path) {

		int TIMEOUT_MILLISEC = 10000;  // = 10 seconds
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpClient client = new DefaultHttpClient(httpParams);
		HttpPost request = new HttpPost(Utils.SERVER_URL + path);
		try {
			//request.setEntity(new ByteArrayEntity(
			//		content.toString().getBytes("UTF8")));

			StringEntity se = new StringEntity(content.toString(), "UTF-8");
			se.setContentType("application/json; charset=UTF-8");
			request.setEntity(se);
			HttpResponse response = client.execute(request);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return true;
			}
			return false;
		} catch (Exception e) {
			if (DEBUG_MODE) {
				e.printStackTrace();
			}
			return false;
		}
	}
}