package tfm.uniovi.pirateseas.utils.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ammunitions;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.ShipType;

/**
 * Class to manage the app preferences save and load operations
 * 
 * @see: https://developers.google.com/games/services/checklist#1_sign-in
 *
 */
public class GameHelper {
	
	public static Player helperPlayer;
	public static Ship helperShip;
	public static Map helperMap;
	
	private static final int DEFAULT_DIRECTION = 90;
	private static final int DEFAULT_SHIP_WIDTH = 2;
	private static final int DEFAULT_SHIP_HEIGHT = 3;
	private static final int DEFAULT_SHIP_LENGTH = 5;

	/**
	 * Method to save the game at preferences
	 * @param context context to know which activity is calling the method
	 * @param player player
	 * @param ship ship
	 * @param map map
	 * @return true if data got saved on preferences, false otherwise
	 */
	public static boolean saveGameAtPreferences(Context context, Player player, Ship ship, Map map){
		
		boolean res;
		
		SharedPreferences mPreferences = context.getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(Constants.PREF_PLAYER_LEVEL, player.getLevel());
		editor.putInt(Constants.PREF_PLAYER_GOLD, player.getGold());
		editor.putInt(Constants.PREF_PLAYER_XP, player.getExperience());
		editor.putInt(Constants.PREF_PLAYER_MAP_PIECES, player.getMapPieces());
		editor.putInt(Constants.PREF_SHIP_COORDINATES_X, ship.getCoordinates().x);
		editor.putInt(Constants.PREF_SHIP_COORDINATES_Y, ship.getCoordinates().y);
		for(Ammunitions a : Ammunitions.values()){
			editor.putInt(a.getName(), ship.getAmmunition(a));
		}
		editor.putInt(Constants.PREF_SHIP_HEALTH, ship.getHealth());
		editor.putInt(Constants.PREF_SHIP_TYPE, ship.getShipType().ordinal());

		editor.putLong(Constants.PREF_MAP_SEED, map.getMapSeed());
		editor.putInt(Constants.PREF_MAP_ACTIVECELL, map.getActiveCell());
		String[] mapContent = map.getMapContent();
		StringBuilder mapJointContent = new StringBuilder();

		for(String s : mapContent){
			mapJointContent.append(s);
			mapJointContent.append(";");
		}
		editor.putString(Constants.PREF_MAP_CONTENT, mapJointContent.toString());
		editor.putInt(Constants.PREF_MAP_HEIGHT, map.getMapHeight());
		editor.putInt(Constants.PREF_MAP_WIDTH, map.getMapWidth());
		
		res = editor.commit();
		
		return res;
	}


	/**
	 * Method to load the game at preferences
	 * @param context context to know which activity is calling the method
	 * @param player player
	 * @param ship ship
	 * @param map map
	 */
	public static void loadGameAtPreferences(Context context, Player player, Ship ship, Map map){
		
		SharedPreferences mPreferences = context.getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);

		player.setGold(mPreferences.getInt(Constants.PREF_PLAYER_GOLD, 0));
		player.setExperience(mPreferences.getInt(Constants.PREF_PLAYER_XP, 0));
		player.setMapPieces(mPreferences.getInt(Constants.PREF_PLAYER_MAP_PIECES, 0));
		
		helperPlayer = player;
		
		Point p = new Point(mPreferences.getInt(Constants.PREF_SHIP_COORDINATES_X, 0), mPreferences.getInt(Constants.PREF_SHIP_COORDINATES_Y, 0));
		int ammo = mPreferences.getInt(Constants.PREF_SHIP_AMMUNITIONS, 20);
		ShipType st = ShipType.values()[mPreferences.getInt(Constants.PREF_SHIP_TYPE, 0)];
		int hp = mPreferences.getInt(Constants.PREF_SHIP_HEALTH, st.defaultHealthPoints());
		ship = new Ship(context, ship, st, p, DEFAULT_DIRECTION, DEFAULT_SHIP_LENGTH, hp, ammo);
		ship.setPlayable(true);
			
		helperShip = ship;

		map.setMapSeed(mPreferences.getLong(Constants.PREF_MAP_SEED, 0));
		map.setActiveCell(mPreferences.getInt(Constants.PREF_MAP_ACTIVECELL, 0));
		int mapHeight = mPreferences.getInt(Constants.PREF_MAP_HEIGHT,Constants.MAP_MIN_HEIGHT);
		int mapWidth = mPreferences.getInt(Constants.PREF_MAP_WIDTH,Constants.MAP_MIN_WIDTH);
		map.setMapHeight(mapHeight);
		map.setMapWidth(mapWidth);
		map.setMapLength(mapHeight, mapWidth);
		map.setMapContent(mPreferences.getString(Constants.PREF_MAP_CONTENT,Constants.EMPTY_STRING).split(";"));

		helperMap = map;
	}

}
