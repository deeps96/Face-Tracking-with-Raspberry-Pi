package com.deeps.opencvhaarcascadestraining;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;


/**
 *	@author Deeps
 */

public class MainFrame extends JFrame implements PanelInterface{

	//Konstanten
	private final int WIDTH = 804, HEIGHT = 580;
	private final String loadFileChooserDir = "F:/WindowsUserDataAuslagerung/Downloads/ZDatenschrottplatz/TestVideo";

	//Laufzeitparameter
	private BufferedImage currentFrame;
	private Rectangle selectedRect;
	private Point startPoint, lastPoint;
	private boolean isLoading;

	//Graphische Elemente
	private JPanel contentPane, videoPanel;
	private JLabel titleLabel, instructionLabel;
	private JXPanel videoSurroundPanel;
	private JButton loadVideoButton, saveAllAsNegativeButton;

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
				Engine.shutdown();
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
		videoSurroundPanel.setBounds(10, 60, 778, 482);
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
				Graphics2D g2d = (Graphics2D) g;
				if (isLoading || selectedRect != null)
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

				if (currentFrame != null)
					g2d.drawImage(currentFrame, 0, 0, null);
				else
					g.fillRect(0, 0, videoPanel.getWidth(), videoPanel.getHeight());

				if (!isLoading && selectedRect != null && currentFrame != null && selectedRect.getWidth() > 0 && selectedRect.getHeight() > 0){
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
					g2d.drawImage(
							currentFrame.getSubimage((int) selectedRect.getX(), (int) selectedRect.getY(), (int) selectedRect.getWidth(), (int) selectedRect.getHeight()),
							(int) selectedRect.getX(), (int) selectedRect.getY(), (int) selectedRect.getWidth(), (int) selectedRect.getHeight(), null);
					g.setColor(Color.RED);
					g.drawRect((int) selectedRect.getX(), (int) selectedRect.getY(), (int) selectedRect.getWidth(), (int) selectedRect.getHeight());
				}
				
				if (Engine.getCurrentFrameNumber() != -1){
					g.setColor(Color.GREEN);
					g.drawString(
							"Aktuelles Frame: " + Engine.getCurrentFrameNumber() + " von " + Engine.getFrameLength() + " : " + 
									Math.round(((double) Engine.getCurrentFrameNumber() / (double) Engine.getFrameLength()) * 100.0 * 1000) / 1000.0 + " %", 10, 20);
				}
			}
		};
		videoPanel.setBounds(5, 5, videoSurroundPanel.getWidth() - 10, videoSurroundPanel.getHeight() - 50);
		videoPanel.setBorder(null);
		videoPanel.setFocusable(true);
		videoPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		videoPanel.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				videoPanel.requestFocus();
			}
		});
		videoPanel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (Engine.isVideoLoaded())
					switch (e.getKeyCode()) {
					case KeyEvent.VK_M :
						if (Engine.getCurrentFrameNumber() == Engine.getFrameLength())
							JOptionPane.showMessageDialog(null, "Ende des Videos erreicht.", "Information", JOptionPane.INFORMATION_MESSAGE);
						else
							Engine.loadNextImageFromVideo();
						break;
					case KeyEvent.VK_SPACE :
						if (selectedRect != null) {
							Engine.saveCurrentAsPositiveFrame(selectedRect);
							if (Engine.getCurrentFrameNumber() == Engine.getFrameLength())
								JOptionPane.showMessageDialog(null, "Ende des Videos erreicht.", "Information", JOptionPane.INFORMATION_MESSAGE);
							else
								Engine.loadNextImageFromVideo();
						} else
							JOptionPane.showMessageDialog(null, "Bitte erst Objekt markieren", "Parameter fehlen", JOptionPane.INFORMATION_MESSAGE);
						break;
					case KeyEvent.VK_N :
						Engine.saveCurrentAsNegativeFrame();
						if (Engine.getCurrentFrameNumber() == Engine.getFrameLength())
							JOptionPane.showMessageDialog(null, "Ende des Videos erreicht.", "Information", JOptionPane.INFORMATION_MESSAGE);
						else
							Engine.loadNextImageFromVideo();
						break;
					case KeyEvent.VK_J :
						String input = JOptionPane.showInputDialog("Bitte gib die Nummer des Ziel - Frames ein.");
						if (input != null && input.length() > 0)
							Engine.jumpToFrame(Integer.parseInt(input));
						break;
					}
			}
		});
		videoPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				super.mousePressed(arg0);
				startPoint = arg0.getPoint();
				selectedRect = null;
				videoPanel.repaint();
			}
		});
		videoPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if (currentFrame != null) {

					int dX = 0, dY = 0, endPointX = 0, endPointY = 0;
					if (lastPoint != null){
						dX = (int) (e.getX() - lastPoint.getX());
						dY = (int) (e.getY() - lastPoint.getY());
					}

					if (e.getX() + dX >= 0 && e.getX() + dX <= videoPanel.getWidth() - 1)
						endPointX = e.getX();
					else if (e.getX() + dX > videoPanel.getWidth() - 1)
						endPointX = videoPanel.getWidth() - 1;
					if (e.getY() + dY >= 0 && e.getY() + dY <= videoPanel.getHeight() - 1)
						endPointY = e.getY();
					else if (e.getY() + dY > videoPanel.getHeight() - 1)
						endPointY = videoPanel.getHeight() - 1;

					selectedRect = new Rectangle(new Point(
							(int) Math.min(startPoint.getX(), endPointX), 
							(int) Math.min(startPoint.getY(), endPointY)));
					selectedRect.setSize(
							(int) Math.max(startPoint.getX(), endPointX) - (int) selectedRect.getX(),
							(int) Math.max(startPoint.getY(), endPointY) - (int) selectedRect.getY());
					videoPanel.repaint();
				}
				lastPoint = e.getPoint();
			}
		});
		videoSurroundPanel.add(videoPanel);

		loadVideoButton = new JButton("Lade Video");
		loadVideoButton.setBounds(15, videoPanel.getY() + videoPanel.getHeight() + 10, 100, 20);
		loadVideoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(loadFileChooserDir);
				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "*.mp4 - Dateien";
					}

					@Override
					public boolean accept(File f) {
						// Auch Unterverzeichnisse anzeigen
						if (f.isDirectory())
							return true;
						return f.getName().toLowerCase().endsWith(".mp4");
					}
				});
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					if (!Engine.loadVideo(fileChooser.getSelectedFile()))
						JOptionPane.showMessageDialog(null, "Video konnte nicht geladen werden!", "Einlesefehler", JOptionPane.ERROR_MESSAGE);
					else
						Engine.loadNextImageFromVideo();
				}
			}
		});
		videoSurroundPanel.add(loadVideoButton);

		instructionLabel = new JLabel("[M] - nächstes Frame [J] - Springe zu Frame [Space] - Als Positiv markieren [N] - Als Negativ markieren");
		instructionLabel.setBounds(130, loadVideoButton.getY(), 500, 20);
		videoSurroundPanel.add(instructionLabel);

		saveAllAsNegativeButton = new JButton("Alle negativ");
		saveAllAsNegativeButton.setBounds(645, loadVideoButton.getY(), 100, 20);
		saveAllAsNegativeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Engine.saveAllAsNegative();
			}
		});
		videoSurroundPanel.add(saveAllAsNegativeButton);

		Engine.registerPanelInterface(this);
	}
	@Override
	public void setCurrentFrame(Image pCurrentFrame) {
		selectedRect = null;
		this.currentFrame = Engine.convertImageToBufferedImage(pCurrentFrame);
		videoPanel.repaint();
	}
	@Override
	public void setIsLoading(boolean pIsLoading) {
		isLoading = pIsLoading;
		videoPanel.repaint();
	}

}
