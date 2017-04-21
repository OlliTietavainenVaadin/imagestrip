package org.vaadin.peter.imagestrip.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * VStripItem is an image inside the VStrip
 * 
 * @author Peter Lehto / IT Mill Oy Ltd
 */

public class VStripItem extends FlowPanel {

    /* Current location */

    private int x;
    private int y;

    /* Location before animation started */
    private int initialX;
    private int initialY;

    /* Location where animation is supposed to end */
    private int targetX;
    private int targetY;

    /* Item's dimensions including borders (not just the image) */
    private final int width;
    private final int height;

    private final VImage image;
    private final Image imageElement;

    private final VStrip strip;

    private VStripItem(String alignment, int width, int height,
	    final VImage image, final VStrip strip) {

	sinkEvents(Event.ONCLICK);

	this.width = width;
	this.height = height;
	this.image = image;
	this.strip = strip;

	setStyleName("image-border " + alignment);

	imageElement = new Image();
	imageElement.setStyleName("image");
	imageElement.setUrl(image.getURL());

	add(imageElement);
	centerImage();

	DOM.setStyleAttribute(getElement(), "width", width + "px");
	DOM.setStyleAttribute(getElement(), "height", height + "px");

	if ("horizontal".equals(alignment)) {
	    DOM.setStyleAttribute(getElement(), "top", "0px");
	    DOM.setStyleAttribute(getElement(), "left", x + "px");
	} else if ("vertical".equals(alignment)) {
	    DOM.setStyleAttribute(getElement(), "top", y + "px");
	    DOM.setStyleAttribute(getElement(), "left", "0px");
	}

    }

    @Override
    public void onBrowserEvent(Event event) {
	switch (event.getTypeInt()) {
	case Event.ONCLICK: {
	    if (image != null) {
		strip.selectImage(image.getImageIndex(), true);
	    }
	    break;
	}
	}
    }

    public static VStripItem horizontalStripItem(int width, int height,
	    VImage image, VStrip strip) {
	return new VStripItem("horizontal", width, height, image, strip);
    }

    public static VStripItem verticalStripItem(int width, int height,
	    VImage image, VStrip strip) {
	return new VStripItem("vertical", width, height, image, strip);
    }

    /**
     * Sets item's current x-position
     * 
     * @param newX
     */

    public void setX(int newX) {
	this.x = newX;
	DOM.setStyleAttribute(getElement(), "left", x + "px");
    }

    /**
     * Sets item's current y-position
     * 
     * @param newY
     */

    public void setY(int newY) {
	this.y = newY;
	DOM.setStyleAttribute(getElement(), "top", y + "px");
    }

    /**
     * @return item's current x-position
     */

    public int getX() {
	return x;
    }

    /**
     * @return item's current y-position
     */

    public int getY() {
	return y;
    }

    /**
     * Sets animation target position (position where image should be after
     * animation)
     * 
     * @param targetX
     * @param targetY
     */

    public void setTargetPosition(int targetX, int targetY) {
	this.targetX = targetX;
	this.targetY = targetY;

	initialX = x;
	initialY = y;
    }

    /**
     * @return x-coordinate of animation target (x of position where image
     *         should be after animation)
     */

    public int getTargetX() {
	return targetX;
    }

    /**
     * @return y-coordinate of animation target (y of position where image
     *         should be after animation)
     */

    public int getTargetY() {
	return targetY;
    }

    /**
     * @return x-coordinate of image before animation started
     */

    public int getInitialX() {
	return initialX;
    }

    /**
     * @return y-coordinate of image before animation started
     */

    public int getInitialY() {
	return initialY;
    }

    /**
     * Performs calculations required to center image element to containing
     * element
     */

    public void centerImage() {
	int imageX = (width - image.getWidth()) / 2;
	int imageY = (height - image.getHeight()) / 2;

	DOM.setStyleAttribute(imageElement.getElement(), "left", imageX + "px");
	DOM.setStyleAttribute(imageElement.getElement(), "top", imageY + "px");
    }

    public int getImageIndex() {
	return image.getImageIndex();
    }

    public void setSelected(boolean selected) {
	String className = getStyleName();

	if (selected) {
	    if (!className.contains("image-border-selected")) {
		className = className.concat(" image-border-selected");
	    }
	} else {
	    className = className.replaceAll(" image-border-selected", "");
	}

	setStyleName(className);
    }
}
