package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import tfm.uniovi.pirateseas.R;

/**
 * Enum with the different types of ammunition
 */
public enum Ammunitions {
	DEFAULT(R.mipmap.txtr_ammo_default, "DEFAULT"),
	AIMED(R.mipmap.txtr_ammo_aimed, "AIMED"),
	DOUBLE(R.mipmap.txtr_ammo_double, "DOUBLE"),
	SWEEP(R.mipmap.txtr_ammo_sweep, "SWEEP");
	
	private final int mDrawableValue;
	private final String mName;

	/**
	 * Constructor
	 * @param drawableValue Drawable resource
	 * @param name Name
	 */
	Ammunitions(int drawableValue, String name) {
		this.mDrawableValue = drawableValue;
		this.mName = name;
	}

	/**
	 * Get the ammunition's name
	 * @return Ammunition's name
	 */
	public String getName(){
		return mName;
	}

	/**
	 * Get the ammunition's drawable resource
	 * @return Ammunition's drawable resource
	 */
	public int drawableValue(){
		return mDrawableValue;
	}
}
