package com.deeps.seminarworkdemo;

import java.awt.image.BufferedImage;

/**
 *	@author Deeps
 */

public interface PanelInterface {
	public void setCurrentFrame(BufferedImage currentFrame);
	public void recordingHasStopped();
	public void recordingHasStarted();
}
