package tfm.uniovi.pirateseas.view.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Main menu activity
 */
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

	private Button btnLoadGame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		context = this;

		mMode = Constants.MODE;

		// Get Screen
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		screenResolutionWidth = size.x;
		screenResolutionHeight = size.y;

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
		editor.apply();

		TextView txtTitle = findViewById(R.id.txtTitleLabel);
		txtTitle.setTypeface(customFont);

		Button btnNewGame = findViewById(R.id.btn_newgame);
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

		Button btnTutorial = findViewById(R.id.btn_tutorial);
		btnTutorial.setTypeface(customFont);
		btnTutorial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				launchGame(true, null);
			}
		});

		btnLoadGame = findViewById(R.id.btn_loadgame);
		btnLoadGame.setTypeface(customFont);
		btnLoadGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				newGame = false;
				launchSensorActivity();
			}
		});

		ImageButton btnSettings = findViewById(R.id.btn_settings);
		btnSettings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent settingsIntent = new Intent(context,
						SettingsActivity.class);
				startActivity(settingsIntent);
			}
		});

		ImageButton btnHelp = findViewById(R.id.btn_help);
		btnHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent helpIntent = new Intent(context, HelpActivity.class);
				startActivity(helpIntent);
			}
		});

		Button btnExit = findViewById(R.id.btn_exit);
		btnExit.setTypeface(customFont);
		btnExit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		if(!MusicManager.getInstance().hasRegisteredSounds()) {
            AsyncTask<Void, Integer, Boolean> loadSoundsTask = new LoadSounds(this);
            loadSoundsTask.execute();
        } else {
		    MusicManager.getInstance().changeSong(this, MusicManager.MUSIC_GAME_MENU);
        }
	}

	/**
	 * Launch the next activity in the starting game flow
	 * @param displayTutorial Sets if the launching game process should show the tutorial or not
	 * @param sensorTypes Set the sensor values as an array to be handled
	 */
	private void launchGame(boolean displayTutorial, int[] sensorTypes) {
		if (!displayTutorial) {
			// Load game
			Intent screenIntent = new Intent(context, ScreenSelectionActivity.class);
			screenIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
			screenIntent.putExtra(Constants.TAG_LOAD_GAME, false);
			screenIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, calculateMapHeight());
			screenIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, calculateMapWidth());
			startActivity(screenIntent);
		} else {
			//	New game
			Intent tutorialIntent = new Intent(context, TutorialActivity.class);
			tutorialIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
			tutorialIntent.putExtra(Constants.TAG_LOAD_GAME, true);
			startActivity(tutorialIntent);
		}
	}

	/**
	 * Launch the Sensor activity
	 */
	private void launchSensorActivity(){
		Intent checkSensorListIntent = new Intent(context, SensorActivity.class);
		startActivityForResult(checkSensorListIntent, Constants.REQUEST_SENSOR_LIST);
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutMainMenu).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		// Run permissions request only the first time
		checkAppVersion();

		// ISSUE #9 (Test pending)

		if(MusicManager.getInstance() != null && MusicManager.getInstance().isLoaded() && !MusicManager.getInstance().isPlaying())
            MusicManager.getInstance().playBackgroundMusic();

		super.onResume();
	}

	/**
	 * Request permissions for the first use of the app
	 */
	public void requestPermissionsFirstTime(){
		// New runtime permissions request system for version 23 and above
		// @see: https://stackoverflow.com/questions/32083913/android-gps-requires-access-fine-location-error-even-though-my-manifest-file
		// @see: https://stackoverflow.com/questions/32266425/android-6-0-permission-denial-requires-permission-android-permission-write-sett
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			/*
			if (!Settings.System.canWrite(context)) {

				if(!hasPermission(INITIAL_PERMS[0])) {
					Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
					intent.setData(Uri.parse("package:" + this.getPackageName()));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivityForResult(intent, Constants.REQUEST_PERMISSIONS);
				}
			}
		    */
			loadSettings();
		}
	}

	/**
	 * Checks the app version
	 * @see: http://blog.cubeactive.com/app-version-number-android-tutorial/
	 * @see: https://developer.android.com/studio/publish/versioning.html?hl=es-419
	 */
	private void checkAppVersion() {
		int currentAppVersionCode = getCurrentAppVersionCode();
		int oldAppVersion = mPreferences.getInt(Constants.APP_VERSION, 0);
		if (oldAppVersion < currentAppVersionCode) {
			try {
				if (oldAppVersion > 0)
					Toast.makeText(this, String.format(Locale.ENGLISH, "App updated from version %d", oldAppVersion), Toast.LENGTH_SHORT).show();
				else
					requestPermissionsFirstTime();
				//Toast.makeText(this, String.format("App started for the first time", oldAppVersion), Toast.LENGTH_SHORT).show();
			} finally {
				SharedPreferences.Editor preferencesEditor = mPreferences.edit();
				preferencesEditor.putInt(Constants.APP_VERSION, currentAppVersionCode);
				preferencesEditor.apply();
			}
		}
	}

	/**
	 * Get the current app version
	 * @return App version
	 * @see: http://blog.cubeactive.com/app-version-number-android-tutorial/
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		// If request is cancelled, the result arrays are empty.
		if (requestCode == Constants.REQUEST_PERMISSIONS) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission was granted, yay!
				loadSettings();
			}
		}
	}

	/**
	 * Calculates the max number of map cells for a map to fit on the screen vertically
	 * @return Number of cells
	 */
	private int calculateMapHeight(){
		Bitmap bmpCover = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_cover);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		int screenHeight = displayMetrics.heightPixels - (2 * (int) getResources().getDimension(R.dimen.small_padding_size));
		int fragmentHeight = bmpCover.getHeight();

		return screenHeight / fragmentHeight;
	}

	/**
	 * Calculates the max number of map cells for a map to fit on the screen horizontally
	 * @return Number of cells
	 */
	private int calculateMapWidth(){
		Bitmap bmpCover = BitmapFactory.decodeResource(getResources(),R.mipmap.txtr_map_cover);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		int screenWidth = displayMetrics.widthPixels - (2 * (int) getResources().getDimension(R.dimen.small_padding_size));
		int fragmentWidth = bmpCover.getWidth();

		return screenWidth / fragmentWidth;
	}

	/**
	 * Load the settings
	 */
	private void loadSettings() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(Settings.System.canWrite(context))
				Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
					Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		}

		if (mPreferences.getLong(Constants.PREF_PLAYER_TIMESTAMP, 0) == 0) {
			btnLoadGame.setEnabled(false);
			mOverwriteWarning = false;
		} else {
			btnLoadGame.setEnabled(true);
			mOverwriteWarning = true;
		}
	}

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case Constants.REQUEST_SENSOR_LIST:
				if (resultCode == RESULT_OK) {
					int[] sensorTypes = data
							.getIntArrayExtra(Constants.TAG_SENSOR_LIST);

					boolean emptyList = true;
					for (int sensorType : sensorTypes) {
						if (sensorType != 0)
							emptyList = false;
					}
					if(emptyList){
						Toast.makeText(context, "No sensors have been detected. No events will be triggered.", Toast.LENGTH_LONG).show();
						SharedPreferences.Editor editor = mPreferences.edit();
						editor.putBoolean(Constants.PREF_DEVICE_NOSENSORS, true);
						editor.apply();
					}

					launchGame(newGame, sensorTypes);
				}
				break;
			case Constants.REQUEST_PERMISSIONS:

				break;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	/*
	  Checks if the app has the valid permission active in the device
	 */
	private boolean hasPermission(String perm) {
		return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
	}

	@SuppressLint("ValidFragment")
	/*
	  Class to show a dialog that asks the player if he/she is sure to overwrite the last saved game
	 */
	public static class OverwriteGameDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();

			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			Button btnNegative = view.findViewById(R.id.btnNegative);
			txtTitle.setText(getResources().getString(R.string.overwrite_dialog_title));
			txtMessage.setText(getResources().getString(R.string.overwrite_dialog_message));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					boolean noSensors = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_DEVICE_NOSENSORS, false);
					boolean shipControlMode = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
					boolean ammoControlMode = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
					boolean levelControlMode = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_LEVEL_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
					boolean pauseControlMode = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_PAUSE_CONTROL_MODE, Constants.PREF_GAME_TOUCH);

					SharedPreferences.Editor editor = ((MainMenuActivity)getActivity()).mPreferences.edit();
					editor.clear();
					editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, shipControlMode);
					editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, ammoControlMode);
					editor.putBoolean(Constants.PREF_LEVEL_CONTROL_MODE, levelControlMode);
					editor.putBoolean(Constants.PREF_PAUSE_CONTROL_MODE, pauseControlMode);
					editor.putBoolean(Constants.TAG_EXE_MODE, Constants.isInDebugMode(((MainMenuActivity)getActivity()).mMode));
					editor.apply();

					if(!noSensors)
						((MainMenuActivity)getActivity()).launchSensorActivity();
					else{
						int[] emptySensorList = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
						((MainMenuActivity)getActivity()).launchGame(!((MainMenuActivity)getActivity()).mOverwriteWarning, emptySensorList);
					}
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

	/**
	 * Class to async load the app sounds
	 * @see: http://stackoverflow.com/questions/7428448/android-soundpool-heapsize-overflow
	 *
	 */
	private static class LoadSounds extends AsyncTask<Void, Integer, Boolean>{

		@SuppressLint("StaticFieldLeak")
		Context context;

		LoadSounds(Context c) {
			context = c;
		}

        @Override
		protected Boolean doInBackground(Void... arg0) {
			MusicManager.getInstance(context).registerSound(MusicManager.SOUND_ENEMY_APPEAR, R.raw.snd_ship_ahoy);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_GAME_PAUSED, R.raw.msc_game_paused);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_BATTLE, R.raw.msc_soundtrack_battle);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_ISLAND, R.raw.msc_soundtrack_island);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_GAME_MENU, R.raw.msc_soundtrack_menu);
			MusicManager.getInstance(context).registerSound(MusicManager.MUSIC_GAME_OVER, R.raw.msc_soundtrack_gameover);
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
			try {
				MusicManager.getInstance(context, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
			} catch (IllegalStateException ignored) {
			}
		}
	}
}
