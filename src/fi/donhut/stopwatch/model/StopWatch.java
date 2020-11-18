package fi.donhut.stopwatch.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.os.Handler;
import android.os.SystemClock;

/**
 * 
 * @author Nhut Do @ 2012
 *
 */
public class StopWatch implements Cloneable {
	
	private long startTime, stopTime, elapsedTime = 0;
	private int lap = 0;
	private boolean isRunning = false;
	
	private Handler handler = null;
	
	private TimeZone defaultTimeZone = TimeZone.getTimeZone("UTF");
	private static SimpleDateFormat clockTimeFormatter = null;
	private Calendar elapsedTimeCal = null;
	
	public StopWatch(Handler handler) {
		clockTimeFormatter = new SimpleDateFormat("KK:mm:ss.SSS");
		clockTimeFormatter.setTimeZone(defaultTimeZone); //SET TO +0 GMT TIME
		
		this.handler = handler;
	}
	
	public void resetTime() {
		elapsedTime = 0;
    	startTime = 0;
    	stopTime = 0;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public int getHour() {
		if(elapsedTimeCal == null) {
			return 0;
		}
		return elapsedTimeCal.get(Calendar.HOUR_OF_DAY);
	}
	
	public String getFormattedHHmmss() {
		return String.format("%02d:%02d:%02d", getHour(), getMin(), getSec());
	}
	
	public String getFormattedMilliseconds() {
		return String.format("%03d", getMsec());
	}
	
	public int getMin() {
		if(elapsedTimeCal == null) {
			return 0;
		}
		return elapsedTimeCal.get(Calendar.MINUTE);
	}

	public int getSec() {
		if(elapsedTimeCal == null) {
			return 0;
		}
		return elapsedTimeCal.get(Calendar.SECOND);
	}

	public int getMsec() {
		if(elapsedTimeCal == null) {
			return 0;
		}
		return elapsedTimeCal.get(Calendar.MILLISECOND);
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	
	public void startClock() {
		setStartTime(SystemClock.elapsedRealtime());
		isRunning = true;
	}
	
	public void stopClock() {
		setStopTime(SystemClock.elapsedRealtime());
		setElapsedTime(getStopTime()-getStartTime());
		
		if(elapsedTimeCal == null) {
			elapsedTimeCal = GregorianCalendar.getInstance(defaultTimeZone);
		}
		elapsedTimeCal.setTimeInMillis(getElapsedTime());
	}
	
	public String getFormattedTime() {
		if(getElapsedTime() != 0) {
			elapsedTimeCal = GregorianCalendar.getInstance(defaultTimeZone);
			elapsedTimeCal.setTimeInMillis(getElapsedTime());
			
    		return clockTimeFormatter.format(elapsedTimeCal.getTime());
    	} else {
    		return "00:00:00.000";
    	}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public int getLap() {
		return lap;
	}

	public void setLap(int lap) {
		this.lap = lap;
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
