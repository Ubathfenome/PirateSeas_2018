package tfm.uniovi.pirateseas.view.graphics.canvasview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
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
import tfm.uniovi.pirateseas.model.canvasmodel.game.scene.Whirlpool;
import tfm.uniovi.pirateseas.model.canvasmodel.ui.StatBar;
import tfm.uniovi.pirateseas.utils.approach2d.DrawableHelper;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;
import tfm.uniovi.pirateseas.view.activities.GameActivity;
import tfm.uniovi.pirateseas.view.activities.ScreenSelectionActivity;

/**
 * Class to represent the game on the screen
 */
public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "CanvasView";
	private static final String EXCEPTION_TAG = "CustomException";

	private static final int SHOT_CHK_DELAY = 50;
	private static final int PLAYER_SHIP_Y_VALUE = 300;

	private int HORIZON_Y_VALUE = 200;

	// Establecer sistema de coordenadas para la pantalla que transforme el tamaño de la pantalla en coordenadas del juego 30 x 15
	private int nPixelsWidth, nPixelsHeight;

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
	private Whirlpool nWhirlpool;
	public Clouds nClouds;

	public Ship nPlayerShip;
	public Ship nEnemyShip;
	public List<Shot> nShotList;

	private StatBar nEnemyHBar;

	private SharedPreferences nPreferences;
	private long nBaseTimestamp;
	private long nGameTimestamp;

	private boolean nInitialized = false;

	private int nCheatCounter;
	private int nGameMode;

	private boolean nShipControlMode;

	private boolean messageSent = false;
	private boolean messageReaded = false;

	int downX = 0, downY = 0;

	double nEnemyShipInitialXcoord;
	double nEnemyShipInitialYcoord;

	public static int mScreenWidth, mScreenHeight;

	/**
	 * Constructor
	 * 
	 * @param context Context
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

	/**
	 * Method to initialize the class
	 */
	public void initialize() {
		nStatus = Constants.GAME_STATE_NORMAL;

		double HORIZON_HEIGHT_MULTIPLIER = 0.3;
		HORIZON_Y_VALUE = (int) (mScreenHeight * HORIZON_HEIGHT_MULTIPLIER);

        // BAR_INITIAL_X_VALUE = (int) (nScreenWidth * 0.1);

		nPreferences = nContext.getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);
		nBaseTimestamp = nPreferences.getLong(Constants.PREF_PLAYER_TIMESTAMP, 0);

		nGameTimer = new GameTimer(nBaseTimestamp);
		nPlayer = new Player();

		// Initialize components
		// Scene
		nSky = new Sky(nContext, 0, 0, nScreenWidth, nScreenHeight);
		nSea = new Sea(nContext, 0, HORIZON_Y_VALUE, nScreenWidth, nScreenHeight);
		nClouds = new Clouds(nContext, 0, 0, nScreenWidth, nScreenHeight);

		// Entities
		nPlayerShip = new Ship(nContext, ShipType.LIGHT, nScreenWidth / 2 - 100, nScreenHeight - PLAYER_SHIP_Y_VALUE,
				nScreenWidth, nScreenHeight, new Point(0, 0), Constants.DEFAULT_PLAYER_SHIP_DIRECTION, Constants.DEFAULT_SHIP_LENGTH, Constants.DEFAULT_PLAYER_SHIP_AMMO);

		nShotList = new ArrayList<>();

		Date date = new Date();
		nMap = new Map(date, Constants.MAP_MIN_HEIGHT, Constants.MAP_MIN_WIDTH);

		loadGame();

		nGameTimestamp = 0;
		nCheatCounter = 0;
		nGameMode = Constants.GAMEMODE_IDLE;
		Log.d(TAG, "Initialization: GameMode set to IDLE");
		nShipControlMode = nPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);

		nInitialized = true;
	}

	/**
	 * Checks whether the class has been initialized
	 * @return true if it has been initialized, false otherwise
	 */
	public boolean isInitialized() {
		return nInitialized;
	}

	/**
	 * Method to load the game saved on the preferences
	 */
	public void loadGame() {
		GameHelper.loadGameAtPreferences(nContext, nPlayer, nPlayerShip, nMap);
		nPlayer = GameHelper.helperPlayer;
		nPlayerShip = GameHelper.helperShip;
		nMap = GameHelper.helperMap;
	}

	/**
	 * Method to save the game on the preferences
	 * @throws SaveGameException Exception if an error happens while saving
	 */
	public void saveGame() throws SaveGameException {
		if (GameHelper.saveGameAtPreferences(nContext, nPlayer, nPlayerShip, nMap))
			Log.v(TAG, "Game saved");
		else
			throw new SaveGameException(nContext.getResources().getString(R.string.exception_save));
	}

	/**
	 * Draws all objects on the screen
	 * 
	 * @param canvas Canvas
	 */
	protected void drawOnScreen(Canvas canvas) {
		nSky.drawOnScreen(canvas);
		nClouds.drawOnScreen(canvas);
		nSea.drawOnScreen(canvas);

		nPlayerShip.drawOnScreen(canvas);

		// Pinta al enemigo y su barra de vida
		if (nEnemyShip != null && nEnemyShip.isAlive()) {
			nEnemyShip.updateImage();
			nEnemyShip.drawOnScreen(canvas);
			nEnemyHBar.drawOnScreen(canvas);
		}

		for (int i = 0, size = nShotList.size(); i < size; i++) {
			Shot s = nShotList.get(i);
			if (s.isAlive() && s.isInBounds(0))
				s.drawOnScreen(canvas);
		}

		if(nWhirlpool!=null) {
			nWhirlpool.getCurrentFrame();
			nWhirlpool.drawOnScreen(canvas);
		}
	}

	/**
	 * Method called if the screen surface changes
	 * @param holder Holder
	 * @param format Format
	 * @param width Width
	 * @param height Height
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		nScreenWidth = width;
		mScreenWidth = width;
		nScreenHeight = height;
		mScreenHeight = height;
		nPixelsWidth = width / Constants.MAX_ENTITY_WIDTH;
		nPixelsHeight = (height / Constants.MAX_ENTITY_HEIGHT) / Constants.FLYING_TIME_MULTIPLIER;
		Log.d(TAG, "Surface changed");
	}

	/**
	 * Method called when the screen surface is created
	 * @param holder Holder
	 */
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

	/**
	 * Method called if the screen surface is destroyed
	 * @param holder Holder
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean tryAgain = true;
		while (tryAgain) {
			try {
				nUpdateThread.join();

				tryAgain = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	/*
	 * Event called when a user touches the screen
	 */
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			downX = x;
			downY = y;
		}

		if (nStatus == Constants.GAME_STATE_NORMAL && nGameMode == Constants.GAMEMODE_BATTLE) {
			// If "Touch control" Then
			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
					boolean reloaded = nPlayerShip.isReloaded(nGameTimestamp);
                    String direction = pressedMotion(new Point(downX, downY), new Point(x, y));
                    Log.d(TAG, "Pressed motion had direction: " + direction);
                    int xDistance = Math.abs(x - downX);
                    int yDistance = Math.abs(y - downY);
                    switch (direction) {
                        case Constants.FRONT:
                            nCheatCounter = 0;
                            if (reloaded && nEnemyShip.isAlive()) {
                                try {
                                    // If using AIMED ammo, dealt damage to enemy instantly
                                    if (nPlayerShip.getSelectedAmmo() == Ammunitions.AIMED && nPlayerShip.getSelectedAmmunition() > 0) {
                                        nEnemyShip.looseHealth((int) (Constants.DEFAULT_SHOOT_DAMAGE * nPlayerShip.getPower()));
                                        nPlayerShip.setSelectedAmmunition(nPlayerShip.getSelectedAmmunition() - 1);
                                    } else {
                                        nShotList.addAll(Arrays.asList(nPlayerShip.shootCannon()));
                                    }
                                    MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_FIRED);
                                } catch (NoAmmoException e) {
                                    Log.e(EXCEPTION_TAG, e.getMessage());
                                    ((GameActivity) nContext).showText(e.getMessage());
                                }
                            } else {
                                try {
                                    throw new CannonReloadingException(
                                            nContext.getResources().getString(R.string.exception_reloading));
                                } catch (CannonReloadingException e) {
                                    Log.e(EXCEPTION_TAG, e.getMessage());
                                    MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_RELOADING);
                                    ((GameActivity)nContext).showText(e.getMessage());
                                }
                            }
                            break;
                        case Constants.RIGHT:
                            if (nShipControlMode) {
                                nCheatCounter = 0;
                                nPlayerShip.move(-(nPlayerShip.getShipType().getSpeed() + xDistance), 0, true);
                                nPlayerShip.moveShipEntity(new Point(nPlayerShip.getCoordinates().x + 1, nPlayerShip.getCoordinates().y));
                            }
                            break;
                        case Constants.LEFT:
                            if (nShipControlMode) {
                                nCheatCounter = 0;
                                nPlayerShip.move(nPlayerShip.getShipType().getSpeed() + xDistance, 0, true);
                                nPlayerShip.moveShipEntity(new Point(nPlayerShip.getCoordinates().x - 1, nPlayerShip.getCoordinates().y));
                            }
                            break;
                        case Constants.BACK:
                            nCheatCounter++;
                            if (nCheatCounter % Constants.CHT_VALUE > 0)
                                Log.v("Cheat", Constants.CHT_VALUE - (nCheatCounter % Constants.CHT_VALUE) + " more touches to go!");
                            if (nCheatCounter % Constants.CHT_VALUE == 0)
                                grantCheat2Player();
                            break;
                    }
                    break;
			}

		}
		return true;
	}

	/**
	 * Easter-egg method to grant cheats to the player
	 */
	private void grantCheat2Player() {
		nPlayer.addExperience(200);
		nPlayer.addGold(50);
		nPlayer.setMapPieces(1);
		nPlayerShip.gainAmmo(10, Ammunitions.SWEEP);
		nPlayerShip.gainHealth(30);
	}

	/**
	 * Method to determine the direction of a touching motion
	 * @param start Touch starting point
	 * @param end Touch ending point
	 * @return String with the direction (LEFT, RIGHT, FRONT, BACK)
	 */
	private String pressedMotion(Point start, Point end) {
		int deltaX = end.x - start.x;
		int deltaY = end.y - start.y;

		if (Math.abs(deltaX) > Math.abs(deltaY)) { // Lateral movement
			return deltaX > 0 ? Constants.RIGHT : Constants.LEFT;
		} else { // Vertical movement
			return deltaY > 0 ? Constants.BACK : Constants.FRONT;
		}
	}

	/**
	 * Updates the view depending on mStatus value
	 */
	public synchronized void updateLogic() throws SaveGameException {
		checkLogicThread();
		switch (nStatus) {
		case Constants.GAME_STATE_NORMAL:
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

	/**
	 * Pause the logic thread
	 */
	public void pauseLogicThread() {
		nUpdateThread.setRunning(false);
	}

	/**
	 * Checks the logic thread status
	 */
	private void checkLogicThread() {
		if (!nUpdateThread.isAlive() && nUpdateThread.getState() != Thread.State.NEW) {
			launchMainLogic();
		}
	}

	/**
	 * Manage the in-game time
	 */
	private void manageTime() {
		nGameTimestamp = nGameTimer.getLastTimestamp();

		nGameTimer.updateHour();

		long baseTs2Save = nGameTimer.getBaseTimestamp();
		if (baseTs2Save != 0 && nBaseTimestamp == 0) {
			SharedPreferences.Editor editor = nPreferences.edit();
			editor.putLong(Constants.PREF_PLAYER_TIMESTAMP, baseTs2Save);
			editor.apply();
		}
	}

	/**
	 * Manage changes on the UI
	 */
	private void manageUI() {
		if (nGameMode == Constants.GAMEMODE_BATTLE) {
			// Manage StatBars
			if (nEnemyShip != null)
				nEnemyHBar.setCurrentValue(nEnemyShip.getHealth());
            ((GameActivity) nContext).updateHealthBar(nPlayerShip.getHealth(), nPlayerShip.getMaxHealth());
            ((GameActivity) nContext).updateExperienceBar(nPlayer.getExperience(), nPlayer.getNextLevelThreshold());

			// Manage UIDisplayElements
			((GameActivity) nContext).mGold.setElementValue(nPlayer.getGold());
			((GameActivity) nContext).mGold.postInvalidate();

			((GameActivity) nContext).mAmmo.setElementValue(nPlayerShip.getSelectedAmmunition());

			if (nPlayerShip.isReloaded(nGameTimestamp))
				((GameActivity) nContext).mAmmo.setReloading(false);
			else
				((GameActivity) nContext).mAmmo.setReloading(true);

			((GameActivity) nContext).mAmmo.postInvalidate();
		} else if(nGameMode == Constants.GAMEMODE_IDLE){
            ((GameActivity) nContext).updateHealthBar(nPlayerShip.getHealth(), nPlayerShip.getMaxHealth());
            ((GameActivity) nContext).updateExperienceBar(nPlayer.getExperience(), nPlayer.getNextLevelThreshold());
        }
	}

	/**
	 * Manage the game mode
	 * @throws SaveGameException Exception if an error happens while saving the game
	 */
	private void manageMode() throws SaveGameException {
		if (nGameMode == Constants.GAMEMODE_ADVANCE) {
			if(messageReaded) {
				// Manage Island reveal on map
				if (nPlayer.hasCompleteMap()) {
					nPlayer.spendMap();
					int index = nMap.getIsland();
					if (index != -1) {
						nMap.clearMapCell(index);
					} else {
						((GameActivity) nContext).showText(getResources().getString(R.string.game_message_islands_discovered));
						nPlayer.addGold(90);
					}

					selectScreen();
				} else {
					selectScreen();
				}

				nGameMode = Constants.GAMEMODE_IDLE;
				Log.d(TAG, "GameMode set to IDLE");
			} else {
				if(!messageSent) {
					int gold = nEnemyShip.getShipType().defaultHealthPoints() / 5;
					int xp = nEnemyShip.getShipType().defaultHealthPoints() / 2;
					nPlayer.addGold(gold);
					nPlayer.addExperience(xp);
					messageSent = true;
					((GameActivity) nContext).enemyDefeated(gold, xp);
					// ((GameActivity)nContext).showText(String.format(getResources().getString(R.string.game_message_enemy_defeated), gold, xp));
				}
			}
		}
	}

	/**
	 * Manages the movement of background elements on the screen
	 */
	private void manageScreen() {
		if (nGameMode == Constants.GAMEMODE_BATTLE) {
			// Set SEA horizon at cam level to shoot enemies
			if (nSea.getHeight() != HORIZON_Y_VALUE)
				nSea.setHeight(HORIZON_Y_VALUE);
		} else if (nGameMode == Constants.GAMEMODE_IDLE) {
			nClouds.move();
            // Set SEA horizon at cam level to shoot enemies
            if (nSea.getHeight() != HORIZON_Y_VALUE)
                nSea.setHeight(HORIZON_Y_VALUE);
		}
	}

	/**
	 * Manage background events
	 */
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

	/**
	 * Manages entities
	 */
	private void manageEntities() {
		managePlayer();
		manageEnemies();
		manageShots();
	}

	/**
	 * Manage active shots
	 */
	private void manageShots() {
		int size = nShotList.size();

		if (nGameMode == Constants.GAMEMODE_BATTLE && size > 0) {
			for (int i = 0; i < size; i++) {
				Shot s = nShotList.get(i);
				if (s.isAlive() && s.isInBounds(0)) {
					switch (s.getShotStatus()) {
					case Constants.SHOT_FIRED:
						if (nGameTimestamp - s.getTimestamp() >= SHOT_CHK_DELAY) {
							MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_FIRED);
							s.setShotStatus(Constants.SHOT_FLYING);
						}
						break;
					case Constants.SHOT_FLYING:
						long timestampDiff = nGameTimestamp - s.getTimestamp();
						if (timestampDiff >= SHOT_CHK_DELAY) {

							if (nEnemyShip != null && nEnemyShip.isAlive()) {
								if (s.getEntityDirection() == Constants.DIRECTION_UP && s.intersection(nEnemyShip)) {
									nEnemyShip.looseHealth(s.getDamage());
									s.setShotStatus(Constants.SHOT_HIT);
									s.looseHealth(s.getDamage());
								}
							}
							if(nPlayerShip != null && nPlayerShip.isAlive()){
								if(s.getEntityDirection() == Constants.DIRECTION_DOWN && s.intersection(nPlayerShip)){
									nPlayerShip.looseHealth(s.getDamage());
									s.setShotStatus(Constants.SHOT_HIT);
									s.looseHealth(s.getDamage());
								}
							}

							// Change shot behaviour for the type of ammunition used
							if(s.getEntityDirection() == Constants.DIRECTION_UP) {
								s.moveShotEntity(s.getEndPoint(), nPixelsWidth, -nPixelsHeight);
							} else {
								s.moveShotEntity(s.getEndPoint(), nPixelsWidth, nPixelsHeight);
							}

							s.setTimestamp(nGameTimestamp);

							if (s.getCoordinates().x == s.getEndPoint().x && s.getCoordinates().y == s.getEndPoint().y) {
								s.setShotStatus(Constants.SHOT_MISSED);
								s.looseHealth(s.getDamage());
							}
						}
						break;
					case Constants.SHOT_HIT:
						// Play hit sound
							MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_HIT);
							s.looseHealth(s.getDamage());
						break;
					case Constants.SHOT_MISSED:
						// Play missed sound
							MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_MISSED);
							s.looseHealth(s.getDamage());
						break;
					}
				} else if(s.isAlive() && !s.isInBounds(0)){
					s.setShotStatus(Constants.SHOT_MISSED);
				} else { // If Shot is dead
				    nShotList.remove(s);
                }
			}
		}
	}

	/**
	 * Manages enemy's behaviour
	 */
	private void manageEnemies() {
		if (nEnemyShip != null) {
			if (!nEnemyShip.isAlive()) {
				nEnemyShip.setStatus(Constants.STATE_DEAD);
				Log.d(TAG, "All enemies have died.");
				nGameMode = Constants.GAMEMODE_ADVANCE;
				Log.d(TAG, "GameMode set to ADVANCE");
			} else {
				// EnemyShip shoot
				if(nEnemyShip.isReloaded(nGameTimestamp)) {
                    try {
						nShotList.addAll(Arrays.asList(nEnemyShip.shootCannon()));
                        MusicManager.getInstance().playSound(MusicManager.SOUND_SHOT_FIRED);
                    } catch (NoAmmoException e) {
                        Log.e(EXCEPTION_TAG, e.getMessage());
						((GameActivity)nContext).showText(e.getMessage());
                    }
                }
				// Establecer comportamiento enemigo con movimiento en circulo
				if(nEnemyShip.getX()+nEnemyShip.getWidth()+nEnemyShip.getShipType().getSpeed() > nScreenWidth) {
					if(nEnemyShip.getY() > nEnemyShipInitialYcoord) {
						nEnemyShip.setEntityDirection(Constants.DIRECTION_UP);
						nEnemyShip.move(0, nEnemyShip.getShipType().getSpeed(), true);
						nEnemyShip.moveShipEntity(new Point(nEnemyShip.getCoordinates().x, nEnemyShip.getCoordinates().y + 1));
					} else {
						nEnemyShip.setEntityDirection(Constants.DIRECTION_LEFT);
						nEnemyShip.move(nEnemyShip.getShipType().getSpeed(), 0, true);
						nEnemyShip.moveShipEntity(new Point(nEnemyShip.getCoordinates().x - 1, nEnemyShip.getCoordinates().y));
					}
				} else if(nEnemyShip.getX()-nEnemyShip.getShipType().getSpeed() < 0) {
                    if (nEnemyShip.getY() <= (nEnemyShipInitialYcoord + Constants.DEFAULT_ENEMY_Y_LIMIT)) {
						nEnemyShip.setEntityDirection(Constants.DIRECTION_DOWN);
                        nEnemyShip.move(0, -nEnemyShip.getShipType().getSpeed(), true);
						nEnemyShip.moveShipEntity(new Point(nEnemyShip.getCoordinates().x, nEnemyShip.getCoordinates().y - 1));
                    } else {
						nEnemyShip.setEntityDirection(Constants.DIRECTION_RIGHT);
						nEnemyShip.move(-nEnemyShip.getShipType().getSpeed(), 0, true);
						nEnemyShip.moveShipEntity(new Point(nEnemyShip.getCoordinates().x + 1, nEnemyShip.getCoordinates().y));
					}
				} else {
					if(nEnemyShip.getEntityDirection() == Constants.DIRECTION_RIGHT) {
						nEnemyShip.move(-nEnemyShip.getShipType().getSpeed(), 0, true);
						nEnemyShip.moveShipEntity(new Point(nEnemyShip.getCoordinates().x + 1, nEnemyShip.getCoordinates().y));
					}else if(nEnemyShip.getEntityDirection() == Constants.DIRECTION_LEFT) {
						nEnemyShip.move(nEnemyShip.getShipType().getSpeed(), 0, true);
						nEnemyShip.moveShipEntity(new Point(nEnemyShip.getCoordinates().x - 1, nEnemyShip.getCoordinates().y));
					}else {
						nEnemyShip.move(nEnemyShip.getShipType().getSpeed(), 0, true);
						nEnemyShip.moveShipEntity(new Point(nEnemyShip.getCoordinates().x - 1, nEnemyShip.getCoordinates().y));
					}
				}
			}
		}
	}

	/**
	 * manage player
	 */
	private void managePlayer() {
		if (!nPlayerShip.isAlive()) {
			// Display "Game Over" Screen with calculated score
			try {
				MusicManager.getInstance().stopBackgroundMusic();
			} catch(IllegalStateException e){
				MusicManager.getInstance().resetPlayer();
			}
			nMap.setActiveCell(nMap.getLastActiveCell());
			MusicManager.getInstance(nContext,MusicManager.MUSIC_GAME_OVER).playBackgroundMusic();
			((GameActivity) nContext).gameOver(nPlayer, nMap);
			nStatus = Constants.GAME_STATE_END;
		}
	}

	/**
	 * Return a random x coordinate for the enemy to spawn
	 * @param shipWidth Ship's width
	 * @return x value
	 */
	private double randomXSpawnValue(int shipWidth) {
		Random r = new Random();
		return r.nextDouble() * (nScreenWidth - shipWidth);
	}

	/**
	 * Activates the maelstorm event
	 */
	public void maelstorm() {
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Maelstorm inbound!");
		nWhirlpool = new Whirlpool(nContext, 0, HORIZON_Y_VALUE, nScreenWidth, nScreenHeight, null);
		// All ships loose some health
		if (nPlayerShip != null && nPlayerShip.isAlive())
			nPlayerShip.looseHealth(Constants.MAELSTORM_DAMAGE);
		if (nEnemyShip != null && nEnemyShip.isAlive())
			nEnemyShip.looseHealth(Constants.MAELSTORM_DAMAGE);
		MusicManager.getInstance().playSound(MusicManager.SOUND_WEATHER_MAELSTROM);
	}

	/**
	 * Spawn a random enemy
	 */
	public void spawnEnemyShip() {
		if (!Constants.isInDebugMode(Constants.MODE))
			Log.d(TAG, "Enemy spawned");
		MusicManager.getInstance().playSound(MusicManager.SOUND_ENEMY_APPEAR);
		try {
			MusicManager.getInstance().stopBackgroundMusic();
		} catch(IllegalStateException e){
			MusicManager.getInstance().resetPlayer();
		}
		MusicManager.getInstance(nContext, MusicManager.MUSIC_BATTLE).playBackgroundMusic();

		ShipType sType = Ship.randomShipType();
		nEnemyShipInitialXcoord = randomXSpawnValue((int) DrawableHelper.getWidth(getResources(), sType.drawableValue()));
		nEnemyShipInitialYcoord = DrawableHelper.getHeight(getResources(), sType.drawableValue()) / 3;

        // Establecer coordenadas de entidad iniciales del barco enemigo
		nEnemyShip = new Ship(nContext, sType,
				nEnemyShipInitialXcoord,
				nEnemyShipInitialYcoord,
				nScreenWidth, nScreenHeight, new Point(Double.valueOf(nEnemyShipInitialXcoord / nPixelsWidth).intValue(), Constants.MAX_ENTITY_HEIGHT), Constants.DEFAULT_ENEMY_SHIP_DIRECTION,
				7, Constants.SHOT_AMMO_UNLIMITED);

		View v = ((GameActivity)nContext).findViewById(R.id.enemyHBarSpace);
		double nEnemyBarXcoord = v.getX();
        double nEnemyBarYcoord = (double) 25;

		nEnemyHBar = new StatBar(nContext, nEnemyBarXcoord, nEnemyBarYcoord, nScreenWidth, nScreenHeight, nEnemyShip.getHealth(),
		nEnemyShip.getShipType().defaultHealthPoints(), Constants.BAR_HEALTH);
	}

	/**
	 * Calls the ScreenSelection Activity
	 * @throws SaveGameException Exception if an error happens while saving the game
	 */
	public void selectScreen() throws SaveGameException {
		// ISSUE #8 (Fixed)
		nMap.clearActiveMapCell();

		saveGame();

		Intent screenSelectionIntent = new Intent(nContext, ScreenSelectionActivity.class);
		screenSelectionIntent.putExtra(Constants.TAG_SENSOR_LIST, ((GameActivity) nContext).getSensorTypes());
		screenSelectionIntent.putExtra(Constants.TAG_LOAD_GAME, ((GameActivity) nContext).hasToLoadGame());
		screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_HEIGHT, ((GameActivity) nContext).getMapHeight());
		screenSelectionIntent.putExtra(Constants.TAG_SCREEN_SELECTION_MAP_WIDTH, ((GameActivity) nContext).getMapWidth());
		screenSelectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Log.d(TAG, "Start ScreenSelection Intent");
		nContext.startActivity(screenSelectionIntent);
		nStatus = Constants.GAME_STATE_END;
		try {
			MusicManager.getInstance().stopBackgroundMusic();
		} catch(IllegalStateException e){
			MusicManager.getInstance().resetPlayer();
		}
		MusicManager.getInstance(nContext, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
	}

	/**
	 * Set game status
	 * @param status Game status
	 */
	public void setStatus(int status) {
		if(nStatus==Constants.GAME_STATE_PAUSE&&status==Constants.GAME_STATE_NORMAL)
			loadSettings();
		nStatus = status;
	}

	/**
	 * Get the Game mode
	 * @return Game mode
	 */
	public int getGamemode() {
		return nGameMode;
	}

	/**
	 * Set the Game mode
	 * @param gamemode New Game mode
	 */
	public void setGamemode(int gamemode) {
		this.nGameMode = gamemode;
		Log.d(TAG, "GameMode set to '" + gamemode + "'");
	}

	/**
	 * Return the remaining shakes to dismiss the clouds
	 * @return remaining shakes
	 */
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

	/**
	 * Set the shake counter
	 * @param counter New remaining shakes
	 */
	public void setShakeMoveCount(int counter) {
		if(nClouds==null)
			Log.e(TAG, "'nClouds' is not initialized yet!");
		else
			nClouds = nUpdateThread.getCanvasViewInstance().nClouds;
		nClouds.setShakeMoveCount(counter);
	}

	/**
	 * Selects the next ammunition type
	 */
	public void selectNextAmmo(){
		if(nPlayerShip != null && nPlayerShip.isAlive())
			nPlayerShip.selectNextAmmo();
	}

	/**
	 * Load the settings from the preferences
	 */
	public void loadSettings() {
		if(nPreferences==null)
			nPreferences = nContext.getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);
		nShipControlMode = nPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
	}

	/**
	 * Sets the enemy defeated message status
	 * @param readed True if the user has accepted the dialog and read the message, false otherwise
	 */
	public void setMessageReaded(boolean readed){
		messageReaded = readed;
	}
}
