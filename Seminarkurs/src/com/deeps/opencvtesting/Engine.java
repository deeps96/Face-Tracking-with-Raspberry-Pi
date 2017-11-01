package com.deeps.opencvtesting;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

/**
 *	@author Deeps
 */

public class Engine {

	private static CascadeClassifier currentCascade;
	private static Mat currentMatWithoutMarkers, currentMatWithMarkers;

	private static int positivesMatches = 0, positivesFound = 0, falsePositivesFound = 0;  

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static boolean loadCascade(String path){
		currentCascade = new CascadeClassifier(path);
		return currentCascade.empty();
	}
	public static BufferedImage loadLastImageAndMarkCascades(){
		if (currentMatWithoutMarkers == null)
			return null;
		markHaarcascadesInCurrentMat(); //schreibt in die currentMatWithMarkers
		try {
			return convertMatToBufferedImage(currentMatWithMarkers);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static BufferedImage loadImageAndMarkCascades(String path){
		if (currentCascade == null)
			return null;
		currentMatWithoutMarkers = Highgui.imread(path);
		markHaarcascadesInCurrentMat(); //schreibt in die currentMatWithMarkers
		try {
			return convertMatToBufferedImage(currentMatWithMarkers);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	private static void markHaarcascadesInCurrentMat(){
		currentMatWithMarkers = new Mat();
		currentMatWithoutMarkers.copyTo(currentMatWithMarkers);

		Mat inGrayColored = new Mat();  
		MatOfRect matches = new MatOfRect();  

		currentMatWithMarkers.copyTo(inGrayColored);  

		Imgproc.cvtColor(currentMatWithMarkers, inGrayColored, Imgproc.COLOR_RGB2GRAY);  
		Imgproc.equalizeHist(inGrayColored, inGrayColored);  
		currentCascade.detectMultiScale(inGrayColored, matches);  
		for(Rect r : matches.toArray()) 
			Core.rectangle(currentMatWithMarkers, r.tl(), r.br(), new Scalar(0, 0, 255));

		//Statistik
		positivesFound += matches.toArray().length;
		positivesMatches += matches.toArray().length;
	}
	private static BufferedImage convertMatToBufferedImage(Mat inMat) throws IOException{
		/*
		 * Snippet (variiert) von http://sumitkumariit.blogspot.de/2013/08/coverting-opencv-mat-to-bufferedimage.html
		 */
		MatOfByte bytemat = new MatOfByte();
		Highgui.imencode(".jpg", inMat, bytemat);
		return ImageIO.read(new ByteArrayInputStream(bytemat.toArray()));
	}

	public static void falsePositives(int amount){
		positivesFound -= amount;
		falsePositivesFound += amount;
		positivesMatches -= amount;
	}
	public static void missingPositives(int amount){
		positivesMatches += amount;
	}

	public static void downloadAllImagesFromURL(String url){
		try {
			Document document = Jsoup.parse(downloadWebCode(url));
			Elements elements = document.select("img");
			if (elements.size() > 0)
				new File("./download").mkdir();
			for (Element e : elements){
				String imgUrl = e.attr("src");
				String imgType = null, fileName = null;
				if (imgUrl.length() > 0) {
					imgType = imgUrl.substring(imgUrl.length() - 3);
					fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1, imgUrl.length() - 4);
				} else {
					imgUrl = e.attr("data-src");
					if (imgUrl.length() > 0){
						imgType = "jpg";
						fileName = imgUrl.substring(imgUrl.lastIndexOf("tbn:") + 4);
					}
				}
				if (imgUrl.length() > 0)
					ImageIO.write(ImageIO.read(new URL(imgUrl)), imgType, new File("./download/" + fileName + "." + imgType));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private static String downloadWebCode(String pUrl) throws IOException {
		URL obj = new URL(pUrl);
		HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
		connection.addRequestProperty("Accept-Language", "de,en-US;q=0.7,en;q=0.3");
		connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String data = "";
			String line;
			while ((line = in.readLine()) != null) {
				data += line;
			}
			in.close();
			return data;
		} catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}

	//Getter & Setter
	public static int getPositivesMatches() {
		return positivesMatches;
	}
	public static int getPositivesFound() {
		return positivesFound;
	}
	public static int getFalsePositivesFound() {
		return falsePositivesFound;
	}
}
