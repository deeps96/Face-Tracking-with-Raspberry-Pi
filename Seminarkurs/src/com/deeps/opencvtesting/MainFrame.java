package com.deeps.opencvtesting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 *	@author Deeps
 */

public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JFrame imageFrame;
	private JLabel loadedCascade;
	private ArrayList<File> filesToShowLeft;

	/**
	 * Launch the application.
	 */
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

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("OpenCV Cascades Tester by Deeps");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Automatisch generierter Methodenstub
				super.windowClosing(e);
				JOptionPane.showMessageDialog(null, "Anzahl Möglicher Positives: " + Engine.getPositivesMatches() + "\r\nAnzahl richtig gefundener Positives: " +
						Engine.getPositivesFound() + "\r\nAnzahl False-Positives: " + Engine.getFalsePositivesFound(), "Bilanz", JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			}
		});
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(1075, 76);
		setLocation((int) (dimension.getWidth() * 0.5 - getWidth() * 0.5), (int) (dimension.getHeight() * 0.75));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setAlwaysOnTop(true);

		JButton loadImageButton = new JButton("Lade Bild");
		loadImageButton.setBounds(10, 11, 115, 30);
		loadImageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(".");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "Bild - Dateien";
					}

					@Override
					public boolean accept(File arg0) {
						if (arg0.isDirectory())
							return true;
						return isFileTypeAccepted(arg0);
					}
				});
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					filesToShowLeft = new ArrayList<>();
					if (fileChooser.getSelectedFiles().length > 1) {
						File[] files = fileChooser.getSelectedFiles();
						for (File f : files)
							filesToShowLeft.add(f);
					} else if (fileChooser.getSelectedFile().isDirectory()) {
						File[] files = fileChooser.getSelectedFile().listFiles();
						for (File f : files)
							if (isFileTypeAccepted(f))
								filesToShowLeft.add(f);
					} else
						filesToShowLeft.add(fileChooser.getSelectedFile());
					showNextImageInNewFrame();
				}
			}
		});
		contentPane.add(loadImageButton);

		JButton loadCascadeButton = new JButton("Lade Cascade");
		loadCascadeButton.setBounds(135, 11, 115, 30);
		loadCascadeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(".");
				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "XML - Dateien";
					}

					@Override
					public boolean accept(File arg0) {
						if (arg0.isDirectory())
							return true;
						return arg0.getName().toLowerCase().endsWith(".xml");
					}
				});
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					Engine.loadCascade(fileChooser.getSelectedFile().getAbsolutePath());
					loadedCascade.setText("Geladenes Cascade: " + fileChooser.getSelectedFile().getName());
					Engine.loadLastImageAndMarkCascades(); //Funktioniert aus irgendeinem Grund nicht - wird irwie immer null
				}			
			}
		});
		contentPane.add(loadCascadeButton);

		loadedCascade = new JLabel("Geladenes Cascade: leer");
		loadedCascade.setFont(new Font("Tahoma", Font.BOLD, 11));
		loadedCascade.setBounds(260, 16, 238, 20);
		contentPane.add(loadedCascade);

		JButton allOkButton = new JButton("Weiter");
		allOkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showNextImageInNewFrame();
			}
		});
		allOkButton.setBounds(508, 11, 115, 30);
		contentPane.add(allOkButton);

		JButton falsePositivesButton = new JButton("False - Positives");
		falsePositivesButton.setBounds(633, 11, 115, 30);
		falsePositivesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String in = JOptionPane.showInputDialog("Wieviele False-Positives?");
				int inInteger = Integer.parseInt(in);
				Engine.falsePositives(inInteger);
			}
		});
		contentPane.add(falsePositivesButton);

		JButton missingPositivesButton = new JButton("Fehlende Positives");
		missingPositivesButton.setBounds(758, 11, 127, 30);
		missingPositivesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String in = JOptionPane.showInputDialog("Wieviele fehlende Positives?");
				int inInteger = Integer.parseInt(in);
				Engine.missingPositives(inInteger);
			}
		});
		contentPane.add(missingPositivesButton);

		JButton loadAllImagesFromUrlButton = new JButton("Lade alle Bilder von URL");
		loadAllImagesFromUrlButton.setBounds(895, 11, 150, 30);
		loadAllImagesFromUrlButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String url = JOptionPane.showInputDialog("Geben Sie bitte die Url ein!");
				if (url != null){
					Engine.downloadAllImagesFromURL(url);
					File downloadDir = new File("./download");
					if (downloadDir.exists() && downloadDir.isDirectory()){
						filesToShowLeft = new ArrayList<>();
						File[] files = downloadDir.listFiles();
						for (File f : files)
							if (isFileTypeAccepted(f))
								filesToShowLeft.add(f);
						showNextImageInNewFrame();
					}
				}
			}
		});
		contentPane.add(loadAllImagesFromUrlButton);
		
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){}
		setVisible(true);
	}

	private void showNextImageInNewFrame(){
		if (imageFrame != null){
			imageFrame.dispose();
			imageFrame = null;
		}
		
		if (filesToShowLeft.size() == 0)
			return;
		
		BufferedImage image = Engine.loadImageAndMarkCascades(filesToShowLeft.get(0).getAbsolutePath());
		if (image == null){
			JOptionPane.showMessageDialog(null, "Cascade muss zuerst geladen werden.", "Cascade == null", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setTitle("OpenCV Cascades Tester by Deeps : " + filesToShowLeft.get(0).getAbsolutePath());
		imageFrame = new JFrame(filesToShowLeft.get(0).getAbsolutePath());
		imageFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		imageFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				imageFrame.dispose();
				imageFrame = null;
				showNextImageInNewFrame();
			}
		});
		imageFrame.setBackground(Color.WHITE);
		imageFrame.setResizable(false);
		imageFrame.setSize(image.getWidth() + 30, image.getHeight() + 80);
		imageFrame.setLocationRelativeTo(null);
		imageFrame.setLayout(null);
		JLabel imageLabel = new JLabel(new ImageIcon(image));
		imageLabel.setBounds(0, 0, imageFrame.getRootPane().getWidth(), imageFrame.getRootPane().getHeight());
		imageFrame.setContentPane(imageLabel);
		imageFrame.setVisible(true);
		requestFocus();
		filesToShowLeft.remove(0);
	}

	private boolean isFileTypeAccepted(File arg0){
		return arg0.getName().toLowerCase().endsWith(".bmp") || arg0.getName().toLowerCase().endsWith(".jpeg") ||
				arg0.getName().toLowerCase().endsWith(".jpg") || arg0.getName().toLowerCase().endsWith(".png") ||
				arg0.getName().toLowerCase().endsWith(".jp2") || arg0.getName().toLowerCase().endsWith(".jpe") ||
				arg0.getName().toLowerCase().endsWith(".pbm") || arg0.getName().toLowerCase().endsWith(".pgm") ||
				arg0.getName().toLowerCase().endsWith(".ppm") || arg0.getName().toLowerCase().endsWith(".sr") ||
				arg0.getName().toLowerCase().endsWith(".ras") || arg0.getName().toLowerCase().endsWith(".tif");
	}
}
