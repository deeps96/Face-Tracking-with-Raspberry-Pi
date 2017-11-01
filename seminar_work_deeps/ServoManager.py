#!/usr/bin/python

from Adafruit_PWM_Servo_Driver import PWM
import time

#servo - settings
pwm = PWM(0x40, debug=False)
channelServoTop = 0
channelServoBottom = 4
servoMin = 220  # left stop
servoMax = 440  # right stop
speed = 10
currentPosTop = servoMin + (servoMax - servoMin) / 2
currentPosBottom = servoMin + (servoMax - servoMin) / 2
isTurning = False
	
pwm.setPWMFreq(50) # Set frequency to 50 Hz

def setServoPulse(channel, pulse):
	global pwm
	pulseLength = 1000000                   # 1,000,000 us per second
	pulseLength /= 50                       # 50 Hz
	pulseLength /= 4096                     # 12 bits of resolution
	pulse *= 1000
	pulse /= pulseLength
	pwm.setPWM(channel, 0, pulse)
  
def moveLeft():
	global currentPosTop, speed, servoMin, channelServoTop, isTurning
	isTurning = True
	if (currentPosTop - speed >= servoMin):
		currentPosTop -= speed
		pwm.setPWM(channelServoTop, 0, currentPosTop)
		#time.sleep(1)
		isTurning = False
		return True
	isTurning = False
	return False

def moveRight():
	global currentPosTop, speed, servoMax, channelServoTop, isTurning
	isTurning = True
	if (currentPosTop + speed <= servoMax):
		currentPosTop += speed
		pwm.setPWM(channelServoTop, 0, currentPosTop)
		#time.sleep(1)
		isTurning = False
		return True
	isTurning = False
	return False

def moveDown():
	global currentPosBottom, speed, servoMin, channelServoBottom, isTurning
	isTurning = True
	if (currentPosBottom - speed >= servoMin):
		currentPosBottom -= speed
		pwm.setPWM(channelServoBottom, 0, currentPosBottom)
		#time.sleep(1)
		isTurning = False
		return True
	isTurning = False
	return False
  
def moveUp():
	global currentPosBottom, speed, servoMax, channelServoBottom, isTurning
	isTurning = True
	if(currentPosBottom + speed <= servoMax):
		currentPosBottom += speed
		pwm.setPWM(channelServoBottom, 0, currentPosBottom)
		#time.sleep(1)
		isTurning = False
		return True
	isTurning = False
	return False
	
def moveBottomToDefault():
	global currentPosTop, servoMin, servoMax, channelServoTop, isTurning
	isTurning = True
	currentPosTop = servoMin + (servoMax - servoMin) / 2 - 5 #20 ... deviation
	pwm.setPWM(channelServoTop, 0, currentPosTop)
	#time.sleep(1)
	isTurning = False

def moveTopToDefault():
	global currentPosBottom, servoMin, servoMax, channelServoBottom, isTurning
	isTurning = True
	currentPosBottom = servoMin + (servoMax - servoMin) / 2 #50 ... deviation
	pwm.setPWM(channelServoBottom, 0, currentPosBottom)
	#time.sleep(1)
	isTurning = False

def setPWM(channel, value):
	global channelServoTop, channelServoBottom, currentPosTop, currentPosBottom, isTurning
	isTurning = True
	if (channel == channelServoTop) :
		currentPosTop = value
	elif (channel == channelServoBottom) :
		currentPosBottom = value
	pwm.setPWM(channel, 0, value)
	#time.sleep(1)
	isTurning = False
	
#Setter & Getter
def setSpeed(newSpeed):
	global speed
	speed = newSpeed
	
def getSpeed():
	global speed
	return speed
	
def getCurrentPosTop():
	global currentPosTop
	return currentPosTop
	
def getCurrentPosBottom():
	global currentPosBottom
	return currentPosBottom