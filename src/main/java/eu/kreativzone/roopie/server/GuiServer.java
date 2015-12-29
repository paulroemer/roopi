package eu.kreativzone.roopie.server;

import java.io.*;
import java.net.*;

/** This java application enables the RoombaGUI.UserInterface (running
 * on another machine) to control the Roomba.  It is compiled and run
 * on the raspberry pi connected to the Roomba, and uses the java
 * implementation of the Roomba Open Interface, RooPie.ROI.  This
 * needs to be running before you start the UserInterface program.
 */
public class GuiServer extends ServerSocket {

	GuiServer(int port) throws IOException{
		super(port);
	}

	public static void main(String[] args) {
        	GuiServer gs = null;
        	ROI roomba = null;
		try{ 
			roomba = new ROI("/dev/ttyAMA0");
			roomba.start();
			roomba.safe();
			gs = new GuiServer(4444);
			System.out.print("Starting socketServer .. ");
			boolean stop = false;
			while( !stop ) {
				Socket skt = gs.accept();
				System.out.println("connection made.");
				roomba.leds(roomba.CHECKROBOT,0,0);
				Thread t1 = new Thread(new DataReader(roomba, skt));
				Thread t2 = new Thread(new DataWriter(roomba, skt));
				t1.start();
				t2.start();
				t2.join();
				t1.join();
				roomba.drive(0,0);
				roomba.leds(0,0,0);
				roomba.safe();
				skt.close();
				System.out.println("connection closed. Stop server [y/n]?");
				int yn=System.in.read(); System.in.read();// \n
				if (yn != 'n' && yn != 'N') {
					stop = true;
				}
			}
		} catch(InterruptedException ex) {
			System.out.println(ex.toString());
		} catch( IOException ex) {
			System.out.println(ex.toString());
		}
		
		System.out.println("turning off the server.");
		try {
			if(gs != null) gs.close();
			System.out.print("shutting down ROI ... ");
			if(roomba != null) {
				roomba.disconnect();
			}
			System.out.println("done");
		} catch(IOException ex) {
			// nop
		}
	}
}
