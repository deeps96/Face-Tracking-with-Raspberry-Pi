package com.deeps.seminarworkdemo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 * @author Deeps
 */

public class Engine {

	private static final String PATH_TO_FACE_HAARCASCADE = "F:/Programme/OpenCV/OpenCV v.2.4.9/opencv/sources/data/haarcascades/haarcascade_frontalface_default.xml";

	private static PanelInterface panelInterface;
	private static boolean isRecording, isFaceDetecting;
	private static CascadeClassifier frontalfaceCascadeClassifier;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		frontalfaceCascadeClassifier = new CascadeClassifier(
				PATH_TO_FACE_HAARCASCADE);
		if (frontalfaceCascadeClassifier.empty())
			JOptionPane.showMessageDialog(
				null,
				"Fehler beim Laden der Haarcascades! (Pfadangabe)");
	}

	public static void registerPanelInterface(PanelInterface pPanelInterface) {
		panelInterface = pPanelInterface;
	}

	public static void startRecording() {
		isRecording = true;
		record();
	}

	public static void startFaceDetecting() {
		if (!isRecording)
			startRecording();
		isFaceDetecting = true;
	}

	public static void stopRecording() {
		isRecording = false;
		if (isFaceDetecting)
			stopFaceDetecting();
		panelInterface.recordingHasStopped();
	}

	public static void stopFaceDetecting() {
		isFaceDetecting = false;
	}

	public static boolean isRecording() {
		return isRecording;
	}

	public static boolean isFaceDetecting() {
		return isFaceDetecting;
	}

	private static void record() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				VideoCapture videoCapture = new VideoCapture(0);
				Mat currentImage = new Mat();
				panelInterface.recordingHasStarted();
				while (isRecording) {
					if (!videoCapture.read(currentImage)) {
						stopRecording();
						JOptionPane.showMessageDialog(
							null,
							"Kamera wurde entfernt.",
							"Verbindung verloren",
							JOptionPane.ERROR_MESSAGE);
						break;
					}
					if (panelInterface == null) {
						stopRecording();
						break;
					}

					if (isFaceDetecting)
						markHaarcascadesInMat(currentImage);

					try {
						panelInterface
								.setCurrentFrame(convertMatToBufferedImage(currentImage));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				videoCapture.release(); // sollte allerdings bereits automatisch
										// geschehen
			}
		}).start();
	}

	private static BufferedImage convertMatToBufferedImage(Mat inMat)
			throws IOException {
		/*
		 * Snippet (variiert) von
		 * http://sumitkumariit.blogspot.de/2013/08/coverting
		 * -opencv-mat-to-bufferedimage.html
		 */
		MatOfByte bytemat = new MatOfByte();
		Highgui.imencode(".jpg", inMat, bytemat);
		return ImageIO.read(new ByteArrayInputStream(bytemat.toArray()));
	}

	private static void markHaarcascadesInMat(Mat in) {
		Mat inGrayColored = new Mat();
		MatOfRect matches = new MatOfRect();

		in.copyTo(inGrayColored);

		Imgproc.cvtColor(in, inGrayColored, Imgproc.COLOR_RGB2GRAY);
		Imgproc.equalizeHist(inGrayColored, inGrayColored);
		frontalfaceCascadeClassifier.detectMultiScale(inGrayColored, matches);
		for (Rect r : matches.toArray())
			Core.rectangle(in, r.tl(), r.br(), new Scalar(0, 255, 0));
	}

}
