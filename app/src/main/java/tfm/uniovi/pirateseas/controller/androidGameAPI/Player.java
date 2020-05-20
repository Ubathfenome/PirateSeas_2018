package tfm.uniovi.pirateseas.controller.androidGameAPI;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.exceptions.NotEnoughGoldException;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Class that represents the player stats. Holds the value of the gold the player won, its experience points among its experience level and the map pieces found
 * @author p7166421
 *
 * @see: http://developer.android.com/distribute/stories/games.html
 * @see: https://developers.google.com/games/services/common/concepts/savedgames
 * @see: 
 *       http://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one
 *       -android-activity-to-another-using-intents
 * 
 */
public class Player implements Parcelable {
	private static final int INVALID_VALUE = -1;
	private static final int[] LOG_BASES = {0, 400, 1900, 4500, 8200, 13000, 18900, 24900};

	private int level;
	private int gold;
	private int experience;
	private int mapPieces;
	
	private boolean hasCompleteMap;

	/**
	 * Dummy constructor for the Player class.
	 * Preloads the Player object
	 */
	public Player() {
		this.level = 0;
		this.gold = 10;
		this.experience = 0;
		this.mapPieces = 0;
		this.hasCompleteMap = false;
	}

	/**
	 * Constructor of the Player class
	 * @param level XP level
	 * @param gold Gold won by the player
	 * @param xp XP points
	 * @param mapPieces Map pieces found
	 * @param map Boolean hasCompleteMap
	 */
	public Player(int level, int gold, int xp, int mapPieces,
			boolean map) {
		this.level = level;
		this.gold = gold;
		this.experience = xp;
		this.mapPieces = mapPieces;
		this.hasCompleteMap = map;
	}

	/**
	 * Constructor for the Parcel object
	 * @param source Source parcel
	 */
	public Player(Parcel source) {
		this.level = source.readInt();
		this.gold = source.readInt();
		this.experience = source.readInt();
		this.mapPieces = source.readInt();
		this.hasCompleteMap = source.readInt() == 1;
	}

	/**
	 * Copy Player method
	 * @param origin Origin Player
	 * @return Copied Player
	 */
	public static Player clonePlayer(Player origin) {
		return new Player(origin.getLevel(), origin.getGold(),
				origin.getExperience(),
				origin.getMapPieces(), origin.hasCompleteMap());
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level
	 * 				the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the gold
	 */
	public int getGold() {
		return gold;
	}

	/**
	 * @param gold
	 *            the gold to set
	 */
	public void setGold(int gold) {
		this.gold = gold;
	}

	/**
	 * Add gold to the Player previous amount
	 * @param gold
	 */
	public void addGold(int gold) {
		this.gold += gold > 0 ? gold : 0;
	}

	/**
	 * Spend gold on an item
	 * @param context Activity that calls the method
	 * @param gold	Gold to spend
	 * @throws NotEnoughGoldException Exception if the player does not have enough gold to purchase the item
	 */
	public void useGold(Context context, int gold)
			throws NotEnoughGoldException {
		if (this.gold < gold)
			throw new NotEnoughGoldException(context.getResources().getString(
					R.string.exception_gold));
		else
			this.gold -= gold;
	}

	/**
	 * @return the experience
	 */
	public int getExperience() {
		return experience;
	}

	/**
	 * Return XP points required to get the next XP level
	 * @return XP points
	 */
	public int getNextLevelThreshold(){
		return LOG_BASES[level + 1];
	}

	/**
	 * @param experience
	 *            the experience to set
	 */
	public void setExperience(int experience) {
		this.experience = 0;
		addExperience(experience);
	}

	/**
	 * Add XP points to the Player XP pool
	 * @param experience XP Points
	 */
	public void addExperience(int experience) {		
		this.experience += experience > 0 ? experience : 0;
		this.level = INVALID_VALUE;
		for(int i = 0, length = LOG_BASES.length; i < length; i++){
			if(this.experience >= LOG_BASES[i])
				this.level = i;
		}
		if(this.level == INVALID_VALUE)
			this.level = LOG_BASES.length - 1;
	}

	/**
	 * Return number of map pieces
	 * @return Map pieces
	 */
	public int getMapPieces() {
		return mapPieces;
	}

	/**
	 * Set the number of map pieces held by the player
	 * @param mapPieces Number of map pieces held by the player
	 */
	public void setMapPieces(int mapPieces) {
		if (mapPieces < 0) {
			this.mapPieces = 0;
		} else {
			if (mapPieces > (2 * Constants.MAP_PIECES_LIMIT-1)) {
				mapPieces = (2 * Constants.MAP_PIECES_LIMIT-1);
			}
			if (mapPieces % Constants.MAP_PIECES_LIMIT == 0 && mapPieces != 0) {
				hasCompleteMap = true;
				mapPieces -= Constants.MAP_PIECES_LIMIT;
			}
			this.mapPieces = mapPieces;
		}
	}

	/**
	 * Add a new Map piece or sets a complete map for the player
	 */
	public void addMapPiece() {
		this.mapPieces++;
		if (mapPieces % Constants.MAP_PIECES_LIMIT == 0) {
			hasCompleteMap = true;
			mapPieces -= Constants.MAP_PIECES_LIMIT;
		}
	}

	/**
	 * Determine if the player has a complete map
	 * @return HasCompleteMap = true; MapPiecesRemains = false
	 */
	public boolean hasCompleteMap() {
		return hasCompleteMap;
	}

	/**
	 * Set if the player has a complete map
	 * @param hasCompleteMap
	 */
	public void giveCompleteMap(boolean hasCompleteMap) {
		this.hasCompleteMap = hasCompleteMap;
	}

	/**
	 * toString
	 * @return Player object as a readable string
	 */
	@Override
	public String toString() {
		return "Player [level=" + level + ", gold=" + gold + ", experience="
				+ experience + ", passedDays=" + 0 + ", mapPieces="
				+ mapPieces + "]";
	}

	/**
	 * Ignore this method
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Write your object's data to the passed-in Parcel
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(level);
		out.writeInt(gold);
		out.writeInt(experience);
		out.writeInt(mapPieces);
		out.writeInt(hasCompleteMap ? 1 : 0);
	}

	/**
	 * This is used to regenerate your object. All Parcelables must have a
	 * CREATOR that implements these two methods
	 */
	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {

		@Override
		public Player createFromParcel(Parcel source) {
			return new Player(source);
		}

		@Override
		public Player[] newArray(int size) {
			return new Player[size];
		}
	};

	/**
	 * Spends the player's complete map
	 */
	public void spendMap() {
		if(hasCompleteMap)
			this.hasCompleteMap = false;
	}
}