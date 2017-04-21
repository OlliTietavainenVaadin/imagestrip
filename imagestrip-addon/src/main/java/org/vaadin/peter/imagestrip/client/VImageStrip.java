package org.vaadin.peter.imagestrip.client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;

/**
 * VImageStrip is client side implementation for ImageStrip component
 * 
 * @author Peter Lehto / IT Mill Oy Ltd
 */

public class VImageStrip extends FlowPanel implements ClickHandler,
		ResizeHandler {

	public final static String CLASS_NAME = "v-imagestrip";

	private String UIDLId;
	private ApplicationConnection connection;

	final static int BUTTON_WIDTH = 16;

	private int imageBoxWidth = -1;
	private int imageBoxHeight = -1;
	private int alignment = -1;
	private boolean animated;
	private boolean selectable;
	private boolean heightSet;
	private boolean widthSet;
	private boolean imagesRequested;

	private final Button scrollLeft;
	private final Button scrollRight;

	private final VStrip strip;
	private int selectedImage;
	private int lastNumberOfImages;

	private HandlerRegistration handlerRegistration;

	public VImageStrip() {

		setStyleName(CLASS_NAME);
		strip = new VStrip(this);

		scrollLeft = new Button();
		scrollLeft.addClickHandler(this);

		scrollRight = new Button();
		scrollRight.addClickHandler(this);

		add(scrollLeft);
		add(strip);
		add(scrollRight);

		handlerRegistration = Window.addResizeHandler(this);
	}

	@Override
	protected void onDetach() {
		handlerRegistration.removeHandler();
	}

	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		UIDLId = uidl.getId();
		connection = client;

		imageBoxWidth = uidl.getIntAttribute("boxWidth");
		imageBoxHeight = uidl.getIntAttribute("boxHeight");
		setAlignment(uidl.getIntAttribute("alignment"));
		animated = uidl.getBooleanAttribute("animated");
		selectable = uidl.getBooleanAttribute("selectable");
		strip.setSelectable(selectable);
		selectedImage = uidl.getIntAttribute("selectedImage");

		List<VImage> imagesToShow = new LinkedList<VImage>();

		if (!imagesRequested) {
			requestImagesFromServer();
			imagesRequested = true;
		}

		if (uidl.getBooleanAttribute("removeAll")) {
			strip.removeImages();
			strip.updateSize();
		}

		if (uidl.getChildByTagName("images") != null) {
			UIDL imagesUIDL = uidl.getChildByTagName("images");

			Iterator<Object> imageIterator = imagesUIDL.getChildIterator();

			while (imageIterator.hasNext()) {
				UIDL imageUIDL = (UIDL) imageIterator.next();

				int index = imageUIDL.getIntAttribute("index");
				String url = connection.translateVaadinUri(imageUIDL
						.getStringAttribute("resource"));
				int width = imageUIDL.getIntAttribute("width");
				int height = imageUIDL.getIntAttribute("height");

				VImage image = new VImage(index, url, width, height);
				imagesToShow.add(image);
			}

			int direction = uidl.getIntAttribute("direction");

			if (direction == -1) {
				strip.moveLeftAndSet(imagesToShow);
			} else if (direction == 1) {
				strip.moveRightAndSet(imagesToShow);
			} else {
				strip.setImages(imagesToShow);
			}
		}

		strip.selectImage(selectedImage, false);
	}

	public int getSelectedImage() {
		return selectedImage;
	}

	public void updateSelectedImageToServer(int imageIndex) {
		selectedImage = imageIndex;
		connection.updateVariable(UIDLId, "clickedImage", imageIndex, true);
	}

	@Override
	public void setWidth(String width) {
		super.setWidth(width);
		widthSet = true;

		if (isSizeSet()) {
			strip.updateSize();
		}
	}

	@Override
	public void setHeight(String height) {
		super.setHeight(height);
		heightSet = true;

		if (isSizeSet()) {
			strip.updateSize();
		}
	}

	public int getAlignment() {
		return alignment;
	}

	public int getImageBoxWidth() {
		return imageBoxWidth;
	}

	public int getImageBoxHeight() {
		return imageBoxHeight;
	}

	public boolean isAnimated() {
		return animated;
	}

	/**
	 * Loads next image from left
	 */

	public void loadToLeft() {
		connection.updateVariable(UIDLId, "cursor", "left", true);
	}

	/**
	 * Loads next image from right
	 */

	public void loadToRight() {
		connection.updateVariable(UIDLId, "cursor", "right", true);
	}

	@Override
	public void onClick(ClickEvent clickEvent) {
		if (scrollLeft.equals(clickEvent.getSource())) {
			loadToRight();
		} else if (scrollRight.equals(clickEvent.getSource())) {
			loadToLeft();
		}
	}

	public boolean isSelectable() {
		return selectable;
	}

	private void requestImagesFromServer() {
		int numOfImages = strip.getNumOfImages();

		if (lastNumberOfImages != numOfImages) {
			lastNumberOfImages = numOfImages;

			connection.updateVariable(UIDLId, "numOfImages", numOfImages, true);
		}
	}

	private void setAlignment(int alignment) {
		this.alignment = alignment;

		if (alignment == 0) {
			scrollLeft.setStyleName("strip-horizontal-scroller left");
			scrollRight.setStyleName("strip-horizontal-scroller right");
		} else {
			scrollLeft.setStyleName("strip-vertical-scroller up");
			scrollRight.setStyleName("strip-vertical-scroller down");
		}
	}

	private boolean isSizeSet() {
		return widthSet && heightSet;
	}

	@Override
	public void onResize(ResizeEvent event) {
		requestImagesFromServer();
	}
}