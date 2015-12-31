package eu.kreativzone.roopie.server;

import java.io.*;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;

import javaslang.collection.List;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * This java application enables the RoombaGUI.UserInterface (running on another
 * machine) to control the Roomba. It is compiled and run on the raspberry pi
 * connected to the Roomba, and uses the java implementation of the Roomba Open
 * Interface, RooPie.ROI. This needs to be running before you start the
 * UserInterface program.
 */
public class GuiServer extends ServerSocket {

	private static ArgumentParser parser = ArgumentParsers.newArgumentParser("roombaserver").description("Roomba Server");
	private static Namespace parsedArgs;

	GuiServer(int port) throws IOException {
		super(port);
	}

	private static void configure(String[] args) throws ArgumentParserException {
		parser.addArgument("--dummy").action(Arguments.storeTrue());
		parsedArgs = parser.parseArgs(args);
	}

	public static void main(String[] args) {
		try {
			configure(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			return;
		}
		
		GuiServer gs = null;
		IRoombaOpenInterface roomba = null;
		try {
			roomba = parsedArgs.getBoolean("dummy") ? new RoombaDummy() : new Roomba("/dev/ttyAMA0");
			roomba.start();
			roomba.safe();
			gs = new GuiServer(4444);
			System.out.print("Starting socketServer .. ");
			boolean stop = false;
			while (!stop) {
				Socket skt = gs.accept();
				System.out.println("connection made.");
				roomba.leds(IRoombaOpenInterface.CHECKROBOT, 0, 0);
				Thread t1 = new Thread(new DataReader(roomba, skt));
				Thread t2 = new Thread(new DataWriter(roomba, skt));
				t1.start();
				t2.start();
				t2.join();
				t1.join();
				roomba.drive(0, 0);
				roomba.leds(0, 0, 0);
				roomba.safe();
				skt.close();
				System.out.println("connection closed. Stop server [y/n]?");
				int yn = System.in.read();
				System.in.read();// \n
				if (yn != 'n' && yn != 'N') {
					stop = true;
				}
			}
		} catch (InterruptedException ex) {
			System.out.println(ex.getMessage());
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}

		System.out.println("turning off the server.");
		try {
			if (gs != null)
				gs.close();
			System.out.print("shutting down ROI ... ");
			if (roomba != null) {
				roomba.disconnect();
			}
			System.out.println("done");
		} catch (IOException ex) {
			// nop
		}
	}
}
