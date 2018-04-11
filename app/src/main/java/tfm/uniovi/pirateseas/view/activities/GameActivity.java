package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.sensors.events.EventDayNightCycle;
import tfm.uniovi.pirateseas.controller.sensors.events.EventShakeClouds;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherMaelstrom;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ammunitions;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.model.canvasmodel.ui.UIDisplayElement;
import tfm.uniovi.pirateseas.view.graphics.canvasview.CanvasView;

/**
 * 
 * @author p7166421
 *
 * @see: http://android-developers.blogspot.com.es/2011/11/making-android-games-that-play-nice.html
 */
public class GameActivity extends Activity implements SensorEventListener {

	private static final String TAG = "GameActivity";

	private Context context;

	private CanvasView mCanvasView;
	private static final int SENSOR_UPDATE_SECONDS = 2;

	protected int[] sensorTypes = null;
	protected long sensorLastTimestamp;

	boolean loadGame = false;

	private int lightLevel;
	private int mapHeight;
	private int mapWidth;

	SharedPreferences mPreferences;

	private boolean shipControlMode;
	private boolean ammoControlMode;
	private boolean levelControlMode;
	private boolean pauseControlMode;

	private float lastX, lastY, lastZ;

	protected SensorManager mSensorManager;
	protected List<Sensor> triggeringSensors;

	public ImageButton btnPause, btnChangeAmmo;
	public UIDisplayElement mGold, mAmmo;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		mCanvasView = new CanvasView(this);

		Intent data = getIntent();

		// Receive the device event triggering sensor list
		triggeringSensors = new ArrayList<Sensor>();
		sensorTypes = data.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		int totalSensors = 0;
		if(sensorTypes!= null)
			totalSensors = sensorTypes.length;
		for (int i = 0; i < totalSensors; i++) {
			if (sensorTypes[i] != 0) {
				triggeringSensors.add(mSensorManager.getDefaultSensor(sensorTypes[i]));
			}
		}

		loadGame = data.getBooleanExtra(Constants.TAG_LOAD_GAME, true);
		mapHeight = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, Constants.MAP_MIN_HEIGHT);
		mapWidth = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, Constants.MAP_MIN_WIDTH);

		mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);
		shipControlMode = mPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		ammoControlMode = mPreferences.getBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		levelControlMode = mPreferences.getBoolean(Constants.PREF_LEVEL_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		pauseControlMode = mPreferences.getBoolean(Constants.PREF_PAUSE_CONTROL_MODE, Constants.PREF_GAME_TOUCH);

		// Launch the game!!
		setContentView(R.layout.activity_game);

		btnPause = (ImageButton) findViewById(R.id.btnPause);
		btnPause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent pauseIntent = new Intent(context, PauseActivity.class);
				context.startActivity(pauseIntent);
				Log.d(TAG, "Start Pause Intent");
			}
		});

		btnChangeAmmo = (ImageButton) findViewById(R.id.btnChangeAmmo);
		btnChangeAmmo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				CanvasView currentCView = mCanvasView.nUpdateThread.getCanvasViewInstance();
				Ship playerShip = currentCView.nPlayerShip;
				if(playerShip != null) {
					currentCView.selectNextAmmo();
					view.setBackgroundResource(Ammunitions.values()[playerShip.getSelectedAmmunition()].drawableValue());
					mAmmo.setElementValue(playerShip.getAmmunition(Ammunitions.values()[playerShip.getSelectedAmmunition()]));
				}
			}
		});

		mGold = (UIDisplayElement) findViewById(R.id.playerGold);
		mGold.setElementValue(0);
		mAmmo = (UIDisplayElement) findViewById(R.id.playerAmmunition);
		mAmmo.setElementValue(0);
	}

	public boolean hasToLoadGame() {
		return loadGame;
	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(this);

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (mCanvasView != null)
			mCanvasView.setStatus(Constants.GAME_STATE_PAUSE);

		super.onPause();
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutGame).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mCanvasView.setStatus(Constants.GAME_STATE_NORMAL);

		if (!CanvasView.nUpdateThread.isAlive() && CanvasView.nUpdateThread.getState() != Thread.State.NEW) {
			if (!Constants.isInDebugMode(Constants.MODE))
				Log.e(TAG, "MainLogic is DEAD. Re-starting...");
			mCanvasView.launchMainLogic();
			CanvasView.nUpdateThread.start();
		}

		for (int i = 0, size = triggeringSensors.size(); i < size; i++) {
			Sensor s = triggeringSensors.get(i);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
		}

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		exitGame();
	}

	public void exitGame(){
		// Pop up messageBox asking if the user is sure to leave
		LeaveGameDialogFragment exitDialog = new LeaveGameDialogFragment();
		exitDialog.show(getFragmentManager(), "LeaveGameDialog");
	}

	public static class LeaveGameDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			builder.setTitle(getResources().getString(R.string.exit_dialog_title))
					.setMessage(R.string.exit_dialog_message)
					.setPositiveButton(R.string.exit_dialog_positive, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Exit
							// mCanvasView.setStatus(Constants.GAME_STATE_END);
							// CanvasView.nUpdateThread.setRunning(false);
							Log.d(TAG,"Finish Game Activity");
							dummyActivity.finish();
						}
					}).setNegativeButton(R.string.exit_dialog_negative, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancels the dialog
						}
					});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		long deltaTime = event.timestamp - sensorLastTimestamp;
		double deltaSeconds = deltaTime * Constants.NANOS_TO_SECONDS;
		if (arrayContainsValue(sensorTypes, sensor.getType())) {
			switch (sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) { // Hold sensor
																// updates
					// Parameters
					float axisSpeedX = event.values[0];
					float axisSpeedY = event.values[1];
					float axisSpeedZ = event.values[2];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_ACCELEROMETER: Gravity (m/s^2): " + axisSpeedX + " / " + axisSpeedY + " / "
								+ axisSpeedZ);

					// Event
					if (EventWeatherMaelstrom.generateMaelstrom(axisSpeedY, axisSpeedZ)) {
						// Notify CanvasView to damage the ships
						if (mCanvasView.getGamemode() == Constants.GAMEMODE_BATTLE) {
							Toast.makeText(context, "Maelstorm inbound!", Toast.LENGTH_SHORT).show();
							mCanvasView.maelstorm();
						}
					} else {
						// TODO Gestionar los movimientos del barco del jugador dependiendo de los valores de los sensores
						// @see: https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
						if(mCanvasView.getGamemode() == Constants.GAMEMODE_BATTLE) {
							if(mCanvasView.nPlayerShip != null && mCanvasView.nPlayerShip.isAlive()){

								float speed = Math.abs(axisSpeedX + axisSpeedY + axisSpeedZ - lastX - lastY - lastZ);
								Log.d(TAG, "TYPE_ACCELEROMETER: Detected speed is: " + speed);

								// mCanvasView.nPlayerShip.move(0,0,true);
							}
						}
					}

					lastX = axisSpeedX;
					lastY = axisSpeedY;
					lastZ = axisSpeedZ;
					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) {
					// Parameters
					float microTeslaX = event.values[0];
					float microTeslaY = event.values[1];
					float microTeslaZ = event.values[2];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_MAGNETIC_FIELD: Magnetic field (uT): " + microTeslaX + " / " + microTeslaY
								+ " / " + microTeslaZ);

					// Event
					// Establish an event in a future version of the game

					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_GYROSCOPE:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) { // Hold sensor
																// updates
					// Parameters
					float axisSpeedX = event.values[0];
					float axisSpeedY = event.values[1];
					float axisSpeedZ = event.values[2];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_GYROSCOPE: Gyroscope (rad/s): x = " + axisSpeedX + "; y = " + axisSpeedY
								+ "; z = " + axisSpeedZ);

					// Event
					// Establish an event in a future version of the game

					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_LIGHT:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) { // Hold sensor
																// updates
					// Parameters
					float lux = event.values[0];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_LIGHT: Light (l): " + lux);

					// Event
					// Save light level as global variable
					lightLevel = (int) lux;

					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_PRESSURE:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) { // Hold sensor
																// updates
					// Parameters
					float millibar = event.values[0];

					// Event
					EventDayNightCycle.pressure = millibar;

					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_PROXIMITY:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) {
					// Parameters
					float centimeters = event.values[0];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG,
								"TYPE_PROXIMITY: Proximity (cm): " + centimeters + " // " + sensor.getMaximumRange());

					// Event
					// Establish an event in a future version of the game

					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_GRAVITY:
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) { // Hold sensor
																// updates
					// Parameters
					float linearAccelerationX = event.values[0];
					float linearAccelerationY = event.values[1];
					float linearAccelerationZ = event.values[2];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_LINEAR_ACCELERATION: Acceleration force (m/s^2): " + linearAccelerationX
								+ " / " + linearAccelerationY + " / " + linearAccelerationZ);

					// Event
					Log.d(TAG, "TYPE_LINEAR_ACCELERATION: Acceleration force (m/s^2): " + linearAccelerationX  + " < " + EventShakeClouds.threshold);
					if (Math.abs(linearAccelerationX) > EventShakeClouds.threshold){
						shakeClouds();
					}

					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_ROTATION_VECTOR:
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) { // Hold sensor
																// updates
					// Parameters
					float airHumidityPercent = event.values[0];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_RELATIVE_HUMIDITY: Humidity (%): " + airHumidityPercent + " // "
								+ sensor.getMaximumRange());

					// Event
					// Establish an event in a future version of the game

					sensorLastTimestamp = event.timestamp;
				}
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				break;
			}
		}
	}

	/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ComponentName caller = this.getCallingActivity();
		if (caller != null)
			Log.d(TAG, "GameActivity called by " + caller);

		if(data!= null){
			int canvasGameMode = mCanvasView.getGamemode();
			
			if (!mCanvasView.isInitialized()) {
				mCanvasView.initialize();
			}
			switch (requestCode) {
				case Constants.REQUEST_SCREEN_SELECTION:
					if (resultCode == Activity.RESULT_OK) {
						// Get randomized encounter boolean value from Intent data
						boolean encounter = data.getBooleanExtra(Constants.TAG_RANDOM_ENCOUNTER, false);
						// If true check if light level is lower than XX level
						if (encounter) {
							// If it is lower, generate fog (where an enemy will be
							// hiding)
							if (lightLevel < Constants.LIGHT_THRESHOLD) {
								if(canvasGameMode == Constants.GAMEMODE_ADVANCE){
									Log.d(TAG, "Spawn fog on CanvasView (" + lightLevel + " < " + Constants.LIGHT_THRESHOLD + ")");
									mCanvasView.setGamemode(Constants.GAMEMODE_IDLE);
									Log.d(TAG, "GameActivity: GameMode set to IDLE");
									mCanvasView.spawnClouds();
								} else if (mCanvasView.getGamemode() == Constants.GAMEMODE_IDLE){
									
								}
							}
							// Else show enemy
							else {
								if (canvasGameMode != Constants.GAMEMODE_BATTLE){
									mCanvasView.setGamemode(Constants.GAMEMODE_BATTLE);
									Log.d(TAG, "GameActivity: GameMode set to BATTLE");
									// MusicManager.getInstance().stopBackgroundMusic();
									// MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
								}
								Log.d(TAG, "Spawn enemy on CanvasView CanvasView (" + lightLevel + " < "
										+ Constants.LIGHT_THRESHOLD + ")");
								mCanvasView.spawnEnemyShip();
							}
		
						}
						// Else call mCanvasView.selectScreen(); again
						else {
							try {
								mCanvasView.selectScreen();
							} catch (SaveGameException e) {
								Log.e(TAG, e.getMessage());
							}
						}
					}
					break;
			}
	
			mCanvasView.launchMainLogic();
		}
	}
	*/

	private boolean arrayContainsValue(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value)
				return true;
		}
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Sensor " + sensor.getName() + " got changed in " + accuracy);
	}

	public void gameOver(Player nPlayer) {
		Intent gameOverIntent = new Intent(this, GameOverActivity.class);
		// Parcelable Extra with Player object content
		gameOverIntent.putExtra(Constants.TAG_GAME_OVER, Player.clonePlayer(nPlayer));
		Log.d(TAG, "Start GameOver Intent");
		this.startActivity(gameOverIntent);
		shutdownGame();
	}

	/**
	 * Free resources
	 */
	public void shutdownGame() {
		mCanvasView.pauseLogicThread();
		mCanvasView = null;
		Log.d(TAG,"Finish Game Activity");
		finish();
	}

	public void shakeClouds() {
		if(mCanvasView != null) {
			int shakeCount = mCanvasView.getShakeMoveCount();
			mCanvasView.setShakeMoveCount(shakeCount + 1);
			Toast.makeText(context, String.format(getResources().getString(R.string.message_shakesleft), (Constants.SHAKE_LIMIT - (shakeCount + 1))), Toast.LENGTH_SHORT).show();
		}
	}

	public int[] getSensorTypes(){
		return sensorTypes;
	}

	public int getMapHeight(){
		return mapHeight;
	}

	public int getMapWidth(){
		return mapWidth;
	}

}