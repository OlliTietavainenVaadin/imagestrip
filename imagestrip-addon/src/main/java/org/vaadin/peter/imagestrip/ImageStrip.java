package org.vaadin.peter.imagestrip;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.LegacyComponent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ImageStrip is a general purpose image viewer widget that displays given image
 * resources for the user.
 *
 * Strip can be used also to select images using setSelectable, setValue and
 * getValue methods.
 *
 * Strip can be scroller using UI's own scrolling buttons but as well with
 * scrollLeft and scrollRight methods.
 *
 * Image transitions can also be animated with the setAnimated method.
 * ImageStrip uses server-side scaling to scale images to proper sizes.
 *
 * @author Peter Lehto / IT Mill Oy Ltd
 */
public class ImageStrip extends AbstractField implements LegacyComponent {

    private static final long serialVersionUID = 3214300856058497608L;
    /**
     * All images added to this component
     */

    private final List<Image> images;
    /**
     * Images which are to be sent to the server
     */

    private final List<Image> imagesToTransfer;
    /**
     * List of currently visible images
     */

    private final Set<Image> visibleImages;
    /**
     * Map of ids attached to resources
     */

    private final Map<Integer, Image> imageIds;
    /**
     * ImageIndex represents how many images have been inserted to the strip
     * already
     */

    private int imageIndex;
    /**
     * Number of images that are allowed to be visible simultaneously, if value
     * is negative, there is no limit
     */

    private int maxAllowed = -1;
    /**
     * Number of images that can fit to image area simultaneously
     */

    private int maxFitting;
    /**
     * Current scroll position
     */

    private int cursor;
    /**
     * Current alignment
     */

    private Alignment alignment;
    /**
     * Width of the container around the images (same for every image)
     */

    private int imageBoxWidth;
    /**
     * Height of the container around the images (same for every image)
     */

    private int imageBoxHeight;
    /**
     * Maximum width of the image, must be <= than the imageBoxWidth
     */

    private int imageMaxWidth;
    /**
     * Maximum height of the image, must be <= than the imageBoxHeight
     */

    private int imageMaxHeight;
    /**
     * Is strip animated
     */

    private boolean animated;
    /**
     * Direction of movement (-1 left, 1 right)
     */

    private int direction;
    /**
     * Should strip be clear during next update
     */

    private boolean clear;
    /**
     * Is strip selectable
     */

    private boolean selectable;

    private Object value;

    /**
     * Creates an empty image strip with horizontal alignment
     */

    public ImageStrip() {
        images = new LinkedList<Image>();

        imagesToTransfer = new LinkedList<Image>();
        imageIds = new HashMap<Integer, Image>();
        visibleImages = new HashSet<Image>();

        alignment = Alignment.HORIZONTAL;

        setWidth(100, Unit.PERCENTAGE);
        setHeight(120, Unit.PIXELS);

        imageBoxWidth = 120;
        imageBoxHeight = 120;
        imageMaxWidth = 110;
        imageMaxHeight = 110;

        animated = true;
    }

    /**
     * Creates an empty image strip with given alignment
     *
     * @param alignment
     */

    public ImageStrip(Alignment alignment) {
        this();

        this.alignment = alignment;

        if (Alignment.VERTICAL.equals(alignment)) {
            setWidth(120, Sizeable.UNITS_PIXELS);
            setHeight(600, Sizeable.UNITS_PIXELS);
        }
    }

    /**
     * Sets an image with the given imageId selected
     */

    @Override
    public void setValue(Object imageId) {
        if (isSelectable()) {
            super.setValue(imageId);
            requestRepaint();
        }
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    protected void doSetValue(Object o) {
        this.value = o;
    }

    /**
     * @return true if this strip is selectable, false otherwise
     */

    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Sets this ImageStrip selectable
     *
     * @param selectable
     */

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("alignment", alignment.ordinal());
        target.addAttribute("animated", animated);
        target.addAttribute("boxWidth", imageBoxWidth);
        target.addAttribute("boxHeight", imageBoxHeight);
        target.addAttribute("selectable", selectable);
        target.addAttribute("removeAll", clear);
        target.addAttribute("direction", direction);

        // Images to transfer
        if (imagesToTransfer.size() > 0) {
            // send image resources and their associated id numbers to client
            target.startTag("images");
            for (Image image : imagesToTransfer) {
                target.startTag("image");
                target.addAttribute("resource", image.getResource());
                target.addAttribute("index", image.getImageIndex());
                target.addAttribute("width", image.getWidth());
                target.addAttribute("height", image.getHeight());
                target.endTag("image");
            }
            target.endTag("images");

            imagesToTransfer.clear();
        }

        if (getValue() != null) {
            Object value = getValue();
            Image selectedImage = null;
            if (value instanceof Image) {
                selectedImage = (Image) value;
                target.addAttribute("selectedImage", selectedImage.getImageIndex());

            } else {
                target.addAttribute("selectedImage", -1);
            }
        } else {
            target.addAttribute("selectedImage", -1);
        }
    }

    /**
     * @param image
     * @return true if given image is currently visible in imageStrip
     */

    public boolean isImageVisible(Image image) {
        return visibleImages.contains(image);
    }

    @Override
    public void changeVariables(Object source, Map variables) {

        // On client resize
        if (variables.containsKey("numOfImages")) {
            maxFitting = Integer.parseInt(variables.get("numOfImages").toString());

            direction = 0;
            sendImages(cursor);
        }

        // On click to left or right (up or down) arrow
        if (variables.containsKey("cursor")) {
            if ("left".equals(variables.get("cursor"))) {
                scrollToLeft();
            } else if ("right".equals(variables.get("cursor"))) {
                scrollToRight();
            }
        }

        if (variables.containsKey("clickedImage")) {
            int clickedImageIndex = Integer.parseInt(variables.get("clickedImage").toString());
            setValue(imageIds.get(clickedImageIndex));
        }
    }

    /**
     * Adds new image from given FileResource to this strip. Return value is the
     * image object that can be used to select images from the image strip using
     * setValue method.
     *
     * @param fileResource
     * @return Image object that can be used to select image if selection mode is turned on
     */

    public Image addImage(FileResource fileResource) {
        return addImageInternal(fileResource);
    }

    /**
     * Adds new image from given ExternalResource to this strip. Return value is
     * the image object that can be used to select images from the image strip
     * using setValue method.
     *
     * @param externalResource
     * @return Image object that can be used to select image if selection mode is turned on
     */

    public Image addImage(ExternalResource externalResource) {
        return addImageInternal(externalResource);
    }

    /**
     * Adds new image from given URL to this strip. Return value is the image
     * object that can be used to select images from the image strip using
     * setValue method.
     *
     * @param URL
     * @return Image object that can be used to select image if selection mode is turned on
     */

    public Image addImage(String URL) {
        return addImage(new ExternalResource(URL));
    }

    /**
     * Sets the number of images which are allowed to be visible simultaneously.
     * Negative value removes the limit.
     *
     * @param maxAllowed
     */

    public void setMaxAllowed(int maxAllowed) {
        this.maxAllowed = maxAllowed;
        requestRepaint();
    }

    /**
     * Sets the width of image container inside image strip. This should not be
     * confused with setWidth method.
     *
     * @param imageBoxWidth
     */

    public void setImageBoxWidth(int imageBoxWidth) {
        this.imageBoxWidth = imageBoxWidth;
        ImageTools.clearCache();
        requestRepaint();
    }

    /**
     * Sets the height of image container inside image strip. This should not be
     * confused with setHeight method.
     *
     * @param imageHeight
     */

    public void setImageBoxHeight(int imageHeight) {
        this.imageBoxHeight = imageHeight;
        ImageTools.clearCache();
        requestRepaint();
    }

    /**
     * @return true if this strip is animated
     */

    public boolean isAnimated() {
        return animated;
    }

    /**
     * Sets strip scrolling to use animation
     *
     * @param animated
     */

    public void setAnimated(boolean animated) {
        this.animated = animated;
        requestRepaint();
    }

    /**
     * Scroll imagestrip to left by one image
     */

    public void scrollToLeft() {
        cursor += 1;
        direction = -1;

        if (cursor >= images.size()) {
            cursor = 0;
        }

        sendImages(cursor);
    }

    /**
     * Scroll imagestrip to right by one image
     */

    public void scrollToRight() {
        cursor -= 1;
        direction = 1;

        if (cursor < 0) {
            cursor = images.size() - 1;
        }

        sendImages(cursor);
    }

    /**
     * @param maxWidth
     * @throws IllegalArgumentException
     *     if given maxWidth is greater than the width of imageBox
     */

    public void setImageMaxWidth(int maxWidth) {
        if (maxWidth > imageBoxWidth) {
            throw new IllegalArgumentException("Image max width cannot be wider than image box's width which is " + imageBoxWidth);
        }
        this.imageMaxWidth = maxWidth;
    }

    /**
     * @param maxHeight
     * @throws IllegalArgumentException
     *     if given maxHeight is greater than the height of imageBox
     */

    public void setImageMaxHeight(int maxHeight) {
        if (maxHeight > imageBoxHeight) {
            throw new IllegalArgumentException("Image max height cannot be higher than image box's height which is " + imageBoxHeight);
        }
        this.imageMaxHeight = maxHeight;
    }

    /**
     * Sends images to client side component
     *
     * @param cursor
     *     is the index position from which to start
     */

    private void sendImages(int cursor) {
        visibleImages.clear();

        if (images.size() > 0) {
            // Set starting point one before cursor
            int index = cursor - 1;

            if (index < 0) {
                index = images.size() - 1;
            }

            // Determine how many images we need to send
            int maxImages = 0;

            // If there is no limit set, send as many as we have or as fit
            if (maxAllowed < 0) {
                maxImages = Math.min(maxFitting, images.size());
            }
            // Otherwise send as many as we have or max allowed
            else {
                maxImages = Math.min(Math.min(maxFitting, maxAllowed), images.size());
            }

            // Add two extra images for pre-loading and animation
            maxImages += 2;

            for (int i = 0; i < maxImages; i++) {
                if (index > images.size() - 1) {
                    index = 0;
                }

                Image image = images.get(index);
                imagesToTransfer.add(image);
                index++;
            }

            for (int j = 1; j < imagesToTransfer.size() - 1; j++) {
                visibleImages.add(imagesToTransfer.get(j));
            }

            requestRepaint();
        }
    }

    /**
     * Converts given Resource to ImageStrip.Image
     *
     * @param resource
     * @return ImageStrip.Image representing given resource
     */

    private Image readImageResource(Resource resource) {

        File imageFile = null;

        if (resource instanceof FileResource) {
            FileResource fResource = (FileResource) resource;

            try {
                imageFile = ImageTools.resizeImage(fResource.getSourceFile(), imageMaxWidth, imageMaxHeight);

                FileResource scaledImage = new FileResource(imageFile);

                int width = ImageTools.getImageWidth(imageFile);
                int height = ImageTools.getImageHeight(imageFile);

                return new Image(imageIndex++, scaledImage, width, height);
            } catch (FileNotFoundException e) {
                return null;
            } catch (ImageToolsException e) {
                return null;
            }
        } else if (resource instanceof ExternalResource) {
            ExternalResource eResource = (ExternalResource) resource;

            try {
                imageFile = ImageTools.resizeImage(eResource.getURL(), imageMaxWidth, imageMaxHeight);

                FileResource scaledImage = new FileResource(imageFile);

                int width = ImageTools.getImageWidth(imageFile);
                int height = ImageTools.getImageHeight(imageFile);

                return new Image(imageIndex++, scaledImage, width, height);
            } catch (ImageToolsException e) {
                return null;
            }
        }

        throw new UnsupportedOperationException("Only FileResources and ExternalResource are currently supported");
    }

    private Image addImageInternal(Resource resource) {
        Image image = readImageResource(resource);

        direction = 0;
        images.add(image);
        imageIds.put(image.getImageIndex(), image);

        imagesToTransfer.clear();
        sendImages(cursor);

        return image;
    }

    public enum Alignment {

        /**
         * Align ImageStrip horizontally
         */

        HORIZONTAL,

        /**
         * Align ImageStrip vertically
         */

        VERTICAL;
    }

    /**
     * ImageStrip.Image is ImageStrip's internal representation for the image.
     * Image can be used to select values from the ImageStrip using setValue
     * method.
     */

    public static class Image {

        private final int imageIndex;
        private final Resource resource;

        private final int width;
        private final int height;

        private Image(int imageIndex, Resource resource, int width, int height) {
            this.imageIndex = imageIndex;
            this.resource = resource;
            this.width = width;
            this.height = height;
        }

        public int getImageIndex() {
            return imageIndex;
        }

        public Resource getResource() {
            return resource;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
