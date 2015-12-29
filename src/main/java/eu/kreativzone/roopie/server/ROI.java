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

public class ROI {
	/**
	 * packet ids as per the iRobot Roomba 500 Open Interface Spec. These return
	 * single values; Group packet ids (0-6,100-107) are returned as an array
	 * that uses these to index individual packets
	 */
	public static final int BUMPS = 7;
	public static final int WALL = 8;
	public static final int CLIFF_L = 9;
	public static final int CLIFF_FL = 10;
	public static final int CLIFF_FR = 11;
	public static final int CLIFF_R = 12;
	public static final int V_WALL = 13;
	public static final int OVER_CURRENT = 14;
	public static final int DIRT = 15;
	public static final int P16 = 16;
	public static final int IR_OMNI = 17;
	public static final int BUTTONS = 18;
	public static final int DISTANCE = 19;
	public static final int ANGLE = 20;
	public static final int CHARGING_STATE = 21;
	public static final int VOLTS = 22;
	public static final int AMPS = 23;
	public static final int TEMPERATURE = 24;
	public static final int BATTERY_CHARGE = 25;
	public static final int BATTERY_CAPACITY = 26;
	public static final int WALL_SIG = 27;
	public static final int CLIFF_L_SIG = 28;
	public static final int CLIFF_FL_SIG = 29;
	public static final int CLIFF_FR_SIG = 30;
	public static final int CLIFF_R_SIG = 31;
	public static final int P32 = 32;
	public static final int P33 = 33;
	public static final int CHARGING_SOURCE = 34;
	public static final int MODE = 35;
	public static final int SONG_NUMBER = 36;
	public static final int SONG_PLAYING = 37;
	public static final int STREAM_NUMBER = 38;
	public static final int REQ_VELOCITY = 39;
	public static final int REQ_RADIUS = 40;
	public static final int REQ_VELOCITY_R = 41;
	public static final int REQ_VELOCITY_L = 42;
	public static final int ENCODER_R = 43;
	public static final int ENCODER_L = 44;
	public static final int LIGHT_BUMPER = 45;
	public static final int LIGHT_BUMP_L = 46;
	public static final int LIGHT_BUMP_FL = 47;
	public static final int LIGHT_BUMP_CL = 48;
	public static final int LIGHT_BUMP_CR = 49;
	public static final int LIGHT_BUMP_FR = 50;
	public static final int LIGHT_BUMP_R = 51;
	public static final int IR_CHAR_L = 52;
	public static final int IR_CHAR_R = 53;
	public static final int MOTOR_CURRENT_L = 54;
	public static final int MOTOR_CURRENT_R = 55;
	public static final int MOTOR_CURRENT_B = 56;
	public static final int MOTOR_CURRENT_SB = 57;
	public static final int STASIS = 58;

	public static final int CHECKROBOT = 8;
	public static final int DOCK = 4;
	public static final int SPOT = 2;
	public static final int DEBRIS = 1;

	public static final int TIMEOUT_VALUE = 1000; // 1 second

	// table of bytes returned for each packet, and its sign
	private static final short[] pktTable = new short[70];

	private SerialPort serialPort;
	private OutputStream outStream;
	private InputStream inStream;
	protected int[] streamData = null; // see stream below
	protected int[] newPktList = null;

	public ROI(String portName) throws IOException {
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

	/**
	 * Register event handler for data available event
	 *
	 * @param eventHandler
	 *            Event handler
	 */
	public void addDataAvailableEventHandler(SerialPortEventListener eventHandler) {
		try {
			// Add the serial port event listener
			serialPort.addEventListener(eventHandler);
			serialPort.notifyOnDataAvailable(true);
		} catch (java.util.TooManyListenersException ex) {
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * Disconnect the serial port. Under linux you will get a stale lock if this
	 * is not called before turning things off.
	 */
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

	/**
	 * This command starts to OI. You must always send the Start command before
	 * sending any other commands to the OI.
	 */
	public void start() throws IOException {
		outStream.write((byte) 128);
	}

	/**
	 * This command sets the baud rate in bits per second at which OI commands
	 * an data are sent according to the baud code sent in the data byte. The
	 * default baud rate at power up is 115200 bps, but the starting baud rate
	 * can be changed to 19200 by holding down the Clean button while powering
	 * on Roomba until you hear a sequence of decending tones. Once the baud
	 * rate is changed, it persists until Roomba is power cycled by pressing the
	 * power button or removing the battery, or when the battery voltage falls
	 * below the minimum required for processor operation. You must wait 100ms
	 * after sending this command before sending additional commands at the new
	 * baud rate.
	 * <p>
	 * Baud codes - 0: 300, 1:600, 2:1200, 3:2400, 4:4800, 5:9600, 6:14400,
	 * 7:19200, 8:28800, 9:38400, 10:57600, 11:115200
	 */
	public void baud(int code) throws IOException {
		outStream.write((byte) 129);
		outStream.write((byte) code);
	}

	/**
	 * This command puts the OI in Passive mode. Note it is identical to the
	 * start command.
	 */
	public void passive() throws IOException {
		outStream.write((byte) 128);
	}

	/**
	 * This command puts the OI into Safe mode, enabling the user control of
	 * Roomba. It turns off all LEDs. The OI can be in Passive, Safe, or Full
	 * mode to accept this command. If a safety condition occurs (see above)
	 * Roomba reverts automatically to Passive mode.
	 */
	public void safe() throws IOException {
		outStream.write((byte) 131);
	}

	/**
	 * This command gives you complete control over Roomba by putting the OI
	 * into Full mode, and turning off the cliff, wheel-drop and internal
	 * charger safety features. That is, in Full mode, Roomba executes any
	 * command that you send it, even if the internal charger is plugged in, or
	 * command triggers a cliff or wheel drop condition.
	 */
	public void full() throws IOException {
		outStream.write((byte) 132);
	}

	/**
	 */
	public void clean() throws IOException {
		outStream.write((byte) 135);
	}

	/**
	 */
	public void max() throws IOException {
		outStream.write((byte) 136);
	}

	/**
	 */
	public void spot() throws IOException {
		outStream.write((byte) 134);
	}

	/**
	 */
	public void seekDock() throws IOException {
		outStream.write((byte) 143);
	}

	/**
	 * This command sends Roomba a new schedule. To disable scheduled cleaning,
	 * send all 0s. (Days is a mistery - 40 is used in the examples)
	 */
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

	/**
	 * This command sets Roomba's clock. Day: 0 - 6 (Sunday - Saturday)
	 * hour:0-23 minute:0-59
	 */
	public void setDayTime(int day, int hour, int minute) throws IOException {
		outStream.write((byte) 168);
		outStream.write((byte) day);
		outStream.write((byte) hour);
		outStream.write((byte) minute);
	}

	/**
	 * This command powers down Roomba.
	 */
	public void power() throws IOException {
		outStream.write((byte) 133);
	}

	/**
	 * This command controls Roomba's drive wheels. It takes two integers, the
	 * first specifies the average velocity of the drive wheels in millimeters
	 * per second (-500 to 500). The next specify the radius in millimeters at
	 * which Roomba will turn (-2000 to 2000). Special cases: Straight = 32768
	 * or 32767 (hex 8000 or 7FFF), Turn in place = -1 (clockwise) or 1
	 * (counter-clockwise)
	 */
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

	/**
	 * This command lets you control the raw forward and backward motion of
	 * Roomba's drive wheels independently. Valid range is -255 to 255. A
	 * positive PWM makes that wheel drive forward, while a negative PWM makes
	 * it drive backward.
	 */
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

	/**
	 * This command lets you control the speed of Roomba's main brush, and
	 * vacuum independently. With each data byte, you specify the duty cycle for
	 * the low side driver (max 128). For example, if you want to control a
	 * motor with 25\% of battery voltage, choose a duty cycle of 128 * 25\% =
	 * 32. The main brush and side brush can be run in either direction. The
	 * vacuum only runs forward. Positive speeds turn the motor in its default
	 * (cleaning) direction. Default direction for the side brush is
	 * counterclockwise. Default direction for the main brush/flapper is inward.
	 */
	public void pwmMotors(int main, int side, int vac) throws IOException {
		outStream.write((byte) 144);
		outStream.write((byte) main);
		outStream.write((byte) side);
		outStream.write((byte) vac);
	}

	/**
	 * This command controls the LEDs common to all models of Roomba 500. The
	 * Clean/Power LED is specified by two data bytes: one for the colour and
	 * the other for the intensity.
	 * <ul>
	 * <li>letBits can be one of CHECKROBOT,DOCK,SPOT or DEBRIS, or any
	 * combination bit or-ed together eg DOCK|DEBRIS
	 * <li>color can be 0-255. 0 = green, 255 = red. Intermediate values give
	 * intermediate colors (orange, yellow, etc)
	 * <li>intensity can be 0-255. 0 = off, 255 = full intensity.
	 * </ul>
	 */
	public void leds(int ledBits, int color, int intensity) throws IOException {
		outStream.write((byte) 139);
		outStream.write((byte) ledBits);
		outStream.write((byte) color);
		outStream.write((byte) intensity);
	}

	/**
	 * This command lets you push Roomba's buttons. The buttons will
	 * automatically release after 1/6th of a second.
	 * <p>
	 * bit 7:clock 6:schedule 5:day 4:hour 3:minute 2:dock 1:spot 0:clean
	 */
	public void buttons(char which) throws IOException {
		outStream.write((byte) 165);
		outStream.write((byte) which);
	}

	/**
	 * This command lets you specify up to four songs to the OI that you can
	 * play at a later time. Each song is associated with a song number. The
	 * play command uses the song number to identify your song selection. Each
	 * song can contain up to sixteen notes. Each note is associated with a note
	 * number that uses MIDI note definitions and a duration that is specified
	 * in 64ths of a second. The number of data bytes varies, depending on the
	 * length of the song specified. A one note song is specified by four data
	 * bytes. For each additional note within a song, add two data bytes.
	 */
	public void song(int songNumber, int[] notes, int[] durations) throws IOException {
		throw new RuntimeException("not implemented");
	}

	/**
	 * This command lets you select a song to play from the songs added to
	 * Roomba using the Song command. You must add one or more songs to Roomba
	 * using the Song command in order for the Play command to work.
	 */
	public void play(int songNumber) throws IOException {
		outStream.write((byte) 141);
		outStream.write((byte) songNumber);
	}

	/**
	 * This command requests the OI to send a single sensor value (packets 7-58
	 * inclusive) as specified in the ROI.
	 */
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

	/**
	 * This command requests the OI to send a Group Packet and returns the
	 * buffer with the individual packets assigned. The first arg is the
	 * required group (0-6,100-107) and the second is an int array into which
	 * the individual packets should be put. It is this array that is returned.
	 * If a value in the array is not set by the group, then its value is left
	 * as is. Note the second arg needs to be atleast 59 long.
	 */
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

	/**
	 * This command lets you ask for a list of sensor packets. The result is
	 * returned once, as in the Sensors command. The robot returns the packets
	 * in the order you specify.
	 * <p>
	 * the first arg is the list of packets required; the second arg is the
	 * place to put the values for those packets. The return value is the second
	 * argument.
	 */
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

	/**
	 * This command starts a stream of data packets. The list of packets
	 * requested is sent every 15ms, which is the rate Roomba uses to update
	 * data. It is up to the user to ensure that the number of packets can be
	 * sent in 15ms at the current baud rate - see the ROI specification
	 * document for details.
	 * <p>
	 * Data is returned in an array, A, where A[x] is the reading on the sensor,
	 * x, where x = AMPS,ANGLE ... WALL_SIG. Note all sensor values are
	 * integers, some are signed, some are not; some are two bytes, some are
	 * one. All are converted to conventional java int values. The returned
	 * array is internal - don't try and modify or set it.
	 * <p>
	 * Only the last call to the method counts, and a call to stream(new int[0])
	 * means no packets are sent until stream(...) is called again. A call to
	 * pauseResumeStream() will put on hold any updates to the internal array -
	 * no data is sent from the roomba until pauseResumeStream is called again,
	 * allowing time for other commands/sensor packets to be sent.
	 * <p>
	 * If the data is corrupted, the result will have a non zero value in
	 * position 0,
	 * <ol>
	 * <li>checksum failed
	 * <li>wrong packet ID (corrupt data - something else causing Roomba to send
	 * stuff?)
	 * <li>there was too much data so it probably wasn't sent in the last 15 ms
	 * <li>failed to get the header when expected - syncronizing
	 * </ol>
	 */
	public int[] stream(int[] packets) throws IOException {
		// the runnable in the Thread below picks up changes to newPktList
		if (packets != null)
			newPktList = packets;
		else
			newPktList = new int[0];
		if (streamData != null)
			return streamData;
		// else set up the Thread to process data
		final ROI theRoi = this;
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

	/**
	 * This command lets you stop and restart the stream without clearing the
	 * list of requested packets.
	 */
	public void pauseResumeStream() throws IOException {
		throw new RuntimeException("Not implemented");
	}
}
