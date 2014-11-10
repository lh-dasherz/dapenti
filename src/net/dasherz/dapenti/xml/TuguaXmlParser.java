package net.dasherz.dapenti.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class TuguaXmlParser {
	public static class TuguaItem {
		private String title;
		private String link;
		private String author;
		private String pubDate;
		private String description;

		public TuguaItem() {
			super();
		}

		public TuguaItem(String title, String link, String author, String pubDate, String description) {
			super();
			this.title = title;
			this.link = link;
			this.author = author;
			this.pubDate = pubDate;
			this.description = description;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getLink() {
			return link;
		}

		public void setLink(String link) {
			this.link = link;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getPubDate() {
			return pubDate;
		}

		public void setPubDate(String pubDate) {
			this.pubDate = pubDate;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	private static final String ns = null;

	public List<TuguaItem> parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readRss(parser);
		} finally {
			in.close();
		}
	}

	private List<TuguaItem> readRss(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "rss");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("channel")) {
				return readChannel(parser);
			} else {
				skip(parser);
			}
		}

		return null;
	}

	private List<TuguaItem> readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
		List<TuguaItem> items = new ArrayList<>();
		parser.require(XmlPullParser.START_TAG, ns, "channel");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("item")) {
				items.add(readItem(parser));
			} else {
				skip(parser);
			}
		}

		return items;
	}

	private TuguaItem readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "item");
		TuguaItem item = new TuguaItem();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("title")) {
				item.setTitle(readTitle(parser));
			} else if (name.equals("link")) {
				item.setLink(readLink(parser));
			} else if (name.equals("author")) {
				item.setAuthor(readAuthor(parser));
			} else if (name.equals("pubDate")) {
				item.setPubDate(readPubDate(parser));
			} else if (name.equals("description")) {
				item.setDescription(readDescription(parser));
			} else {
				skip(parser);
			}
		}
		return item;
	}

	private String readPubDate(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "pubDate");
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "pubDate");
		return text;
	}

	private String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "description");
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "description");
		return text;
	}

	private String readAuthor(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "author");
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "author");
		return text;
	}

	private String readLink(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "link");
		String link = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "link");
		return link;
	}

	private String readTitle(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "title");
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "title");
		return title;
	}

	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

}