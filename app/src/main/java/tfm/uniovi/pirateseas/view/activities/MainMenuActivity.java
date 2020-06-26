package tfm.uniovi.pirateseas.view.activities;

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
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.controller.sensors.SensorType;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
import tfm.uniovi.pirateseas.controller.sensors.events.EventDayNightCycle;
import tfm.uniovi.pirateseas.controller.sensors.events.EventShakeClouds;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherLight;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherMaelstrom;
import tfm.uniovi.pirateseas.controller.sensors.events.NoEvent;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;

/**
 * Main menu activity
 */
public class MainMenuActivity extends Activity {

	private static final String TAG = "MainMenuActivity";
	private int[] activeSensors;
	private List<AppSensorEvent> sensorEvents;
	private boolean mOverwriteWarning = false;
	private int mMode;
	private int mGamesNumber;

	protected Context context;
	protected SharedPreferences mPreferences;

	protected static int screenResolutionWidth;
	protected static int screenResolutionHeight;

	private long lastClickTimestamp = 0;

	private Button btnLoadGame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);


		context = this;

		mMode = Constants.MODE;

		sensorEvents = new ArrayList<>();

		// Get Screen
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		screenResolutionWidth = size.x;
		screenResolutionHeight = size.y;

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mPreferences = context.getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);

		mGamesNumber = mPreferences.getInt(Constants.TAG_GAMES_NUMBER, Constants.ZERO_INT);

		SharedPreferences activityPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		boolean shipControlMode = Boolean.parseBoolean(activityPreferences.getString(Constants.PREF_SHIP_CONTROL_MODE, String.valueOf(Constants.PREF_IS_ACTIVE)));
		boolean ammoControlMode = Boolean.parseBoolean(activityPreferences.getString(Constants.PREF_AMMO_CONTROL_MODE, String.valueOf(Constants.PREF_IS_ACTIVE)));
		boolean shootControlMode = Boolean.parseBoolean(activityPreferences.getString(Constants.PREF_SHOOT_CONTROL_MODE, String.valueOf(Constants.PREF_IS_ACTIVE)));

		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(Constants.PREF_DEVICE_WIDTH_RES, screenResolutionWidth);
		editor.putInt(Constants.PREF_DEVICE_HEIGHT_RES, screenResolutionHeight);
		editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, shipControlMode);
		editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, ammoControlMode);
		editor.putBoolean(Constants.PREF_SHOOT_CONTROL_MODE, shootControlMode);
		editor.putBoolean(Constants.TAG_EXE_MODE, Constants.isInDebugMode(mMode));
		editor.apply();

		Button btnNewGame = findViewById(R.id.btn_newgame);
		btnNewGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(SystemClock.elapsedRealtime() - lastClickTimestamp < 1000){
					return;
				}

				lastClickTimestamp = SystemClock.elapsedRealtime();

				if(mOverwriteWarning){
					OverwriteGameDialogFragment overwriteDialog = new OverwriteGameDialogFragment();
					overwriteDialog.show(getFragmentManager(), "OverwriteGameDialog");
				} else {
					launchGame(true, activeSensors);
				}
			}
		});

		Button btnTutorial = findViewById(R.id.btn_tutorial);
		btnTutorial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(SystemClock.elapsedRealtime() - lastClickTimestamp < 1000){
					return;
				}

				lastClickTimestamp = SystemClock.elapsedRealtime();

				Intent tutorialIntent = new Intent(context, TutorialActivity.class);
				tutorialIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
				tutorialIntent.putExtra(Constants.TAG_LOAD_GAME, true);
				startActivity(tutorialIntent);
			}
		});

		btnLoadGame = findViewById(R.id.btn_loadgame);
		btnLoadGame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(SystemClock.elapsedRealtime() - lastClickTimestamp < 1000){
					return;
				}

				lastClickTimestamp = SystemClock.elapsedRealtime();

				launchGame(false, activeSensors);
			}
		});

		ImageButton btnSettings = findViewById(R.id.btn_settings);
		btnSettings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(SystemClock.elapsedRealtime() - lastClickTimestamp < 1000){
					return;
				}

				lastClickTimestamp = SystemClock.elapsedRealtime();

				Intent settingsIntent = new Intent(context,
						SettingsActivity.class);
				settingsIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
				startActivity(settingsIntent);
			}
		});

		ImageButton btnHelp = findViewById(R.id.btn_help);
		btnHelp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(SystemClock.elapsedRealtime() - lastClickTimestamp < 1000){
					return;
				}

				lastClickTimestamp = SystemClock.elapsedRealtime();

				Intent helpIntent = new Intent(context, HelpActivity.class);
				startActivity(helpIntent);
			}
		});

		Button btnExit = findViewById(R.id.btn_exit);
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

		// Initialize sensorEvents setting whether to linked sensor is active on the device or not
		SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		initializeSensorEvents(mSensorManager);

		activeSensors = checkSensorPreferences();
	}

	private int[] checkSensorPreferences() {
		mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);
		String prefStringSensorList = mPreferences.getString(Constants.PREF_SENSOR_LIST, Constants.EMPTY_STRING);

		String[] preferenceStringArray;
		int[] preferenceIntArray = new int[SensorType.values().length];

		if(prefStringSensorList != null && prefStringSensorList.length() > 0){
			preferenceStringArray = prefStringSensorList.split(Constants.LIST_SEPARATOR);
			for(int i = 0; i < preferenceStringArray.length; i++){
				int value = Integer.parseInt(preferenceStringArray[i]);
				preferenceIntArray[i] = value;
			}
		}

		if(sensorArrayIsNotInitialized(preferenceIntArray)){
			preferenceIntArray = extractActiveSensors(sensorEvents);

			prefStringSensorList = toStringList(preferenceIntArray);

			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(Constants.TAG_SENSOR_LIST, prefStringSensorList);
			editor.apply();
		}

		return preferenceIntArray;
	}

	private boolean sensorArrayIsNotInitialized(int[] intArray) {
		for(int value : intArray){
			if(Constants.ZERO_INT != value) {
				return false;
			}
		}
		return true;
	}

	private void initializeSensorEvents(SensorManager mSensorManager) {
		// Set event generator' sensor list
		sensorEvents.add(new EventWeatherMaelstrom(
				EventWeatherMaelstrom.class.getSimpleName(),
				SensorType.TYPE_ACCELEROMETER,
				R.mipmap.img_event_whirlpool,
				R.mipmap.img_event_whirlpool_thumb,
				R.mipmap.img_sensor_accelerometer_thumb,
				R.string.event_whirlpool_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_ACCELEROMETER.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_MAGNETIC_FIELD,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_magnetic_field_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_MAGNETIC_FIELD.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_GYROSCOPE,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_gyroscope_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_GYROSCOPE.getCode())!= null));
		sensorEvents.add(new EventWeatherLight(
				EventWeatherLight.class.getSimpleName(),
				SensorType.TYPE_LIGHT,
				R.mipmap.img_event_light,
				R.mipmap.img_event_light_thumb,
				R.mipmap.img_sensor_light_thumb,
				R.string.event_light_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_LIGHT.getCode())!= null));
		sensorEvents.add(new EventDayNightCycle(
				EventDayNightCycle.class.getSimpleName(),
				SensorType.TYPE_PRESSURE,
				R.mipmap.img_event_day_night_cycle,
				R.mipmap.img_event_day_night_thumb,
				R.mipmap.img_sensor_pressure_thumb,
				R.string.event_day_night_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_PRESSURE.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_PROXIMITY,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_proximity_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_PROXIMITY.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_GRAVITY,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_gravity_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_GRAVITY.getCode())!= null));
		sensorEvents.add(new EventShakeClouds(
				EventShakeClouds.class.getSimpleName(),
				SensorType.TYPE_LINEAR_ACCELERATION,
				R.mipmap.img_movement_spawn,
				R.mipmap.img_event_clouds_thumb,
				R.mipmap.img_sensor_linear_acceleration_thumb,
				R.string.event_clouds_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_LINEAR_ACCELERATION.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_ROTATION_VECTOR,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_rotation_vector_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_ROTATION_VECTOR.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_RELATIVE_HUMIDITY,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_relative_humidity_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_RELATIVE_HUMIDITY.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_AMBIENT_TEMPERATURE,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_ambient_temperature_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_AMBIENT_TEMPERATURE.getCode())!= null));
	}

	private String toStringList(int[] preferenceIntArray) {
		StringBuilder builder = new StringBuilder();
		for(int value : preferenceIntArray){
			builder.append(value);
			builder.append(Constants.LIST_SEPARATOR);
		}
		return builder.toString();
	}

	private int[] extractActiveSensors(List<AppSensorEvent> sensorEvents) {
		int[] activeSensors = new int[sensorEvents.size()];
		for(int i = 0; i < sensorEvents.size(); i++){
			AppSensorEvent sensorEvent = sensorEvents.get(i);
			boolean sensorAvailable = sensorEvent.isSensorAvailable();
			boolean sensorActive = sensorEvent.isSensorActive();
			activeSensors[i] = (sensorAvailable && sensorActive)?1:0;
		}
		return activeSensors;
	}

	/**
	 * Launch the next activity in the starting game flow
	 * @param displayTutorial Sets if the launching game process should show the tutorial or not
	 * @param sensorTypes Set the sensor values as an array to be handled
	 */
	private void launchGame(boolean displayTutorial, int[] sensorTypes) {

		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(Constants.TAG_GAMES_NUMBER, ++mGamesNumber);
		editor.apply();

		if(displayTutorial && !mPreferences.getBoolean(Constants.PREF_TUTORIAL_ALREADY_SHOWN, false)){
			Intent tutorialIntent = new Intent(context, TutorialActivity.class);
			tutorialIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
			tutorialIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
			tutorialIntent.putExtra(Constants.TAG_LOAD_GAME, false);
			startActivity(tutorialIntent);
		} else {
			// Load game
			Intent screenIntent = new Intent(context, ScreenSelectionActivity.class);
			screenIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
			screenIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
			screenIntent.putExtra(Constants.TAG_LOAD_GAME, displayTutorial);
			screenIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, calculateMapHeight());
			screenIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, calculateMapWidth());
			startActivity(screenIntent);
		}
	}

	/**
	 * Launch the Sensor activity
	 */
	private void launchSensorActivity(){
		Intent checkSensorListIntent = new Intent(context, SensorActivity.class);
		checkSensorListIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
		startActivity(checkSensorListIntent);
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutMainMenu).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		// Run permissions request only the first time
		checkAppVersion();

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
				if (oldAppVersion > 0) {
					Log.i(TAG,String.format(Locale.ENGLISH, "App updated from version %d", oldAppVersion));
				} else {
					requestPermissionsFirstTime();
					Log.i(TAG, "App started for the first time");
				}

				// Show devices\' sensors list only after a fresh install or after an update.
				launchSensorActivity();
			} finally {
				SharedPreferences.Editor preferencesEditor = mPreferences.edit();
				preferencesEditor.putInt(Constants.APP_VERSION, currentAppVersionCode);
				preferencesEditor.apply();
			}
		} else {
			loadSettings();
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

		GameHelper.loadGameAtPreferences(this, new Player(), new Ship(), new Map(new Date(), Constants.MAP_MIN_HEIGHT, Constants.MAP_MIN_WIDTH));
		Map helperMap = GameHelper.helperMap;

		if(helperMap.getMapLength() == Constants.MAP_MIN_LENGTH) {
			btnLoadGame.setEnabled(false);
			mOverwriteWarning = false;
		} else {
			btnLoadGame.setEnabled(true);
			mOverwriteWarning = true;
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

			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity, R.style.Dialog_No_Border);
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

					boolean tutorialAlreadyShown = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_TUTORIAL_ALREADY_SHOWN, false);
					int appVersion = ((MainMenuActivity)getActivity()).mPreferences.getInt(Constants.APP_VERSION, Constants.ZERO_INT);

					boolean shipControlMode = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_IS_ACTIVE);
					boolean ammoControlMode = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_IS_ACTIVE);
					boolean shootControlMode = ((MainMenuActivity)getActivity()).mPreferences.getBoolean(Constants.PREF_SHOOT_CONTROL_MODE, Constants.PREF_IS_ACTIVE);

					SharedPreferences.Editor editor = ((MainMenuActivity)getActivity()).mPreferences.edit();
					editor.clear();
					// Sets default preferences
					editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, shipControlMode);
					editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, ammoControlMode);
					editor.putBoolean(Constants.PREF_SHOOT_CONTROL_MODE, shootControlMode);
					editor.putBoolean(Constants.TAG_EXE_MODE, Constants.isInDebugMode(((MainMenuActivity)getActivity()).mMode));
					editor.putBoolean(Constants.PREF_TUTORIAL_ALREADY_SHOWN, tutorialAlreadyShown);
					editor.putInt(Constants.APP_VERSION, appVersion);
					editor.apply();

					((MainMenuActivity)getActivity()).launchGame(true,	((MainMenuActivity)getActivity()).activeSensors);
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
			AlertDialog d = builder.create();
			d.setView(view, 0,0,0,0);
			return d;
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
