package tfm.uniovi.pirateseas.controller.sensors.events;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

/**
 * Event for the Shake Clouds behaviour
 */
public class EventShakeClouds extends AppSensorEvent{
	
	public static final float threshold = 4;

	public EventShakeClouds(String name, SensorType sensorType, int imageResource, int eventThumbnailResource, int sensorThumbnailResource, int messageResource, boolean isSensorAvailable) {
		super(name, sensorType, imageResource, eventThumbnailResource, sensorThumbnailResource, messageResource, isSensorAvailable, true);
	}
}
