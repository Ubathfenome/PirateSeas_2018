package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.Random;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.exceptions.NoAmmoException;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.utils.approach2d.DrawableHelper;
import tfm.uniovi.pirateseas.view.graphics.canvasview.CanvasView;

/**
 * Class that represents the ships in the game
 */
public class Ship extends Entity implements Parcelable{
	// Crear array de variables para almacenar la cantidad de cada tipo de municion
	private int nAmmoTypes = Ammunitions.values().length;
	private int[] nAmmunitions = new int[nAmmoTypes];
	private Ammunitions selectedAmmo;
	private int selectedAmmoIndex;
	
	private int mReloadTime;
	private float mPower;
	private float mRange;
	private long timestampLastShot;
	
	private boolean isPlayable;
	private boolean wasIdle;
	
	private ShipType sType;
	
	private Context context;

	/**
	 * Default constructor
	 */
	public Ship(){
		super(null, 0, 0, 0, 0, new Point(0, 0), Constants.DEFAULT_PLAYER_SHIP_DIRECTION, 0);
		for(int i = 0; i < nAmmoTypes; i++){
			nAmmunitions[i] = 0;
		}
		
		this.sType = ShipType.LIGHT;
		this.setIdle(true);
        this.isPlayable = false;
        this.selectedAmmo = Ammunitions.DEFAULT;
		this.selectedAmmoIndex = Ammunitions.valueOf(selectedAmmo.getName()).ordinal();
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
    /*
     * Constructor for new ship receiving previous ship data
     */
	public Ship(Context context, ShipType sType, double x, double y, double canvasWidth, 
				double canvasHeight, Point coordinates, int direction, int length, int ammo){
		super(context, x, y, canvasWidth, canvasHeight, coordinates, direction, length);
		
		this.context = context;
		
		this.selectedAmmo = Ammunitions.DEFAULT;
		this.selectedAmmoIndex = Ammunitions.valueOf(selectedAmmo.getName()).ordinal();
		this.nAmmunitions[selectedAmmoIndex] = ammo;
		
		this.sType = sType;
		this.mRange = sType.rangeMultiplier();
		this.mPower = sType.powerMultiplier();
		this.mReloadTime = (int) sType.powerMultiplier() * Constants.DEFAULT_SHIP_RELOAD;
		gainHealth(this.mMaxHealth = sType.defaultHealthPoints());		
		
		this.isPlayable = ammo != Constants.SHOT_AMMO_UNLIMITED;
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			if(isPlayable)
				setImage(context.getResources().getDrawable(sType.drawableValue(), null));
			else{
				switch(sType.ordinal()){
					case 0:
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_front, null));
						break;
					case 1:
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_front, null));
						break;
					case 2:
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_front, null));
						break;
				}
			}
		} else {
			if(isPlayable)
				setImage(context.getResources().getDrawable(sType.drawableValue()));
			else{
				switch(sType.ordinal()){
					case 0:
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_front));
						break;
					case 1:
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_front));
						break;
					case 2:
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_front));
						break;
				}
			}
		}
		
		if(mHealthPoints > 0)
			setStatus(Constants.STATE_ALIVE);
		
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    /*
     * Constructor for new ship based on a previous ship's data
     */
	public Ship(Context context, Ship baseShip, ShipType sType, Point coordinates, int direction, int length, int health, int ammo){
		super(context, baseShip.x, baseShip.y, baseShip.mCanvasWidth, baseShip.mCanvasHeight, coordinates, direction, length);

		this.context = context;

		this.nAmmunitions[0] = ammo;
		this.selectedAmmo = Ammunitions.DEFAULT;
		this.selectedAmmoIndex = Ammunitions.valueOf(selectedAmmo.getName()).ordinal();

		this.sType = sType;
		this.mRange = sType.rangeMultiplier();
		this.mPower = sType.powerMultiplier();
		this.mReloadTime = (int) sType.powerMultiplier() * Constants.DEFAULT_SHIP_RELOAD;
		this.mMaxHealth = sType.defaultHealthPoints() > health ? sType.defaultHealthPoints() : health;
		gainHealth(health);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			setImage(context.getResources().getDrawable(sType.drawableValue(), null));
		} else {
			setImage(context.getResources().getDrawable(sType.drawableValue()));
		}

		if(mHealthPoints > 0)
			setStatus(Constants.STATE_ALIVE);
	}

    /**
     * Parcel constructor
     * @param in Parcel
     */
	private Ship(Parcel in) {
		this();
		nAmmoTypes = in.readInt();
		nAmmunitions = in.createIntArray();
		selectedAmmoIndex = in.readInt();
		mReloadTime = in.readInt();
		mPower = in.readFloat();
		mRange = in.readFloat();
		timestampLastShot = in.readLong();
		isPlayable = in.readByte() != 0;
		wasIdle = in.readByte() != 0;
		mHealthPoints = in.readInt();
		mMaxHealth = in.readInt();
		sType = ShipType.values()[in.readInt()];
	}

    /**
     * Parcel's CREATOR
     */
	public static final Creator<Ship> CREATOR = new Creator<Ship>() {
		@Override
		public Ship createFromParcel(Parcel in) {
			return new Ship(in);
		}

		@Override
		public Ship[] newArray(int size) {
			return new Ship[size];
		}
	};

    /**
     * Method to add ammunition of a certian type to the ship
     * @param ammo Ammunition ammount
     * @param ammoType Ammunition type
     */
	public void gainAmmo(int ammo, Ammunitions ammoType){
		if(ammo > 0){
			nAmmunitions[Ammunitions.valueOf(ammoType.getName()).ordinal()] += ammo;
		} else
			throw new IllegalArgumentException("Encontrado valor de puntos negativo al modificar mAmmunition");
	}

    /**
     * Method to shoot the selected Ammunition forwards
     * @return Shot entity
     * @throws NoAmmoException Exception triggers when the ship tries to shoot and does not have ammo fir it
     */
	public Shot shootCannon() throws NoAmmoException {
		Shot cannonballVector = null;

		if (nAmmunitions[selectedAmmoIndex] > 0 || nAmmunitions[selectedAmmoIndex] == Constants.SHOT_AMMO_UNLIMITED) {
			timestampLastShot = SystemClock.elapsedRealtime();
            int halfShipWidth = mWidth / 2;
            int halfShotWidth = Shot.shotWidth / 2;
            double shotCanvasWidth = DrawableHelper.getWidth(context.getResources(), R.mipmap.txtr_ammo_default);
            double shotCanvasHeight = DrawableHelper.getHeight(context.getResources(), R.mipmap.txtr_ammo_default);
			if(this.isPlayable()) {
				switch (selectedAmmoIndex) {
					case 0:
						cannonballVector = new Shot(context, x + halfShipWidth - halfShotWidth, y - Shot.shotHeight - 10,
								mCanvasWidth, mCanvasHeight, new Point(
								this.entityCoordinates.x, this.entityCoordinates.y
								+ entityLength / 2), new Point(this.entityCoordinates.x,
								Constants.DEFAULT_SHIP_BASIC_RANGE
										* sType.rangeMultiplier()), Constants.DEFAULT_PLAYER_SHIP_DIRECTION,
								(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
										.powerMultiplier()), timestampLastShot);
						break;
					case 1:
						break;
					case 2:
						Shot[] cannonDoubleArray = new Shot[2];
						for (int i = 0, length = cannonDoubleArray.length; i < length; i++) {
							// Calculate value -1 when id is 0, +1 when id is 1
							int xValue = i==0?-1:1;
							cannonballVector = new Shot(context, x + halfShipWidth - halfShotWidth, y - Shot.shotHeight - 10,
									mCanvasWidth, mCanvasHeight, new Point(
									this.entityCoordinates.x, this.entityCoordinates.y
									+ entityLength / 2), new Point(xValue,
									Constants.DEFAULT_SHIP_BASIC_RANGE
											* sType.rangeMultiplier()), Constants.DEFAULT_PLAYER_SHIP_DIRECTION,
									(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
											.powerMultiplier()), timestampLastShot);
						}
						break;
					case 3:
						int shotsOnScreen = CanvasView.mScreenWidth / Shot.shotWidth;
						Shot[] cannonSweepArray = new Shot[shotsOnScreen - 1];
						for (int i = 0, length = cannonSweepArray.length; i < length; i++) {
							int xValue = i - (shotsOnScreen/2);
							cannonballVector = new Shot(context, x + halfShipWidth - halfShotWidth, y - Shot.shotHeight - 10,
									mCanvasWidth, mCanvasHeight, new Point(
									this.entityCoordinates.x, this.entityCoordinates.y
									+ entityLength / 2), new Point(xValue,
									Constants.DEFAULT_SHIP_BASIC_RANGE
											* sType.rangeMultiplier()), Constants.DEFAULT_PLAYER_SHIP_DIRECTION,
									(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
											.powerMultiplier()), timestampLastShot);
						}
						break;
				}

				if (nAmmunitions[selectedAmmoIndex] != Constants.SHOT_AMMO_UNLIMITED)
					nAmmunitions[selectedAmmoIndex]--;
			} else {
				// Crear constructor de disparos para disparos del barco enemigo
				Point origin = new Point(this.entityCoordinates.x, this.entityCoordinates.y
						+ entityLength / 2);
				Point destination = new Point(this.entityCoordinates.x,
						Constants.DEFAULT_SHIP_BASIC_RANGE
								* sType.rangeMultiplier());
				// Set shot image coordinates within horizon bounds
				cannonballVector = new Shot(context, x + halfShipWidth - halfShotWidth, y + mHeight + 10,
                        mCanvasWidth, mCanvasHeight, origin, destination, Constants.DIRECTION_DOWN,
						(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
								.powerMultiplier()), timestampLastShot);
			}
		} else {
			throw new NoAmmoException(context.getResources().getString(
					R.string.exception_ammo));
		}

		return cannonballVector;
	}

    /*
     * Update ship's drawable
     */
	public void updateImage(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			if(isPlayable){
				setImage(context.getResources().getDrawable(sType.drawableValue(), null));
			} else {
				switch(sType.ordinal()){
				case 0:
					if(entityDirection == Constants.DIRECTION_LEFT){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_left, null));
					} else if(entityDirection == Constants.DIRECTION_RIGHT) {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_right, null));
					} else if(entityDirection == Constants.DIRECTION_UP) {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_back, null));
					} else {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_front, null));
					}

					break;
				case 1:
					if(entityDirection == Constants.DIRECTION_LEFT){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_left, null));
					} else if(entityDirection == Constants.DIRECTION_RIGHT) {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_right, null));
					} else if(entityDirection == Constants.DIRECTION_UP){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_back, null));
					} else {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_front, null));
					}

					break;
				case 2:
					if(entityDirection == Constants.DIRECTION_LEFT){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_left, null));
					} else if(entityDirection == Constants.DIRECTION_RIGHT) {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_right, null));
					} else if(entityDirection == Constants.DIRECTION_UP){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_back, null));
					} else {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_front, null));
					}

					break;
				}
			}
			
		} else {
			if(isPlayable){
				setImage(context.getResources().getDrawable(sType.drawableValue()));
			} else {
				switch(sType.ordinal()){
				case 0:
					if(entityDirection == Constants.DIRECTION_LEFT){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_left));
					} else if(entityDirection == Constants.DIRECTION_RIGHT) {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_right));
					} else if(entityDirection == Constants.DIRECTION_UP){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_back));
					} else {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_light_front));
					}

					break;
				case 1:
					if(entityDirection == Constants.DIRECTION_LEFT){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_left));
					} else if(entityDirection == Constants.DIRECTION_RIGHT) {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_right));
					} else if(entityDirection == Constants.DIRECTION_UP){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_back));
					} else {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_medium_front));
					}

					break;
				case 2:
					if(entityDirection == Constants.DIRECTION_LEFT){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_left));
					} else if(entityDirection == Constants.DIRECTION_RIGHT) {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_right));
					} else if(entityDirection == Constants.DIRECTION_UP){
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_back));
					} else {
						setImage(context.getResources().getDrawable(R.mipmap.enemy_heavy_front));
					}

					break;
				}
			}
		}		
	}

    /**
     * Select the next Ammunition type
     */
	public void selectNextAmmo(){
		int actualAmmoIndex = selectedAmmoIndex;
		
		if(actualAmmoIndex + 1 == nAmmoTypes)
			actualAmmoIndex = 0;
		else
			actualAmmoIndex++;
		
		this.selectedAmmoIndex = actualAmmoIndex;
		this.selectedAmmo = Ammunitions.values()[selectedAmmoIndex];
	}

    /**
     * Select the previous Ammunition type
     */
	public void selectPreviousAmmo(){
		int actualAmmoIndex = selectedAmmoIndex;
		
		if(actualAmmoIndex == 0)
			actualAmmoIndex = nAmmoTypes -1;
		else
			actualAmmoIndex--;
		
		this.selectedAmmoIndex = actualAmmoIndex;
		this.selectedAmmo = Ammunitions.values()[selectedAmmoIndex];
	}

    /**
     * Check if the ship is reloaded
     * @param timestamp Current timestamp
     * @return true if the cannons are reloaded, false otherwise
     */
	public boolean isReloaded(long timestamp){
		return (timestamp - timestampLastShot) > (mReloadTime * 1000);
	}

	/**
	 * @return the mRange
	 */
	public float getRange() {
		return mRange;
	}

	/**
	 * @param mRange the mRange to set
	 */
	public void setRange(float mRange) {
		this.mRange = mRange;
	}

    /**
     * Add range to the ship
     * @param f range to add
     */
	public void addRange(float f) {
		this.mRange += f;
	}

    /**
     * Get the ship's power
     * @return Ship's power
     */
	public float getPower() {
		return mPower;
	}

    /**
     * Add power to the ship
     * @param f Power to add
     */
	public void addPower(float f) {
		this.mPower += f;		
	}

    /**
     * Get the ship's max health
     * @return Max health
     */
	public int getMaxHealth() {
		return mMaxHealth;
	}

    /**
     * Set the ship's max health
     * @param maxHealth New max health
     */
	public void setMaxHealth(int maxHealth) {
		this.mMaxHealth = maxHealth;
	}

    /**
     * Get the remaining ammunition of the specified Ammunition type
     * @param a AmmunitionType
     * @return Remaining ammunition
     */
	public int getAmmunition(Ammunitions a){
		return nAmmunitions[a.ordinal()];
	}

    /**
     * Checks if the Ship is playable by the Player
     * @return true if the ship is playable by the user
     */
	private boolean isPlayable() {
		return isPlayable;
	}

	@NonNull
	@Override
    /*
     * toString
     */
	public String toString() {
		return "Ship [sType=" + sType + ", nAmmunitions=" + nAmmunitions[selectedAmmoIndex] + ", mReloadTime="
				+ mReloadTime + ", mRange=" + mRange + ", timestampLastShot="
				+ timestampLastShot + ", entityDirection=" + entityDirection
				+ ", entityCoordinates=" + entityCoordinates + "]";
	}

    /**
     * Get the selected ammunition index
     * @return Selected ammunition index
     */
    public int getSelectedAmmunitionIndex() {
        return selectedAmmoIndex;
    }

    /**
     * Get the remaining ammunition of the selected Ammunition type
     * @return Remaining ammunition
     */
	public int getSelectedAmmunition() {
		return nAmmunitions[selectedAmmoIndex];
	}

    /**
     * Get the selected Ammunition Type
     * @return Selected ammunition Type
     */
	public Ammunitions getSelectedAmmo(){
		return Ammunitions.values()[selectedAmmoIndex];
	}

    /**
     * Get the Ship's type
     * @return Ship's type
     */
	public ShipType getShipType() {
		return sType;
	}

    /**
     * Get the ship's type index of the specified ship type
     * @param s Ship type
     * @return Index
     */
	private int getShipTypeIndex(ShipType s){
		int index = 0;
		ShipType[] sTypes = ShipType.values();
		for(int i = 0; i < sTypes.length; i++){
			ShipType st = sTypes[i];
			if(s.name().equals(st.name()))
				index = i;
		}
		return index;
	}

    /**
     * Set the ship as Idle (no movement)
     * @param wasIdle true if stopped, false otherwise
     */
	private void setIdle(boolean wasIdle) {
		this.wasIdle = wasIdle;
	}

    /**
     * Select a random ShipType
     * @return Random ShipType
     */
	public static ShipType randomShipType() {
		ShipType[] types = ShipType.values();
		int length = types.length;
		int randomIndex = new Random().nextInt(length);
		return types[randomIndex];
	}

    /**
     * Move the ship's coordinates to the destiny Point
     * @param destiny Destination's point
     */
	public void moveShipEntity (Point destiny){
		int xDiff = 0;
		int yDiff = 0;
		int nextX = 0;
		int nextY = 0;

		// Get current Point
		Point curr = new Point(entityCoordinates.x, entityCoordinates.y);

		// Set difference with destiny Point
		if(destiny.x > curr.x){			// Destiny to the right
			xDiff = destiny.x - curr.x;	// Get the positive needed amount to reach the destiny
		} else if(destiny.x < curr.x) {	// Destiny to the left
			xDiff = curr.x - destiny.x;	// Get the positive needed amount to reach the destiny
		}
		if(destiny.y > curr.y){			// Destiny to the front
			yDiff = destiny.y - curr.y;	// Get the positive needed amount to reach the destiny
		} else if(destiny.y < curr.y) { // Destiny to the back
			yDiff = curr.y - destiny.y;	// Get the positive needed amount to reach the destiny
		}

		// Calculate next Point coordinates
		if(xDiff > 0){
			if(destiny.x > curr.x){			// Destiny to the right
				nextX = curr.x + 1;			// Next point moved 1 position to the side
			} else if(destiny.x < curr.x) {	// Destiny to the left
				nextX = curr.x - 1;			// Next point moved 1 position to the side
			}
		}
		if(yDiff > 0){
			if(destiny.y > curr.y){			// Destiny to the front
				nextY = curr.y + 1;			// Next point moved 1 position to the front
			} else if(destiny.y < curr.y) {	// Destiny to the back
				nextY = curr.y - 1;			// Next point moved 1 position to the back
			}
		}

		// Set next Point coordinates
		Point next = new Point(xDiff > 0 ? nextX : curr.x, yDiff > 0 ? nextY : curr.y);
		entityCoordinates = new Point(next.x, next.y);
	}

    /**
     * Set the remaining selected ammunition's value
     * @param ammo New remaining ammunition value
     */
	public void setSelectedAmmunition(int ammo){
		nAmmunitions[selectedAmmoIndex] = ammo;
	}

    /**
     * Update ship type
     * (Light > Medium > Heavy)
     * @param newShipType New ShipType
     */
	public void updateShipType(ShipType newShipType) {
		this.sType = newShipType;
		this.mRange = sType.rangeMultiplier();
		this.mPower = sType.powerMultiplier();
		this.mReloadTime = (int) sType.powerMultiplier() * Constants.DEFAULT_SHIP_RELOAD;
		gainHealth(this.mMaxHealth = sType.defaultHealthPoints());
	}

	@Override
    /*
     * Ignore this method
     */
	public int describeContents() {
		return 0;
	}

	@Override
    /*
     * Method to save Ship's values to the Parcel
     */
	public void writeToParcel(Parcel parcel, int i) {

		parcel.writeInt(nAmmoTypes);
		parcel.writeIntArray(nAmmunitions);
		parcel.writeInt(selectedAmmoIndex);
		parcel.writeInt(mReloadTime);
		parcel.writeFloat(mPower);
		parcel.writeFloat(mRange);
		parcel.writeLong(timestampLastShot);
		parcel.writeByte((byte) (isPlayable ? 1 : 0));
		parcel.writeByte((byte) (wasIdle ? 1 : 0));
		parcel.writeInt(mHealthPoints);
		parcel.writeInt(mMaxHealth);
		int shipEnum = getShipTypeIndex(sType);
		parcel.writeInt(shipEnum);
	}

    /**
     * Set whether the ship is Playable or not
     * @param playable True if the ship is Playable by the user, false otherwise
     */
    public void setPlayable(boolean playable) {
        this.isPlayable = playable;
    }
}