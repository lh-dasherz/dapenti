package net.dasherz.dapenti.xml;

public class TuguaItem {
	private String title;
	private String link;
	private String author;
	private long pubDate;
	private String description;

	public TuguaItem() {
		super();
	}

	public TuguaItem(String title, String link, String author, long pubDate, String description) {
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

	public long getPubDate() {
		return pubDate;
	}

	public void setPubDate(long pubDate) {
		this.pubDate = pubDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
