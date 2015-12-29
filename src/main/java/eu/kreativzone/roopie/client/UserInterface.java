package eu.kreativzone.roopie.client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Run this java application on a laptop on the network your Roomba's Pi is on,
 * and you can move the Roomba about the room by using the mouses to drag the
 * red dot about the control panel. The Roomba's Pi needs to be running
 * RooPie.GuiServer before this is started.
 */

class UserInterface {
	private DataInputStream in;
	private DataOutputStream out;

	UserInterface(String mc, int port) {
		// make the connection
		try {
			Socket connection = new Socket(mc, port);
			in = new DataInputStream(connection.getInputStream());
			out = new DataOutputStream(connection.getOutputStream());
		} catch (java.net.UnknownHostException ex) {
			System.out.println(
					"Unknown host: " + ex.getMessage() + " - is" + " the pi on the Roomba connected to the network?");
		} catch (java.net.ConnectException ex) {
			System.out.println("Found the raspberry pi, but is " + "RooPie.GUIServer running on the pi on the Roomba?");
		} catch (IOException ex) {
			System.err.println(ex.toString());
		}
		// connection is now set up if there is one. Now set up the GUI
		JFrame frame = new JFrame("Roomba 500 series ROI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JTextField msgs = new JTextField("");
		final LevelMeter lm1 = new LevelMeter(0, 410, 100, 15); // 4095/10
		final LevelMeter lm2 = new LevelMeter(0, 410, 100, 15);
		final LevelMeter lm3 = new LevelMeter(0, 410, 100, 15);
		final LevelMeter lm4 = new LevelMeter(0, 410, 100, 15);
		final LevelMeter lm5 = new LevelMeter(0, 410, 100, 15);
		final LevelMeter lm6 = new LevelMeter(0, 410, 100, 15);
		final JoyStick stick = new JoyStick(Color.WHITE);
		JButton b1 = new JButton("Clean");
		JButton b2 = new JButton("Dock");
		JButton b3 = new JButton("Stop");
		// do layout
		JPanel p = new JPanel();
		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		p2.add(stick);
		p2.add(lm1);
		p2.add(lm2);
		p2.add(lm3);
		p2.add(lm4);
		p2.add(lm5);
		p2.add(lm6);
		p.setLayout(new BorderLayout());
		p1.setLayout(new FlowLayout());
		p1.add(b1);
		p1.add(b2);
		p1.add(b3);
		p.add(p1, BorderLayout.NORTH);
		p.add(p2, BorderLayout.CENTER);
		p.add(msgs, BorderLayout.SOUTH);
		frame.getContentPane().add(p);
		frame.pack();
		frame.setVisible(true);

		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					msgs.setText("clean");
					stick.zero();
					out.writeInt(4);
				} catch (Exception ex) {
					msgs.setText("no connection.");
				}
				msgs.repaint();
			}
		});

		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					msgs.setText("seek dock");
					stick.zero();
					out.writeInt(5);
				} catch (Exception ex) {
					msgs.setText("no connection.");
				}
				msgs.repaint();
			}
		});
		// make the button send a message down the socket
		b3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					msgs.setText("b3 pressed");
					stick.zero();
					out.writeInt(3);
				} catch (Exception ex) {
					msgs.setText("no connection.");
				}
				msgs.repaint();
			}
		});
		// now make stick write its position down the socket
		stick.addStickListener(new StickListener() {
			public void stickMoved(StickEvent ev) {
				try {
					// stick returns a value between 0 and 100
					int r = ev.X * 40 - 2000; // a value between -2000 and 2000
					if (r > 0)
						r = 2001 - r; // 2000 .. 1
					else if (r < 0)
						r = -2001 - r;// -2000 .. -1
					else
						r = 32768;
					out.writeInt(1);
					out.writeInt(r);
					int v = 500 - ev.Y * 10; // a value between -500 and 500
					out.writeInt(2);
					out.writeInt(v);
				} catch (IOException ex) {
					System.err.println(ex.toString());
				}
			}
		});
		// and create a thread to read sensor packets from the socket
		Thread readr = new Thread(new Runnable() {
			public void run() {
				if (in == null)
					return;
				try {
					while (true) {
						Thread.sleep(15);
						if (in.available() > 0) {
							// we are expecting 7 ints - they wait for data
							int error = in.readInt();
							if (error != 0) {
								msgs.setText("Error " + error + " streaming data.");
							} else {
								lm1.setValue(in.readInt());
								lm2.setValue(in.readInt());
								lm3.setValue(in.readInt());
								lm4.setValue(in.readInt());
								lm5.setValue(in.readInt());
								lm6.setValue(in.readInt());
							}
						}
					}
				} catch (IOException ex) {
					System.out.println(ex.toString());
				} catch (InterruptedException ex) {
				}
				System.out.println("--- stopped reading ---");
			}
		});
		readr.start();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// The UserInterface communicates with the program
				// PiServer running on raspberrypi. It is PiServer
				// that uses the Roomba ROI interface to communicate
				// with the hardware.
				// new UserInterface("raspberrypi",4444);

				// the rpi is given a static ip address
				new UserInterface("192.168.1.73", 4444);
			}
		});
	}
}
