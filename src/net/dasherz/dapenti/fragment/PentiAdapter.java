package net.dasherz.dapenti.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dasherz.dapenti.R;
import net.dasherz.dapenti.database.DBConstants;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PentiAdapter extends BaseAdapter {

	private final HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

	private final Context ctx;
	private final List<Map<String, String>> data;
	private String footer;

	public PentiAdapter(Context ctx, List<Map<String, String>> data, String footer) {
		super();
		this.ctx = ctx;
		this.data = data;
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
			if (getData().get(position).get(DBConstants.ITEM_CONTENT_TYPE)
					.equals(String.valueOf(DBConstants.CONTENT_TYPE_TWITTE))) {
				return getData().get(position).get(DBConstants.ITEM_DESCRIPTION);
			} else {
				return getData().get(position).get(DBConstants.ITEM_TITLE);
			}
		} else {
			return getFooter();

		}
	}

	@Override
	public long getItemId(int position) {
		if (position < getData().size()) {
			return Integer.parseInt(getData().get(position).get(DBConstants.ITEM_ID));
		} else {
			return -1;

		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// LayoutInflater inflater = LayoutInflater.from(ctx);
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View entry = inflater.inflate(R.layout.tugua_entry, parent, false);
		TextView textView = (TextView) entry.findViewById(R.id.tugua_entry);
		// textView.setBackgroundResource(R.drawable.bkg);
		if (position < getData().size()) {
			textView.setText(getItem(position).toString());
		} else {
			textView.setText(getFooter());
			textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		}
		if (mSelection.get(position) != null) {
			entry.setBackgroundColor(ctx.getResources().getColor(R.color.holo_blue_color));
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
