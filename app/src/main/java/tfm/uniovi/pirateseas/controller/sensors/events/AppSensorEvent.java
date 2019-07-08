package tfm.uniovi.pirateseas.controller.sensors.events;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

public class AppSensorEvent {

    private String eventName;
    private SensorType sensorType;
    private boolean active;
    private int imageResource;
    private int thumbnailResource;

    public AppSensorEvent(String name, SensorType sensorType, int imageResource, int thumbnailResource){
        this.eventName = name;
        this.sensorType = sensorType;
        this.imageResource = imageResource;
        this.thumbnailResource = thumbnailResource;
        setActive(false);
    }

    public AppSensorEvent(AppSensorEvent event) {
        this.eventName = event.getEventName();
        this.sensorType = event.getSensorType();
        this.imageResource = event.getImageResource();
        this.thumbnailResource = event.getThumbnailResource();
        setActive(event.isActive());
    }

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
