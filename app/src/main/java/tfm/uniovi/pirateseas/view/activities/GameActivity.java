package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.VoiceInteractor;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.sensors.events.EventDayNightCycle;
import tfm.uniovi.pirateseas.controller.sensors.events.EventShakeClouds;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherMaelstrom;
import tfm.uniovi.pirateseas.exceptions.NoAmmoException;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ammunitions;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.model.canvasmodel.ui.UIDisplayElement;
import tfm.uniovi.pirateseas.view.graphics.canvasview.CanvasView;

/**
 * Activity that hold the game Canvas and the UI layer
 *
 * @see: http://android-developers.blogspot.com.es/2011/11/making-android-games-that-play-nice.html
 */
public class GameActivity extends AppCompatActivity implements SensorEventListener {

	private static final String TAG = "GameActivity";
	private static final long DURATION_MILLIS = 1500;

	private Context context;

	private CanvasView mCanvasView;
	private static final int SENSOR_UPDATE_SECONDS = 2;
	private static final int ACCELEROMETER_THRESHOLD = 2;

	protected int[] sensorTypes = null;
	protected long sensorLastTimestamp;

	boolean loadGame = false;

	private int lightLevel;
	private int mapHeight;
	private int mapWidth;

	SharedPreferences mPreferences;

	private boolean shipControlMode;
	private boolean ammoControlMode;

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

		// Launch the game!!
		setContentView(R.layout.activity_game);

		btnPause = findViewById(R.id.btnPause);
		btnPause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent pauseIntent = new Intent(context, PauseActivity.class);
				CanvasView currentCView = mCanvasView.nUpdateThread.getCanvasViewInstance();
				Ship playerShip = currentCView.nPlayerShip;
				pauseIntent.putExtra(Constants.PAUSE_SHIP, playerShip);
				context.startActivity(pauseIntent);
				Log.d(TAG, "Start Pause Intent");
			}
		});

		btnChangeAmmo = findViewById(R.id.btnChangeAmmo);
		btnChangeAmmo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(ammoControlMode) {
					CanvasView currentCView = mCanvasView.nUpdateThread.getCanvasViewInstance();
					Ship playerShip = currentCView.nPlayerShip;
					if (playerShip != null) {
						currentCView.selectNextAmmo();
						int selectedAmmo = playerShip.getSelectedAmmunition();
						int drawValue = Ammunitions.values()[playerShip.getSelectedAmmunitionIndex()].drawableValue();
						btnChangeAmmo.setImageResource(drawValue);
						btnChangeAmmo.invalidate();
						mAmmo.setElementValue(selectedAmmo);
					}
				}
			}
		});

		mGold = findViewById(R.id.playerGold);
		mGold.setElementValue(0);
		mAmmo = findViewById(R.id.playerAmmunition);
		mAmmo.setElementValue(0);

	}

	/**
	 * Show temporary descriptions (like a tooltip)
	 * @param text
	 */
	public void showText(String text){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View v = findViewById(R.id.rootLayoutGame);
			Snackbar gameSnackbar = Snackbar.make(v, text, Snackbar.LENGTH_SHORT);
			gameSnackbar.show();
		} else {
			LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup layoutGroup = getWindow().getDecorView().findViewById(android.R.id.content);
			mInflater.inflate(R.layout.custom_snackbar, layoutGroup);
			TextView txtMessage = findViewById(R.id.txtCanvasMsg);
			txtMessage.setAlpha(0f);
			txtMessage.setText(text);
			txtMessage.animate().alpha(1f).setDuration(DURATION_MILLIS);
			txtMessage.animate().alpha(0f).setDuration(DURATION_MILLIS);
		}
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

		// Reload saved settings in preferences
		mCanvasView.nUpdateThread.getCanvasViewInstance().loadSettings();

		super.onResume();

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			if (isVoiceInteraction()) {
				startVoiceTrigger();
			}
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private void startVoiceTrigger() {
		Log.d(TAG, "startVoiceTrigger: ");
		Option option = null;

		option = new Option("fuego", 0);

		option.addSynonym("dispara");
		option.addSynonym("ok");
		option.addSynonym("ya");
		option.addSynonym("fire");
		option.addSynonym("shoot");

		getVoiceInteractor()
				.submitRequest(new PickOptionRequest(new VoiceInteractor.Prompt("go"), new Option[]{option}, null) {
					@Override
					public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
						if (finished && selections.length == 1) {
							Message message = Message.obtain();
							message.obj = result;
							// Shoot action
                            try {
                                mCanvasView.nShotList.add(mCanvasView.nPlayerShip.shootCannon());
                            } catch (NoAmmoException e) {
                                showText(e.getMessage());
                            }
                        } else {
							getActivity().finish();
						}
					}
					@Override
					public void onCancel() {
						getActivity().finish();
					}
				});
	}


	@Override
	public void onBackPressed() {
		exitGame();
	}

	/**
	 * Class to create a Dialog that asks the player if he/she is sure of leaving the game activity
	 */
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
        CanvasView cView = mCanvasView.nUpdateThread.getCanvasViewInstance();
		if (arrayContainsValue(sensorTypes, sensor.getType())) {
			switch (sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:

				if (deltaSeconds >= SENSOR_UPDATE_SECONDS) { // Hold sensor updates
					// Parameters
					float axisSpeedX = event.values[0];
					float axisSpeedY = event.values[1];
					float axisSpeedZ = event.values[2];

					double angleX = Math.toDegrees(Math.asin (axisSpeedX / SensorManager.GRAVITY_EARTH));
					double angleY = Math.toDegrees(Math.asin (axisSpeedY / SensorManager.GRAVITY_EARTH));
					double angleZ = Math.toDegrees(Math.asin (axisSpeedZ / SensorManager.GRAVITY_EARTH));

					Log.d(TAG, "TYPE_ACCELEROMETER: Acc:angle = "+axisSpeedX+":"+angleX+"º / "+axisSpeedY+":"+angleY+"º / "+axisSpeedZ+":"+angleZ+"º	");
					// Event
					if (EventWeatherMaelstrom.generateMaelstrom(axisSpeedY, axisSpeedZ)) {
						// Notify CanvasView to damage the ships
						if (cView.getGamemode() == Constants.GAMEMODE_BATTLE) {
							showText("Maelstorm inbound!");

							Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
							// Vibrate for 500 milliseconds
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
								v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
							} else {
								//deprecated in API 26
								v.vibrate(500);
							}

							cView.maelstorm();
						}
					}
				}

				// Gestionar los movimientos del barco del jugador dependiendo de los valores de los sensores
				// @see: https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
				if(cView.getGamemode() == Constants.GAMEMODE_BATTLE) {
					// Parameters
					float axisSpeedX = event.values[0];
					float axisSpeedY = event.values[1];
					float axisSpeedZ = event.values[2];

					if (!shipControlMode && battleIsGoing(cView)) {
						if (Math.abs(axisSpeedY) >= ACCELEROMETER_THRESHOLD) {
							int shipSpeed = cView.nPlayerShip.getShipType().getSpeed();
							float speed = Math.abs(axisSpeedX + axisSpeedY + axisSpeedZ - lastX - lastY - lastZ);

							Log.d(TAG, "TYPE_ACCELEROMETER: Ship movement would be: " + shipSpeed + " + " + speed + " = " + (shipSpeed + speed)
									+ " to the " + (axisSpeedY < 0 ? "left" : "right"));
							if (axisSpeedY < 0) {
								cView.nPlayerShip.move((shipSpeed + speed), 0, true);
								cView.nPlayerShip.moveShipEntity(new Point(cView.nPlayerShip.getCoordinates().x - 1, cView.nPlayerShip.getCoordinates().y));
							} else if (axisSpeedY > 0) {
								cView.nPlayerShip.move(-(shipSpeed + speed), 0, true);
								cView.nPlayerShip.moveShipEntity(new Point(cView.nPlayerShip.getCoordinates().x + 1, cView.nPlayerShip.getCoordinates().y));
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
					float axisGyroSpeedX = event.values[0];
					float axisGyroSpeedY = event.values[1];
					float axisGyroSpeedZ = event.values[2];

					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_GYROSCOPE: Gyroscope (rad/s): x = " + axisGyroSpeedX + "; y = " + axisGyroSpeedY
								+ "; z = " + axisGyroSpeedZ);

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

	/**
	 * Checks whether an active battle is taking place
	 * @param cView
	 * @return true if there is an alive enemy and the player is still alive
	 */
	private boolean battleIsGoing(CanvasView cView) {
		return cView.nPlayerShip != null && cView.nPlayerShip.isAlive() && cView.nEnemyShip != null && cView.nEnemyShip.isAlive();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!ammoControlMode) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				// Change to the next type
				Ship playerShip = mCanvasView.nPlayerShip;
				if(playerShip != null) {
					playerShip.selectNextAmmo();
					int selectedAmmo = playerShip.getSelectedAmmunition();
					int drawValue = Ammunitions.values()[playerShip.getSelectedAmmunitionIndex()].drawableValue();
					btnChangeAmmo.setImageResource(drawValue);
					btnChangeAmmo.invalidate();
					mAmmo.setElementValue(selectedAmmo);
				}
			} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				// Change to the previous type
				Ship playerShip = mCanvasView.nPlayerShip;
				if(playerShip != null) {
					playerShip.selectPreviousAmmo();
					int selectedAmmo = playerShip.getSelectedAmmunition();
					int drawValue = Ammunitions.values()[playerShip.getSelectedAmmunitionIndex()].drawableValue();
					btnChangeAmmo.setImageResource(drawValue);
					btnChangeAmmo.invalidate();
					mAmmo.setElementValue(selectedAmmo);
				}
			}
		}
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Sensor " + sensor.getName() + " got changed in " + accuracy);
	}

	/**
	 * Checks if an integer array has a certain value in it
	 * @param array
	 * @param value
	 * @return true if it hold it, false otherwise
	 */
	private boolean arrayContainsValue(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value)
				return true;
		}
		return false;
	}

	/**
	 * Calls the activity to end the game
	 * @param nPlayer
	 * @param map
	 */
	public void gameOver(Player nPlayer, Map map) {
		Intent gameOverIntent = new Intent(this, GameOverActivity.class);
		// Parcelable Extra with Player object content
		gameOverIntent.putExtra(Constants.TAG_GAME_OVER_PLAYER, Player.clonePlayer(nPlayer));
		gameOverIntent.putExtra(Constants.TAG_GAME_OVER_MAP, map);
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

	/**
	 * Exit the game and go back to hte main menu screen
	 */
	public void exitGame(){
		// Pop up messageBox asking if the user is sure to leave
		LeaveGameDialogFragment exitDialog = new LeaveGameDialogFragment();
		exitDialog.show(getFragmentManager(), "LeaveGameDialog");
	}

	/**
	 * Shake the clouds once
	 */
	public void shakeClouds() {
		if(mCanvasView != null) {
			int shakeCount = mCanvasView.getShakeMoveCount();
			mCanvasView.setShakeMoveCount(shakeCount + 1);
			showText(String.format(getResources().getString(R.string.message_shakesleft), (Constants.SHAKE_LIMIT - (shakeCount + 1))));
		}
	}

	/**
	 * checks if there is a saved game
	 * @return true if a saved game exists on the preferences, false otherwise
	 */
	public boolean hasToLoadGame() {
		return loadGame;
	}

	/**
	 * Get the sensor types array
	 * @return The device's sensor types integer array
	 */
	public int[] getSensorTypes(){
		return sensorTypes;
	}

	/**
	 * Get the number of vertical cells fro the map to fit in the device's screen
	 * @return Number of cells
	 */
	public int getMapHeight(){
		return mapHeight;
	}

	/**
	 * Get the number of horizontal cells fro the map to fit in the device's screen
	 * @return Number of cells
	 */
	public int getMapWidth(){
		return mapWidth;
	}
}