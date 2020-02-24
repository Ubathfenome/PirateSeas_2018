package tfm.uniovi.pirateseas.controller.sensors.events;

import android.util.Log;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

public class AppSensorEvent {

    private String eventName;
    private SensorType sensorType;
    private boolean active;
    private int imageResource;
    private int eventThumbnailResource;
    private int sensorThumbnailResource;
    private int messageResource;

    AppSensorEvent(String name, SensorType sensorType, int imageResource, int eventThumbnailResource, int sensorThumbnailResource, int messageResource, boolean isActive){
        this.eventName = name;
        this.sensorType = sensorType;
        this.imageResource = imageResource;
        this.eventThumbnailResource = eventThumbnailResource;
        this.sensorThumbnailResource = sensorThumbnailResource;
        this.messageResource = messageResource;
        setActive(isActive);
    }

    int getMessageResource() { return messageResource; }

    private String getEventName() {
        return eventName;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public String getSensorName(){
        return sensorType.name();
    }

    boolean isSensorActive() {
        return active;
    }

    private void setActive(boolean active) {
        Log.d("AppSensorEvent", "Your device has a " + getSensorName() + " sensor. " + (NoEvent.class.getSimpleName().equals(getEventName()) ? "There is no event related to it." : "Its event will be triggered"));
        this.active = active;
    }

    int getImageResource() {
        return imageResource;
    }

    int getEventThumbnailResource() {
        return eventThumbnailResource;
    }

    int getSensorThumbnailResource() {
        return sensorThumbnailResource;
    }

    public void setSensorThumbnailResource(int sensorThumbnailResource) {
        this.sensorThumbnailResource = sensorThumbnailResource;
    }

    boolean hasEvent(){
        return NoEvent.class.getSimpleName().equals(getEventName());
    }
}
