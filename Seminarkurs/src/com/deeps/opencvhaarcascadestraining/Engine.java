package com.deeps.opencvhaarcascadestraining;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.opencv.core.Core;

/**
 * @author Deeps
 */

public class Engine {

	private static String analysedDirPath;
	private static PanelInterface panelInterface;
	private static FrameGrabber frameGrabber;
	private static Image currentFrameImage;
	private static boolean isVideoLoaded = false;
	private static int posStartCounter = 0, negStartCounter = 0;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void shutdown() {
		if (frameGrabber != null)
			try {
				frameGrabber.stop();
				frameGrabber.release();
			} catch (Exception e) {
				e.printStackTrace();
			}

	}

	public static void registerPanelInterface(PanelInterface pPanelInterface) {
		panelInterface = pPanelInterface;
	}

	public static boolean loadVideo(File videoFile) {
		try {
			frameGrabber = new OpenCVFrameGrabber(videoFile);
			frameGrabber.start();
			analysedDirPath = videoFile.getParent() + "/analysed";
			createSubFolders();
			isVideoLoaded = true;
		} catch (Exception e) {
			e.printStackTrace();
			isVideoLoaded = false;
		}
		return isVideoLoaded;
	}

	private static void createSubFolders() {
		File analysedDir = new File(analysedDirPath);
		File positiveDir = new File(analysedDirPath + "/pos");
		File negativeDir = new File(analysedDirPath + "/neg");
		analysedDir.mkdir();
		positiveDir.mkdir();
		negativeDir.mkdir();
		File[] files = positiveDir.listFiles();
		for (File f : files)
			posStartCounter = Math.max(
				posStartCounter,
				Integer.parseInt(f.getName().substring(0, 3)));
		files = negativeDir.listFiles();
		for (File f : files)
			negStartCounter = Math.max(
				negStartCounter,
				Integer.parseInt(f.getName().substring(0, 3)));
	}

	public static void loadNextImageFromVideo() {
		if (panelInterface == null)
			return;
		try {
			if (isVideoLoaded) {
				IplImage currentFrame = frameGrabber.grab();
				currentFrameImage = currentFrame.getBufferedImage()
						.getScaledInstance(768, 432, Image.SCALE_DEFAULT);
				panelInterface.setCurrentFrame(currentFrameImage);
			}
		} catch (Exception e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
	}

	public static void saveAllAsNegative() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (frameGrabber.getFrameNumber() < frameGrabber
						.getLengthInFrames()) {
					saveCurrentAsNegativeFrame();
					loadNextImageFromVideo();
				}
				JOptionPane.showMessageDialog(
					null,
					"Vorgang abgeschlossen.",
					"Fertig",
					JOptionPane.INFORMATION_MESSAGE);
				JOptionPane
						.showMessageDialog(
							null,
							"Es wird empfohlen, die gespeicherten Bilder nocheinmal zu kontrollieren.",
							"Hinweis",
							JOptionPane.INFORMATION_MESSAGE);
			}
		}).start();
	}

	public static void saveCurrentAsPositiveFrame(Rectangle selectedRect) {
		// TestVideos/analysed/pos/000_frame.bmp
		String subPath = "pos/"
				+ String.format("%03d", frameGrabber.getFrameNumber()
						+ posStartCounter) + "_frame.bmp";
		saveCurrentFrameAsBMP(analysedDirPath + "/" + subPath);
		writeTextIntoFile(analysedDirPath + "/infos_pos.txt", subPath + " 1 "
				+ (int) selectedRect.getX() + " " + (int) selectedRect.getY()
				+ " " + (int) selectedRect.getWidth() + " "
				+ (int) selectedRect.getHeight());
	}

	public static void saveCurrentAsNegativeFrame() {
		String subPath = "neg/"
				+ String.format("%03d", frameGrabber.getFrameNumber()
						+ negStartCounter) + "_frame.bmp";
		saveCurrentFrameAsBMP(analysedDirPath + "/" + subPath);
		writeTextIntoFile(analysedDirPath + "/infos_neg.txt", subPath);
	}

	private static boolean saveCurrentFrameAsBMP(String path) {
		try {
			BufferedImage tmpImg = convertImageToBufferedImage(currentFrameImage);
			if (tmpImg == null)
				return saveCurrentFrameAsBMP(path);
			else
				ImageIO.write(tmpImg, "BMP", new File(path));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void writeTextIntoFile(String path, String text) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(path, true));
			writer.println(text);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void jumpToFrame(final int pTargetFrameNumber) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int targetFrameNumber = pTargetFrameNumber;
				if (frameGrabber != null
						&& targetFrameNumber != frameGrabber.getFrameNumber()) {
					if (targetFrameNumber > frameGrabber.getLengthInFrames())
						targetFrameNumber = frameGrabber.getLengthInFrames();
					else if (targetFrameNumber < 0)
						targetFrameNumber = 0;

					try {
						panelInterface.setIsLoading(true);
						if (targetFrameNumber < frameGrabber.getFrameNumber()) {
							frameGrabber.stop();
							frameGrabber.start();
						}
						if (targetFrameNumber > 0)
							while (frameGrabber.getFrameNumber() < targetFrameNumber - 1)
								frameGrabber.grab();
						loadNextImageFromVideo();
						panelInterface.setIsLoading(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public static BufferedImage convertImageToBufferedImage(Image img) {
		/*
		 * Snippet von
		 * http://stackoverflow.com/questions/13605248/java-converting
		 * -image-to-bufferedimage
		 */
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		if (img.getWidth(null) < 0 && img.getHeight(null) < 0)
			return null;

		BufferedImage bimage = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_RGB);

		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		return bimage;
	}

	// Getter & Setter
	public static boolean isVideoLoaded() {
		return isVideoLoaded;
	}

	public static int getCurrentFrameNumber() {
		if (frameGrabber == null)
			return -1;
		else
			return frameGrabber.getFrameNumber();
	}

	public static int getFrameLength() {
		if (frameGrabber == null)
			return -1;
		else
			return frameGrabber.getLengthInFrames();
	}
}
