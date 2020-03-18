package com.example.model;

public class Favorite {
	private String url;
	private String category;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	@Override
	public String toString() {
		return "{url: " + url + ", category: " + category + "}";
	}
}
