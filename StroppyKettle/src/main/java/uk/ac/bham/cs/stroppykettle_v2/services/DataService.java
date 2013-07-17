package uk.ac.bham.cs.stroppykettle_v2.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import uk.ac.bham.cs.stroppykettle_v2.R;
import uk.ac.bham.cs.stroppykettle_v2.io.GenericSendTask;
import uk.ac.bham.cs.stroppykettle_v2.io.LogsSendTask;

public class DataService extends IntentService implements GenericSendTask.SentListener {

	private static final int CONNECTIONS_TASK_ID = 0;
	private static final int LOGS_TASK_ID = 1;
	private static final int INTERACTIONS_TASK_ID = 2;

	public DataService() {
		super(DataService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		// Send Connections.
		//ConnectionsSendTask conTask = new ConnectionsSendTask(CONNECTIONS_TASK_ID, this, this);
		//conTask.execute(settings.getLong(getString(R.string.send_last_connections_key), 0));

		// Send Logs
		LogsSendTask logTask = new LogsSendTask(LOGS_TASK_ID, this, this);
		logTask.execute(settings.getLong(getString(R.string.send_last_logs_key), 0));

		// Send Interactions
		//InteractionsSendTask interactionsTask = new InteractionsSendTask(INTERACTIONS_TASK_ID, this, this);
		//interactionsTask.execute(settings.getLong(getString(R.string.send_last_interactions_key), 0));
	}

	@Override
	public void onSent(int id, long startDate, boolean success) {

		if (success) {
			Toast.makeText(this, "Sending Successful!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Sending Failed...", Toast.LENGTH_SHORT).show();
		}

		if (!success) return;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();

		switch (id) {
			case CONNECTIONS_TASK_ID:
				editor.putLong(getString(R.string.send_last_connections_key), startDate);
				break;
			case LOGS_TASK_ID:
				editor.putLong(getString(R.string.send_last_logs_key), startDate);
				break;
			case INTERACTIONS_TASK_ID:
				editor.putLong(getString(R.string.send_last_interactions_key), startDate);
				break;
		}
		editor.commit();
	}
}
