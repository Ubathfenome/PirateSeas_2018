package tfm.uniovi.pirateseas.controller.sensors.events;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

@SuppressWarnings("unused")
/*
  Event to generate the maelstorm that damages every ship on the screen
 */
public class EventWeatherMaelstrom extends AppSensorEvent{
	private static final String TAG = "EventWeatherMaelstrom";
	private static final int AVERAGE = 10;
	private static final int THRESHOLD = 9;

	public EventWeatherMaelstrom(String name, SensorType sensorType, int imageResource, int eventThumbnailResource, int sensorThumbnailResource, int messageResource, boolean isSensorAvailable) {
		super(name, sensorType, imageResource, eventThumbnailResource, sensorThumbnailResource, messageResource, isSensorAvailable, true);
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

		// Log.d(TAG, "ySpeed: " + Math.abs(ySpeed) + " (" + THRESHOLD + ")");

		return Math.abs(ySpeed) >= THRESHOLD;
    }
}