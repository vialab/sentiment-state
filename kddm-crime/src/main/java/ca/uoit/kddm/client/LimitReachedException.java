package ca.uoit.kddm.client;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LimitReachedException extends Exception {

	int resetTime;
	

	/**
	 * @param resetTime reset time in seconds.
	 */
	public LimitReachedException(int resetTime) {
		this.resetTime = resetTime;
	}
	
	public LimitReachedException() {
	}

	public int getSecondsUntilReset(){
		int seconds = (int) ((resetTime * 1000L - System.currentTimeMillis()) / 1000);
		return seconds > 0 ? seconds : 0;
	}

	@Override
	public String getMessage() {
		return String.format("Limit for the resource requested has been reached. "
				+ "Wait for the next rate limit window in %d seconds.", getSecondsUntilReset());
	}
	
	public void setResetTime(int resetTime) {
		this.resetTime = resetTime;
	}
//	public static void main(String[] args){
//		System.out.println(30*1000L/1000);
//	}
}
