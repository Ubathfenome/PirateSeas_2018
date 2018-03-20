package tfm.uniovi.pirateseas.view.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;

public class MainMenuActivity extends Activity {

	private static final String TAG = "MainMenuActivity";
	private boolean newGame = false;
	private boolean mOverwriteWarning = false;
	private int mMode;

	private static final String[] INITIAL_PERMS = {Manifest.permission.WRITE_SETTINGS};
	protected Context context;
	protected SharedPreferences mPreferences;

	protected static int screenResolutionWidth;
	protected static int screenResolutionHeight;

	private TextView txtTitle;
	private Button btnNewGame;
	private Button btnLoadGame;
	private ImageButton btnSettings;
	private ImageButton btnHelp;
	private Button btnExit;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		context = this;

		mMode = Constants.MODE;

		// Get Screen
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			Point size = new Point();
			getWindowManager().getDefaultDisplay().getSize(size);
			screenResolutionWidth = size.x;
			screenResolutionHeight = size.y;
		} else {
			Display display = ((WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			screenResolutionWidth = display.getWidth();
			screenResolutionHeight = display.getHeight();
		}

		mPreferences = context.getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(Constants.PREF_DEVICE_WIDTH_RES, screenResolutionWidth);
		editor.putInt(Constants.PREF_DEVICE_HEIGHT_RES, screenResolutionHeight);
		editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		editor.putBoolean(Constants.PREF_LEVEL_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		editor.putBoolean(Constants.PREF_PAUSE_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		editor.putBoolean(Constants.TAG_EXE_MODE, Constants.isInDebugMode(mMode));
		editor.commit();

		txtTitle = findViewById(R.id.txtTitleLabel);
		txtTitle.setTypeface(customFont);

		btnNewGame = (Button) findViewById(R.id.btn_newgame);
		btnNewGame.setTypeface(customFont);
		btnNewGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mOverwriteWarning){
					OverwriteGameDialogFragment overwriteDialog = new OverwriteGameDialogFragment();
					overwriteDialog.show(getFragmentManager(), "OverwriteGameDialog");
				} else {
					newGame = true;
					launchSensorActivity();
				}
			}
		});

		btnLoadGame = (Button) findViewById(R.id.btn_loadgame);
		btnLoadGame.setTypeface(customFont);
		btnLoadGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				newGame = false;
				launchSensorActivity();
			}
		});

		btnSettings = (ImageButton) findViewById(R.id.btn_settings);
		btnSettings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent settingsIntent = new Intent(context,
						SettingsActivity.class);
				startActivity(settingsIntent);
			}
		});

		btnHelp = (ImageButton) findViewById(R.id.btn_help);
		btnHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent helpIntent = new Intent(context, HelpActivity.class);
				startActivity(helpIntent);
			}
		});

		btnExit = (Button) findViewById(R.id.btn_exit);
		btnExit.setTypeface(customFont);
		btnExit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		AsyncTask<Void, Integer, Boolean> loadSoundsTask = new LoadSounds();
		loadSoundsTask.execute();
	}

	private void launchGame(boolean displayTutorial, int[] sensorTypes) {
		Player dummyPlayer;
		Ship dummyShip;
		Map dummyMap;

		if (displayTutorial == false) {
			// Load game
			Intent screenIntent = new Intent(context, ScreenSelectionActivity.class);
			screenIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
			screenIntent.putExtra(Constants.TAG_LOAD_GAME, !displayTutorial);
			screenIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, calculateMapHeight());
			screenIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, calculateMapWidth());
			startActivity(screenIntent);
		} else {
			//	New game
			Intent tutorialIntent = new Intent(context, TutorialActivity.class);
			tutorialIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
			tutorialIntent.putExtra(Constants.TAG_LOAD_GAME, !displayTutorial);
			startActivity(tutorialIntent);
		}
	}

	private void launchSensorActivity(){
		Intent checkSensorListIntent = new Intent(context, SensorActivity.class);
		startActivityForResult(checkSensorListIntent, Constants.REQUEST_SENSOR_LIST);
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutMainMenu).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		// Run permissions request only the first time
		checkAppVersion();

		super.onResume();
	}

	public void requestPermissionsFirstTime(){
		// New runtime permissions request system for version 23 and above
		// @see: https://stackoverflow.com/questions/32083913/android-gps-requires-access-fine-location-error-even-though-my-manifest-file
		// @see: https://stackoverflow.com/questions/32266425/android-6-0-permission-denial-requires-permission-android-permission-write-sett
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Settings.System.canWrite(context)) {
				if(!hasPermission(INITIAL_PERMS[0])) {
					if(!shouldShowRequestPermissionRationale(INITIAL_PERMS[0]))
						requestPermissions(INITIAL_PERMS, Constants.REQUEST_PERMISSIONS);
				}
			}
			else {
				Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + this.getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}
	}

	/**
	 * @sourcec: http://blog.cubeactive.com/app-version-number-android-tutorial/
	 * @see: https://developer.android.com/studio/publish/versioning.html?hl=es-419
	 */
	private void checkAppVersion() {
		int currentAppVersionCode = getCurrentAppVersionCode();
		int oldAppVersion = mPreferences.getInt(Constants.APP_VERSION, 0);
		if (oldAppVersion < currentAppVersionCode) {
			try {
				if (oldAppVersion > 0)
					Toast.makeText(this, String.format("App updated from version %d", oldAppVersion), Toast.LENGTH_SHORT).show();
				else
					requestPermissionsFirstTime();
				//Toast.makeText(this, String.format("App started for the first time", oldAppVersion), Toast.LENGTH_SHORT).show();
			} finally {
				SharedPreferences.Editor preferencesEditor = mPreferences.edit();
				preferencesEditor.putInt(Constants.APP_VERSION, currentAppVersionCode);
				preferencesEditor.commit();
			}
		} else {
			loadSettings();
		}
	}

	/**
	 * @return
	 * @source: http://blog.cubeactive.com/app-version-number-android-tutorial/
	 */
	private int getCurrentAppVersionCode() {
		int versionCode = -1;
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionCode = packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case Constants.REQUEST_PERMISSIONS: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission was granted, yay! Do the contacts-related task you need to do.
					loadSettings();
				}
				return;
			}
		}
	}

	private int calculateMapHeight(){
		Bitmap bmpCover = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_cover);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		int screenHeight = displayMetrics.heightPixels;
		int fragmentHeight = bmpCover.getHeight();
		int mapHeight = screenHeight / fragmentHeight;

		return mapHeight;
	}

	private int calculateMapWidth(){
		Bitmap bmpCover = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_cover);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		int screenWidth = displayMetrics.widthPixels;
		int fragmentWidth = bmpCover.getWidth();
		int mapWidth = screenWidth / fragmentWidth;

		return mapWidth;
	}

	private void loadSettings() {
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);

		if (mPreferences.getLong(Constants.PREF_PLAYER_TIMESTAMP, 0) == 0) {
			btnLoadGame.setEnabled(false);
			mOverwriteWarning = false;
		} else {
			btnLoadGame.setEnabled(true);
			mOverwriteWarning = true;
		}
	}

	@Override
	protected void onDestroy() {
		MusicManager.getInstance().stopBackgroundMusic();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case Constants.REQUEST_SENSOR_LIST:
				if (resultCode == RESULT_OK) {
					int[] sensorTypes = data
							.getIntArrayExtra(Constants.TAG_SENSOR_LIST);

					boolean emptyList = true;
					for(int i = 0, length = sensorTypes.length; i< length; i++){
						if(sensorTypes[i] != 0)
							emptyList = false;
					}
					if(emptyList){
						Toast.makeText(context, "No sensors have been detected. No events will be triggered.", Toast.LENGTH_LONG).show();
						SharedPreferences.Editor editor = mPreferences.edit();
						editor.putBoolean(Constants.PREF_DEVICE_NOSENSORS, true);
						editor.commit();
					}

					launchGame(newGame, sensorTypes);
				}
				break;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private boolean hasPermission(String perm) {
		return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
	}

	@SuppressLint("ValidFragment")
	public class OverwriteGameDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			builder.setTitle(
					getResources().getString(R.string.overwrite_dialog_title))
					.setMessage(R.string.overwrite_dialog_message)
					.setPositiveButton(R.string.overwrite_dialog_positive,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									boolean noSensors = mPreferences.getBoolean(Constants.PREF_DEVICE_NOSENSORS, false);

									SharedPreferences.Editor editor = mPreferences.edit();
									editor.clear();
									editor.putBoolean(Constants.TAG_EXE_MODE, Constants.isInDebugMode(mMode));
									editor.commit();

									if(!noSensors)
										launchSensorActivity();
									else{
										int[] emptySensorList = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
										launchGame(!mOverwriteWarning, emptySensorList);
									}
								}
							})
					.setNegativeButton(R.string.exit_dialog_negative,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									// User cancels the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	/**
	 * http://stackoverflow.com/questions/7428448/android-soundpool-heapsize-overflow
	 * @author Miguel
	 *
	 */
	private class LoadSounds extends AsyncTask<Void, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(Void... arg0) {
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_ENEMY_APPEAR, R.raw.snd_ship_ahoy);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_GAME_PAUSED, R.raw.msc_game_paused);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_BATTLE, R.raw.msc_soundtrack_battle);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_ISLAND, R.raw.msc_soundtrack_island);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_GAME_MENU, R.raw.msc_soundtrack_menu);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_GOLD_GAINED, R.raw.snd_gold_gained);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_GOLD_SPENT, R.raw.snd_gold_spent);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_SHOT_FIRED, R.raw.snd_shot_fired);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_SHOT_HIT, R.raw.snd_shot_hit);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_SHOT_MISSED, R.raw.snd_shot_missed);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_SHOT_RELOADING, R.raw.snd_shot_reload);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_SHOT_EXPLOSION, R.raw.snd_shot_explosion);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_WEATHER_FOG, R.raw.snd_weather_fog);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_WEATHER_STORM, R.raw.snd_weather_storm);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_WEATHER_MAELSTROM, R.raw.snd_weather_maelstorm);
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_XP_GAINED, R.raw.snd_xp_gained);
			return true;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(TAG,"AudioPool loaded");
			MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
		}
	}
}
