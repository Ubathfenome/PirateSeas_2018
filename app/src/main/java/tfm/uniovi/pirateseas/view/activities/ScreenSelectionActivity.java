package tfm.uniovi.pirateseas.view.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
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

	private TextView txtScreenSelectionLabel;
	private LinearLayout layoutMapBackground;

	private Player p = null;
	private Ship ship = null;
	private Map map = null;

	int mapWidth;
	int mapHeight;
	int mapLength;
	int active;
	int lastActive;

	int[] sensorTypes = null;
	boolean loadGame;

	private Context context;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_screen_selection_ui);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.context = this;

		Intent intent = getIntent();

		sensorTypes = intent.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		loadGame = intent.getBooleanExtra(Constants.TAG_LOAD_GAME, false);
		mapHeight = intent.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, Constants.MAP_MIN_HEIGHT);
		mapWidth = intent.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, Constants.MAP_MIN_WIDTH);

		Date date = new Date();
		GameHelper.loadGameAtPreferences(this,p = new Player(), ship = new Ship(), map = new Map(date, mapHeight, mapWidth));
		p = GameHelper.helperPlayer;
		ship = GameHelper.helperShip;
		map = GameHelper.helperMap;

		if(map.getMapLength() == Constants.MAP_MIN_LENGTH)
			map = null;

		layoutMapBackground = findViewById(R.id.layoutMapBackground);
		Drawable currentMapDrawable = getCurrentMap(date);
		layoutMapBackground.setBackground(currentMapDrawable);

		mapWidth = map.getMapWidth();
		mapHeight = map.getMapHeight();
		mapLength = map.getMapLength();
		active = map.getActiveCell();
		lastActive = map.getLastActiveCell();

		final boolean encounter = randomEncounter();

		ImageButton btnLeft = findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's left cell content
				if(active%mapWidth != 0){
					map.setLastActiveCell(active);
					map.setActiveCell(active-1);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()) {
						try {
							MusicManager.getInstance().stopBackgroundMusic();
						} catch (IllegalStateException e) {
							MusicManager.getInstance().resetPlayer();
						}
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else if (map.isActiveCellCleared() && !map.isActiveCellIsland()) {
						// ScreenSelection activity
						MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
						reloadSelection();
					} else {
						if (!map.isActiveCellIsland()) {
							// Game activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
							MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
							startBattleGame();
						} else {
							// Shop activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
							MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
							enterRandomIsland();
						}
					}
				} else {
					wrongWayMessage();
				}
			}
		});

		ImageButton btnUp = findViewById(R.id.btnFront);
		btnUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's up cell content
				if(active-mapWidth>=0){
					map.setLastActiveCell(active);
					map.setActiveCell(active-mapWidth);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()) {
						try {
							MusicManager.getInstance().stopBackgroundMusic();
						} catch (IllegalStateException e) {
							MusicManager.getInstance().resetPlayer();
						}
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else if (map.isActiveCellCleared() && !map.isActiveCellIsland()) {
						// ScreenSelection activity
						MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
						reloadSelection();
					} else {
						if (!map.isActiveCellIsland()) {
							// Game activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
							MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
							startBattleGame();
						} else {
							// Shop activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
							MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
							enterRandomIsland();
						}
					}
				} else {
					wrongWayMessage();
				}
			}
		});

		ImageButton btnRight = findViewById(R.id.btnRight);
		btnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's right cell content
				if((active+1)%mapWidth != 0){
					map.setLastActiveCell(active);
					map.setActiveCell(active+1);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()) {
						try {
							MusicManager.getInstance().stopBackgroundMusic();
						} catch (IllegalStateException e) {
							MusicManager.getInstance().resetPlayer();
						}
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else if (map.isActiveCellCleared() && !map.isActiveCellIsland()) {
						// ScreenSelection activity
						MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
						reloadSelection();
					} else {
						if (!map.isActiveCellIsland()) {
							// Game activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
							MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
							startBattleGame();
						} else {
							// Shop activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
							MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
							enterRandomIsland();
						}
					}
				} else {
					wrongWayMessage();
				}
			}
		});

		ImageButton btnDown = findViewById(R.id.btnDown);
		btnDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's down cell content
				if((active+mapWidth)<mapLength){
					map.setLastActiveCell(active);
					map.setActiveCell(active+mapWidth);
					if(map.isActiveCellCleared() && map.isActiveCellIsland()) {
						try {
							MusicManager.getInstance().stopBackgroundMusic();
						} catch (IllegalStateException e) {
							MusicManager.getInstance().resetPlayer();
						}
						MusicManager.getInstance(context, MusicManager.MUSIC_ISLAND).playBackgroundMusic();
						enterVisitedIsland();
					} else if (map.isActiveCellCleared() && !map.isActiveCellIsland()) {
						// ScreenSelection activity
						MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
						reloadSelection();
					} else {
						if (!map.isActiveCellIsland()) {
							// Game activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
							MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
							startBattleGame();
						} else {
							// Shop activity
							try {
								MusicManager.getInstance().stopBackgroundMusic();
							} catch(IllegalStateException e){
								MusicManager.getInstance().resetPlayer();
							}
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

		TextView txtAnimationTitle = findViewById(R.id.txtAnimationTitle);
		txtAnimationTitle.setTextColor(Color.BLACK);
		txtAnimationTitle.setTypeface(customFont);

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
		revealCellAnimation(R.mipmap.txtr_map_water, newGameIntent);
	}

	/**
	 * Relaunch the select screen activity
	 */
	private void reloadSelection(){
        String message = getResources().getString(R.string.message_nothinghere);
        txtScreenSelectionLabel.setText(message);
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
		String message = getText(R.string.message_islandvisited) + "\n" + txtScreenSelectionLabel.getText();
		// Notify that the island has already been visited and cannot be visited twice
        txtScreenSelectionLabel.setText(message);
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

		revealCellAnimation(R.mipmap.txtr_map_island, shopIntent);

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
		int screenHeight = displayMetrics.heightPixels - (2 * (int) getResources().getDimension(R.dimen.small_padding_size));
		int screenWidth = displayMetrics.widthPixels - (2 * (int) getResources().getDimension(R.dimen.small_padding_size));
		Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

		int fragmentHeight = bmpCover.getHeight();
		int fragmentWidth = bmpCover.getWidth();

		float heightRatio = screenHeight / (float) fragmentHeight;
		float widthRatio = screenWidth / (float) fragmentWidth;

		int mapHeight = (int) heightRatio;
		int mapWidth = (int) widthRatio;

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
			if(active==i && s.contains("0")){		// Overlap edge image to easier identify of active cell
				bmpContent[i] = DrawableHelper.overlapBitmaps(bmpContent[i], bmpActive);
			} else if(lastActive == i){
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

	@Override
    /*
     * Method called when the back button of the device is pressed
     */
	public void onBackPressed() {
		// Exit game. Return to main menu
		Intent mainMenuIntent = new Intent(context, MainMenuActivity.class);
		try {
			MusicManager.getInstance().stopBackgroundMusic();
		} catch(IllegalStateException e){
			MusicManager.getInstance().resetPlayer();
		}
		MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
		mainMenuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mainMenuIntent);
		finish();
	}

    /**
     * Animates an imageView to show the player the newly selected map cell content
     * @param revealedResource Drawable resource to be shown
     */
	private void revealCellAnimation(int revealedResource, final Intent intent){

		layoutMapBackground.setVisibility(View.GONE);

		ImageView imgCellReveal = findViewById(R.id.imgCellReveal);
        ImageView imgCellRevealed = findViewById(R.id.imgCellRevealed);
        imgCellReveal.setVisibility(View.VISIBLE);
		imgCellRevealed.setVisibility(View.VISIBLE);
		imgCellReveal.setAlpha(1f);
		imgCellRevealed.setAlpha(0f);
		imgCellRevealed.setBackgroundResource(revealedResource);

		long animDuration = 5000;

		imgCellReveal.animate().alpha(0f).setDuration(animDuration);

		imgCellRevealed.animate().alpha(1f).setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				startActivity(intent);
				finish();
			}
		});
	}
}
