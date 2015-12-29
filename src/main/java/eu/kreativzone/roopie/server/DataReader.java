package eu.kreativzone.roopie.server;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

class DataReader implements Runnable {
	ROI roomba;
	Socket skt;

	public DataReader(ROI mc, Socket skt) {
		roomba = mc;
		this.skt = skt;
	}

	public void run() {
		InputStream is = null;
		try {
			is = skt.getInputStream();
			DataInputStream in = new DataInputStream(is);
			int r = 0;
			int v = 0;
			final int SAFE = 0;
			final int PASSIVE = 1;
			int mode = SAFE;
			while (true) {
				int code = in.readInt();
				switch (code) {
				case 1:
					r = in.readInt();
					if (mode != SAFE) {
						roomba.safe();
						mode = SAFE;
					}
					roomba.drive(v, r);
					break;
				case 2:
					v = in.readInt();
					if (mode != SAFE) {
						roomba.safe();
						mode = SAFE;
					}
					roomba.drive(v, r);
					break;
				case 3:
					roomba.passive();
					break;
				case 4:
					if (mode != PASSIVE) {
						roomba.passive();
						mode = PASSIVE;
					}
					roomba.clean();
					break;
				case 5:
					if (mode != PASSIVE) {
						roomba.passive();
						mode = PASSIVE;
					}
					roomba.seekDock();
					break;
				default:
					System.out.println("Unrecognized code: " + code);
				}
			}
		} catch (SocketException ex) {
			System.err.println(ex.toString());
		} catch (EOFException ex) {
			// standard exiting condition
		} catch (IOException ex) {
			System.err.println(ex.toString());
		}
		try {
			if (is != null)
				is.close();
		} catch (IOException ex) {
			System.err.println(ex.toString());
		}
	}
}