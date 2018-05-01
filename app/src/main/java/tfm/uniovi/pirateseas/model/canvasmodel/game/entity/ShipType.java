package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import tfm.uniovi.pirateseas.R;

public enum ShipType {
	LIGHT (R.mipmap.txtr_ship_light, 100, 3, 1f, 15),
	MEDIUM (R.mipmap.txtr_ship_medium, 250, 2, 1.5f, 6),
	HEAVY (R.mipmap.txtr_ship_heavy, 400, 2, 2f, 3);
	
	private final int mDrawableValue;
	private final int mDefaultHealthPoints;
	private final int mRangeMultiplier;
	private final float mPowerMultiplier;
	private final int mSpeed;
	
	ShipType (int drawableValue, int healthPoints, int range, float power, int speed){
		this.mDrawableValue = drawableValue;
		this.mDefaultHealthPoints = healthPoints;
		this.mRangeMultiplier = range;
		this.mPowerMultiplier = power;
		this.mSpeed = speed;
	}
	
	public int drawableValue(){
		return mDrawableValue;
	}
	
	public int defaultHealthPoints(){
		return mDefaultHealthPoints;
	}
	
	public int rangeMultiplier(){
		return mRangeMultiplier;
	}
	
	public float powerMultiplier(){
		return mPowerMultiplier;
	}

	public int getSpeed() {
		return mSpeed;
	}
}
