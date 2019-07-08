package tfm.uniovi.pirateseas.controller.sensors.events;

import android.hardware.SensorManager;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Event for the Day Night cycle that obscures the screen at night and brightens it al day
 */
public class EventDayNightCycle extends AppSensorEvent {
	private static final String TAG = "EventDayNightCycle";
	
	private static final int MAX_SHADOW_VALUE = 255;
	private static final float PSA = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	private static final float HOUR_VALUE_RATIO = (MAX_SHADOW_VALUE * 2) / (Constants.HOURS_PER_DAY * PSA); // Ratio = 510 values / 60 hours per day; -> X values per hour
	public static float pressure = PSA;

	public EventDayNightCycle(String name, SensorType sensorType, int imageResource, int thumbnailResource) {
		super(name, sensorType, imageResource, thumbnailResource);
	}

	/**
	 * Filter to apply to the background image of the sky
	 * @param hour inGame hour value
	 * @return Filter to apply
	 */
	public static int getSkyFilter(float hour){
		// Changes between day to night and viceversa
		int filterValue = 0;
		if(hour <= (Constants.HOURS_PER_DAY / 2))
			filterValue = (int) (hour * pressure * HOUR_VALUE_RATIO);
		else
			filterValue = (int) ((Constants.HOURS_PER_DAY - hour) * pressure * HOUR_VALUE_RATIO);
		// Log.d(TAG, "Sky mask is " + filterValue + " while hour is " + hour);
		return filterValue;
	}
}