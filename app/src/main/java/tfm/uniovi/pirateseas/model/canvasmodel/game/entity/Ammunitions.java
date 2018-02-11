package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import tfm.uniovi.pirateseas.R;

public enum Ammunitions {
	DEFAULT(R.mipmap.txtr_ammo_default, "DEFAULT"),
	AIMED(R.mipmap.txtr_ammo_aimed, "AIMED"),
	DOUBLE(R.mipmap.txtr_ammo_double, "DOUBLE"),
	SWEEP(R.mipmap.txtr_ammo_sweep, "SWEEP");
	
	private final int mDrawableValue;
	private final String mName;
	
	Ammunitions(int drawableValue, String name) {
		this.mDrawableValue = drawableValue;
		this.mName = name;
	}
	
	public String getName(){
		return mName;
	}
	
	public int drawableValue(){
		return mDrawableValue;
	}
}
