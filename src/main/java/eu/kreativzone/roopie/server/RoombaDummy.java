/**
 * 
 */
package eu.kreativzone.roopie.server;

import java.io.IOException;

import com.jcabi.aspects.Loggable;

import gnu.io.SerialPortEventListener;

/**
 * @author paul
 *
 */
@Loggable
public class RoombaDummy implements IRoombaOpenInterface {

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#addDataAvailableEventHandler(gnu.io.SerialPortEventListener)
	 */
	@Override
	public void addDataAvailableEventHandler(SerialPortEventListener eventHandler) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#start()
	 */
	@Override
	public void start() throws IOException {
		System.out.println("Start");
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#baud(int)
	 */
	@Override
	public void baud(int code) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#passive()
	 */
	@Override
	public void passive() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#safe()
	 */
	@Override
	public void safe() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#full()
	 */
	@Override
	public void full() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#clean()
	 */
	@Override
	public void clean() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#max()
	 */
	@Override
	public void max() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#spot()
	 */
	@Override
	public void spot() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#seekDock()
	 */
	@Override
	public void seekDock() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#schedule(int, int, int, int, int, int, int, int, int, int, int, int, int, int, int)
	 */
	@Override
	public void schedule(int days, int sunHour, int sunMinute, int monHour, int monMinute, int tueHour, int tueMinute,
			int wedHour, int wedMinute, int thrHour, int thrMinute, int friHour, int friMinute, int satHour,
			int satMinute) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#setDayTime(int, int, int)
	 */
	@Override
	public void setDayTime(int day, int hour, int minute) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#power()
	 */
	@Override
	public void power() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#drive(int, int)
	 */
	@Override
	public void drive(int vel, int radius) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#drivePwm(int, int)
	 */
	@Override
	public void drivePwm(int left, int right) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#pwmMotors(int, int, int)
	 */
	@Override
	public void pwmMotors(int main, int side, int vac) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#leds(int, int, int)
	 */
	@Override
	public void leds(int ledBits, int color, int intensity) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#buttons(char)
	 */
	@Override
	public void buttons(char which) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#song(int, int[], int[])
	 */
	@Override
	public void song(int songNumber, int[] notes, int[] durations) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#play(int)
	 */
	@Override
	public void play(int songNumber) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#sensors(int)
	 */
	@Override
	public int sensors(int pid) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#sensors(int, int[])
	 */
	@Override
	public int[] sensors(int groupPacketId, int[] results) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#queryList(int[], int[])
	 */
	@Override
	public int[] queryList(int[] packets, int[] results) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#stream(int[])
	 */
	@Override
	public int[] stream(int[] packets) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.kreativzone.roopie.server.IRoombaOpenInterface#pauseResumeStream()
	 */
	@Override
	public void pauseResumeStream() throws IOException {
		// TODO Auto-generated method stub

	}

}
