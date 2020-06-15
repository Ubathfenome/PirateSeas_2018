package tfm.uniovi.pirateseas.global;

import android.content.Context;

/**
 * Class to set all project's global variables
 */
public class Constants{

    // Modes
	private static final int MODE_DEBUG = 0x0;
	@SuppressWarnings("unused")
	private static final int MODE_RELEASE = 0x1;

	// Set on release
	public static final int MODE = MODE_DEBUG;

	public static final int GAMEMODE_ADVANCE = 0x1;
	public static final int GAMEMODE_BATTLE = 0x2;
	public static final int GAMEMODE_IDLE = 0x3;

	public static final String APP_VERSION = "app_version";
	
	// Tags
	public static final String TAG_EXE_MODE = "tfm.uniovi.pirateseas.EXE_MODE";
	public static final String TAG_GAMES_NUMBER = "tfm.uniovi.pirateseas.GAMES_NUMBER";
	public static final String TAG_SENSOR_LIST = "tfm.uniovi.pirateseas.SENSOR_LIST";
	public static final String TAG_SENSOR_EVENTS = "tfm.uniovi.pirateseas.SENSOR_EVENTS";
	public static final String TAG_LOAD_GAME = "tfm.uniovi.pirateseas.LOAD_GAME";
	public static final String TAG_GAME_OVER = "tfm.uniovi.pirateseas.GAME_OVER";
	public static final String TAG_GAME_OVER_PLAYER = "GameOverPlayer";
	public static final String TAG_GAME_OVER_MAP = "GameOverMap";
	public static final String TAG_PREF_NAME = "tfm.uniovi.pirateseas.PREFERENCES";
	public static final String TAG_SCREEN_SELECTION_MAP_HEIGHT = "tfm.uniovi.pirateseas.SCREEN_SELECTION_MAP_HEIGHT";
	public static final String TAG_SCREEN_SELECTION_MAP_WIDTH = "tfm.uniovi.pirateseas.SCREEN_SELECTION_MAP_WIDTH";

	public static final int REQUEST_PERMISSIONS = 0x01;
	
	// Entities
	public static final int STATE_ALIVE = 0;
	public static final int STATE_DEAD = 1;

    public static final int DIRECTION_RIGHT = 0;
    public static final int DIRECTION_UP = 90;
    public static final int DIRECTION_LEFT = 180;
    public static final int DIRECTION_DOWN = 270;

	public static final int CHT_VALUE = 20;
	public static final int MAELSTORM_DAMAGE = 15;

	public static final int DEFAULT_ENEMY_SHIP_DIRECTION = DIRECTION_LEFT;
	public static final int DEFAULT_ENEMY_Y_LIMIT = 60;

	public static final int DEFAULT_PLAYER_SHIP_DIRECTION = DIRECTION_UP;
	public static final int DEFAULT_PLAYER_SHIP_AMMO = 20;
	public static final int DEFAULT_SHIP_LENGTH = 5;
	
	public static final int DEFAULT_SHIP_RELOAD = 4;
	public static final int DEFAULT_SHIP_BASIC_RANGE = 8;
	public static final float DEFAULT_SHOOT_DAMAGE = 10;

	public static final int SHIP_MAX_POWER = 500;
	public static final int SHIP_MAX_RANGE = 30;

	public static final int MAP_PIECES_LIMIT = 6;

	public static final int SHOT_AMMO_UNLIMITED = -1;
	public static final int SHOT_FIRED = 0;
	public static final int SHOT_FLYING = 1;
	public static final int SHOT_HIT = 2;
	public static final int SHOT_MISSED = 3;
	
	// Bar Types
	public static final int BAR_HEALTH = 0;
	public static final int BAR_EXPERIENCE = 1;
	
	// Global variables	
	public static final int GAME_FPS = 60;				// FPS = Frames-Per-Second
	public static final int GAME_MPIGD = 10;			// MPIGD = Minutes-Per-In-Game-Day
	public static final int ISLAND_SPAWN_RATE = 80;

	public static final int MAP_MIN_HEIGHT = 1;
	public static final int MAP_MIN_WIDTH = 1;
	public static final int MAP_MIN_LENGTH = MAP_MIN_WIDTH * MAP_MIN_HEIGHT;
	
	public static final int GAME_STATE_NORMAL = 0;
	public static final int GAME_STATE_PAUSE = 1;
	public static final int GAME_STATE_END = 2;

	private static final int SECONDS_PER_IN_GAME_HOUR = 60;
	public static final int HOURS_PER_DAY = GAME_MPIGD * SECONDS_PER_IN_GAME_HOUR;
	private static final int MILLIS_TO_SECONDS = 1000;
	public static final double MILLIS_TO_SECONDS_INV = Math.pow(MILLIS_TO_SECONDS, -1); 
	public static final double NANOS_TO_SECONDS = Math.pow(10, -9);

	public static final int SHOT_SPEED = 15;
	public static final long GRACE_PERIOD = 1500;

    public static final int SHAKE_LIMIT = 2;
    public static final int TUTORIAL_NUM_PAGES = 11;

    public static final int MAX_ENTITY_WIDTH = 15;
    public static final int MAX_ENTITY_HEIGHT = 15;

	public static final String ARG_GOLD = "argumentGold";
	public static final String ARG_XP = "argumentXp";
	public static final String ARG_MAP_PIECE = "argumentMapPiece";

	public static final String EMPTY_STRING = "";
	public static final String LIST_SEPARATOR = ";";

	public static final String PREF_PLAYER_TIMESTAMP = "playerTimestampPref";
	public static final String PREF_PLAYER_LEVEL = "playerLevelPref";
	public static final String PREF_PLAYER_GOLD = "playerGoldPref";
	public static final String PREF_PLAYER_XP = "playerExperiencePref";
	public static final String PREF_PLAYER_MAP_PIECES = "playerMapPiecesPref";
	public static final String PREF_SHIP_COORDINATES_X = "shipCoordinatesXPref";
	public static final String PREF_SHIP_COORDINATES_Y = "shipCoordinatesYPref";
	public static final String PREF_SHIP_HEALTH = "shipHealthPref";
	public static final String PREF_SHIP_TYPE = "shipTypePref";
	public static final String PREF_MAP_SEED = "mapSeed";
	public static final String PREF_MAP_ACTIVECELL = "mapActiveCell";
    public static final String PREF_MAP_LASTACTIVECELL = "mapLastActiveCell";
	public static final String PREF_MAP_CONTENT = "mapContent";
	public static final String PREF_MAP_HEIGHT = "mapHeight";
	public static final String PREF_MAP_WIDTH = "mapWidth";
	public static final String PREF_TUTORIAL_ALREADY_SHOWN = "tutorialAlreadyShown";

	public static final String PREF_DEVICE_HEIGHT_RES = "deviceHeightPref";
	public static final String PREF_DEVICE_WIDTH_RES = "deviceWidthPref";

	// Definir constantes para las nuevas preferencias
	public static final String PREF_SENSOR_LIST = "settings_sensor_list_preference";
	public static final String PREF_SHIP_CONTROL_MODE = "settings_ship_preference";
	public static final String PREF_AMMO_CONTROL_MODE = "settings_ammunition_preference";
	public static final String PREF_SHOOT_CONTROL_MODE = "settings_shoot_preference";
	public static final boolean PREF_IS_ACTIVE = true;
	public static final String PREF_VOLUME_VALUE = "settings_volume_preference";
	public static final String PREF_SENSORS_EVENTS = "settings_sensors_events_preference";
	public static final String PREF_WIPE_MEMORY = "settings_wipe_memory_preference";

	public static final String FONT_NAME = "Skullsandcrossbones-RppKM";
	
	public static final String FRONT = "Front";
	public static final String BACK = "Back";
	public static final String RIGHT = "Right";
	public static final String LEFT = "Left";
	
	public static final String ITEMLIST_NATURE = "Nature";
	public static final String NATURE_SHOP = "Shop";
	public static final String NATURE_TREASURE = "Treasure";

	public static final String ITEM_KEY_CREW = "Crew";
	public static final String ITEM_KEY_REPAIRMAN = "Repairman";
	public static final String ITEM_KEY_NEST = "Nest";
	public static final String ITEM_KEY_MATERIALS = "Materials";
	public static final String ITEM_KEY_MAPPIECE = "MapPiece";
	public static final String ITEM_KEY_MAP = "Map";
	public static final String ITEM_KEY_BLACKPOWDER = "BlackPowder";
	public static final String ITEM_KEY_VALUABLE = "Valuable";
	public static final String ITEM_KEY_AMMO_SIMPLE = "Ammo simple";
	public static final String ITEM_KEY_AMMO_AIMED = "Ammo aimed";
	public static final String ITEM_KEY_AMMO_DOUBLE = "Ammo double";
	public static final String ITEM_KEY_AMMO_SWEEP = "Ammo sweep";
    public static final String PAUSE_SHIP = "PAUSESHIP";
	public static final String PAUSE_PLAYER = "PAUSEPLAYER";
	public static final String PAUSE_MAP = "PAUSEMAP";

	public static final int ZERO_INT = 0;

	public static final String SHIP_ENTITY = "SHIP";
	public static final String SHIP_HEALTH = "Health";
	public static final String SHIP_AMMO = "Ammunition";
	public static final String SHIP_RANGE = "Range";
	public static final String SHIP_MAX_HEALTH = "MaxHealth";
	public static final String PLAYER_ENTITY = "PLAYER";
	public static final String PLAYER_MAP_PIECE = "MapPieces";
	public static final String SHIP_POWER = "Power";
	public static final String PLAYER_GOLD = "Gold";

	/**
	 * Method that evaluates if the game is in debugging mode or not
	 * @param mMode Execution mode
	 * @return true if the game is in debug mode, false otherwise
	 */
	public static boolean isInDebugMode(int mMode) {
		return mMode == Constants.MODE_DEBUG;
	}

	/**
	 * get String from resource
	 * @param content Activity that calls the method
	 * @param resource resource index
	 * @return resource string
	 */
	public static String getString(Context content, int resource) {	return content.getResources().getString(resource); }
}