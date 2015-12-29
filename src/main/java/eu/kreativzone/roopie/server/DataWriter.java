package eu.kreativzone.roopie.server;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

class DataWriter implements Runnable {
	ROI roomba;
	Socket skt;

	DataWriter(ROI mc, Socket skt) {
		roomba = mc;
		this.skt = skt;
	}

	public void run() {
		OutputStream os = null;
		int[] x = { 46, 47, 48, 49, 50, 51 }; // the packets to stream
		try {
			os = skt.getOutputStream();
			DataOutputStream out = new DataOutputStream(os);
			x = roomba.stream(x); // x is now roomba.steamData
			while (!skt.isClosed()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// nop
				}
				
				out.writeInt(x[0]); // send any err code first
				out.writeInt(x[roomba.LIGHT_BUMP_L]); // 46
				out.writeInt(x[roomba.LIGHT_BUMP_FL]); // 47
				out.writeInt(x[roomba.LIGHT_BUMP_CL]); // 48
				out.writeInt(x[roomba.LIGHT_BUMP_CR]); // 49
				out.writeInt(x[roomba.LIGHT_BUMP_FR]); // 50
				out.writeInt(x[roomba.LIGHT_BUMP_R]); // 51
			}
		} catch (SocketException ex) {
			// std exit condition
		} catch (EOFException ex) {
			// std exit condition
		} catch (IOException ex) {
			System.err.println(ex.toString());
		}
		try {
			if (os != null) {
				os.close();
			}
		} catch (IOException ex) {
			System.err.println(ex.toString());
		}
	}
}