package net.dasherz.dapenti.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dasherz.dapenti.R;
import net.dasherz.dapenti.R.color;
import net.dasherz.dapenti.R.id;
import net.dasherz.dapenti.R.layout;
import net.dasherz.dapenti.R.menu;
import net.dasherz.dapenti.activity.TuguaDetailActivity;
import net.dasherz.dapenti.constant.Constants;
import net.dasherz.dapenti.database.DBConstants;
import net.dasherz.dapenti.database.PentiDatabaseHelper;
import net.dasherz.dapenti.util.NetUtil;
import net.dasherz.dapenti.xml.TuguaXmlParser;
import net.dasherz.dapenti.xml.TuguaXmlParser.TuguaItem;

import org.xmlpull.v1.XmlPullParserException;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class TuguaFragment extends Fragment {

	PentiDatabaseHelper dbhelper;
	View root;
	ListView listView;
	int recordCount;
	private SwipeRefreshLayout swipeLayout;
	PentiAdapter adapter;

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (dbhelper != null) {
			dbhelper.close();
			Log.d("DB", "closing dbhelper.");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		recordCount = 0;
		root = inflater.inflate(R.layout.list, container, false);
		listView = (ListView) root.findViewById(R.id.tuguaListView);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			int checkitemCount = 0;

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				checkitemCount = 0;
				adapter.clearSelection();
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.list_select_menu, menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				if (item.getItemId() == R.id.copy_title) {
					StringBuffer buffer = new StringBuffer();
					for (Integer integer : adapter.getCurrentCheckedPosition()) {
						buffer.append(adapter.getItem(integer));
					}
					Log.d("AD", buffer.toString());
					ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(
							android.content.Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("titles", buffer.toString());
					clipboard.setPrimaryClip(clip);
					Toast.makeText(getActivity(), "已经复制标题到剪贴板。", Toast.LENGTH_SHORT).show();
				} else if (item.getItemId() == R.id.add_favourite) {
					// TODO
				}
				Toast.makeText(getActivity(), "add fav clicked.", Toast.LENGTH_SHORT).show();
				return true;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				Toast.makeText(getActivity(), "onItemCheckedStateChanged.", Toast.LENGTH_SHORT).show();
				if (checked) {
					checkitemCount++;
					adapter.setNewSelection(position, checked);
				} else {
					checkitemCount--;
					adapter.removeSelection(position);
				}
				mode.setTitle(checkitemCount + "个项目已选择");
			}
		});
		swipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				if (!swipeLayout.isRefreshing()) {
					getLatestData();
				}

			}
		});
		dbhelper = new PentiDatabaseHelper(getActivity(), DBConstants.DATABASE_NAME, null, DBConstants.version);
		loadList();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (adapter.getItem(position).toString().equals(Constants.LOAD_MORE)) {
					List<Map<String, String>> extraData = readDataFromDatabase();
					adapter.getData().addAll(extraData);
					if (extraData.size() == 0) {
						adapter.setFooter(Constants.NO_MORE_NEW);
					}
					adapter.notifyDataSetChanged();
				} else if (position < adapter.getCount() - 1 && adapter.getData().get(position) instanceof Map) {
					Map<String, String> item = adapter.getData().get(position);
					Log.d("TEST", item.toString());
					Intent intent = new Intent(getActivity(), TuguaDetailActivity.class);
					intent.putExtra(DBConstants.ITEM_TITLE, item.get(DBConstants.ITEM_TITLE));
					intent.putExtra(DBConstants.ITEM_DESCRIPTION, item.get(DBConstants.ITEM_DESCRIPTION));
					intent.putExtra(DBConstants.ITEM_LINK, item.get(DBConstants.ITEM_LINK));
					startActivity(intent);
				}
			}
		});
		return root;
	}

	private class PentiAdapter extends BaseAdapter {

		private final HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

		private final Context ctx;
		private final List<Map<String, String>> data;
		private final String itemName;
		private String footer;

		public PentiAdapter(Context ctx, List<Map<String, String>> data, String itemName, String footer) {
			super();
			this.ctx = ctx;
			this.data = data;
			this.itemName = itemName;
			this.footer = footer;
		}

		public void setNewSelection(int position, boolean value) {
			mSelection.put(position, value);
			notifyDataSetChanged();
		}

		public boolean isPositionChecked(int position) {
			Boolean result = mSelection.get(position);
			return result == null ? false : result;
		}

		public Set<Integer> getCurrentCheckedPosition() {
			return mSelection.keySet();
		}

		public void removeSelection(int position) {
			mSelection.remove(position);
			notifyDataSetChanged();
		}

		public void clearSelection() {
			mSelection.clear();// = new HashMap<Integer, Boolean>();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return getData().size() + 1;
		}

		@Override
		public Object getItem(int position) {
			if (position < getData().size()) {
				return getData().get(position).get(itemName);
			} else {
				return getFooter();

			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// LayoutInflater inflater = LayoutInflater.from(ctx);
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View entry = inflater.inflate(R.layout.tugua_entry, parent, false);
			TextView textView = (TextView) entry.findViewById(R.id.tugua_entry);
			// textView.setBackgroundResource(R.drawable.bkg);
			if (position < getData().size()) {
				textView.setText(getData().get(position).get(itemName));
			} else {
				textView.setText(getFooter());
				textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			}
			if (mSelection.get(position) != null) {
				entry.setBackgroundColor(getResources().getColor(R.color.holo_blue_color));
			}
			return entry;
		}

		public List<Map<String, String>> getData() {
			return data;
		}

		public String getFooter() {
			return footer;
		}

		public void setFooter(String footer) {
			this.footer = footer;
		}
	}

	private void loadList() {

		List<Map<String, String>> data = readDataFromDatabase();
		if (data.size() == 0) {
			if (isTableEmpty()) {
				listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
						new String[] { "正在加载..." }));
				getLatestData();
			} else {
				Toast.makeText(getActivity(), "没有更多数据了", Toast.LENGTH_SHORT).show();
			}

		} else {
			adapter = new PentiAdapter(getActivity(), data, DBConstants.ITEM_TITLE, Constants.LOAD_MORE);
			listView.setAdapter(adapter);

		}
	}

	private boolean isTableEmpty() {
		Cursor cursor = dbhelper.getReadableDatabase().rawQuery(DBConstants.SELECT_TUGUA_ALL, null);
		return cursor.getCount() == 0;
	}

	public void reloadList() {
		loadList();
		Log.d("TEST", "reload invoked.");
	}

	public void getLatestData() {
		new RefreshTuguaTask().execute(Constants.URL_TUGUA);
		Log.d("TEST", "get latest data.");
	}

	private List<Map<String, String>> readDataFromDatabase() {
		// FIXME move database operation to AsyncTask
		String limit = String.valueOf(recordCount);
		Cursor cursor = dbhelper.getReadableDatabase().rawQuery(DBConstants.SELECT_TUGUA, new String[] { limit });
		recordCount += cursor.getCount();
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(DBConstants.ITEM_TITLE, cursor.getString(1));
			map.put(DBConstants.ITEM_LINK, cursor.getString(2));
			map.put(DBConstants.ITEM_AUTHOR, cursor.getString(3));
			map.put(DBConstants.ITEM_PUB_DATE, cursor.getString(4));
			map.put(DBConstants.ITEM_DESCRIPTION, cursor.getString(5));
			data.add(map);
		}
		Log.d("DB", "total size: " + data.size());
		cursor.close();
		return data;
	}

	private class RefreshTuguaTask extends AsyncTask<String, Void, List<TuguaItem>> {

		@Override
		protected List<TuguaItem> doInBackground(String... urls) {
			try {
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				e.printStackTrace();
				// return getResources().getString(R.string.connection_error);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				// return getResources().getString(R.string.xml_error);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<TuguaItem> items) {
			if (items == null) {
				Toast.makeText(getActivity(), "更新出错了", Toast.LENGTH_SHORT).show();
				swipeLayout.setRefreshing(false);
				return;
			}
			int itemUpdated = 0;
			PentiDatabaseHelper dbhelper = new PentiDatabaseHelper(getActivity(), DBConstants.DATABASE_NAME, null,
					DBConstants.version);
			// dbhelper.getWritableDatabase().execSQL("delete from tugua_item");
			for (TuguaItem item : items) {
				Cursor cursor = dbhelper.getReadableDatabase().query(false, DBConstants.TABLE_TUGUA,
						new String[] { DBConstants.ITEM_TITLE }, "title=?", new String[] { item.getTitle() }, null,
						null, null, null);
				if (cursor.getCount() == 0) {
					ContentValues valus = new ContentValues();
					valus.put(DBConstants.ITEM_TITLE, item.getTitle());
					valus.put(DBConstants.ITEM_LINK, item.getLink());
					valus.put(DBConstants.ITEM_AUTHOR, item.getAuthor());
					valus.put(DBConstants.ITEM_PUB_DATE, item.getPubDate());
					valus.put(DBConstants.ITEM_DESCRIPTION, item.getDescription());
					dbhelper.getWritableDatabase().insert(DBConstants.TABLE_TUGUA, null, valus);
					itemUpdated++;
					Log.d("DB", "insert new record: " + item.getTitle());
				}
			}
			dbhelper.close();
			if (itemUpdated > 0) {
				// load from beginning
				recordCount = 0;
				reloadList();
			} else {
				Toast.makeText(getActivity(), "已经是最新了", Toast.LENGTH_SHORT).show();
			}
			swipeLayout.setRefreshing(false);
		}
	}

	private List<TuguaItem> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException,
			ParseException {
		InputStream stream = null;
		TuguaXmlParser stackOverflowXmlParser = new TuguaXmlParser();
		List<TuguaItem> items = null;

		try {
			stream = NetUtil.downloadUrl(urlString);
			items = stackOverflowXmlParser.parse(stream);
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		return items;
	}

}
