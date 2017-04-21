package org.vaadin.peter.imagestrip;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * ImageTools contains general purpose methods for image manipulation
 * 
 * @author Peter Lehto
 */

public class ImageTools {

	private static Map<String, File> scaledImages = new HashMap<String, File>();

	public static File resizeImage(String url, int maxWidth, int maxHeight)
			throws ImageToolsException {

		if (cacheContainsFile(url)) {
			return scaledImages.get(url);
		}

		try {
			// Read image from file
			BufferedImage sourceImage = ImageIO.read(new URL(url));

			BufferedImage scaledImage = scaleBufferedImage(sourceImage,
					maxWidth, maxHeight);
			File out = writeScaledImageToDisk(scaledImage);

			scaledImages.put(url, out);
			return out;
		} catch (IOException e) {
			throw new ImageToolsException("Unable to resize image "
					+ e.getMessage());
		}
	}

	/**
	 * Resizes image given as imageFile parameter. Resized image is stored to
	 * given targetFolder.
	 * 
	 * Image is forced to given sizes unless maintainAspectRatio parameter is
	 * set to true. In this case image will be scaled so that longer edge is
	 * scaled to given length and shorter one is scaled according to aspect.
	 * 
	 * @param imageFile
	 * @param maxWidth
	 * @param maxHeight
	 * 
	 * @return File pointer to scaled image
	 * 
	 * @throws ImageToolsException
	 *             if scaling image fails
	 * @throws FileNotFoundException
	 *             if given image file does not exist or if target folder
	 *             doesn't exist or is not a folder.
	 */

	public static File resizeImage(File imageFile, int maxWidth, int maxHeight)
			throws ImageToolsException, FileNotFoundException {

		if (!imageFile.exists()) {
			throw new FileNotFoundException("Could not find file "
					+ imageFile.getAbsolutePath());
		}

		if (cacheContainsFile(imageFile.getAbsolutePath())) {
			return scaledImages.get(imageFile.getAbsolutePath());
		}

		try {
			// Read image from file
			BufferedImage sourceImage = ImageIO.read(imageFile);

			BufferedImage scaledImage = scaleBufferedImage(sourceImage,
					maxWidth, maxHeight);
			File out = writeScaledImageToDisk(scaledImage);

			scaledImages.put(imageFile.getAbsolutePath(), out);
			return out;
		} catch (IOException e) {
			throw new ImageToolsException("Unable to resize image "
					+ e.getMessage());
		}
	}

	private static boolean cacheContainsFile(String identifier) {
		if (scaledImages.containsKey(identifier)) {
			return scaledImages.get(identifier).exists();
		}

		return false;
	}

	private static File writeScaledImageToDisk(BufferedImage scaledImage)
			throws IOException {

		FileOutputStream output = null;

		try {
			File targetFile = File.createTempFile("imagestrip", "");
			output = new FileOutputStream(targetFile);

			ImageIO.write(scaledImage, "jpg", output);
			return targetFile;
		} catch (IOException e) {
			throw e;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Exception ignored) {
					// NOP
				}
			}
		}
	}

	private static BufferedImage scaleBufferedImage(BufferedImage sourceImage,
			int maxWidth, int maxHeight) {
		// Calculate scaled image's dimensions
		float calculatedWidth = 0;
		float calculatedHeight = 0;

		float aspectRatio = (float) sourceImage.getWidth()
				/ (float) sourceImage.getHeight();

		if (aspectRatio > 1) {
			calculatedWidth = maxWidth;
			calculatedHeight = maxWidth / aspectRatio;

			if (calculatedHeight > maxHeight) {
				calculatedWidth = maxHeight * aspectRatio;
				calculatedHeight = calculatedWidth / aspectRatio;
			}
		} else {
			calculatedWidth = maxHeight * aspectRatio;
			calculatedHeight = maxHeight;

			if (calculatedWidth > maxWidth) {
				calculatedHeight = maxWidth / aspectRatio;
				calculatedWidth = calculatedHeight * aspectRatio;
			}
		}

		// Create empty image with new dimensions
		BufferedImage scaledImage = new BufferedImage((int) calculatedWidth,
				(int) calculatedHeight, BufferedImage.TYPE_INT_RGB);

		Graphics2D graphics = (Graphics2D) scaledImage.getGraphics();
		graphics.setComposite(AlphaComposite.Src);

		// Draw to scaled image
		graphics.drawImage(sourceImage, 0, 0, (int) calculatedWidth,
				(int) calculatedHeight, null);

		graphics.dispose();

		return scaledImage;
	}

	/**
	 * @param imageFile
	 * @return width of image in pixels from given file
	 * @throws ImageToolsException
	 */

	public static int getImageWidth(File imageFile) throws ImageToolsException {
		try {
			BufferedImage sourceImage = ImageIO.read(imageFile);
			return sourceImage.getWidth();
		} catch (Exception e) {
			throw new ImageToolsException("Error retrieving image's width");
		}
	}

	/**
	 * @param imageFile
	 * @return height of image in pixels from given file
	 * @throws ImageToolsException
	 */

	public static int getImageHeight(File imageFile) throws ImageToolsException {
		try {
			BufferedImage sourceImage = ImageIO.read(imageFile);
			return sourceImage.getHeight();
		} catch (Exception e) {
			throw new ImageToolsException("Error retrieving image's height");
		}
	}

	public static void clearCache() {
		scaledImages.clear();
	}
}
