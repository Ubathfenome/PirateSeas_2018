package tfm.uniovi.pirateseas.controller.androidGameAPI;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Random;

import tfm.uniovi.pirateseas.global.Constants;

/**
 * Created by Miguel on 04/12/2017.
 */

public class Map implements Parcelable{
    private long mapSeed;
    private int activeMapCell;
    private int mapHeight;
    private int mapWidth;
    private int mapLength;
    private String[] mapContent;

    public Map(Date date, int height, int width){
        mapSeed = date.getTime();
        this.mapHeight = height;
        this.mapWidth = width;
        this.mapLength = mapHeight * mapWidth;
        mapContent = generateContent(mapSeed);
        activeMapCell = 0;
    }

    public Map (Parcel source){
        this.mapSeed = source.readLong();
        this.activeMapCell = source.readInt();
        this.mapHeight = source.readInt();
        this.mapWidth = source.readInt();
        this.mapLength = mapHeight * mapWidth;
        source.readStringArray(mapContent);
        // this.mapContent = source.createStringArray();
    }

    private String[] generateContent(long mapSeed) {
        String[] content = new String[mapLength];
        Random r = new Random();
        r.setSeed(mapSeed);
        for(int i = 0; i < mapLength; i++){
            // Randomize the type of the map cell (island = true / water = false)
            boolean type = r.nextBoolean();
            if (type){
                // Initial creation of the cell is meant to be shadowed (0 = clear / 1 = shadow)
                content[i] = "I1";
            } else {
                content[i] = "W1";
            }
        }

        return content;
    }

    public void clearActiveMapCell(){
        String activeCell = mapContent[activeMapCell];
        char activeCellType = activeCell.charAt(0);
        mapContent[activeMapCell] = activeCellType + "0";
    }

    public void obscureActiveMapCell(){
        String activeCell = mapContent[activeMapCell];
        char activeCellType = activeCell.charAt(0);
        mapContent[activeMapCell] = activeCellType + "1";
    }

    public void setMapSeed(long seed) { this.mapSeed = seed; }

    public void setActiveCell(int index){
        this.activeMapCell = index;
    }

    public int getActiveCell(){
        return this.activeMapCell;
    }

    public boolean isActiveCellIsland(){
        return mapContent[activeMapCell].charAt(0) == 'I';
    }

    public boolean isActiveCellCleared(){
        return mapContent[activeMapCell].charAt(1) == '0';
    }

    public long getMapSeed(){
        return this.mapSeed;
    }

    public void setMapLength(int height, int width) {
        setMapHeight(height);
        setMapWidth(width);
        this.mapLength = height * width;
    }

    public int getMapLength(){ return this.mapLength; }

    public void setMapHeight(int height) { this.mapHeight = height; }

    public int getMapHeight() { return this.mapHeight; }

    public void setMapWidth(int width) { this.mapWidth = width; }

    public int getMapWidth() { return this.mapWidth; }

    public String[] getMapContent(){
        return this.mapContent;
    }

    public void setMapContent(String[] content){
        if(content.length == Constants.MAP_LENGTH)
            this.mapContent = content;
        else
            throw new InvalidParameterException("Map content length mismatch");
    }

    public boolean isAllClear(){
        for(int i = 0; i < Constants.MAP_LENGTH; i++){
            String cell = mapContent[i];
            if (cell.endsWith("1")){
                return false;
            }
        }
        return true;
    }

    /**
     * Ignore this method
     */
    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeLong(this.mapSeed);
        out.writeInt(this.activeMapCell);
        out.writeInt(this.mapHeight);
        out.writeInt(this.mapWidth);
        out.writeStringArray(this.mapContent);
    }

    /**
     * This is used to regenerate your object. All Parcelables must have a
     * CREATOR that implements these two methods
     */
    public static final Parcelable.Creator<Map> CREATOR = new Parcelable.Creator<Map>() {

        @Override
        public Map createFromParcel(Parcel source) {
            return new Map(source);
        }

        @Override
        public Map[] newArray(int size) {
            return new Map[size];
        }
    };
}
