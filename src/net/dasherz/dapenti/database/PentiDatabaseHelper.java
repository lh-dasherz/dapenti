package net.dasherz.dapenti.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dasherz.dapenti.xml.PentiItem;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PentiDatabaseHelper extends SQLiteOpenHelper {

	public PentiDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DBConstants.CREATE_TABLE_TUGUA_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DB", "Currently no update.");
	}

	public int insertItemsIfNotExist(List<PentiItem> items, int contentType) {
		int itemUpdated = 0;
		for (PentiItem item : items) {
			Cursor cursor = this.getReadableDatabase().query(false, DBConstants.TABLE_TUGUA,
					new String[] { DBConstants.ITEM_TITLE }, "title=?", new String[] { item.getTitle() }, null, null,
					null, null);
			if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(DBConstants.ITEM_TITLE, item.getTitle());
				values.put(DBConstants.ITEM_LINK, item.getLink());
				values.put(DBConstants.ITEM_AUTHOR, item.getAuthor());
				values.put(DBConstants.ITEM_PUB_DATE, item.getPubDate());
				values.put(DBConstants.ITEM_DESCRIPTION, item.getDescription());
				values.put(DBConstants.ITEM_CONTENT_TYPE, String.valueOf(contentType));
				values.put(DBConstants.ITEM_IS_FAVOURITE, "0");
				this.getWritableDatabase().insert(DBConstants.TABLE_TUGUA, null, values);
				itemUpdated++;
				Log.d("DB", "insert new record: " + item.getTitle());
			}
			cursor.close();
		}
		return itemUpdated;
	}

	public List<Map<String, String>> readItems(int contentType, int from, int to) {
		String offset = String.valueOf(from);
		String limit = String.valueOf(to - from);
		String type;
		Cursor cursor;
		// for favorite
		if (contentType == -1) {
			cursor = this.getReadableDatabase().rawQuery(
					"select * from tugua_item where is_favourite='1' order by pubDate DESC  Limit ? Offset ?",
					new String[] { limit, offset });
		} else {
			type = String.valueOf(contentType);
			cursor = this.getReadableDatabase()
					.rawQuery(DBConstants.SELECT_TUGUA, new String[] { type, limit, offset });
		}

		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(DBConstants.ITEM_ID, cursor.getString(0));
			map.put(DBConstants.ITEM_TITLE, cursor.getString(1));
			map.put(DBConstants.ITEM_LINK, cursor.getString(2));
			map.put(DBConstants.ITEM_AUTHOR, cursor.getString(3));
			map.put(DBConstants.ITEM_PUB_DATE, cursor.getString(4));
			String desc = cursor.getString(5);
			if (cursor.getString(6).equals(String.valueOf(DBConstants.CONTENT_TYPE_TWITTE))) {
				desc = convertHtmlToText(desc);
			}
			map.put(DBConstants.ITEM_DESCRIPTION, desc);
			map.put(DBConstants.ITEM_CONTENT_TYPE, cursor.getString(6));
			data.add(map);
		}
		Log.d("DB", "current data size: " + data.size());
		cursor.close();
		return data;
	}

	private String convertHtmlToText(String desc) {
		desc = desc.replaceAll("<DIV.+?>", "").replaceAll("</DIV>", "").replaceAll("<A.+?>", "").replaceAll("</A>", "")
				.replaceAll("<I.+?>", "").replaceAll("</I>", "").replaceAll("<EM.+?>", "").replaceAll("</EM>", "")
				.replaceAll("<FONT.+?>", "").replaceAll("</FONT>", "").replaceAll("&nbsp;", " ")
				.replaceAll("&#8943;", "--");
		return desc;
	}

	public int getCountForType(int contentType) {
		Cursor cursor = this.getReadableDatabase().rawQuery(DBConstants.SELECT_TUGUA_ALL,
				new String[] { String.valueOf(contentType) });
		return cursor.getCount();
	}

	public void addToFav(String ids) {
		// this.getWritableDatabase().rawQuery("update tugua_item set is_favourite =1 where _id in ( ?)",
		// new String[] { ids });
		String[] idArray = ids.split(",");
		ContentValues values = new ContentValues();
		values.put("is_favourite", "1");
		for (String id : idArray) {
			this.getWritableDatabase().update(DBConstants.TABLE_TUGUA, values, "_id=?", new String[] { id });

			// .ex(("update tugua_item set is_favourite = '1' where _id = ?;",
			// new String[] { id });
			Log.d("DB", "update tugua_item set is_favourite =1 where _id = " + id);
		}
		// Log.d("DB", "update tugua_item set is_favourite =1 where _id = " +
		// id);
	}

	public int getCountForFav() {
		Cursor cursor = this.getReadableDatabase().rawQuery(
				"select * from tugua_item where is_favourite='1' order by pubDate DESC;", null);
		return cursor.getCount();
	}

}
