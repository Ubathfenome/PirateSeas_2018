package tfm.uniovi.pirateseas.view.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
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

	private ImageButton btnLeft, btnRight, btnUp, btnDown;

	private Player p = null;
	private Ship ship = null;
	private Map map = null;

	int mapWidth;
	int mapHeight;
	int mapLength;
	int active;
	int lastActive;
	int clearedMaps;

	int[] sensorTypes = null;
	private List<AppSensorEvent> sensorEvents;
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

		sensorEvents = new ArrayList<>();

		Intent data = getIntent();
		sensorEvents = data.getParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS);

		sensorTypes = data.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		loadGame = data.getBooleanExtra(Constants.TAG_LOAD_GAME, false);
		mapHeight = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, Constants.MAP_MIN_HEIGHT);
		mapWidth = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, Constants.MAP_MIN_WIDTH);

		Date date = new Date();
		GameHelper.loadGameAtPreferences(this,p = new Player(), ship = new Ship(), map = new Map(date, mapHeight, mapWidth));
		p = GameHelper.helperPlayer;
		ship = GameHelper.helperShip;
		map = GameHelper.helperMap;

		if(map.getMapLength() == Constants.MAP_MIN_LENGTH) {
            map = null;
        } else {
            active = map.getActiveCell();
            lastActive = map.getLastActiveCell();
        }

		layoutMapBackground = findViewById(R.id.layoutMapBackground);
		Drawable currentMapDrawable = getCurrentMap(date);
		layoutMapBackground.setBackground(currentMapDrawable);

		mapWidth = map.getMapWidth();
		mapHeight = map.getMapHeight();
		mapLength = map.getMapLength();
		clearedMaps = 0;

		btnLeft = findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's left cell content
				if(v.isEnabled()) {
					map.setLastActiveCell(active);
					map.setActiveCell(active - 1);
					processOption();
				}
			}
		});

		btnUp = findViewById(R.id.btnFront);
		btnUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's up cell content
				if(v.isEnabled()) {
					map.setLastActiveCell(active);
					map.setActiveCell(active-mapWidth);
					processOption();
				}
			}
		});

		btnRight = findViewById(R.id.btnRight);
		btnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's right cell content
				if(v.isEnabled()) {
					map.setLastActiveCell(active);
					map.setActiveCell(active+1);
					processOption();
				}
			}
		});

		btnDown = findViewById(R.id.btnDown);
		btnDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get map's down cell content
				if(v.isEnabled()) {
					map.setLastActiveCell(active);
					map.setActiveCell(active+mapWidth);
					processOption();
				}
			}
		});

		// Update arrow images if active cell is on any border of the map
		updateArrowButtonsState();

		txtScreenSelectionLabel = findViewById(R.id.txtScreenSelectionLabel);
		txtScreenSelectionLabel.setTypeface(customFont);

		TextView txtAnimationTitle = findViewById(R.id.txtAnimationTitle);
		txtAnimationTitle.setTextColor(Color.BLACK);
		txtAnimationTitle.setTypeface(customFont);

		if(map.isAllClear()){
			// Map completed! Clear map from preferences and start new map
			Toast.makeText(context, getString(R.string.message_mapcompleted), Toast.LENGTH_LONG).show();
			clearedMaps++;
			Map newMap = new Map(new Date(), Constants.MAP_MIN_HEIGHT, Constants.MAP_MIN_WIDTH);
			newMap.setClearedMaps(clearedMaps);
			// Create new and better PlayerShip?
			ShipType st = ship.getShipType();
			ShipType newShipType = getBetterShipType(st);
			ship.updateShipType(newShipType);
			GameHelper.saveGameAtPreferences(context, p, ship, newMap);
		}
	}

	private void updateArrowButtonsState() {
		btnLeft.setEnabled(active%mapWidth != 0);
		btnUp.setEnabled(active-mapWidth>=0);
		btnRight.setEnabled((active+1)%mapWidth != 0);
		btnDown.setEnabled((active+mapWidth)<mapLength);
	}

	private void processOption() {
		if (map.isActiveCellCleared() && map.isActiveCellIsland()) {
			enterVisitedIsland();
		} else if (map.isActiveCellCleared() && !map.isActiveCellIsland()) {
			// ScreenSelection activity
			reloadSelection();
		} else {
			if (!map.isActiveCellIsland()) {
				// Game activity
				MusicManager.getInstance().changeSong(context, MusicManager.MUSIC_BATTLE);
                startBattleGame();
			} else {
				// Shop activity
				MusicManager.getInstance().changeSong(context, MusicManager.MUSIC_ISLAND);
                enterRandomIsland();
			}
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
	 * Launch the battle activity
	 */
	private void startBattleGame(){
		GameHelper.saveGameAtPreferences(this, p, ship, map);

		Intent newGameIntent = new Intent(context, GameActivity.class);
		newGameIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
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
//        String message = getResources().getString(R.string.message_nothinghere);
//        txtScreenSelectionLabel.setText(message);
		GameHelper.saveGameAtPreferences(this, p, ship, map);

		launchResetIntent();
	}

	private void launchResetIntent(){
		Intent resetIntent = new Intent(this, ScreenSelectionActivity.class);
		resetIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
		resetIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
		resetIntent.putExtra(Constants.TAG_LOAD_GAME, loadGame);
		resetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Log.d(TAG,"Reset ScreenSelection Intent");
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);
		startActivity(resetIntent);
	}

	/**
	 * Save the game when going into an already visited island
	 */
	private void enterVisitedIsland() {
		String message = getText(R.string.message_islandvisited) + "\n" + txtScreenSelectionLabel.getText();
		// Notify that the island has already been visited and cannot be visited twice
		txtScreenSelectionLabel.setText(message);
		GameHelper.saveGameAtPreferences(this, p, ship, map);

		launchResetIntent();
	}

	/**
	 * Launch a random island Activity
	 */
	private void enterRandomIsland() {
		GameHelper.saveGameAtPreferences(this, p, ship, map);

		Random rand = new Random();
		boolean yesNo = rand.nextBoolean();
		Intent shopIntent = new Intent(this, ShopActivity.class);
		shopIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
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
		//noinspection UnusedAssignment
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
		}

		if(mapContent[active].contains("0")){		// Overlap edge image to easier identify of active cell
			bmpContent[active] = DrawableHelper.overlapBitmaps(bmpContent[active], bmpActive);
		} else if(mapContent[lastActive].contains("0")){
			active = lastActive;
			map.setActiveCell(active);
			bmpContent[active] = DrawableHelper.overlapBitmaps(bmpContent[active], bmpActive);
		}

		bitmap = DrawableHelper.mergeBitmaps(bmpContent, screenHeight, screenWidth);
		Resources res = this.getResources();

		return new BitmapDrawable(res, bitmap);
	}

	@Override
    /*
     * Method called when the back button of the device is pressed
     */
	public void onBackPressed() {
		// Exit game. Return to main menu

		// Pop up messageBox asking if the user is sure to leave
		LeaveGameDialogFragment exitDialog = new LeaveGameDialogFragment();
		exitDialog.show(getFragmentManager(), "LeaveGameDialog");
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

	/**
	 * Class to create a Dialog that asks the player if he/she is sure of leaving the game activity
	 */
	public static class LeaveGameDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity, R.style.Dialog_No_Border);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			@SuppressLint("InflateParams")
			View view = inflater.inflate(R.layout.custom_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			Button btnNegative = view.findViewById(R.id.btnNegative);
			txtTitle.setText(getResources().getString(R.string.exit_dialog_title));
			txtMessage.setText(getResources().getString(R.string.exit_dialog_message));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// Exit
					Log.d(TAG,"Finish ScreenSelection Activity");
					Intent mainMenuIntent = new Intent(dummyActivity, MainMenuActivity.class);

					MusicManager.getInstance().changeSong(dummyActivity, MusicManager.MUSIC_GAME_MENU);

                    mainMenuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(mainMenuIntent);
					dummyActivity.finish();
				}
			});
			btnNegative.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					dismiss();
				}
			});
			builder.setView(view);

			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
}
