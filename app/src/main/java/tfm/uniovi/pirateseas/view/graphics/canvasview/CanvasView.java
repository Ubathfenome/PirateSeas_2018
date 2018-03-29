package tfm.uniovi.pirateseas.view.graphics.canvasview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.controller.sensors.events.EventDayNightCycle;
import tfm.uniovi.pirateseas.controller.timer.GameTimer;
import tfm.uniovi.pirateseas.exceptions.CannonReloadingException;
import tfm.uniovi.pirateseas.exceptions.NoAmmoException;
import tfm.uniovi.pirateseas.exceptions.SaveGameException;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ammunitions;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.ShipType;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Shot;
import tfm.uniovi.pirateseas.model.canvasmodel.game.scene.Clouds;
import tfm.uniovi.pirateseas.model.canvasmodel.game.scene.Sea;
import tfm.uniovi.pirateseas.model.canvasmodel.game.scene.Sky;
import tfm.uniovi.pirateseas.model.canvasmodel.ui.StatBar;
import tfm.uniovi.pirateseas.utils.approach2d.DrawableHelper;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;
import tfm.uniovi.pirateseas.view.activities.GameActivity;
import tfm.uniovi.pirateseas.view.activities.PauseActivity;
import tfm.uniovi.pirateseas.view.activities.ScreenSelectionActivity;

public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "CanvasView";
	private static final String EXCEPTION_TAG = "CustomException";

	private static final int CHT_VALUE = 20;

	private static final int SHOT_CHK_DELAY = 75;
	private static final int MAELSTORM_DAMAGE = 15;

	private static final int DEFAULT_PLAYER_SHIP_DIRECTION = 90;
	private static final int DEFAULT_SHIP_WIDTH = 2;
	private static final int DEFAULT_SHIP_HEIGHT = 3;
	private static final int DEFAULT_SHIP_LENGTH = 5;
	private static final int DEFAULT_PLAYER_SHIP_AMMO = 20;

	private static final int DEFAULT_ENEMY_SHIP_DIRECTION = 270;

	private int HORIZON_Y_VALUE = 200;
	private double HORIZON_HEIGHT_MULTIPLIER = 0.3;
	private double ENEMY_BAR_HEIGHT_MULTIPLIER = 0.2;
	private double BAR_MIN_MARGIN = 25;
	private static final int BAR_SEPARATION = 20;
	private static int BAR_INITIAL_X_VALUE = 150;

	private Context nContext;
	public static MainLogic nUpdateThread;

	private int nScreenWidth;
	private int nScreenHeight;
	public static int nStatus;

	private GameTimer nGameTimer;
	public Player nPlayer;
	public Map nMap;

	private Sky nSky;
	private Sea nSea;
	public Clouds nClouds;

	public Ship nPlayerShip;
	private Ship nEnemyShip;
	private List<Shot> nShotList;

	private StatBar nPlayerHBar, nPlayerXPBar;
	private StatBar nEnemyHBar;

	private SharedPreferences nPreferences;
	private long nBaseTimestamp;
	private long nGameTimestamp;
	private long[] nShotLastTimeChecked;

	private boolean nInitialized = false;
	private boolean nDebug = false;

	private int nCheatCounter;
	private int nGameMode;

	private boolean nControlMode;

	int downX = 0, downY = 0;



	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public CanvasView(Context context) {
		this(context, null);
	}

	public CanvasView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CanvasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getHolder().addCallback(this);
		setFocusable(true);
		this.nContext = context;
		launchMainLogic();
	}

	/**
	 * Launches the main thread
	 */
	public void launchMainLogic() {
		if (nUpdateThread != null) {
			nUpdateThread.setRunning(false);
			nUpdateThread.interrupt();
		}
		nUpdateThread = null;
		nUpdateThread = new MainLogic(getHolder(), this);

		// System.out.println("MainLogic thread is alive: " +
		// nUpdateThread.isAlive());
		// System.out.println("MainLogic thread is running: " +
		// nUpdateThread.getRunning());
		// System.out.println("MainLogic thread state is: " +
		// nUpdateThread.getState());

		if (nUpdateThread.isAlive()) {
			if (!nUpdateThread.getRunning()) {
				nUpdateThread.setRunning(true);
			}
		}
	}

	public void initialize() {
		nStatus = Constants.GAME_STATE_NORMAL;

		HORIZON_Y_VALUE = (int) (nScreenHeight * HORIZON_HEIGHT_MULTIPLIER);
		// BAR_INITIAL_X_VALUE = (int) (nScreenWidth * 0.1);

		nPreferences = nContext.getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);
		nBaseTimestamp = nPreferences.getLong(Constants.PREF_PLAYER_TIMESTAMP, 0);
		nDebug = nPreferences.getBoolean(Constants.TAG_EXE_MODE, false);

		nGameTimer = new GameTimer(nBaseTimestamp);
		nPlayer = new Player();

		// Initialize components
		// Scene
		nSky = new Sky(nContext, 0, 0, nScreenWidth, nScreenHeight);
		nSea = new Sea(nContext, 0, HORIZON_Y_VALUE, nScreenWidth, nScreenHeight);
		nClouds = new Clouds(nContext, 0, 0, nScreenWidth, nScreenHeight);

		// Entities
		nPlayerShip = new Ship(nContext, ShipType.LIGHT, nScreenWidth / 2 - 100, nScreenHeight - HORIZON_Y_VALUE,
				nScreenWidth, nScreenHeight, new Point(0, 0), DEFAULT_PLAYER_SHIP_DIRECTION, DEFAULT_SHIP_WIDTH,
				DEFAULT_SHIP_HEIGHT, DEFAULT_SHIP_LENGTH, DEFAULT_PLAYER_SHIP_AMMO);

		nShotList = new ArrayList<Shot>();

		Date date = new Date();
		nMap = new Map(date, Constants.MAP_MIN_HEIGHT, Constants.MAP_MIN_WIDTH);

		if (((GameActivity) nContext).hasToLoadGame())
			loadGame();

		// Game User Interface
		nPlayerHBar = new StatBar(nContext, BAR_INITIAL_X_VALUE, nScreenHeight - (BAR_MIN_MARGIN + BAR_SEPARATION), nScreenWidth, nScreenHeight,
				nPlayerShip.getHealth(), nPlayerShip.getType().defaultHealthPoints(), Constants.BAR_HEALTH);
		nPlayerXPBar = new StatBar(nContext, BAR_INITIAL_X_VALUE, nScreenHeight - BAR_MIN_MARGIN, nScreenWidth, nScreenHeight,
                nPlayer.getNextLevelThreshold(), 0, Constants.BAR_EXPERIENCE);

		nGameTimestamp = 0;
		nShotLastTimeChecked = null;
		nCheatCounter = 0;
		nGameMode = Constants.GAMEMODE_IDLE;
		Log.d(TAG, "Initialization: GameMode set to IDLE");
		nControlMode = nPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);

		nInitialized = true;
	}

	public boolean isInitialized() {
		return nInitialized;
	}

	public void loadGame() {
		GameHelper.loadGameAtPreferences(nContext, nPlayer, nPlayerShip, nMap);
		nPlayer = GameHelper.helperPlayer;
		nPlayerShip = GameHelper.helperShip;
		nMap = GameHelper.helperMap;
	}

	public void saveGame() throws SaveGameException {
		if (GameHelper.saveGameAtPreferences(nContext, nPlayer, nPlayerShip, nMap))
			if (!Constants.isInDebugMode(Constants.MODE))
				Log.v(TAG, "Game saved");
			else
				throw new SaveGameException(nContext.getResources().getString(R.string.exception_save));
	}

	/**
	 * Draws all objects on the screen
	 * 
	 * @param canvas
	 */
	protected void drawOnScreen(Canvas canvas) {
		nSky.drawOnScreen(canvas);
		nClouds.drawOnScreen(canvas);
		nSea.drawOnScreen(canvas);

		nPlayerShip.drawOnScreen(canvas);

		// Pinta al enemigo y su barra de vida
		if (nEnemyShip != null && nEnemyShip.isAlive()) {
			nEnemyShip.drawOnScreen(canvas);
			nEnemyHBar.drawOnScreen(canvas);
		}

		for (int i = 0, size = nShotList.size(); i < size; i++) {
			Shot s = nShotList.get(i);
			if (s.isAlive() && s.isInBounds(HORIZON_Y_VALUE))
				s.drawOnScreen(canvas);
		}

		nPlayerHBar.drawOnScreen(canvas);
		nPlayerXPBar.drawOnScreen(canvas);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		nScreenWidth = width;
		nScreenHeight = height;
		Log.d(TAG, "Surface changed");
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Surface Created");

		if (!nUpdateThread.isAlive()) {
			launchMainLogic();
			nUpdateThread.setRunning(true);
			nUpdateThread.setName("GameLogicThread");
			nUpdateThread.start();
		} else {
			nUpdateThread.setRunning(true);
		}

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean tryAgain = true;
		while (tryAgain) {
			try {
				nUpdateThread.join();

				tryAgain = false;
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean useAmmoKeys = nPreferences.getBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		if (useAmmoKeys) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				// Change to the next type
				nPlayerShip.selectNextAmmo();
			} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				// Change to the previous type
				nPlayerShip.selectPreviousAmmo();
			}
		}
		return true;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			downX = x;
			downY = y;
		}

		if (nStatus == Constants.GAME_STATE_NORMAL && nGameMode == Constants.GAMEMODE_BATTLE) {
			// If "Sensor control" Then
			if (!nControlMode) {
				// TODO Transform sensor acceleration into movement event

			} else {

				// ElseIf "Touch control" Then

				if ((x > (getWidth() - (getWidth() / 6))) && (y < getHeight() / 6)) {
					nStatus = Constants.GAME_STATE_PAUSE;
					Intent pauseActivityIntent = new Intent(nContext, PauseActivity.class);
					Log.d(TAG, "Start Pause Intent");
					nContext.startActivity(pauseActivityIntent);
				} else {
					switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
						break;
					case MotionEvent.ACTION_UP:
						boolean reloaded = nPlayerShip.isReloaded(nGameTimestamp);
						if (reloaded) {
							String direction = pressedMotion(new Point(downX, downY), new Point(x, y));
							if (direction.equals(Constants.FRONT)) {

								nCheatCounter = 0;

								try {
									nShotList.add(nPlayerShip.shootFront());
								} catch (NoAmmoException e) {
									Log.e(EXCEPTION_TAG, e.getMessage());
									Toast.makeText(nContext, e.getMessage(), Toast.LENGTH_SHORT).show();
								}

							} else if (direction.equals(Constants.RIGHT)) {
								Shot[] shotArray = null;
								nCheatCounter = 0;

								try {
									shotArray = nPlayerShip.shootSide();
									for (int i = 0, length = shotArray.length; i < length; i++) {
										nShotList.add(shotArray[i]);
									}
								} catch (NoAmmoException e) {
									Log.e(EXCEPTION_TAG, e.getMessage());
									Toast.makeText(nContext, e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							} else if (direction.equals(Constants.LEFT)) {
								Shot[] shotArray = null;
								nCheatCounter = 0;

								try {
									shotArray = nPlayerShip.shootSide();
									for (int i = 0, length = shotArray.length; i < length; i++) {
										nShotList.add(shotArray[i]);
									}
								} catch (NoAmmoException e) {
									Log.e(EXCEPTION_TAG, e.getMessage());
									Toast.makeText(nContext, e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							} else if (direction.equals(Constants.BACK)) {
								nCheatCounter++;
								if (nCheatCounter % CHT_VALUE > 0)
									Log.v("Cheat", CHT_VALUE - (nCheatCounter % CHT_VALUE) + " more touches to go!");
								if (nCheatCounter % CHT_VALUE == 0)
									grantCheat2Player();
							}
						} else {
							try {
								throw new CannonReloadingException(
										nContext.getResources().getString(R.string.exception_reloading));
							} catch (CannonReloadingException e) {
								Log.e(EXCEPTION_TAG, e.getMessage());
								if (!nDebug)
									MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_RELOADING);
								Toast.makeText(nContext, e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
						break;
					}
				}
			}
		}
		return true;
	}

	private void grantCheat2Player() {
		nPlayer.addExperience(200);
		nPlayer.addGold(50);
		nPlayer.setMapPieces(1);
		nPlayerShip.gainAmmo(10, Ammunitions.SWEEP);
		nPlayerShip.gainHealth(30);
	}

	private String pressedMotion(Point start, Point end) {
		int deltaX = end.x - start.x;
		int deltaY = end.y - start.y;

		if (Math.abs(deltaX) > Math.abs(deltaY)) { // Lateral movement
			return deltaX > 0 ? Constants.RIGHT : Constants.LEFT;
		} else { // Vertical movement
			return deltaY > 0 ? Constants.BACK : Constants.FRONT;
		}
	}

	public static void pauseGame(boolean hasToBePaused) {
		if (hasToBePaused && nStatus == Constants.GAME_STATE_NORMAL) {
			nStatus = Constants.GAME_STATE_PAUSE;
		} else if (!hasToBePaused && nStatus == Constants.GAME_STATE_PAUSE) {
			nStatus = Constants.GAME_STATE_NORMAL;
		}
	}

	/**
	 * Updates the view depending on mStatus value
	 */
	public synchronized void updateLogic() throws SaveGameException {
		checkLogicThread();
		switch (nStatus) {
		case Constants.GAME_STATE_NORMAL:
			switch(nGameMode){
				case Constants.GAMEMODE_BATTLE:
					Log.d(TAG,"Current GAMEMODE is: BATTLE");
					break;
				case Constants.GAMEMODE_ADVANCE:
					Log.d(TAG,"Current GAMEMODE is: ADVANCE");
					break;
				case Constants.GAMEMODE_IDLE:
					Log.d(TAG,"Current GAMEMODE is: IDLE");
					break;
			}
			manageTime();
			manageEvents();
			manageMode();
			manageScreen();
			manageEntities();
			manageUI();
			break;
		case Constants.GAME_STATE_PAUSE:
			break;
		case Constants.GAME_STATE_END:
			nUpdateThread.setRunning(false);
			nUpdateThread.interrupt();
            ((GameActivity) nContext).finish();
			break;
		}
	}

	public void pauseLogicThread() {
		nUpdateThread.setRunning(false);
	}

	private void checkLogicThread() {
		// System.out.println("MainLogic thread is alive: " +
		// nUpdateThread.isAlive());
		// System.out.println("MainLogic thread is running: " +
		// nUpdateThread.getRunning());
		// System.out.println("MainLogic thread state is: " +
		// nUpdateThread.getState());

		if (!nUpdateThread.isAlive() && nUpdateThread.getState() != Thread.State.NEW) {
			launchMainLogic();
		}

	}

	private void manageTime() {
		nGameTimestamp = nGameTimer.getLastTimestamp();

		nGameTimer.updateHour();

		long baseTs2Save = nGameTimer.getBaseTimestamp();
		if (baseTs2Save != 0 && nBaseTimestamp == 0) {
			SharedPreferences.Editor editor = nPreferences.edit();
			editor.putLong(Constants.PREF_PLAYER_TIMESTAMP, baseTs2Save);
			editor.commit();
		}
		int passedDays = nGameTimer.getDay();
		if (passedDays > 0) {
			// nPlayer.setPassedDays(passedDays);
		}
	}

	private void manageUI() {
		if (nGameMode == Constants.GAMEMODE_BATTLE) {
			// Manage StatBars
			if (nEnemyShip != null)
				nEnemyHBar.setCurrentValue(nEnemyShip.getHealth());
			nPlayerHBar.setCurrentValue(nPlayerShip.getHealth());
			nPlayerXPBar.setCurrentValue(nPlayer.getExperience());
			nPlayerXPBar.setMaxValue(nPlayer.getNextLevelThreshold());

			// Manage UIDisplayElements
			((GameActivity) nContext).mGold.setElementValue(nPlayer.getGold());
			((GameActivity) nContext).mGold.postInvalidate();

			((GameActivity) nContext).mAmmo.setElementValue(nPlayerShip.getSelectedAmmunition());

			if (nPlayerShip.isReloaded(nGameTimestamp))
				((GameActivity) nContext).mAmmo.setReloading(false);
			else
				((GameActivity) nContext).mAmmo.setReloading(true);

			((GameActivity) nContext).mAmmo.postInvalidate();
		} else if (nGameMode == Constants.GAMEMODE_ADVANCE) {
			// Manage UIDisplayElements
			((GameActivity) nContext).mGold.setElementValue(nPlayer.getGold());
			((GameActivity) nContext).mGold.postInvalidate();

			((GameActivity) nContext).mAmmo.setElementValue(nPlayerShip.getSelectedAmmunition());

			if (nPlayerShip.isReloaded(nGameTimestamp))
				((GameActivity) nContext).mAmmo.setReloading(false);
			else
				((GameActivity) nContext).mAmmo.setReloading(true);

			((GameActivity) nContext).mAmmo.postInvalidate();
		} else if (nGameMode == Constants.GAMEMODE_IDLE) {
			// DO NOTHING
		}
	}

	private void manageMode() throws SaveGameException {
		if (nGameMode == Constants.GAMEMODE_ADVANCE) {
			// Manage Island reveal on map
			if (nPlayer.hasCompleteMap()) {
				nPlayer.spendMap();
				int index = nMap.getIsland();
				if(index != -1) {
					nMap.clearMapCell(index);
				} else {
					Toast.makeText(nContext, "All islands have been discovered. We'll sell this map for 90 gold coins!", Toast.LENGTH_SHORT).show();
					nPlayer.addGold(90);
				}

				selectScreen();
			} else {
				selectScreen();
			}

			nGameMode = Constants.GAMEMODE_IDLE;
			Log.d(TAG, "GameMode set to IDLE");
		}
	}

	private void manageScreen() {
		if (nGameMode == Constants.GAMEMODE_BATTLE) {
			// Set SEA horizon at cam level to shoot enemies
			if (nSea.getHeight() != HORIZON_Y_VALUE)
				nSea.setHeight(HORIZON_Y_VALUE);
		} else if (nGameMode == Constants.GAMEMODE_ADVANCE) {

		} else if (nGameMode == Constants.GAMEMODE_IDLE) {
			nClouds.move();
		}
	}

	private void manageEvents() {
		nSky.setFilterValue(EventDayNightCycle.getSkyFilter(nGameTimer.getHour()));

		nSea.setFilterValue(EventDayNightCycle.getSkyFilter(nGameTimer.getHour()));

		switch (nGameMode) {
		case Constants.GAMEMODE_BATTLE:
			break;
		case Constants.GAMEMODE_ADVANCE:
			break;
		case Constants.GAMEMODE_IDLE:
			// If the enemy has not yet spawned, nEnemyShip will be null
			if (nEnemyShip == null) {
				if (nClouds != null) {
					if (nClouds.getShakeMoveCount() >= Constants.SHAKE_LIMIT) {
						nGameMode = Constants.GAMEMODE_BATTLE;
						spawnEnemyShip();
						Log.d(TAG, "GameMode set to BATTLE");
					}
				}
			}
			break;
		}
	}

	private void manageEntities() {
		managePlayer();
		manageEnemies();
		manageShots();
	}

	private void manageShots() {
		int size = nShotList.size();

		if (nGameMode == Constants.GAMEMODE_BATTLE && size > 0) {
			boolean[] deadShots = new boolean[size];
			nShotLastTimeChecked = new long[size];
			for (int i = 0; i < size; i++) {
				deadShots[i] = false;
				Shot s = nShotList.get(i);
				if (s.isAlive() && s.isInBounds(HORIZON_Y_VALUE)) {
					switch (s.getShotStatus()) {
					case Constants.SHOT_FIRED:
						if (nGameTimestamp - s.getTimestamp() >= SHOT_CHK_DELAY) {
							if (!nDebug)
								MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_FIRED);
							s.setShotStatus(Constants.SHOT_FLYING);
							nShotLastTimeChecked[i] = nGameTimestamp;
						}
						break;
					case Constants.SHOT_FLYING:
						if (nGameTimestamp - nShotLastTimeChecked[i] >= SHOT_CHK_DELAY) {

							if (nEnemyShip != null) {
								if (s.intersectionWithEntity(nEnemyShip)) {
									nEnemyShip.looseHealth(s.getDamage());
									s.setShotStatus(Constants.SHOT_HIT);
								}
							}
							if (s.intersectionWithEntity(nPlayerShip)) {
								if (nPlayerShip.getHealth() >= s.getDamage())
									nPlayerShip.looseHealth(s.getDamage());
								s.setShotStatus(Constants.SHOT_HIT);
							}

							s.moveEntity(s.getEndPoint());
							nShotLastTimeChecked[i] = nGameTimestamp;

							if (s.getCoordinates().x == s.getEndPoint().x && s.getCoordinates().y == s.getEndPoint().y)
								s.setShotStatus(Constants.SHOT_MISSED);
						}
						break;
					case Constants.SHOT_HIT:
						// Play hit sound
						if (nGameTimestamp - nShotLastTimeChecked[i] >= SHOT_CHK_DELAY) {
							if (!nDebug)
								MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_HIT);
							s.looseHealth(s.getDamage());
							deadShots[i] = true;
						}
						break;
					case Constants.SHOT_MISSED:
						// Play missed sound
						if (nGameTimestamp - nShotLastTimeChecked[i] >= SHOT_CHK_DELAY) {
							if (!nDebug)
								MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_MISSED);
							s.looseHealth(s.getDamage());
							deadShots[i] = true;
						}
						break;
					}
				}
			}

			for (int i = size - 1; i >= 0; i--) {
				if (deadShots[i]) {
					nShotList.remove(i);
					nShotLastTimeChecked[i] = 0;
				}
			}
		}
	}

	private void manageEnemies() {
		if (nEnemyShip != null) {
			if (!nEnemyShip.isAlive()) {
				nEnemyShip.setStatus(Constants.STATE_DEAD);
				Log.d(TAG, "All enemies have died.");
				nGameMode = Constants.GAMEMODE_ADVANCE;
				Log.d(TAG, "GameMode set to ADVANCE");
				nPlayer.addGold(nEnemyShip.getType().defaultHealthPoints() / 5);
				nPlayer.addExperience(nEnemyShip.getType().defaultHealthPoints() / 2);
			} else {
				// Establecer comportamiento enemigo
				if(nEnemyShip.getX()+nEnemyShip.getWidth()+nEnemyShip.getShipType().getSpeed() > nScreenWidth) {
					nEnemyShip.move(0, 30, false);
				} else if(nEnemyShip.getX()-nEnemyShip.getShipType().getSpeed() < 0){
					nEnemyShip.move(0,-30,false);
				} else {
					nEnemyShip.move(nEnemyShip.getShipType().getSpeed(), 0, true);
				}


			}
		}
	}

	/**
	 * Rotate the world around the player whenever he/she moves
	 */
	private void managePlayer() {
		if (nPlayerShip.isAlive()) {
			if (nEnemyShip != null && nEnemyShip.isAlive()) {
				nEnemyShip.move(nEnemyShip.getSpeedXLevel(), 0, true);
			}
		} else {
			// Display "Game Over" Screen with calculated score
			((GameActivity) nContext).gameOver(nPlayer);
			nStatus = Constants.GAME_STATE_END;
		}
	}

	private double randomXSpawnValue(int shipWidth) {
		Random r = new Random();
		double d = r.nextDouble() * (nScreenWidth - shipWidth);
		return d;
	}

	public void maelstorm() {
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Maelstorm inbound!");
		// All ships loose some health
		if (nPlayerShip != null && nPlayerShip.isAlive())
			nPlayerShip.looseHealth(MAELSTORM_DAMAGE);
		if (nEnemyShip != null && nEnemyShip.isAlive())
			nEnemyShip.looseHealth(MAELSTORM_DAMAGE);
	}

	public void spawnEnemyShip() {
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Enemy spawned");
		MusicManager.getInstance().playSound(MusicManager.SOUND_ENEMY_APPEAR);
		MusicManager.getInstance().stopBackgroundMusic();
		MusicManager.getInstance(nContext, MusicManager.MUSIC_BATTLE).playBackgroundMusic();

		ShipType sType = Ship.randomShipType();
		double nEnemyShipXcoord = randomXSpawnValue((int) DrawableHelper.getWidth(getResources(), sType.drawableValue()));
		double nEnemyShipYcoord = DrawableHelper.getHeight(getResources(), sType.drawableValue()) / 3;

		nEnemyShip = new Ship(nContext, sType,
				nEnemyShipXcoord,
				nEnemyShipYcoord,
				nScreenWidth, nScreenHeight, new Point(15, 20), DEFAULT_ENEMY_SHIP_DIRECTION, 3, 4,
				7, Constants.SHOT_AMMO_UNLIMITED);

		View v = ((GameActivity)nContext).findViewById(R.id.enemyHBarSpace);
		double nEnemyBarXcoord = v.getX();
		double nEnemyBarYcoord = BAR_MIN_MARGIN;

		nEnemyHBar = new StatBar(nContext, nEnemyBarXcoord, nEnemyBarYcoord, nScreenWidth, nScreenHeight, nEnemyShip.getHealth(),
		nEnemyShip.getType().defaultHealthPoints(), Constants.BAR_HEALTH);
	}

	public void selectScreen() throws SaveGameException {
		saveGame();

		Intent screenSelectionIntent = new Intent(nContext, ScreenSelectionActivity.class);
		screenSelectionIntent.putExtra(Constants.TAG_SENSOR_LIST, ((GameActivity) nContext).getSensorTypes());
		screenSelectionIntent.putExtra(Constants.TAG_LOAD_GAME, ((GameActivity) nContext).hasToLoadGame());
		screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, ((GameActivity) nContext).getMapHeight());
		screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, ((GameActivity) nContext).getMapWidth());

		Log.d(TAG, "Start ScreenSelection Intent");
		nContext.startActivity(screenSelectionIntent);
		nStatus = Constants.GAME_STATE_END;
	}

	public void setStatus(int status) {
		nStatus = status;
	}

	public boolean hasEnemyShip() {
		return nEnemyShip != null;
	}

	public int getGamemode() {
		return nGameMode;
	}

	public void setGamemode(int gamemode) {
		this.nGameMode = gamemode;
		Log.d(TAG, "GameMode set to '" + gamemode + "'");
	}

	public int getShakeMoveCount() {
		if(nClouds==null) {
			Log.e(TAG, "'nClouds' is not initialized yet!");
			nClouds = nUpdateThread.getCanvasViewInstance().nClouds;
			if(nClouds == null)
				return 0;
			else
				return nClouds.getShakeMoveCount();
		} else {
			return nClouds.getShakeMoveCount();
		}
	}

	public void setShakeMoveCount(int counter) {
		if(nClouds==null)
			Log.e(TAG, "'nClouds' is not initialized yet!");
		else
			nClouds = nUpdateThread.getCanvasViewInstance().nClouds;
		nClouds.setShakeMoveCount(counter);
	}
}
