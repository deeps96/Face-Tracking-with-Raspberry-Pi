#!/usr/bin/python

import sys
import time
import math
import cv2.cv as cv
from optparse import OptionParser
import ServoManager

#go into standard position
ServoManager.moveTopToDefault()
ServoManager.moveBottomToDefault()

## better, but slow
#scale_factor=2, min_neighbors=3, flags=0 

##faster
#scale_factor=1.2, min_neighbors=2, flags=CV_HAAR_DO_CANNY_PRUNING
#cv.SetCaptureProperty(capture, cv.CV_CAP_PROP_FRAME_HEIGHT, 240)
#cv.SetCaptureProperty(capture, cv.CV_CAP_PROP_FRAME_WIDTH, 320)

cascadePath = "haarcascade_frontalface_alt.xml"
min_size = (20, 20)
image_scale = 3
haar_scale = 1.2
min_neighbors = 2
haar_flags = cv.CV_HAAR_DO_CANNY_PRUNING

speedNormal = 10
speedFast = 20

cameraFPS = 27

parser = OptionParser()
parser.add_option("--width", type="int", dest="width", help="Set the width of the Camerainput", default=320)
parser.add_option("--height", type="int", dest="height", help="Set the height of the Camerainput", default=240)
parser.add_option("--show", action="store_true", dest="show", help="Set whether the frame should be displayed, or not", default=False)

(options, args) = parser.parse_args()
width = options.width
height = options.height
showFrame = options.show

def detect_and_draw_and_move(img, cascade):
	global showFrame, width, height, speedNormal, speedFast
    # allocate temporary images
	gray = cv.CreateImage((img.width,img.height), 8, 1)  #size, bit-depth, channels p. pixel
	small_img = cv.CreateImage((cv.Round(img.width / image_scale),
								cv.Round (img.height / image_scale)), 8, 1)

    # convert color input image to grayscal
	cv.CvtColor(img, gray, cv.CV_BGR2GRAY)

    # scale input image for faster processing
	cv.Resize(gray, small_img, cv.CV_INTER_LINEAR)

	cv.EqualizeHist(small_img, small_img)
	
	if(cascade):
		faces = cv.HaarDetectObjects(small_img, cascade, cv.CreateMemStorage(0),
                                     haar_scale, min_neighbors, haar_flags, min_size)
		if faces:
			facesFound = 0
			xMove = 0
			yMove = 0
			for ((x, y, w, h), n) in faces:
				# the input to cv.HaarDetectObjects was resized, so scale the
				# bounding box of each face and convert it to two CvPoints
				xS = int(x * image_scale)
				yS = int(y * image_scale)
				wS = int(w * image_scale)
				hS = int(h * image_scale)
				currentXMove = int(img.width / 2 - (xS + wS / 2))
				currentYMove = int(img.height / 2 - (yS + hS / 2))
				xMove += currentXMove
				yMove += currentYMove
				facesFound += 1
				pt1 = (xS, yS)
				pt2 = (xS + wS, yS + hS)
				print "face found x = %d y = %d w = %d h = %d" % (xS, yS, wS, hS)
				print "image dimensions: w = %d h = %d" % (img.height, img.width)
				print "center of the face x = %d y = %d" % (xS + wS / 2, yS + hS / 2) 
				print "move x = %d and y = %d" % (currentXMove, currentYMove)

				if showFrame : 
					cv.Rectangle(img, pt1, pt2, cv.RGB(255, 0, 0), 3, 8, 0)
			
			xMove /= facesFound
			yMove /= facesFound
			
			if (math.fabs(xMove) >= 0.06 * width) :
				if (math.fabs(xMove) >= 0.15 * width) :
					ServoManager.setSpeed(speedFast)
				else :
					ServoManager.setSpeed(speedNormal)
				if (xMove < 0) :
					ServoManager.moveRight()
				if (xMove > 0) :
					ServoManager.moveLeft()
					
			if (math.fabs(yMove) >= 0.08 * height) :
				if (math.fabs(yMove) >= 0.2 * height) :
					ServoManager.setSpeed(speedFast)
				else :
					ServoManager.setSpeed(speedNormal)
				if (yMove < 0) :
					ServoManager.moveDown()
				if (yMove > 0) :
					ServoManager.moveUp()
	if showFrame :					
		cv.ShowImage("result", img)

if __name__ == '__main__':
	
	cascade = cv.Load(cascadePath)
		
	if showFrame:
		cv.NamedWindow("result", 1)

	frame_copy = None
	while True:
		capture = cv.CreateCameraCapture(0)
		cv.SetCaptureProperty(capture, cv.CV_CAP_PROP_FRAME_HEIGHT, height)
		cv.SetCaptureProperty(capture, cv.CV_CAP_PROP_FRAME_WIDTH, width)
		
		frame = cv.QueryFrame(capture)
		
		if not frame:
			cv.WaitKey(0)
			break
			
		if not frame_copy:
			frame_copy = cv.CreateImage((frame.width,frame.height),
										cv.IPL_DEPTH_8U, frame.nChannels)
		
		if frame.origin == cv.IPL_ORIGIN_TL:
			cv.Copy(frame, frame_copy)
		else:
			cv.Flip(frame, frame_copy, 0)
			
		detect_and_draw_and_move(frame_copy, cascade)
		
		if cv.WaitKey(10) >= 0:
			break
			
		del(capture)
	
	if showFrame :
		cv.DestroyWindow("result")
