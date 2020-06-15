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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.controller.sensors.SensorType;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
import tfm.uniovi.pirateseas.controller.sensors.events.EventDayNightCycle;
import tfm.uniovi.pirateseas.controller.sensors.events.EventShakeClouds;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherMaelstrom;
import tfm.uniovi.pirateseas.exceptions.NoAmmoException;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ammunitions;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.model.canvasmodel.ui.UIDisplayElement;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;
import tfm.uniovi.pirateseas.view.graphics.canvasview.CanvasView;

/**
 * Activity that hold the game Canvas and the UI layer
 *
 * @see: http://android-developers.blogspot.com.es/2011/11/making-android-games-that-play-nice.html
 */
@SuppressWarnings("AccessStaticViaInstance")
public class GameActivity extends AppCompatActivity implements SensorEventListener {

	private static final String TAG = "GameActivity";
	private static final long DURATION_MILLIS = 1500;
	private static final long MAELSTORM_COOLDOWN = (long) (60 * Math.pow(10, 9));

	private Context context;

	private CanvasView mCanvasView;
	private ImageView imgMicStatus;
	private static final int SENSOR_UPDATE_SECONDS = 2;
	private static final int ACCELEROMETER_THRESHOLD = 2;
    private static final int REQUEST_RECORD_PERMISSION = 100;

    protected int[] sensorTypes = null;
    protected long sensorLastTimestamp;

    private long lastClickTimestamp = 0;
    private long battleStartedTimestamp = 0;
    private long lastMaelstormTimestamp = 0;

    boolean loadGame = false;

    private int mapHeight;
    private int mapWidth;

    SharedPreferences mPreferences;

    private boolean shipControlMode;
    private boolean ammoControlMode;
    private boolean shootControlMode;

    private float lastX, lastY, lastZ;

    protected SensorManager mSensorManager;
    protected List<Sensor> triggeringSensors;
	private List<AppSensorEvent> sensorEvents;

    public ImageButton btnPause, btnChangeAmmo;
    public UIDisplayElement mAmmo;

    public ProgressBar nPlayerHealthBar;

    private ProgressBar nShakesForceBar;

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private boolean mIsListening;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		mCanvasView = new CanvasView(this);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        mSpeechRecognizer.setRecognitionListener(listener);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                getString(R.string.lang));
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

		sensorEvents = new ArrayList<>();

		Intent data = getIntent();
		sensorEvents = data.getParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS);

		// Receive the device event triggering sensor list
		triggeringSensors = new ArrayList<>();
		sensorTypes = data.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		int totalSensors = 0;
		if(sensorTypes!= null)
			totalSensors = sensorTypes.length;
		for (int i = 0; i < totalSensors; i++) {
			if (sensorTypes[i] != 0) {
				int sensorCode = SensorType.values()[i].getCode();
				triggeringSensors.add(mSensorManager.getDefaultSensor(sensorCode));
			}
		}

		loadGame = data.getBooleanExtra(Constants.TAG_LOAD_GAME, true);
		mapHeight = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, Constants.MAP_MIN_HEIGHT);
		mapWidth = data.getIntExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, Constants.MAP_MIN_WIDTH);

		mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);
		shipControlMode = mPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, !Constants.PREF_IS_ACTIVE);
		ammoControlMode = mPreferences.getBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_IS_ACTIVE);
		shootControlMode = mPreferences.getBoolean(Constants.PREF_SHOOT_CONTROL_MODE, !Constants.PREF_IS_ACTIVE);

		// Launch the game!!
		setContentView(R.layout.activity_game);

		btnPause = findViewById(R.id.btnPause);
		btnPause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(SystemClock.elapsedRealtime() - lastClickTimestamp < 1000){
					return;
				}

				lastClickTimestamp = SystemClock.elapsedRealtime();

				Intent pauseIntent = new Intent(context, PauseActivity.class);
				CanvasView currentCView = mCanvasView.nUpdateThread.getCanvasViewInstance();
				Ship nPlayerShip = currentCView.nPlayerShip;
				Player nPlayer = currentCView.nPlayer;
				Map nMap = currentCView.nMap;
				currentCView.pauseLogicThread();
				pauseIntent.putExtra(Constants.PAUSE_SHIP, nPlayerShip);
				pauseIntent.putExtra(Constants.PAUSE_PLAYER, nPlayer);
				pauseIntent.putExtra(Constants.PAUSE_MAP, nMap);
				pauseIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
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

		imgMicStatus = findViewById(R.id.imgMicStatus);

		mAmmo = findViewById(R.id.playerAmmunition);
		mAmmo.setElementValue(0);

        nPlayerHealthBar = findViewById(R.id.prgHealthBar);
        nPlayerHealthBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

		nShakesForceBar = findViewById(R.id.shakesProgressBar);
		nShakesForceBar.setMax((int) EventShakeClouds.threshold);
		nShakesForceBar.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
	}

    /**
     * Method called with the result of the permissions request
     * @param requestCode requested code
     * @param permissions requested permissions
     * @param grantResults granted permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_RECORD_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if(shootControlMode) {
					mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
					imgMicStatus.setImageResource(R.drawable.ic_mic_usage);
				}
			} else {
				showText("Permission Denied!");
			}
		}
    }

	/**
	 * Show temporary descriptions (like a tooltip)
	 * @param text Text string to show
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

		if(!isFinishing())
			MusicManager.getInstance().pauseBackgroundMusic();

		super.onPause();
	}

    @Override
    protected void onDestroy() {
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
        super.onDestroy();
    }

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutGame).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mCanvasView.setStatus(Constants.GAME_STATE_NORMAL);

		if (!CanvasView.nUpdateThread.isAlive() && CanvasView.nUpdateThread.getState() != Thread.State.NEW) {
			if (Constants.isInDebugMode(Constants.MODE))
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
		// Check if the voice commands have been activated. Ready the speechRecognizer
		shipControlMode = mCanvasView.nUpdateThread.getCanvasViewInstance().getShipControlMode();
		shootControlMode = mCanvasView.nUpdateThread.getCanvasViewInstance().getShootControlMode();

		if(!MusicManager.getInstance().isPlaying())
			MusicManager.getInstance().playBackgroundMusic();

		super.onResume();
	}

	@Override
	public void onBackPressed() {
		exitGame();
	}

    public void updateHealthBar(int health, int max) {
	    nPlayerHealthBar.setProgress(health);
	    nPlayerHealthBar.setMax(max);
    }

	public void startVoiceRecognitionRequest() {
		if(shootControlMode){
			if(!mIsListening) {
				ActivityCompat.requestPermissions
						(GameActivity.this,
								new String[]{Manifest.permission.RECORD_AUDIO},
								REQUEST_RECORD_PERMISSION);
				imgMicStatus.setImageResource(R.drawable.ic_mic_usage);
				mIsListening = true;
			} else {
				mSpeechRecognizer.stopListening();
				imgMicStatus.setImageResource(R.drawable.ic_mic_enabled);
				mIsListening = false;
			}
		} else {
			imgMicStatus.setImageResource(R.drawable.ic_mic_disabled);
		}
	}

	/**
	 * Class to create a Dialog that asks the player if he/she is sure of leaving the game activity
	 */
	public static class LeaveGameDialogFragment extends DialogFragment {
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
			txtTitle.setText(getResources().getString(R.string.exit_dialog_title));
			txtMessage.setText(getResources().getString(R.string.exit_dialog_message));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// Exit
					// canvasView.setStatus(Constants.GAME_STATE_END);
					// CanvasView.nUpdateThread.setRunning(false);
					Log.d(TAG,"Finish Game Activity");
					CanvasView currentCanvasView = ((GameActivity)dummyActivity).mCanvasView.nUpdateThread.getCanvasViewInstance();
					Ship nPlayerShip = currentCanvasView.nPlayerShip;
					Player nPlayer = currentCanvasView.nPlayer;
					Map nMap = currentCanvasView.nMap;
					nMap.setActiveCell(nMap.getLastActiveCell());
					GameHelper.saveGameAtPreferences(dummyActivity, nPlayer, nPlayerShip, nMap);
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

	public static class EnemyDefeatedFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			int gold = getArguments().getInt(Constants.ARG_GOLD, 0);
			int xp = getArguments().getInt(Constants.ARG_XP, 0);
			boolean mapPiece = getArguments().getBoolean(Constants.ARG_MAP_PIECE);
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity, R.style.Dialog_No_Border);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_positive_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			txtTitle.setText(getResources().getString(R.string.game_message_enemy_defeated_title));
			if(mapPiece){
				txtMessage.setText(getResources().getString(R.string.generic_strings_join, getResources().getString(R.string.game_message_enemy_defeated, gold, xp), getResources().getString(R.string.game_message_heavy_enemy_defeated)));
			} else {
				txtMessage.setText(String.format(getResources().getString(R.string.game_message_enemy_defeated), gold, xp));
			}
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// Exit
					setMessageAsRead(true);
					dismiss();
				}
			});
			builder.setView(view);

			// Create the AlertDialog object and return it
			return builder.create();
		}

		private void setMessageAsRead(boolean b) {
			((GameActivity)getActivity()).mCanvasView.nUpdateThread.getCanvasViewInstance().setMessageReaded(b);
		}
	}

	public static class PlayerDefeatedFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity, R.style.Dialog_No_Border);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_positive_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			txtTitle.setText(getResources().getString(R.string.game_message_player_defeated_title));
			txtMessage.setText(getResources().getString(R.string.game_message_player_defeated));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// Exit
					setMessageAsRead(true);
					dismiss();
				}
			});
			builder.setView(view);

			// Create the AlertDialog object and return it
			return builder.create();
		}

		private void setMessageAsRead(boolean b) {
			((GameActivity)getActivity()).mCanvasView.nUpdateThread.getCanvasViewInstance().setMessageReaded(b);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		long deltaTime = event.timestamp - sensorLastTimestamp;
		double deltaSeconds = deltaTime * Constants.NANOS_TO_SECONDS;
        CanvasView cView = mCanvasView.nUpdateThread.getCanvasViewInstance();
		if (sensorIsActive(sensorTypes, sensor.getType())) {
			switch (sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				// Gestionar los movimientos del barco del jugador dependiendo de los valores de los sensores
				// @see: https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
				if(cView.getGamemode() == Constants.GAMEMODE_BATTLE) {
					// Parameters
					float axisSpeedX = event.values[0];
					float axisSpeedY = event.values[1];
					float axisSpeedZ = event.values[2];

					long timeSinceBattleStarted = 0;
					long timeSinceLastMaelstorm = 0;

					if (!shipControlMode && battleIsGoing(cView)) {
						if (battleStartedTimestamp == 0)
							battleStartedTimestamp = event.timestamp;
						timeSinceBattleStarted = event.timestamp - battleStartedTimestamp;
						timeSinceLastMaelstorm = event.timestamp - lastMaelstormTimestamp;
						if (Math.abs(axisSpeedY) >= ACCELEROMETER_THRESHOLD) {
							int shipSpeed = cView.nPlayerShip.getShipType().getSpeed();
							float speed = Math.abs(axisSpeedX + axisSpeedY + axisSpeedZ - lastX - lastY - lastZ);

							// Log.d(TAG, "TYPE_ACCELEROMETER: Ship movement would be: " + shipSpeed + " + " + speed + " = " + (shipSpeed + speed) + " to the " + (axisSpeedY < 0 ? "left" : "right"));
							if (axisSpeedY < 0) {
								cView.nPlayerShip.move((shipSpeed + speed), 0, true);
								cView.nPlayerShip.moveShipEntity(new Point(cView.nPlayerShip.getCoordinates().x - 1, cView.nPlayerShip.getCoordinates().y));
							} else if (axisSpeedY > 0) {
								cView.nPlayerShip.move(-(shipSpeed + speed), 0, true);
								cView.nPlayerShip.moveShipEntity(new Point(cView.nPlayerShip.getCoordinates().x + 1, cView.nPlayerShip.getCoordinates().y));
							}
						}

						if (EventWeatherMaelstrom.generateMaelstrom(axisSpeedY) &&
								(timeSinceBattleStarted >= Constants.GRACE_PERIOD) &&
								(timeSinceLastMaelstorm >= MAELSTORM_COOLDOWN)) {
							lastMaelstormTimestamp = event.timestamp;
							// Notify CanvasView to damage the ships
							Log.d(TAG, "Maelstorm generated @ " + sensorLastTimestamp + " (" + axisSpeedY + ")");
							showText(getString(R.string.maelstorm_inbound));

							Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
							long vibrationDuration = 500;
							// Vibrate for 500 milliseconds
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
								v.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
							} else {
								//deprecated in API 26
								v.vibrate(vibrationDuration);
							}

							cView.maelstorm();

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

					/*
					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_MAGNETIC_FIELD: Magnetic field (uT): " + microTeslaX + " / " + microTeslaY + " / " + microTeslaZ);
					*/

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

					/*
					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_GYROSCOPE: Gyroscope (rad/s): x = " + axisGyroSpeedX + "; y = " + axisGyroSpeedY 	+ "; z = " + axisGyroSpeedZ);
					*/

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

					/*
					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_LIGHT: Light (l): " + lux);
					*/

					// Event
					// Save light level as global variable
                    int lightLevel = (int) lux;

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

					/*
					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_PROXIMITY: Proximity (cm): " + centimeters + " // " + sensor.getMaximumRange());
					*/

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

					/*
					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_LINEAR_ACCELERATION: Acceleration force (m/s^2): " + linearAccelerationX
								+ " / " + linearAccelerationY + " / " + linearAccelerationZ);
					*/

					// Event
					// Log.d(TAG, "TYPE_LINEAR_ACCELERATION: Acceleration force (m/s^2): " + linearAccelerationX  + " < " + EventShakeClouds.threshold);
					float currentValue = Math.abs(linearAccelerationX);
					nShakesForceBar.setProgress((int) currentValue);
					if (currentValue > EventShakeClouds.threshold){
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

					/*
					if (!Constants.isInDebugMode(Constants.MODE))
						Log.d(TAG, "TYPE_RELATIVE_HUMIDITY: Humidity (%): " + airHumidityPercent + " // "
								+ sensor.getMaximumRange());
					*/

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
	 * @param cView canvas view
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
	    /*
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Sensor " + sensor.getName() + " got changed in " + accuracy);
		*/
	}

	/**
	 * Checks if an integer array has a certain value in it
	 * @param array array
	 * @param value value to look for inside the array
	 * @return true if it hold it, false otherwise
	 */
	private boolean sensorIsActive(int[] array, int value) {
        // Coger la lista de sensorEvents,
        // extraer la posicion del AppSensorEvent con codigo 'value' y
        // verificar si esa posicion esta a 1 en 'array'
        int position = findAppSensorEventIndexByCode(value);
        return position != -1 && array[position] == 1;
	}

    private int findAppSensorEventIndexByCode(int value) {
	    for(int i = 0; i < sensorEvents.size(); i++){
	        AppSensorEvent event = sensorEvents.get(i);
	        if(event.getSensorType().getCode() == value) {
                return i;
            }
        }
	    return -1;
    }

    /**
	 * Calls the activity to end the game
	 * @param nPlayer player object
	 * @param map map object
	 */
	public void gameOver(Player nPlayer, Map map) {
		Intent gameOverIntent = new Intent(this, GameOverActivity.class);
		// Parcelable Extra with Player object content
		gameOverIntent.putExtra(Constants.TAG_GAME_OVER_PLAYER, Player.clonePlayer(nPlayer));
		gameOverIntent.putExtra(Constants.TAG_GAME_OVER_MAP, map.getClearedMaps() * map.getMapLength() + map.getClearedCells());
		gameOverIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
		gameOverIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
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
	 * Show a dialog with the enemy loot info
	 * @param gold Gold gained after defeating the enemy
	 * @param xp Experience points won after defeating the enemy
	 */
	public void enemyDefeated(int gold, int xp, boolean mapPiece){
		EnemyDefeatedFragment enemyDefeatedDialog = new EnemyDefeatedFragment();
		Bundle args = new Bundle();
		args.putInt(Constants.ARG_GOLD, gold);
		args.putInt(Constants.ARG_XP, xp);
		args.putBoolean(Constants.ARG_MAP_PIECE, mapPiece);
		enemyDefeatedDialog.setArguments(args);
		enemyDefeatedDialog.setCancelable(false);
		enemyDefeatedDialog.show(getFragmentManager(), "EnemyDefeatedDialog");
	}

	/**
	 * Show a dialog stating that the player is dead
	 */
	public void playerDefeated(){
		PlayerDefeatedFragment enemyDefeatedDialog = new PlayerDefeatedFragment();
		enemyDefeatedDialog.setCancelable(false);
		enemyDefeatedDialog.show(getFragmentManager(), "PlayerDefeatedDialog");
	}

	/**
	 * Shake the clouds once
	 */
	public void shakeClouds() {
		if(mCanvasView != null) {
			int shakeCount = mCanvasView.getShakeMoveCount();
			mCanvasView.setShakeMoveCount(shakeCount + 1);
			int shakesLeft = Constants.SHAKE_LIMIT - (shakeCount + 1);
			showText(String.format(getResources().getString(R.string.message_shakesleft), shakesLeft));
			if(shakesLeft <= Constants.ZERO_INT){
				View v = findViewById(R.id.shakeCloudsUILayer);
				if(v != null){
					v.setVisibility(View.GONE);
				}
			}
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
     *
     * Get the sensor events list
     * @return The devices's sensor events list
     */
	public List<AppSensorEvent> getSensorEvents(){
	    return sensorEvents;
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

    /**
     * Speech Recognition Listener
     */
	protected class SpeechRecognitionListener implements RecognitionListener
	{

		@Override
		public void onBeginningOfSpeech()
		{
			Log.d(TAG, "onBeginningOfSpeech");
		}

		@Override
		public void onBufferReceived(byte[] buffer)
		{
			Log.d(TAG, "onBufferReceived");
		}

		@Override
		public void onEndOfSpeech()
		{
			Log.d(TAG, "onEndOfSpeech");
			imgMicStatus.setImageResource(R.drawable.ic_mic_enabled);
		}

		@Override
		public void onError(int error)
		{
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
			imgMicStatus.setImageResource(R.drawable.ic_mic_usage);
			Log.e(TAG, "onError = " + getErrorText(error));
		}

		@Override
		public void onEvent(int eventType, Bundle params)
		{
			Log.d(TAG, "onEvent");
		}

		@Override
		public void onPartialResults(Bundle partialResults)
		{
			Log.d(TAG, "onPartialResults");
		}

		@Override
		public void onReadyForSpeech(Bundle params)
		{
			Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
		}

		@Override
		public void onResults(Bundle results)
		{
			Log.d(TAG, "onResults"); //$NON-NLS-1$
			ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			// matches are the return values of speech recognition engine
			// Use these values for whatever you wish to do

            if(matches!=null && checkMatches(matches))
                doAction();

		}

        /**
         * Method that checks the spoken results with the key words list and returns if any of them match
         * @param matches spoken words
         * @return true if spoken words are within key words list
         */
        private boolean checkMatches(ArrayList<String> matches) {
		    for(String match : matches) {
		        if(match.contains(getString(R.string.command_fire))){
		            return true;
                } else if(match.contains(getString(R.string.command_shoot))){
                    return true;
                } else if(match.contains(getString(R.string.command_ok))){
		            return true;
                } else if(match.contains(getString(R.string.command_go))){
		            return true;
                }
            }
		    return false;
        }

        @Override
		public void onRmsChanged(float rmsdB)
		{
			// Log.d(TAG, "onRmsChanged");
		}

        /**
         * Method called when the recognized words match any of the key words list
         */
        private void doAction() {
            try {
				imgMicStatus.setImageResource(R.drawable.ic_mic_enabled);
				CanvasView canvasView = mCanvasView.nUpdateThread.getCanvasViewInstance();
				canvasView.nShotList.addAll(Arrays.asList(canvasView.nPlayerShip.shootCannon()));
            } catch (NoAmmoException e) {
                showText(e.getMessage());
            }
        }

        /**
         * Returns info feedback over the possible errors of the speech recognizer
         * @param errorCode error code
         * @return Error string associated with the error code
         */
        String getErrorText(int errorCode) {
            String message;
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "No match";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error from server";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "No speech input";
                    break;
                default:
                    message = "Didn't understand, please try again.";
                    break;
            }
            return message;
        }
	}
}