package uk.ac.bham.cs.stroppykettle_v2.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import uk.ac.bham.cs.stroppykettle_v2.provider.StroppyKettleDatabase;

public class ExportActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ExportDatabaseFileTask task = new ExportDatabaseFileTask(this);
		task.execute();
	}

	class ExportDatabaseFileTask extends AsyncTask<Void, Void, Boolean> {
		private final ProgressDialog progressDialog;

		private Context context;

		public ExportDatabaseFileTask(Context context) {
			this.context = context;
			progressDialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			this.progressDialog.setMessage("Exporting database...");
			this.progressDialog.show();
		}

		protected Boolean doInBackground(Void... args) {
			File dbFile = new File(Environment.getDataDirectory() + "/data/uk.ac.bham.cs.stroppykettle_v2/databases/" + StroppyKettleDatabase.DATABASE_NAME);

			File exportDir = new File(Environment.getExternalStorageDirectory(), "");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File file = new File(exportDir, dbFile.getName());

			try {
				file.createNewFile();
				this.copyFile(dbFile, file);
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		protected void onPostExecute(final Boolean success) {
			if (this.progressDialog.isShowing()) {
				this.progressDialog.dismiss();
			}
			if (success) {
				Toast.makeText(context, "Export successful!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show();
			}
			ExportActivity.this.finish();
		}

		void copyFile(File src, File dst) throws IOException {
			FileChannel inChannel = new FileInputStream(src).getChannel();
			FileChannel outChannel = new FileOutputStream(dst).getChannel();
			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} finally {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			}
		}
	}
}
