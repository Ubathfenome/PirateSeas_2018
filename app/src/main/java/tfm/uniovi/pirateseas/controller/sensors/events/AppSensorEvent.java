package tfm.uniovi.pirateseas.controller.sensors.events;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;
import tfm.uniovi.pirateseas.utils.persistence.EnumHelper;

public class AppSensorEvent implements Parcelable {

    private final String eventName;
    private final SensorType sensorType;
    private boolean available;
    private boolean active;
    private final int imageResource;
    private final int eventThumbnailResource;
    private int sensorThumbnailResource;
    private final int messageResource;

    AppSensorEvent(String name, SensorType sensorType, int imageResource, int eventThumbnailResource, int sensorThumbnailResource, int messageResource, boolean isAvailable, boolean isActive){
        this.eventName = name;
        this.sensorType = sensorType;
        this.imageResource = imageResource;
        this.eventThumbnailResource = eventThumbnailResource;
        this.sensorThumbnailResource = sensorThumbnailResource;
        this.messageResource = messageResource;
        setAvailable(isAvailable);
        setActive(isActive);
    }

    private AppSensorEvent(Parcel in) {
        eventName = in.readString();
        sensorType = SensorType.values()[in.readInt()];
        available = in.readByte() != 0;
        active = in.readByte() != 0;
        imageResource = in.readInt();
        eventThumbnailResource = in.readInt();
        sensorThumbnailResource = in.readInt();
        messageResource = in.readInt();
    }

    public static final Creator<AppSensorEvent> CREATOR = new Creator<AppSensorEvent>() {
        @Override
        public AppSensorEvent createFromParcel(Parcel in) {
            return new AppSensorEvent(in);
        }

        @Override
        public AppSensorEvent[] newArray(int size) {
            return new AppSensorEvent[size];
        }
    };

    int getMessageResource() { return messageResource; }

    private String getEventName() {
        return eventName;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    int getSensorName(){
        return sensorType.getName();
    }

    public boolean isSensorAvailable() {
        return available;
    }

    private void setAvailable(boolean available) {
        Log.d("AppSensorEvent", "Your device has a " + getSensorName() + " sensor. " + (NoEvent.class.getSimpleName().equals(getEventName()) ? "There is no event related to it." : "Its event will be triggered"));
        this.available = available;
    }

    private void setActive(boolean isActive) {
        this.active = isActive;
    }

    public boolean isSensorActive() {
        return this.active;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(eventName);
        EnumHelper<SensorType> ph = new EnumHelper<>();
        parcel.writeInt(ph.getEnumIndex(sensorType));
        parcel.writeByte((byte) (available ? 1 : 0));
        parcel.writeByte((byte) (active ? 1 : 0));
        parcel.writeInt(imageResource);
        parcel.writeInt(eventThumbnailResource);
        parcel.writeInt(sensorThumbnailResource);
        parcel.writeInt(messageResource);
    }
}
