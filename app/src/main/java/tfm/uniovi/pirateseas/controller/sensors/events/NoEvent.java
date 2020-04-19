package tfm.uniovi.pirateseas.controller.sensors.events;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

/**
 * Event to show when there is no event related to a sensor
 */
public class NoEvent extends AppSensorEvent {
    private static final String TAG = "NoEvent";

    public NoEvent(String name, SensorType sensorType, int imageResource, int eventThumbnailResource, int sensorThumbnailResource, int messageResource, boolean isSensorAvailable) {
        super(name, sensorType, imageResource, eventThumbnailResource, sensorThumbnailResource, messageResource, isSensorAvailable, false);
    }
}
