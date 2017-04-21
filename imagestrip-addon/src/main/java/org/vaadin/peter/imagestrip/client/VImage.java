package org.vaadin.peter.imagestrip.client;

public class VImage {

	private final int imageIndex;
	private final String url;

	private final int width;
	private final int height;

	public VImage(int imageIndex, String url, int width, int height) {
		this.imageIndex = imageIndex;
		this.url = url;
		this.width = width;
		this.height = height;
	}

	public int getImageIndex() {
		return imageIndex;
	}

	public String getURL() {
		return url;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
