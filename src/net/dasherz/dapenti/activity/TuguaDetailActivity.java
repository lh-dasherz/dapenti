package net.dasherz.dapenti.activity;

import java.io.IOException;

import net.dasherz.dapenti.R;
import net.dasherz.dapenti.R.id;
import net.dasherz.dapenti.R.layout;
import net.dasherz.dapenti.R.menu;
import net.dasherz.dapenti.database.DBConstants;
import net.dasherz.dapenti.util.NetUtil;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TuguaDetailActivity extends Activity {

	TextView titleView;
	WebView tuguaWebView;
	String title, url, link;
	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tugua_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		titleView = (TextView) findViewById(R.id.tuguaTitle);
		tuguaWebView = (WebView) findViewById(R.id.tuguaDetailPage);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);

		Intent intent = getIntent();
		title = intent.getStringExtra(DBConstants.ITEM_TITLE);
		url = intent.getStringExtra(DBConstants.ITEM_DESCRIPTION);
		link = intent.getStringExtra(DBConstants.ITEM_LINK);
		titleView.setText(title);
		boolean whetherBlockImage = NetUtil.whetherBlockImage(this);
		tuguaWebView.getSettings().setBlockNetworkImage(whetherBlockImage);
		// tuguaWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		// tuguaWebView.loadUrl(url);
		new LoadPageTask().execute(url);
	}

	class LoadPageTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String lines = null;
			// StringBuffer buffer = new StringBuffer();
			try {
				lines = NetUtil.getContentOfURL(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// for (int i = 0; i < lines.size(); i++) {
			// String line = lines.get(i);
			//
			// if (line.contains("<IMG") && !line.contains("gif")) {
			// line = line.replace("<IMG", "<IMG width=\"100%\"");
			// }
			// buffer.append(line);
			// }
			if (lines == null) {
				lines = "��ȡ����ʧ�ܡ�";
			}
			String content = "<html xmlns=\"http://www.w3.org/1999/xhtml\" ><head><meta http-equiv='content-type' content='text/html; charset=utf-8' /></head><body>"
					+ lines + "</body>";
			lines = null;
			return content;
		}

		@Override
		protected void onPostExecute(String result) {
			// tuguaWebView.loadData(new String((result.getBytes("UTF-8"))),
			// "text/html", "UTF-8");
			tuguaWebView.loadData(result, "text/html; charset=UTF-8", null);
			progressBar.setVisibility(View.GONE);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tugua_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		if (id == R.id.add_favourite) {
			// TODO
			return true;
		}
		if (id == R.id.copy_title) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("title", title);
			clipboard.setPrimaryClip(clip);
			Toast.makeText(this, "�Ѿ����Ʊ��⵽�����塣", Toast.LENGTH_SHORT).show();
			return true;
		}
		if (id == R.id.open_in_browser) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			startActivity(browserIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}