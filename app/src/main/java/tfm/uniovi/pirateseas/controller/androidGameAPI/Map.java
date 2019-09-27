package tfm.uniovi.pirateseas.controller.androidGameAPI;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Random;

import tfm.uniovi.pirateseas.global.Constants;

/**
 * Class that represents the map where the player will be navigating.
 * The size of the map is adapted to fit within the device's screen.
 * Map contents is auto generated the first time or when the player unlocks all the cells of a previous map.
 * Map cells are hidden at the creation and will unlock upon visit of the player.
 *
 * Created by UO179050 on 04/12/2017.
 */

public class Map implements Parcelable{
    private long mapSeed;
    private int activeMapCell;
    private int lastActiveMapCell;
    private int mapHeight;
    private int mapWidth;
    private int mapLength;
    private String[] mapContent;

    /**
     * Map class constructor
     * @param date Actual system date. Received to generate the Random seed
     * @param height Number of vertical cells that fit correctly on the device
     * @param width Number of horizontal cell that fit correctly on the screen
     */
    public Map(Date date, int height, int width){
        mapSeed = date.getTime();
        this.mapHeight = height;
        this.mapWidth = width;
        this.mapLength = mapHeight * mapWidth;
        mapContent = generateContent(mapSeed);
        activeMapCell = 0;
        lastActiveMapCell = 0;
    }

    /**
     * Map class constructor for Parcelable content
     * @param source Source parcel
     */
    public Map (Parcel source){
        this.mapSeed = source.readLong();
        this.activeMapCell = source.readInt();
        this.lastActiveMapCell = source.readInt();
        this.mapHeight = source.readInt();
        this.mapWidth = source.readInt();
        this.mapLength = mapHeight * mapWidth;
        this.mapContent = source.createStringArray();
        // source.readStringArray(mapContent);
    }

    /**
     * Map random content generator
     * @param mapSeed Map seed number received to feed the Random object
     * @return String array with the content of each cell of the map
     */
    private String[] generateContent(long mapSeed) {
        String[] content = new String[mapLength];
        Random r = new Random();
        r.setSeed(mapSeed);
        for(int i = 0; i < mapLength; i++){
            // Randomize the type of the map cell (island = true / water = false)
            int randomValue = r.nextInt(100);
            if (randomValue >= Constants.ISLAND_SPAWN_RATE){
                // Initial creation of the cell is meant to be shadowed (0 = clear / 1 = shadow)
                content[i] = "I1";
            } else {
                content[i] = "W1";
            }
        }

        return content;
    }

    /**
     * Unlocks the active cell
     */
    public void clearActiveMapCell(){
        String activeCell = mapContent[activeMapCell];
        char activeCellType = activeCell.charAt(0);
        mapContent[activeMapCell] = activeCellType + "0";
    }

    /**
     * Hides the content of the active cell
     */
    public void obscureActiveMapCell(){
        String activeCell = mapContent[activeMapCell];
        char activeCellType = activeCell.charAt(0);
        mapContent[activeMapCell] = activeCellType + "1";
    }

    /**
     * Unlocks the content of the cell 'index'
     * @param index Cell to unlock
     */
    public void clearMapCell(int index){
        String cell = mapContent[index];
        char cellType = cell.charAt(0);
        mapContent[index] = cellType + "0";
    }

    /**
     * Hides the content of the cell 'index'
     * @param index Cell to hide
     */
    public void obscureMapCell(int index){
        String cell = mapContent[index];
        char cellType = cell.charAt(0);
        mapContent[index] = cellType + "1";
    }

    /**
     * Returns the index of the next hidden island cell
     * @return Map cell index
     */
    public int getIsland(){
        for(int i = 0; i < mapLength; i++){
            String cell = mapContent[i];
            if(cell.contains("I")){
                if(cell.contains("1")){
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Set the map seed
     * @param seed New map seed
     */
    public void setMapSeed(long seed) { this.mapSeed = seed; }

    /**
     * Set the active cell to 'index'
     * @param index New active cell index
     */
    public void setActiveCell(int index){
        this.activeMapCell = index;
    }

    /**
     * Return the active cell index
     * @return Active cell index
     */
    public int getActiveCell(){
        return this.activeMapCell;
    }

    /**
     * Set the last active cell to 'index'
     * @param index New last active cell index
     */
    public void setLastActiveCell(int index){
        this.lastActiveMapCell = index;
    }

    /**
     * Return the last active cell index
     * @return Last active cell index
     */
    public int getLastActiveCell(){
        return this.lastActiveMapCell;
    }

    /**
     * Determine if the active cell is an island
     * @return IsIsland = true; IsWater = false;
     */
    public boolean isActiveCellIsland(){
        return mapContent[activeMapCell].charAt(0) == 'I';
    }

    /**
     * Determine if the active cell is cleared
     * @return IsCleared = true; IsHidden = false;
     */
    public boolean isActiveCellCleared(){
        return mapContent[activeMapCell].charAt(1) == '0';
    }

    /**
     * Return map seed
     * @return Map seed
     */
    public long getMapSeed(){
        return this.mapSeed;
    }

    /**
     * Set new map length (height * width)
     * @param height New map height
     * @param width New map width
     */
    public void setMapLength(int height, int width) {
        setMapHeight(height);
        setMapWidth(width);
        this.mapLength = height * width;
    }

    /**
     * Return Map length
     * @return Map lenght
     */
    public int getMapLength(){ return this.mapLength; }

    /**
     * Set map Height
     * @param height New map height
     */
    public void setMapHeight(int height) { this.mapHeight = height; }

    /**
     * Return map height
     * @return Map height
     */
    public int getMapHeight() { return this.mapHeight; }

    /**
     * Set new map width
     * @param width New map width
     */
    public void setMapWidth(int width) { this.mapWidth = width; }

    /**
     * Return Map width
     * @return Map width
     */
    public int getMapWidth() { return this.mapWidth; }

    /**
     * Return Map content
     * @return String array with Map content
     */
    public String[] getMapContent(){
        return this.mapContent;
    }

    /**
     * Set new Map content
     * @param content New map content
     */
    public void setMapContent(String[] content){
        if(content.length == mapLength)
            this.mapContent = content;
        else
            throw new InvalidParameterException("Map content length mismatch");
    }

    /**
     * Return the number of cleared cells of the map
     * @return Number of cleared cells
     */
    public int getClearedCells(){
        int clearedCells = 0;
        for(int i = 0; i < mapLength; i++){
            String cell = mapContent[i];
            if(cell.endsWith("1")){
                clearedCells++;
            }
        }

        return clearedCells;
    }

    /**
     * Determine if the whole map has been cleared
     * @return IsAllClear = true; RemainsHiddenCells = false
     */
    public boolean isAllClear(){
        for(int i = 0; i < mapLength; i++){
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

    /**
     * Save Map content into a Parcel
     * @param out Parcel to be filled
     * @param flags Parcel flags
     */
    public void writeToParcel(Parcel out, int flags){
        out.writeLong(this.mapSeed);
        out.writeInt(this.activeMapCell);
        out.writeInt(this.lastActiveMapCell);
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
