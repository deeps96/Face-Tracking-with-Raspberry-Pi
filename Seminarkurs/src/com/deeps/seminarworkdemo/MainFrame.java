package com.deeps.seminarworkdemo;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 *	@author Deeps
 */

public class MainFrame extends JFrame implements PanelInterface{

	//Konstanten
	private final int WIDTH = 750, HEIGHT = 862;

	//Laufzeitparameter
	private Image currentFrame;

	//Graphische Elemente
	private JPanel contentPane, videoPanel;
	private JLabel titleLabel;
	private JXPanel videoSurroundPanel;
	private JButton startRecordingButton, startFaceDetectingButton;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new MainFrame();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainFrame() {
		setTitle("Demo with OpenCV by Deeps");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Engine.stopRecording();
				super.windowClosed(e);
				dispose();
			}
		});
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setLocationRelativeTo(null);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){}
		setVisible(true);

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setLayout(null);
		setContentPane(contentPane);

		titleLabel = new JLabel(getTitle(), JLabel.CENTER);
		titleLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		titleLabel.setBounds(0, 10, getRootPane().getWidth(), 25);
		contentPane.add(titleLabel);

		videoSurroundPanel = new JXPanel();
		videoSurroundPanel.setBounds(10, 60, 724, 764);
		videoSurroundPanel.setBackground(Color.WHITE);
		DropShadowBorder shadow = new DropShadowBorder();
		shadow.setShadowColor(Color.BLACK);
		shadow.setShowLeftShadow(true);
		shadow.setShowRightShadow(true);
		shadow.setShowBottomShadow(true);
		shadow.setShowTopShadow(true);
		videoSurroundPanel.setBorder(shadow);
		videoSurroundPanel.setLayout(null);
		contentPane.add(videoSurroundPanel);

		videoPanel = new JPanel(){
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (currentFrame != null)
					g.drawImage(currentFrame, 0, 0, null);
				else
					g.fillRect(0, 0, videoPanel.getWidth(), videoPanel.getHeight());
			}
		};
		videoPanel.setBounds(5, 5, videoSurroundPanel.getWidth() - 10, videoSurroundPanel.getHeight() - 50);
		videoPanel.setBorder(null);
		videoSurroundPanel.add(videoPanel);

		startRecordingButton = new JButton("Starte Kameraaufnahme");
		startRecordingButton.setBounds(15, videoPanel.getY() + videoPanel.getHeight() + 10, 200, 20);
		startRecordingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Engine.isRecording()) 
					Engine.stopRecording();
				else 
					Engine.startRecording();
			}
		});
		videoSurroundPanel.add(startRecordingButton);
		
		startFaceDetectingButton = new JButton("Starte Gesichtserkennung");
		startFaceDetectingButton.setBounds(240, videoPanel.getY() + videoPanel.getHeight() + 10, 200, 20);
		startFaceDetectingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Engine.isFaceDetecting()) {
					Engine.stopFaceDetecting();
				    startFaceDetectingButton.setText("Starte Gesichtserkennung");
				}else {
					Engine.startFaceDetecting();
					startFaceDetectingButton.setText("Stoppe Gesichtserkennung");
				}
			}
		});
		videoSurroundPanel.add(startFaceDetectingButton);

		Engine.registerPanelInterface(this);
	}

	@Override
	public void setCurrentFrame(BufferedImage pCurrentFrame) {
		if (Engine.isRecording()){
			this.currentFrame = pCurrentFrame.getScaledInstance(videoSurroundPanel.getWidth(), videoSurroundPanel.getHeight(), Image.SCALE_DEFAULT);
			videoPanel.repaint();
		}
	}

	@Override
	public void recordingHasStopped() {
		currentFrame = null;
		videoPanel.repaint();	
		startRecordingButton.setText("Starte Kameraaufnahme");
        startFaceDetectingButton.setText("Starte Gesichtserkennung");
	}
	@Override
	public void recordingHasStarted() {
		startRecordingButton.setText("Stoppe Kameraaufnahme");
	}

}
