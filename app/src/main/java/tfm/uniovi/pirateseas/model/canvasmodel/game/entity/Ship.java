package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import java.util.Random;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.exceptions.NoAmmoException;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.view.graphics.canvasview.CanvasView;

public class Ship extends Entity implements Parcelable{
	// Crear array de variables para almacenar la cantidad de cada tipo de municion
	private int nAmmoTypes = Ammunitions.values().length;
	private int[] nAmmunitions = new int[nAmmoTypes];
	private Ammunitions selectedAmmo;
	private int selectedAmmoIndex= 0;
	
	private int mReloadTime;
	private float mPower;
	private float mRange;
	private long timestampLastShot;
	
	private boolean isPlayable;
	private boolean wasIdle;
	
	private ShipType sType;
	
	private Context context;
	
	public Ship(){
		super(null, 0, 0, 0, 0, new Point(0, 0), Constants.DEFAULT_PLAYER_SHIP_DIRECTION, 0, 0, 0);
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
	public Ship(Context context, ShipType sType, double x, double y, double canvasWidth, 
				double canvasHeight, Point coordinates, int direction, int width, int height, int length, int ammo){
		super(context, x, y, canvasWidth, canvasHeight, coordinates, direction, width, height, length);
		
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
	public Ship(Context context, Ship baseShip, ShipType sType, Point coordinates, int direction, int width, int height, int length, int health, int ammo){
		super(context, baseShip.x, baseShip.y, baseShip.mCanvasWidth, baseShip.mCanvasHeight, coordinates, direction, width, height, length);
		
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
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public Ship(Context context, Ship baseShip, ShipType sType, Point coordinates, int direction, int width, int height, int length, int health, int[] ammoTypes,
			int selectedAmmoType) {
		super(context, baseShip.x, baseShip.y, baseShip.mCanvasWidth, baseShip.mCanvasHeight, coordinates, direction, width, height, length);
		
		this.context = context;
		
		this.nAmmunitions = ammoTypes;
		this.selectedAmmo = Ammunitions.values()[selectedAmmoType];
		this.selectedAmmoIndex = selectedAmmoType;
		
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

	protected Ship(Parcel in) {
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

	public void gainAmmo(int ammo, Ammunitions ammoType){
		if(ammo > 0){
			nAmmunitions[Ammunitions.valueOf(ammoType.getName()).ordinal()] += ammo;
		} else
			throw new IllegalArgumentException("Encontrado valor de puntos negativo al modificar mAmmunition");
	}
	
	public Shot shootCannon() throws NoAmmoException {
		Shot cannonballVector = null;

		if (nAmmunitions[selectedAmmoIndex] > 0 || nAmmunitions[selectedAmmoIndex] == Constants.SHOT_AMMO_UNLIMITED) {
			timestampLastShot = SystemClock.elapsedRealtime();
			if(this.isPlayable()) {
				switch (selectedAmmoIndex) {
					case 0:
						cannonballVector = new Shot(context, x + (mWidth / 2) - (Shot.shotWidth / 2), y - Shot.shotHeight - 10,
								this.mCanvasWidth, this.mCanvasHeight, new Point(
								this.entityCoordinates.x, this.entityCoordinates.y
								+ entityLength / 2), new Point(this.entityCoordinates.x,
								Constants.DEFAULT_SHIP_BASIC_RANGE
										* sType.rangeMultiplier()), Constants.DEFAULT_PLAYER_SHIP_DIRECTION,
								(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
										.powerMultiplier()), timestampLastShot);
						break;
					case 1:
						cannonballVector = new Shot(context, x + (mWidth / 2) - (Shot.shotWidth / 2), y - Shot.shotHeight - 10,
								this.mCanvasWidth, this.mCanvasHeight, new Point(
								this.entityCoordinates.x, this.entityCoordinates.y
								+ entityLength / 2), new Point(this.entityCoordinates.x,
								Constants.DEFAULT_SHIP_BASIC_RANGE
										* sType.rangeMultiplier()), Constants.DEFAULT_PLAYER_SHIP_DIRECTION,
								(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
										.powerMultiplier()), timestampLastShot);
						break;
					case 2:
						Shot[] cannonDoubleArray = new Shot[2];
						for (int i = 0, length = cannonDoubleArray.length; i < length; i++) {
							// Calculate value -1 when id is 0, +1 when id is 1
							int xValue = i==0?-1:1;
							cannonballVector = new Shot(context, x + (mWidth / 2) - (Shot.shotWidth / 2), y - Shot.shotHeight - 10,
									this.mCanvasWidth, this.mCanvasHeight, new Point(
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
							cannonballVector = new Shot(context, x + (mWidth / 2) - (Shot.shotWidth / 2), y - Shot.shotHeight - 10,
									this.mCanvasWidth, this.mCanvasHeight, new Point(
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
				cannonballVector = new Shot(context, x + (mWidth / 2) - (Shot.shotWidth / 2), y + mHeight + 10,
						this.mCanvasWidth, this.mCanvasHeight, origin, destination, Constants.DIRECTION_DOWN,
						(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
								.powerMultiplier()), timestampLastShot);
			}
		} else {
			throw new NoAmmoException(context.getResources().getString(
					R.string.exception_ammo));
		}

		return cannonballVector;
	}

	public Shot[] shootSide() throws NoAmmoException {
		Shot[] cannonballArray = new Shot[3];
		Shot cannonballVector = null;

		if (nAmmunitions[selectedAmmoIndex] >= 3 || nAmmunitions[selectedAmmoIndex] == Constants.SHOT_AMMO_UNLIMITED) {
			timestampLastShot = SystemClock.elapsedRealtime();
			for (int i = 0, length = cannonballArray.length; i < length; i++) {

				Point ini = new Point(this.entityCoordinates.x + entityWidth
						/ 2, this.entityCoordinates.y);
				Point fin = new Point(Constants.DEFAULT_SHIP_BASIC_RANGE
						* sType.rangeMultiplier(), i - 1);
				int num = fin.y - ini.y;
				int den = fin.x - ini.x;
				float m = (num * 1.0f) / den;
				double angM = Math.toDegrees(Math.atan(m));
				cannonballVector = new Shot(context, x + (mWidth / 2), y
						+ (mHeight / 4), this.mCanvasWidth, this.mCanvasHeight,
						ini, fin, (int) (-angM),
						(int) (Constants.DEFAULT_SHOOT_DAMAGE * sType
								.powerMultiplier()), timestampLastShot);

				cannonballArray[i] = cannonballVector;
				if (nAmmunitions[selectedAmmoIndex] != Constants.SHOT_AMMO_UNLIMITED)
					nAmmunitions[selectedAmmoIndex]--;
			}
		} else {
			cannonballArray = null;
			throw new NoAmmoException(context.getResources().getString(
					R.string.exception_ammo));
		}

		return cannonballArray;
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
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
					
					// this.mCanvasWidth = DrawableHelper.getWidth(context.getResources(), ShipType.LIGHT.drawableValue());
					// this.mCanvasHeight = DrawableHelper.getHeight(context.getResources(), ShipType.LIGHT.drawableValue());
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
					
					// this.mCanvasWidth = DrawableHelper.getWidth(context.getResources(), ShipType.MEDIUM.drawableValue());
					// this.mCanvasHeight = DrawableHelper.getHeight(context.getResources(), ShipType.MEDIUM.drawableValue());
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
					
					// this.mCanvasWidth = DrawableHelper.getWidth(context.getResources(), ShipType.HEAVY.drawableValue());
					// this.mCanvasHeight = DrawableHelper.getHeight(context.getResources(), ShipType.HEAVY.drawableValue());
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
					
					// this.mCanvasWidth = DrawableHelper.getWidth(context.getResources(), ShipType.LIGHT.drawableValue());
					// this.mCanvasHeight = DrawableHelper.getHeight(context.getResources(), ShipType.LIGHT.drawableValue());
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
					
					// this.mCanvasWidth = DrawableHelper.getWidth(context.getResources(), ShipType.MEDIUM.drawableValue());
					// this.mCanvasHeight = DrawableHelper.getHeight(context.getResources(), ShipType.MEDIUM.drawableValue());
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
					
					// this.mCanvasWidth = DrawableHelper.getWidth(context.getResources(), ShipType.HEAVY.drawableValue());
					// this.mCanvasHeight = DrawableHelper.getHeight(context.getResources(), ShipType.HEAVY.drawableValue());
					break;
				}
			}
			
		}		
	}
	
/*
	public void turn(int degrees){
		setEntityDirection(degrees);
		
		switch(sType){
			case LIGHT:
				switch(degrees){
					case 0:
						setImage(R.drawable.txtr_ship_light_right);
						break;
					case 90:
						setImage(R.drawable.txtr_ship_light_front);
						break;
					case 180:
						setImage(R.drawable.txtr_ship_light_left);
						break;
					case 270:
						setImage(R.drawable.txtr_ship_light_back);
						break;
				}
				break;
			case MEDIUM:
				switch(degrees){
					case 0:
						setImage(R.drawable.txtr_ship_medium_right);
						break;
					case 90:
						setImage(R.drawable.txtr_ship_medium_front);
						break;
					case 180:
						setImage(R.drawable.txtr_ship_medium_left);
						break;
					case 270:
						setImage(R.drawable.txtr_ship_medium_back);
						break;
				}
				break;
			case HEAVY:
				switch(degrees){
					case 0:
						setImage(R.drawable.txtr_ship_heavy_right);
						break;
					case 90:
						setImage(R.drawable.txtr_ship_heavy_front);
						break;
					case 180:
						setImage(R.drawable.txtr_ship_heavy_left);
						break;
					case 270:
						setImage(R.drawable.txtr_ship_heavy_back);
						break;
				}
			break;
		}
	}
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
	
	public void selectPreviousAmmo(){
		int actualAmmoIndex = selectedAmmoIndex;
		
		if(actualAmmoIndex == 0)
			actualAmmoIndex = nAmmoTypes -1;
		else
			actualAmmoIndex--;
		
		this.selectedAmmoIndex = actualAmmoIndex;
		this.selectedAmmo = Ammunitions.values()[selectedAmmoIndex];
	}
	
	/*public boolean changeAmmoType () {
			// Obtener indice actual
			int nextIndexWithAmmo = -1; 
			
			for (int i = 0 ;i < nAmmoTypes; i++) {
				if(nAmmunitions[i] > 0 && i != selectedAmmoIndex){
					nextIndexWithAmmo = i;
					break;
				}
			}
			
			if(nextIndexWithAmmo != -1 && nextIndexWithAmmo != selectedAmmoIndex) {
				selectedAmmoIndex = nextIndexWithAmmo;
				return true;
			} else
				return false;
	}*/
	
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
	
	public void addRange(float f) {
		this.mRange += f;
	}	

	public float getPower() {
		return mPower;
	}

	public void setPower(float mPower) {
		this.mPower = mPower;
	}
	
	public void addPower(float f) {
		this.mPower += f;		
	}

	/**
	 * @return the sType
	 */
	public ShipType getType() {
		return sType;
	}

	public int getMaxHealth() {
		return mMaxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.mMaxHealth = maxHealth;
	}

	public int[] getAmmunitions() {
		return nAmmunitions;
	}
	
	public int getAmmunition(Ammunitions a){
		return nAmmunitions[a.ordinal()];
	}

	public boolean isPlayable() {
		return isPlayable;
	}

	@Override
	public String toString() {
		return "Ship [sType=" + sType + ", nAmmunitions=" + nAmmunitions[selectedAmmoIndex] + ", mReloadTime="
				+ mReloadTime + ", mRange=" + mRange + ", timestampLastShot="
				+ timestampLastShot + ", entityDirection=" + entityDirection
				+ ", entityCoordinates=" + entityCoordinates + "]";
	}

    public int getSelectedAmmunitionIndex() {
        return selectedAmmoIndex;
    }

	public int getSelectedAmmunition() {
		return nAmmunitions[selectedAmmoIndex];
	}

	public ShipType getShipType() {
		return sType;
	}

	public int getShipTypeIndex(ShipType s){
		int index = 0;
		ShipType[] sTypes = ShipType.values();
		for(int i = 0; i < sTypes.length; i++){
			ShipType st = sTypes[i];
			if(s.name().equals(st.name()))
				index = i;
		}
		return index;
	}
	
	public void setShipTypeDefaultSpeed(){
		this.mSpeedXLevel = this.sType.getSpeed();
	}

	public boolean wasIdle() {
		return wasIdle;
	}

	public void setIdle(boolean wasIdle) {
		this.wasIdle = wasIdle;
	}

	public static ShipType randomShipType() {
		ShipType[] types = ShipType.values();
		int length = types.length;
		int randomIndex = new Random().nextInt(length);
		return types[randomIndex];
	}

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

	public void updateShipType(ShipType newShipType) {
		this.sType = newShipType;
		this.mRange = sType.rangeMultiplier();
		this.mPower = sType.powerMultiplier();
		this.mReloadTime = (int) sType.powerMultiplier() * Constants.DEFAULT_SHIP_RELOAD;
		gainHealth(this.mMaxHealth = sType.defaultHealthPoints());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
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

    public void setPlayable(boolean playable) {
        this.isPlayable = playable;
    }
}