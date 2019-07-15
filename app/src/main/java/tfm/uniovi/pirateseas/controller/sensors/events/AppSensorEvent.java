package tfm.uniovi.pirateseas.controller.sensors.events;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

public class AppSensorEvent {

    private String eventName;
    private SensorType sensorType;
    private boolean active;
    private int imageResource;
    private int thumbnailResource;
    private int messageResource;

    public AppSensorEvent(String name, SensorType sensorType, int imageResource, int thumbnailResource, int messageResource){
        this.eventName = name;
        this.sensorType = sensorType;
        this.imageResource = imageResource;
        this.thumbnailResource = thumbnailResource;
        this.messageResource = messageResource;
        setActive(false);
    }

    public AppSensorEvent(AppSensorEvent event) {
        this.eventName = event.getEventName();
        this.sensorType = event.getSensorType();
        this.imageResource = event.getImageResource();
        this.thumbnailResource = event.getThumbnailResource();
        this.messageResource = event.getMessageResource();
        setActive(event.isActive());
    }

    public int getMessageResource() { return messageResource; }

    public String getEventName() {
        return eventName;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public String getSensorName(){
        return sensorType.name();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getThumbnailResource() {
        return thumbnailResource;
    }
}
