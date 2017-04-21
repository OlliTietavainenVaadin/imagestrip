package org.vaadin.peter.imagestrip.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * VStrip is the image area inside VImageStrip
 * 
 * @author Peter Lehto / IT Mill Oy Ltd
 */

public class VStrip extends FlowPanel {

    /**
     * Size of the margin between images
     */

    public final static int IMAGE_MARGIN = 10;

    private final VImageStrip parent;

    private final List<VStripItem> visibleItems;

    private boolean animating;

    public VStrip(VImageStrip parent) {
	this.parent = parent;

	setStyleName("v-strip");

	visibleItems = new ArrayList<VStripItem>();
    }

    public void setSelectable(boolean selectable) {
	if (selectable) {
	    addStyleName("selectable");
	} else {
	    removeStyleName("selectable");
	}
    }

    public void setImages(List<VImage> images) {
	removeImages();

	for (VImage image : images) {
	    VStripItem item = null;

	    if (parent.getAlignment() == 0) {
		item = VStripItem.horizontalStripItem(
		        parent.getImageBoxWidth(), parent.getImageBoxHeight(),
		        image, this);
	    } else {
		item = VStripItem.verticalStripItem(parent.getImageBoxWidth(),
		        parent.getImageBoxHeight(), image, this);
	    }

	    visibleItems.add(item);
	}

	// Initialize item start positions
	int position = 0;

	if (parent.getAlignment() == 0) {
	    position = -parent.getImageBoxWidth() - IMAGE_MARGIN;
	} else {
	    position = -parent.getImageBoxHeight() - IMAGE_MARGIN;
	}

	// Render visible items
	for (VStripItem item : visibleItems) {
	    add(item);

	    if (parent.getAlignment() == 0) {
		item.setX(position);
		item.setY(0);
		position += parent.getImageBoxWidth() + IMAGE_MARGIN;
	    } else {
		item.setX(0);
		item.setY(position);
		position += parent.getImageBoxHeight() + IMAGE_MARGIN;
	    }
	}

	// Update container size to match visible images
	updateSize();
	selectImage(parent.getSelectedImage(), false);
    }

    /**
     * @return number of images that will fit to this strip
     */

    public int getNumOfImages() {
	if (parent.getAlignment() == 0) {
	    return getImageContainerMaxWidth()
		    / ((parent.getImageBoxWidth() + IMAGE_MARGIN + 20));
	} else {
	    return getImageContainerMaxHeight()
		    / ((parent.getImageBoxHeight() + IMAGE_MARGIN + 20));
	}
    }

    /**
     * Updates the size of inner strip container
     */

    public void updateSize() {
	// Set strip width
	setWidth(getPictureAreaWidth() + "px");
	setHeight(getPictureAreaHeight() + "px");

	if (parent.getAlignment() == 0) {
	    DOM.setStyleAttribute(getElement(), "left", ((parent
		    .getOffsetWidth() - getPictureAreaWidth()) / 2)
		    + "px");
	} else {
	    DOM.setStyleAttribute(getElement(), "top", ((parent
		    .getOffsetHeight() - getPictureAreaHeight()) / 2)
		    + "px");
	}
    }

    /**
     * Removes all images from this strip
     */

    public void removeImages() {
	for (VStripItem item : visibleItems) {
	    remove(item);
	}

	visibleItems.clear();
    }

    /**
     * Moves strip's images to left
     */

    public void moveLeftAndSet(final List<VImage> images) {
	// If animation is enabled
	if (parent.isAnimated()) {
	    // If there is no current animation running
	    if (!animating) {
		animating = true;

		for (VStripItem item : visibleItems) {

		    // Horizontal alignment
		    if (parent.getAlignment() == 0) {
			item.setTargetPosition(item.getX()
			        - (parent.getImageBoxWidth() + IMAGE_MARGIN),
			        item.getY());
		    }
		    // Vertical alignment
		    else {
			item.setTargetPosition(item.getX(), item.getY()
			        - (parent.getImageBoxHeight() + IMAGE_MARGIN));
		    }
		}

		Animation animation = new Animation() {
		    @Override
		    protected void onUpdate(double progress) {
			for (VStripItem item : visibleItems) {
			    int newX = (int) ((item.getTargetX() - item
				    .getInitialX()) * progress);
			    int newY = (int) ((item.getTargetY() - item
				    .getInitialY()) * progress);

			    item.setX(item.getInitialX() + newX);
			    item.setY(item.getInitialY() + newY);

			    if (progress >= 1) {
				item.setX(item.getTargetX());
				item.setY(item.getTargetY());
			    }
			}

			if (progress >= 1) {
			    animating = false;
			    setImages(images);
			}
		    }
		};

		animation.run(300);
	    }
	} else {
	    setImages(images);
	}
    }

    /**
     * Moves strip's images to right
     */

    public void moveRightAndSet(final List<VImage> images) {
	// If animation is enabled
	if (parent.isAnimated()) {
	    // If there is no current animation running
	    if (!animating) {
		animating = true;

		for (VStripItem item : visibleItems) {
		    // Horizontal alignment
		    if (parent.getAlignment() == 0) {
			item.setTargetPosition(item.getX()
			        + (parent.getImageBoxWidth() + IMAGE_MARGIN),
			        item.getY());
		    }
		    // Vertical alignment
		    else {
			item.setTargetPosition(item.getX(), item.getY()
			        + (parent.getImageBoxHeight() + IMAGE_MARGIN));
		    }
		}

		Animation animation = new Animation() {
		    @Override
		    protected void onUpdate(double progress) {
			for (VStripItem item : visibleItems) {
			    int newX = (int) ((item.getTargetX() - item
				    .getInitialX()) * progress);
			    int newY = (int) ((item.getTargetY() - item
				    .getInitialY()) * progress);

			    item.setX(item.getInitialX() + newX);
			    item.setY(item.getInitialY() + newY);

			    if (progress >= 1) {
				item.setX(item.getTargetX());
				item.setY(item.getTargetY());
			    }
			}

			if (progress >= 1) {
			    animating = false;
			    setImages(images);
			}
		    }
		};

		animation.run(300);
	    }
	} else {
	    setImages(images);
	}
    }

    public void selectImage(int selectedImage, boolean notifyServer) {
	if (parent.isSelectable()) {

	    for (VStripItem item : visibleItems) {
		if (item.getImageIndex() == selectedImage) {
		    item.setSelected(true);
		} else {
		    item.setSelected(false);
		}
	    }

	    if (notifyServer) {
		parent.updateSelectedImageToServer(selectedImage);
	    }
	}
    }

    private int getImageContainerMaxWidth() {
	return parent.getOffsetWidth() - (2 * VImageStrip.BUTTON_WIDTH);
    }

    private int getImageContainerMaxHeight() {
	return parent.getOffsetHeight() - (2 * VImageStrip.BUTTON_WIDTH);
    }

    /**
     * @return width of picture area
     */

    private int getPictureAreaWidth() {
	if (parent.getAlignment() == 0) {
	    if (visibleItems.isEmpty()) {
		return getImageContainerMaxWidth();
	    } else {
		return (visibleItems.size() - 2)
		        * (parent.getImageBoxWidth() + IMAGE_MARGIN)
		        + IMAGE_MARGIN;
	    }
	} else {
	    return parent.getOffsetWidth();
	}
    }

    /**
     * @return height of picture area
     */

    private int getPictureAreaHeight() {
	if (parent.getAlignment() == 1) {
	    if (visibleItems.isEmpty()) {
		return getImageContainerMaxHeight();
	    } else {
		return (visibleItems.size() - 2)
		        * (parent.getImageBoxHeight() + IMAGE_MARGIN)
		        + IMAGE_MARGIN;
	    }
	} else {
	    return parent.getOffsetHeight();
	}
    }
}
