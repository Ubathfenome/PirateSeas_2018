package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.ShipType;
import tfm.uniovi.pirateseas.utils.approach2d.DrawableHelper;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;

/**
 * Activity to show the player map and decide which is the new map cell that the player should visit
 */
public class ScreenSelectionActivity extends Activity {

	private static final String TAG = "ScreenSelectionActivity";

	private LinearLayout layoutBackground;

	private ImageButton btnLeft;
	private ImageButton btnUp;
	private ImageButton btnDown;
	private ImageButton btnRight;

	private TextView txtScreenSelectionLabel;

	private Player p = null;
	private Ship ship = null;
	private Map map = null;
	private Date date;

	private Drawable currentMapDrawable;
	int mapWidth;
	int mapHeight;
	int mapLength;
	int active;

	int[] sensorTypes = null;
	boolean loadGame;

	@SuppressWarnings("unused")
	private Context context;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_screen_selection_ui);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		this.context = this;

		Intent intent = getIntent();

		sensorTypes = intent.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		loadGame = intent.getBooleanExtra(Constants.TAG_LOAD_GAME, false);
		mapHeight = intent.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, Constants.MAP_MIN_HEIGHT);
		mapWidth = intent.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, Constants.MAP_MIN_WIDTH);

		date = new Date();
		GameHelper.loadGameAtPreferences(this,p = new Player(), ship = new Ship(), map = new Map(date, mapHeight, mapWidth));
		p = GameHelper.helperPlayer;
		ship = GameHelper.helperShip;
		map = GameHelper.helperMap;

		if(map.getMapLength() == Constants.MAP_MIN_LENGTH)
			map = null;

		layoutBackground = findViewById(R.id.layoutBackground);
		currentMapDrawable = getCurrentMap(date);
		layoutBackground.setBackground(currentMapDrawable);

		mapWidth = map.getMapWidth();
		mapHeight = map.getMapHeight();
		mapLength = map.getMapLength();
		active = map.getActiveCell();

		final boolean encounter = randomEncounter();

		btnLeft = findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's left cell content
				if(active%mapWidth != 0){
					map.setActiveCell(active-1);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()){
						MusicManager.getInstance().stopBackgroundMusic();
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else {
						map.clearActiveMapCell();
						if (!map.isActiveCellIsland()) {
							if (encounter) {
								// Game activity
								MusicManager.getInstance().stopBackgroundMusic();
								MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
								startBattleGame();
							} else {
								// ScreenSelection activity
								MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
								reloadSelection();
							}
						} else {
							// Shop activity
							MusicManager.getInstance().stopBackgroundMusic();
							MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
							enterRandomIsland();
						}
					}
				} else {
					wrongWayMessage();
				}
			}
		});

		btnUp = findViewById(R.id.btnFront);
		btnUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's up cell content
				if(active-mapWidth>=0){
					map.setActiveCell(active-mapWidth);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()){
						MusicManager.getInstance().stopBackgroundMusic();
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else {
						map.clearActiveMapCell();
						if (!map.isActiveCellIsland()) {
							if (encounter) {
								// Game activity
								MusicManager.getInstance().stopBackgroundMusic();
								MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
								startBattleGame();
							} else {
								// ScreenSelection activity
								MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
								reloadSelection();
							}
						} else {
							// Shop activity
							MusicManager.getInstance().stopBackgroundMusic();
							MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
							enterRandomIsland();
						}
					}
				} else {
					wrongWayMessage();
				}
			}
		});

		btnRight = findViewById(R.id.btnRight);
		btnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's right cell content
				if((active+1)%mapWidth != 0){
					map.setActiveCell(active+1);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()){
						MusicManager.getInstance().stopBackgroundMusic();
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else {
						map.clearActiveMapCell();
						if (!map.isActiveCellIsland()) {
							if (encounter) {
								// Game activity
								MusicManager.getInstance().stopBackgroundMusic();
								MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
								startBattleGame();
							} else {
								// ScreenSelection activity
								MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
								reloadSelection();
							}
						} else {
							// Shop activity
							MusicManager.getInstance().stopBackgroundMusic();
							MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
							enterRandomIsland();
						}
					}
				} else {
					wrongWayMessage();
				}
			}
		});

		btnDown = findViewById(R.id.btnDown);
		btnDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's down cell content
				if((active+mapWidth)<mapLength){
					map.setActiveCell(active+mapWidth);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()){
						MusicManager.getInstance().stopBackgroundMusic();
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else {
						map.clearActiveMapCell();
						if (!map.isActiveCellIsland()) {
							if (encounter) {
								// Game activity
								MusicManager.getInstance().stopBackgroundMusic();
								MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
								startBattleGame();
							} else {
								// ScreenSelection activity
								MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
								reloadSelection();
							}
						} else {
							// Shop activity
							MusicManager.getInstance().stopBackgroundMusic();
							MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
							enterRandomIsland();
						}
					}
				} else {
					wrongWayMessage();
				}
			}
		});

		txtScreenSelectionLabel = findViewById(R.id.txtScreenSelectionLabel);
		txtScreenSelectionLabel.setTypeface(customFont);

		if(map.isAllClear()){
			// Map completed! Clear map from preferences and start new map
			Toast.makeText(context, getString(R.string.message_mapcompleted), Toast.LENGTH_LONG).show();
			Map newMap = new Map(new Date(), Constants.MAP_MIN_HEIGHT, Constants.MAP_MIN_WIDTH);
			// Create new and better PlayerShip?
			ShipType st = ship.getShipType();
			ShipType newShipType = getBetterShipType(st);
			ship.updateShipType(newShipType);
			GameHelper.saveGameAtPreferences(context, p, ship, newMap);
		}
	}

	/**
	 * Return the next better ship type for the player's ship type
	 * @param st Original Ship Type
	 * @return Better ship type
	 */
	private ShipType getBetterShipType(ShipType st) {
		ShipType[] sTypes = ShipType.values();
		for(int i = 0; i < sTypes.length; i++){
			ShipType sType = sTypes[i];
			if(sType.name().equals(st.name()) && ((i + 1)<=(sTypes.length-1))){
				return sTypes[i+1];
			}
		}
		return st;
	}

	/**
	 * Display a wrong way message
	 */
	private void wrongWayMessage(){
		Toast.makeText(context, getString(R.string.message_wrongway),Toast.LENGTH_SHORT).show();
	}

	/**
	 * Launch the battle activity
	 */
	private void startBattleGame(){
		GameHelper.saveGameAtPreferences(this, p, ship, map);

		Intent newGameIntent = new Intent(context, GameActivity.class);
		newGameIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
		newGameIntent.putExtra(Constants.TAG_LOAD_GAME, loadGame);
		newGameIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, mapHeight);
		newGameIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, mapWidth);
		newGameIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Log.d(TAG,"Start GameActivity Intent");
		startActivity(newGameIntent);
		finish();
	}

	/**
	 * Relaunch the select screen activity
	 */
	private void reloadSelection(){
		Toast.makeText(this, getResources().getString(R.string.message_nothinghere), Toast.LENGTH_SHORT).show();
		GameHelper.saveGameAtPreferences(this, p, ship, map);

		Intent resetIntent = new Intent(this, ScreenSelectionActivity.class);
		resetIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
		resetIntent.putExtra(Constants.TAG_LOAD_GAME, loadGame);
		resetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Log.d(TAG,"Reset ScreenSelection Intent");
		startActivity(resetIntent);
		finish();
	}

	/**
	 * Save the game when going into an already visited island
	 */
	private void enterVisitedIsland() {
		GameHelper.saveGameAtPreferences(this, p, ship, map);
		// Notify that the island has already been visited and cannot be visited twice
        txtScreenSelectionLabel.setText(getText(R.string.message_islandvisited) + "\n" + txtScreenSelectionLabel.getText());
	}

	/**
	 * Launch a random island Activity
	 */
	private void enterRandomIsland() {
		GameHelper.saveGameAtPreferences(this, p, ship, map);
		Random rand = new Random();
		boolean yesNo = rand.nextBoolean();
		Intent shopIntent = new Intent(this, ShopActivity.class);
		shopIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
		shopIntent.putExtra(Constants.TAG_LOAD_GAME, loadGame);
		shopIntent.putExtra(Constants.ITEMLIST_NATURE, yesNo ? Constants.NATURE_SHOP : Constants.NATURE_TREASURE);
		shopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Log.d(TAG,"Start Shop Intent");
		startActivity(shopIntent);
		finish();
	}

	/**
	 * Returns the current map drawable
	 * @param date Active date (used for its seed)
	 * @return Complete drawable for the map
	 */
	private Drawable getCurrentMap(Date date) {
		Bitmap bmpCover = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_cover);
		Bitmap bmpIsland = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_island);
		Bitmap bmpWater = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_water);
		Bitmap bmpActive = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_active);

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int screenHeight = displayMetrics.heightPixels;
		int screenWidth = displayMetrics.widthPixels;
		Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

		int fragmentHeight = bmpCover.getHeight();
		int fragmentWidth = bmpCover.getWidth();

		int mapHeight = screenHeight / fragmentHeight;
		int mapWidth = screenWidth / fragmentWidth;

		if(map == null) {
			map = new Map(date, mapHeight, mapWidth);
			map.clearActiveMapCell();
		}

		int active = map.getActiveCell();

		String[] mapContent = map.getMapContent();
		int mapLength = mapContent.length;
		Bitmap[] bmpContent = new Bitmap[mapLength];

		for(int i = 0; i < mapLength; i++){
			String s = mapContent[i];
			if(s.contains("1")){	// Fog image
				bmpContent[i] = bmpCover;
			} else if (s.contains("I")){	// Island image
				bmpContent[i] = bmpIsland;
			} else {	// Water image
				bmpContent[i] = bmpWater;
			}
			if(active==i){		// Overlap edge image to easier identify of active cell
				bmpContent[i] = DrawableHelper.overlapBitmaps(bmpContent[i], bmpActive);
			}
		}

		bitmap = DrawableHelper.mergeBitmaps(bmpContent, screenHeight, screenWidth);
		Resources res = this.getResources();

		return new BitmapDrawable(res, bitmap);
	}

	/**
	 * Method that generates a random value with rate depending on player's level
	 * @return Should a player have to fight an enemy? true : false
	 */
	protected boolean randomEncounter() {
		int playerLevel = 1;
		if (p != null)
			playerLevel = p.getLevel();

		if(playerLevel == 0)
			playerLevel = 1;

		double logarythm = Math.log(playerLevel);
		return logarythm % 2 == 0;
	}
}
