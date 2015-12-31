package eu.kreativzone.roopie.server;

import java.io.IOException;

import gnu.io.SerialPortEventListener;

public interface IRoombaOpenInterface {
	
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

	/**
	 * Register event handler for data available event
	 *
	 * @param eventHandler
	 *            Event handler
	 */
	void addDataAvailableEventHandler(SerialPortEventListener eventHandler);

	/**
	 * Disconnect the serial port. Under linux you will get a stale lock if this
	 * is not called before turning things off.
	 */
	void disconnect();

	/**
	 * This command starts to OI. You must always send the Start command before
	 * sending any other commands to the OI.
	 */
	void start() throws IOException;

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
	void baud(int code) throws IOException;

	/**
	 * This command puts the OI in Passive mode. Note it is identical to the
	 * start command.
	 */
	void passive() throws IOException;

	/**
	 * This command puts the OI into Safe mode, enabling the user control of
	 * Roomba. It turns off all LEDs. The OI can be in Passive, Safe, or Full
	 * mode to accept this command. If a safety condition occurs (see above)
	 * Roomba reverts automatically to Passive mode.
	 */
	void safe() throws IOException;

	/**
	 * This command gives you complete control over Roomba by putting the OI
	 * into Full mode, and turning off the cliff, wheel-drop and internal
	 * charger safety features. That is, in Full mode, Roomba executes any
	 * command that you send it, even if the internal charger is plugged in, or
	 * command triggers a cliff or wheel drop condition.
	 */
	void full() throws IOException;

	/**
	 */
	void clean() throws IOException;

	/**
	 */
	void max() throws IOException;

	/**
	 */
	void spot() throws IOException;

	/**
	 */
	void seekDock() throws IOException;

	/**
	 * This command sends Roomba a new schedule. To disable scheduled cleaning,
	 * send all 0s. (Days is a mistery - 40 is used in the examples)
	 */
	void schedule(int days, int sunHour, int sunMinute, int monHour, int monMinute, int tueHour, int tueMinute,
			int wedHour, int wedMinute, int thrHour, int thrMinute, int friHour, int friMinute, int satHour,
			int satMinute) throws IOException;

	/**
	 * This command sets Roomba's clock. Day: 0 - 6 (Sunday - Saturday)
	 * hour:0-23 minute:0-59
	 */
	void setDayTime(int day, int hour, int minute) throws IOException;

	/**
	 * This command powers down Roomba.
	 */
	void power() throws IOException;

	/**
	 * This command controls Roomba's drive wheels. It takes two integers, the
	 * first specifies the average velocity of the drive wheels in millimeters
	 * per second (-500 to 500). The next specify the radius in millimeters at
	 * which Roomba will turn (-2000 to 2000). Special cases: Straight = 32768
	 * or 32767 (hex 8000 or 7FFF), Turn in place = -1 (clockwise) or 1
	 * (counter-clockwise)
	 */
	void drive(int vel, int radius) throws IOException;

	/**
	 * This command lets you control the raw forward and backward motion of
	 * Roomba's drive wheels independently. Valid range is -255 to 255. A
	 * positive PWM makes that wheel drive forward, while a negative PWM makes
	 * it drive backward.
	 */
	void drivePwm(int left, int right) throws IOException;

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
	void pwmMotors(int main, int side, int vac) throws IOException;

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
	void leds(int ledBits, int color, int intensity) throws IOException;

	/**
	 * This command lets you push Roomba's buttons. The buttons will
	 * automatically release after 1/6th of a second.
	 * <p>
	 * bit 7:clock 6:schedule 5:day 4:hour 3:minute 2:dock 1:spot 0:clean
	 */
	void buttons(char which) throws IOException;

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
	void song(int songNumber, int[] notes, int[] durations) throws IOException;

	/**
	 * This command lets you select a song to play from the songs added to
	 * Roomba using the Song command. You must add one or more songs to Roomba
	 * using the Song command in order for the Play command to work.
	 */
	void play(int songNumber) throws IOException;

	/**
	 * This command requests the OI to send a single sensor value (packets 7-58
	 * inclusive) as specified in the ROI.
	 */
	int sensors(int pid) throws IOException;

	/**
	 * This command requests the OI to send a Group Packet and returns the
	 * buffer with the individual packets assigned. The first arg is the
	 * required group (0-6,100-107) and the second is an int array into which
	 * the individual packets should be put. It is this array that is returned.
	 * If a value in the array is not set by the group, then its value is left
	 * as is. Note the second arg needs to be atleast 59 long.
	 */
	int[] sensors(int groupPacketId, int[] results) throws IOException;

	/**
	 * This command lets you ask for a list of sensor packets. The result is
	 * returned once, as in the Sensors command. The robot returns the packets
	 * in the order you specify.
	 * <p>
	 * the first arg is the list of packets required; the second arg is the
	 * place to put the values for those packets. The return value is the second
	 * argument.
	 */
	int[] queryList(int[] packets, int[] results) throws IOException;

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
	int[] stream(int[] packets) throws IOException; // stream()

	/**
	 * This command lets you stop and restart the stream without clearing the
	 * list of requested packets.
	 */
	void pauseResumeStream() throws IOException;

}