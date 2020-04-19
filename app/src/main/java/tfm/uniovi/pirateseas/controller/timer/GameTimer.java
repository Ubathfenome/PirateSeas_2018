package tfm.uniovi.pirateseas.controller.timer;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import tfm.uniovi.pirateseas.global.Constants;

/**
 * GameTimer class to manage in-game timelapse's
 */
public class GameTimer {
	
	private int gameDay;
	private int gameHour;
	private long lastTimestamp;
	
	private static long baseTimestamp;

	/**
	 * Constructor
	 */
	private GameTimer(){
		gameDay = 0;
		gameHour = 0;
		lastTimestamp = 0;
		baseTimestamp = 0;
	}

	/**
	 * Constructor
	 * @param bTimestamp baseTimestamp
	 */
	public GameTimer(long bTimestamp){
		this();		
		baseTimestamp = bTimestamp;
	}

	/**
	 * Update in-game hour
	 */
	public void updateHour(){
		long ts = SystemClock.elapsedRealtime();
		
		long deltaTs;
		double deltaSecs = 0;		// Real-Life seconds
		
		if(lastTimestamp == 0)
			baseTimestamp = ts;
		else{
			deltaTs = ts - baseTimestamp;
			deltaSecs = deltaTs * Constants.MILLIS_TO_SECONDS_INV;
		}
		
		lastTimestamp = ts;
		
		gameHour = getGameHoursFromSeconds(deltaSecs);
		gameDay = getGameDayFromGameHours(deltaSecs);	
	}

	/**
	 * Method to get in-game hour
	 * @param realSecs Real World secs
	 * @return In-game hour
	 */
	private int getGameHoursFromSeconds(double realSecs) {
		return (int) (realSecs % Constants.HOURS_PER_DAY);
	}

	/**
	 * Method to get in-game days
	 * @param realSecs Real World secs
	 * @return In-game days
	 */
	private int getGameDayFromGameHours(double realSecs) {
		float inGameHours = (float) (realSecs / Constants.HOURS_PER_DAY);
		return (int) (inGameHours / Constants.GAME_MPIGD);
	}

	/**
	 * Method to get Base Timestamp
	 * @return Base Timestamp
	 */
	public long getBaseTimestamp(){
		return baseTimestamp;
	}

	/**
	 * Method to get the last registered event timestamp
	 * @return Last timestamp
	 */
	public long getLastTimestamp(){
		return lastTimestamp;
	}

	/**
	 * Get in-game hour
	 * @return In-game hour
	 */
	public float getHour(){
		return gameHour;
	}

	/**
	 * Get in-game day
	 * @return In-game day
	 */
	public int getDay(){
		return gameDay;
	}

	@NonNull
	@Override
	/*
	  toString
	 */
	public String toString() {
		return "GameTimer [gameDay=" + gameDay + ", gameHour=" + gameHour + "]";
	}

}