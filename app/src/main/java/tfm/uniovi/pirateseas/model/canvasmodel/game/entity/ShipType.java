package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import tfm.uniovi.pirateseas.R;

/**
 * Enum with all the possible ShipTypes
 */
public enum ShipType {
	LIGHT (R.mipmap.txtr_ship_light, 10, 3, 1f, 15),
	MEDIUM (R.mipmap.txtr_ship_medium, 30, 2, 1.5f, 6),
	HEAVY (R.mipmap.txtr_ship_heavy, 50, 2, 2f, 3);
	
	private final int mDrawableValue;
	private final int mDefaultHealthPoints;
	private final int mRangeMultiplier;
	private final float mPowerMultiplier;
	private final int mSpeed;

	/**
	 * Constructor
	 * @param drawableValue Ship drawable resource
	 * @param healthPoints Ship's default health points
	 * @param range Ship's default range
	 * @param power Ship's default power
	 * @param speed Ship's default speed
	 */
	ShipType (int drawableValue, int healthPoints, int range, float power, int speed){
		this.mDrawableValue = drawableValue;
		this.mDefaultHealthPoints = healthPoints;
		this.mRangeMultiplier = range;
		this.mPowerMultiplier = power;
		this.mSpeed = speed;
	}

	/**
	 * Get the ShipType's default drawable resource
	 * @return Drawable resource
	 */
	public int drawableValue(){
		return mDrawableValue;
	}

	/**
	 * Get the ShipType's default health points
	 * @return ShipType's default health points
	 */
	public int defaultHealthPoints(){
		return mDefaultHealthPoints;
	}

	/**
	 * Get the ShipType's default range
	 * @return ShipType's default range
	 */
	public int rangeMultiplier(){
		return mRangeMultiplier;
	}

	/**
	 * Get the ShipType's default power
	 * @return ShipType's default power
	 */
	public float powerMultiplier(){
		return mPowerMultiplier;
	}

	/**
	 * Get the ShipType's default speed
	 * @return ShipType's default speed
	 */
	public int getSpeed() {
		return mSpeed;
	}
}
