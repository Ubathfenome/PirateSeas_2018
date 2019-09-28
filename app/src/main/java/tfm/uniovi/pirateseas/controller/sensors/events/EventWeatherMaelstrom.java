package tfm.uniovi.pirateseas.controller.sensors.events;

import android.util.Log;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

@SuppressWarnings("unused")
/*
  Event to generate the maelstorm that damages every ship on the screen
 */
public class EventWeatherMaelstrom extends AppSensorEvent{
	private static final String TAG = "EventWeatherMaelstrom";
	private static final int AVERAGE = 10;
	private static final int THRESHOLD = 3;

	public EventWeatherMaelstrom(String name, SensorType sensorType, int imageResource, int thumbnailResource, int messageResource) {
		super(name, sensorType, imageResource, thumbnailResource, messageResource);
	}

	/**
	 * Establish event effects on sensor trigger
 	 */
	public static boolean generateMaelstrom(float ySpeed){
		/*
		double gHypot = Math.sqrt(ySpeed*ySpeed + zSpeed*zSpeed);
		
		// Log.d(TAG, "Gravity (m/s^2): " + gHypot);

        return Math.abs(gHypot - AVERAGE) >= THRESHOLD;
		*/

		Log.d(TAG, "ySpeed: " + ySpeed + " (" + THRESHOLD + ")");

		return Math.abs(ySpeed) >= THRESHOLD;
    }
}