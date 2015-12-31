package eu.kreativzone.roopie.server;

import gnu.io.*;
import java.io.*;

/**
 * Implements the Roomba Open Interface to the 500 series using RXTX. The
 * document is available here: <a href=
 * "http://cfpm.org/~peter/bfz/iRobot_Roomba_500_Open_Interface_Spec.pdf" >http:
 * //cfpm.org/~peter/bfz/iRobot_Roomba_500_Open_Interface_Spec.pdf</a> setting
 * up the pi to use the serial port is here:
 * <a href="http://www.elinux.org/RPi_Serial_Connection" >http://www.elinux.org/
 * RPi_Serial_Connection</a> and the RXTX package with instructions is available
 * here:
 * <a href="http://rxtx.qbang.org/wiki/index.php/Main_Page" >http://rxtx.qbang.
 * org/wiki/index.php/Main_Page</a>.
 *
 * @author peter wallis
 */

public class Roomba implements IRoombaOpenInterface {

	public static final int TIMEOUT_VALUE = 1000; // 1 second

	// table of bytes returned for each packet, and its sign
	private static final short[] pktTable = new short[70];

	private SerialPort serialPort;
	private OutputStream outStream;
	private InputStream inStream;
	protected int[] streamData = null; // see stream below
	protected int[] newPktList = null;

	public Roomba(String portName) throws IOException {
		try {
			if (!portName.startsWith("ttyS")) {
				System.setProperty("gnu.io.rxtx.SerialPorts", portName);
			}

			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
			serialPort = (SerialPort) portId.open("Roomba interface", 5000);
			// see the iRobot Roomba Open Interface Specification
			serialPort.setSerialPortParams(115200, // default is 115200 and the
													// alt is 19200
					SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			outStream = serialPort.getOutputStream();
			inStream = serialPort.getInputStream();
			initPacketSpec();
		} catch (NoSuchPortException e) {
			throw new IOException(e.getMessage());
		} catch (PortInUseException e) {
			throw new IOException(e.getMessage());
		} catch (UnsupportedCommOperationException e) {
			throw new IOException("Unsupported serial port parameter:" + e.getMessage());
		} catch (IOException e) {
			serialPort.close();
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#addDataAvailableEventHandler(gnu.io.SerialPortEventListener)
	 */
	@Override
	public void addDataAvailableEventHandler(SerialPortEventListener eventHandler) {
		try {
			// Add the serial port event listener
			serialPort.addEventListener(eventHandler);
			serialPort.notifyOnDataAvailable(true);
		} catch (java.util.TooManyListenersException ex) {
			System.err.println(ex.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#disconnect()
	 */
	@Override
	public void disconnect() {
		// Note serialPort.close() will hang if there is still data coming
		// on inSteam EVEN IF IT IS CLOSED. The call to stream(null) turns
		// off any data being streamed to it.
		try {
			stream(null); // *** see above.
			passive(); // turn the green light back on
			if (outStream != null)
				outStream.close();
			if (inStream != null)
				inStream.close();
			if (serialPort != null)
				serialPort.close();
		} catch (IOException ex) {
			System.err.println(ex.toString());
		}
	}

	/**
	 * waits until the specified number of bytes can be read from the serial
	 * port or TIMEOUT_VALUE is reached.
	 */
	private void waitFor(int bytesAvailable) throws IOException {
		try {
			for (int z = 10; z > 0; z--) {
				int k = inStream.available();
				if (k >= bytesAvailable)
					return;
				// System.out.println(
				// "waitFor("+bytesAvailable+") - got "+k);
				Thread.sleep(TIMEOUT_VALUE / 10);
			}
			throw new IOException("serial connection time out");
		} catch (InterruptedException e) {
		}
		;
	}

	/**
	 * data returned from the Roomba comes in different sizes and this specifies
	 * how many bytes to get. A negative entry in this table means the result is
	 * signed.
	 */
	private void initPacketSpec() {
		pktTable[0] = 26;
		pktTable[1] = 10;
		pktTable[2] = 6;
		pktTable[3] = 10;
		pktTable[4] = 14;
		pktTable[5] = 12;
		pktTable[6] = 52;
		pktTable[BUMPS] = 1;
		pktTable[WALL] = 1;
		pktTable[CLIFF_L] = 1;
		pktTable[CLIFF_FL] = 1;
		pktTable[CLIFF_FR] = 1;
		pktTable[CLIFF_R] = 1;
		pktTable[V_WALL] = 1;
		pktTable[OVER_CURRENT] = 1;
		pktTable[DIRT] = 1;
		pktTable[P16] = 1;
		pktTable[IR_OMNI] = 1;
		pktTable[BUTTONS] = 1;
		pktTable[DISTANCE] = -2; // two bytes, signed
		pktTable[ANGLE] = -2;
		pktTable[CHARGING_STATE] = 1;
		pktTable[VOLTS] = 2; // mV
		pktTable[AMPS] = -2; // mA, +ive value means charging
		pktTable[TEMPERATURE] = 1;
		pktTable[BATTERY_CHARGE] = 2;
		pktTable[BATTERY_CAPACITY] = 2;
		pktTable[WALL] = 2;
		pktTable[CLIFF_L] = 2;
		pktTable[CLIFF_FL] = 2;
		pktTable[CLIFF_FR] = 2;
		pktTable[CLIFF_R] = 2;
		pktTable[P32] = 1;
		pktTable[P33] = 1;
		pktTable[CHARGING_SOURCE] = 1;
		pktTable[MODE] = 1;
		pktTable[SONG_NUMBER] = 1;
		pktTable[SONG_PLAYING] = 1;
		pktTable[STREAM_NUMBER] = 1; // ???
		pktTable[REQ_VELOCITY] = -2;
		pktTable[REQ_RADIUS] = -2;
		pktTable[REQ_VELOCITY_R] = -2;
		pktTable[REQ_VELOCITY_L] = -2;
		pktTable[ENCODER_R] = 2;
		pktTable[ENCODER_L] = 2;
		pktTable[LIGHT_BUMPER] = 1;
		pktTable[LIGHT_BUMP_L] = 2;
		pktTable[LIGHT_BUMP_FL] = 2;
		pktTable[LIGHT_BUMP_CL] = 2;
		pktTable[LIGHT_BUMP_CR] = 2;
		pktTable[LIGHT_BUMP_FR] = 2;
		pktTable[LIGHT_BUMP_R] = 2;
		pktTable[IR_CHAR_L] = 1;
		pktTable[IR_CHAR_R] = 1;
		pktTable[MOTOR_CURRENT_L] = -2;
		pktTable[MOTOR_CURRENT_R] = -2;
		pktTable[MOTOR_CURRENT_B] = -2;
		pktTable[MOTOR_CURRENT_SB] = -2;
		pktTable[STASIS] = 1;
		pktTable[60] = 80;// Packet 100
		pktTable[61] = 21;// Packet 101
		pktTable[66] = 12;// Packet 106
		pktTable[67] = 9; // Packet 107
	}

	static int one2unsigned(int b) {
		return b & 0xFF;
	}

	static int one2signed(int b) {
		return b;
	}

	static int two2unsigned(int hi, int lo) {
		return hi << 8 & 0xFF00 | lo & 0xFF;
	}

	static int two2signed(int hi, int lo) {
		return hi << 8 | lo & 0xFF;
	}

	/**
	 */
	private int readOne() throws IOException {
		waitFor(1);
		return one2unsigned(inStream.read());
	}

	/**
	 */
	private int readOneSigned() throws IOException {
		waitFor(1);
		return one2signed(inStream.read());
	}

	/**
	 */
	private int readTwo() throws IOException {
		waitFor(2);
		return two2unsigned(inStream.read(), inStream.read());
	}

	/**
	 */
	private int readTwoSigned() throws IOException {
		waitFor(2);
		return two2signed(inStream.read(), inStream.read());
	}

	// ----------------- the ROI interface commands ------------------

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#start()
	 */
	@Override
	public void start() throws IOException {
		outStream.write((byte) 128);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#baud(int)
	 */
	@Override
	public void baud(int code) throws IOException {
		outStream.write((byte) 129);
		outStream.write((byte) code);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#passive()
	 */
	@Override
	public void passive() throws IOException {
		outStream.write((byte) 128);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#safe()
	 */
	@Override
	public void safe() throws IOException {
		outStream.write((byte) 131);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#full()
	 */
	@Override
	public void full() throws IOException {
		outStream.write((byte) 132);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#clean()
	 */
	@Override
	public void clean() throws IOException {
		outStream.write((byte) 135);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#max()
	 */
	@Override
	public void max() throws IOException {
		outStream.write((byte) 136);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#spot()
	 */
	@Override
	public void spot() throws IOException {
		outStream.write((byte) 134);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#seekDock()
	 */
	@Override
	public void seekDock() throws IOException {
		outStream.write((byte) 143);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#schedule(int, int, int, int, int, int, int, int, int, int, int, int, int, int, int)
	 */
	@Override
	public void schedule(int days, int sunHour, int sunMinute, int monHour, int monMinute, int tueHour, int tueMinute,
			int wedHour, int wedMinute, int thrHour, int thrMinute, int friHour, int friMinute, int satHour,
			int satMinute) throws IOException {
		outStream.write((byte) 167);
		outStream.write((byte) days);
		outStream.write((byte) sunHour);
		outStream.write((byte) sunMinute);
		outStream.write((byte) monHour);
		outStream.write((byte) monMinute);
		outStream.write((byte) tueHour);
		outStream.write((byte) tueMinute);
		outStream.write((byte) wedHour);
		outStream.write((byte) wedMinute);
		outStream.write((byte) thrHour);
		outStream.write((byte) thrMinute);
		outStream.write((byte) friHour);
		outStream.write((byte) friMinute);
		outStream.write((byte) satHour);
		outStream.write((byte) satMinute);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#setDayTime(int, int, int)
	 */
	@Override
	public void setDayTime(int day, int hour, int minute) throws IOException {
		outStream.write((byte) 168);
		outStream.write((byte) day);
		outStream.write((byte) hour);
		outStream.write((byte) minute);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#power()
	 */
	@Override
	public void power() throws IOException {
		outStream.write((byte) 133);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#drive(int, int)
	 */
	@Override
	public void drive(int vel, int radius) throws IOException {
		if (vel > 500)
			vel = 500;
		if (vel < -500)
			vel = -500;
		if (radius == 32768 || radius == 32767) {
		} else if (radius > 2000)
			radius = 2000;
		if (radius < -2000)
			radius = -2000;
		short v = (short) vel;
		short r = (short) radius;
		outStream.write((byte) 137);
		outStream.write((byte) (v >> 8 & 0xFF));
		outStream.write((byte) (v & 0xFF));
		outStream.write((byte) (r >> 8 & 0xFF));
		outStream.write((byte) (r & 0xFF));
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#drivePwm(int, int)
	 */
	@Override
	public void drivePwm(int left, int right) throws IOException {
		if (left > 255)
			left = 255;
		if (left < -255)
			left = -255;
		if (right > 255)
			right = 255;
		if (right < -255)
			right = -255;
		short l = (short) left;
		short r = (short) right;
		outStream.write((byte) 146);
		outStream.write((byte) (r >> 8 & 0xFF));
		outStream.write((byte) (r & 0xFF));
		outStream.write((byte) (l >> 8 & 0xFF));
		outStream.write((byte) (l & 0xFF));
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#pwmMotors(int, int, int)
	 */
	@Override
	public void pwmMotors(int main, int side, int vac) throws IOException {
		outStream.write((byte) 144);
		outStream.write((byte) main);
		outStream.write((byte) side);
		outStream.write((byte) vac);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#leds(int, int, int)
	 */
	@Override
	public void leds(int ledBits, int color, int intensity) throws IOException {
		outStream.write((byte) 139);
		outStream.write((byte) ledBits);
		outStream.write((byte) color);
		outStream.write((byte) intensity);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#buttons(char)
	 */
	@Override
	public void buttons(char which) throws IOException {
		outStream.write((byte) 165);
		outStream.write((byte) which);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#song(int, int[], int[])
	 */
	@Override
	public void song(int songNumber, int[] notes, int[] durations) throws IOException {
		throw new RuntimeException("not implemented");
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#play(int)
	 */
	@Override
	public void play(int songNumber) throws IOException {
		outStream.write((byte) 141);
		outStream.write((byte) songNumber);
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#sensors(int)
	 */
	@Override
	public int sensors(int pid) throws IOException {
		if (pid >= 100 || pid < 7)
			throw new RuntimeException("sensors(int) returns a single value, use sensor(int, int[])");
		outStream.write((byte) 142);
		outStream.write((byte) pid);
		if (pktTable[pid] == -2) {
			return readTwoSigned();
		} else if (pktTable[pid] == 1) {
			return readOne();
		} else if (pktTable[pid] == 2) {
			return readTwo();
		} else
			throw new RuntimeException("sensors(int) returns a single value, use sensor(int, int[])");
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#sensors(int, int[])
	 */
	@Override
	public int[] sensors(int groupPacketId, int[] results) throws IOException {
		int start;
		int stop = 0;
		int size = 0;
		switch (groupPacketId) { // ROI spec page 33
		case 0:
			start = 7;
			stop = 26;
			size = 26;
			break;
		case 1:
			start = 7;
			stop = 16;
			size = 10;
			break;
		case 2:
			start = 17;
			stop = 20;
			size = 6;
			break;
		case 3:
			start = 21;
			stop = 26;
			size = 10;
			break;
		case 4:
			start = 27;
			stop = 34;
			size = 14;
			break;
		case 5:
			start = 35;
			stop = 42;
			size = 12;
			break;
		case 6:
			start = 7;
			stop = 42;
			size = 52;
			break;
		case 100:
			start = 7;
			stop = 58;
			size = 80;
			break;
		case 106:
			start = 46;
			stop = 51;
			size = 12;
			break;
		case 107:
			start = 54;
			stop = 58;
			size = 9;
			break;
		default:
			start = -1;
		}
		if (start < 0)
			throw new RuntimeException("Unrecognized group packet: " + groupPacketId);

		outStream.write((byte) 142);
		outStream.write((byte) groupPacketId);
		waitFor(size);
		for (int pid = start; pid <= stop; pid++) {
			if (pktTable[pid] == -2) {
				results[pid] = readTwoSigned();
			} else if (pktTable[pid] == 1) {
				results[pid] = readOne();
			} else if (pktTable[pid] == 2) {
				results[pid] = readTwo();
			} else
				throw new RuntimeException("Error 171");
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#queryList(int[], int[])
	 */
	@Override
	public int[] queryList(int[] packets, int[] results) throws IOException {
		outStream.write((byte) 149);
		outStream.write((byte) packets.length);
		int psz = 0;
		for (int i = 0; i < packets.length; i++) {
			outStream.write((byte) packets[i]);
			int j = pktTable[packets[i]];
			psz += j > 0 ? j : -j;
		}
		waitFor(psz);
		for (int i = 0; i < packets.length; i++) {
			int pid = packets[i];
			if (pktTable[pid] == -2) {
				results[i] = readTwoSigned();
			} else if (pktTable[pid] == 1) {
				results[i] = readOne();
			} else if (pktTable[pid] == 2) {
				results[i] = readTwo();
			} else
				throw new RuntimeException("sensors(int) returns a single value, use sensor(int, int[])");
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#stream(int[])
	 */
	@Override
	public int[] stream(int[] packets) throws IOException {
		// the runnable in the Thread below picks up changes to newPktList
		if (packets != null)
			newPktList = packets;
		else
			newPktList = new int[0];
		if (streamData != null)
			return streamData;
		// else set up the Thread to process data
		final Roomba theRoi = this;
		streamData = new int[60];
		Thread t = new Thread(new Runnable() {
			int[] pktList = null;

			public void run() {
				while (true)
					try {
						if (theRoi.newPktList != null) {
							// send a new Stream command
							pktList = theRoi.newPktList;
							theRoi.newPktList = null;
							if (pktList.length == 0) {
								outStream.write((byte) 150);
								outStream.write((byte) 0); // pause the stream
								streamData = null; // so that it will be reset
								break;
							}
							outStream.write((byte) 148);
							outStream.write((byte) pktList.length);
							for (int i = 0; i < pktList.length - 1; i++)
								outStream.write((byte) pktList[i]);
							inStream.skip(inStream.available());
							outStream.write((byte) pktList[pktList.length - 1]);
						}
						// at this poit we expect [19][n-bytes]...[checksum]
						int header = inStream.read();
						if (header != (byte) 19) {
							streamData[0] = 4; // data is out of sync
							continue;
						}
						// Note the documentation says the header is not used,
						// but it seems it is. And of course doing a checksum
						// on all the send data is a more standard way of doing
						// things.
						int csm = 19;
						// got 19. Have we got the latest data?
						int nBytes = 0xFF & inStream.read();
						int k = nBytes + 3; // number of bytes in a stream
											// element
						if (inStream.available() >= 2 * k - 2) {
							streamData[0] = 3; // there's a backlog of data
							int b = inStream.available() / k;
							inStream.skip(b * k);
							continue;
						}
						// the data is the last sent. Get it, keeping
						// track of the checksum
						csm += nBytes;
						for (int i = 0; i < pktList.length; i++) {
							int z = inStream.read(); // less than 128 packets
							csm += z;
							if ((byte) pktList[i] != z) {
								streamData[0] = 2; // wrong packet ID
								continue;
							}
							z = pktTable[pktList[i]]; // -2, -1, 1 or 2
							boolean signed = z < 0;
							z = z < 0 ? -z : z;
							if (z == 2) {
								int hi = inStream.read();
								int lo = inStream.read();
								csm += one2unsigned(hi) + one2unsigned(lo);
								z = signed ? two2signed(hi, lo) : two2unsigned(hi, lo);
							} else {
								int b = inStream.read();
								csm += one2unsigned(b);
								z = signed ? one2signed(b) : one2unsigned(b);
							}
							streamData[pktList[i]] = z;
						}
						csm += inStream.read(); // the checksum value
						if ((csm & 0xFF) == 0)
							streamData[0] = 0; // success
						else
							streamData[0] = 1; // failed checksum
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				// System.out.println("no more sends");
			}// run
		});
		t.start();
		return streamData;
	} // stream()

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#pauseResumeStream()
	 */
	@Override
	public void pauseResumeStream() throws IOException {
		throw new RuntimeException("Not implemented");
	}
}
