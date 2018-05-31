package tfm.uniovi.pirateseas.controller.sensors.events;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

/**
 * Event for the Shake Clouds behaviour
 */
public class EventShakeClouds {
	
	public static final float threshold = 4;

	/**
	 * Return SensorType used on this event
	 * @return SensorType code
	 */
	public static SensorType getSensorType() {
		return SensorType.TYPE_LINEAR_ACCELERATION;
	}
}
