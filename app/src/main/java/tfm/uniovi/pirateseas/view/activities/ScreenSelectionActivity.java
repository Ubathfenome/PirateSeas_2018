package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.utils.approach2d.DrawableHelper;

public class ScreenSelectionActivity extends Activity {

	private static final String TAG = "ScreenSelectionActivity";

	private LinearLayout layoutBackground;

	private ImageButton btnLeft;
	private ImageButton btnUp;
	private ImageButton btnDown;
	private ImageButton btnRight;

	private Player p = null;
	private Map map = null;

	int[] sensorTypes = null;
	boolean loadGame;
	
	@SuppressWarnings("unused")
	private Context context;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_screen_selection_ui);

		this.context = this;

		layoutBackground = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_screen_selection_ui,null);

		Intent intent = getIntent();

		sensorTypes = intent.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		loadGame = intent.getBooleanExtra(Constants.TAG_LOAD_GAME, false);

		p = intent.getParcelableExtra(Constants.TAG_SCREEN_SELECTION_PLAYERDATA);
		map = intent.getParcelableExtra(Constants.TAG_SCREEN_SELECTION_MAPDATA);

		layoutBackground.setBackground(getCurrentMap(new Date()));

		final boolean encounter = randomEncounter();
		final int active = map.getActiveCell();

		btnLeft = findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's left cell content
				if(active%Constants.MAP_WIDTH != 0){
					map.setActiveCell(active-1);
					map.clearActiveMapCell();
					if(!map.isActiveCellIsland()){
						if(encounter) {
							// Game activity
							startBattleGame();
						} else {
							// ScreenSelection activity
							reloadSelection();
						}
					} else {
						// Shop activity
						enterRandomIsland();
					}
				}
			}
		});

		btnUp = findViewById(R.id.btnFront);
		btnUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's up cell content
				if(active>=Constants.MAP_HEIGHT){
					map.setActiveCell(active-Constants.MAP_WIDTH);
					map.clearActiveMapCell();
					if(!map.isActiveCellIsland()){
						if(encounter) {
							// Game activity
							startBattleGame();
						} else {
							// ScreenSelection activity
							reloadSelection();
						}
					} else {
						// Shop activity
						enterRandomIsland();
					}
				}
			}
		});

		btnRight = findViewById(R.id.btnRight);
		btnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's right cell content
				if((active+1)%Constants.MAP_WIDTH != 0){
					map.setActiveCell(active+1);
					map.clearActiveMapCell();
					if(!map.isActiveCellIsland()){
						if(encounter) {
							// Game activity
							startBattleGame();
						} else {
							// ScreenSelection activity
							reloadSelection();
						}
					} else {
						// Shop activity
						enterRandomIsland();
					}
				}
			}
		});

		btnDown = findViewById(R.id.btnDown);
		btnDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's down cell content
				if((active+Constants.MAP_WIDTH)<Constants.MAP_LENGTH){
					map.setActiveCell(active+Constants.MAP_WIDTH);
					map.clearActiveMapCell();
					if(!map.isActiveCellIsland()){
						if(encounter) {
							// Game activity
							startBattleGame();
						} else {
							// ScreenSelection activity
							reloadSelection();
						}
					} else {
						// Shop activity
						enterRandomIsland();
					}
				}
			}
		});

		MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
	}

	private void startBattleGame(){
		Intent newGameIntent = new Intent(context, GameActivity.class);
		newGameIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
		newGameIntent.putExtra(Constants.TAG_LOAD_GAME, loadGame);
		startActivity(newGameIntent);
	}

	private Drawable getCurrentMap(Date date) {
		if(map == null) {
			map = new Map(date);
			map.clearActiveMapCell();
		}

		int active = map.getActiveCell();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int screenHeight = displayMetrics.heightPixels;
		int screenWidth = displayMetrics.widthPixels;
		Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

		Bitmap bmpCover = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_cover);
		Bitmap bmpIsland = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_island);
		Bitmap bmpWater = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_water);
		Bitmap bmpActive = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_active);

		String[] mapContent = map.getMapContent();
		Bitmap[] bmpContent = new Bitmap[mapContent.length];

		for(int i = 0; i < mapContent.length; i++){
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

		bitmap = DrawableHelper.mergeBitmaps(bmpContent, Constants.MAP_HEIGHT, Constants.MAP_WIDTH);
		Resources res = this.getResources();
		BitmapDrawable bd = new BitmapDrawable(res, bitmap);

		return bd;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	protected boolean randomEncounter() {
		// Generate random value with rate depending on player's level
		int playerLevel = 1;
		if (p != null)
			playerLevel = p.getLevel();
		
		if(playerLevel == 0)
			playerLevel = 1;
		
		double logarythm = Math.log(playerLevel);
		return logarythm % 2 == 0;
	}

	private void enterRandomIsland() {
		Random rand = new Random();
		boolean yesNo = rand.nextBoolean();
		Intent shopIntent = new Intent(this, ShopActivity.class);
		shopIntent.putExtra(Constants.ITEMLIST_NATURE, yesNo ? Constants.NATURE_SHOP : Constants.NATURE_TREASURE);
		Log.d(TAG,"Start Shop ForResult Intent");
		this.startActivityForResult(shopIntent, Constants.REQUEST_ISLAND);
	}
	
	private void reloadSelection(){
		Toast.makeText(this, getResources().getString(R.string.message_nothinghere), Toast.LENGTH_SHORT).show();
		
		Intent resetIntent = new Intent(this, ScreenSelectionActivity.class);
		// TODO Add remaining extras to Intent
		resetIntent.putExtra(Constants.TAG_SCREEN_SELECTION_PLAYERDATA, p);
		Log.d(TAG,"Reset ScreenSelection Intent");
		this.startActivity(resetIntent);
		Log.d(TAG,"Finish ScreenSelection Activty");
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.d(TAG, "ScreenSelectionActivity called by " + this.getCallingActivity().getClassName());
		if (data != null && requestCode == Constants.REQUEST_ISLAND && resultCode == Activity.RESULT_OK)
			reloadSelection();
	}

}
