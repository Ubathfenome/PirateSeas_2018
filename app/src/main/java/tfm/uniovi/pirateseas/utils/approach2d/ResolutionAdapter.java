package tfm.uniovi.pirateseas.utils.approach2d;

import android.content.Context;
import android.content.SharedPreferences;

import tfm.uniovi.pirateseas.global.Constants;

/**
 * Class to improve the view with different resolutions
 * @author Miguel
 *
 */
public class ResolutionAdapter {

	private int screenWidth;
	private int screenHeight;
	private int resolutionWidth;
	private int resolutionHeight;	
	private double factorX;
	private double factorY;
	
	private SharedPreferences mPreferences;

	/**
	 * Constructor
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 */
	public ResolutionAdapter(Context context, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		
		mPreferences = context.getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);
		this.resolutionWidth = mPreferences.getInt(Constants.PREF_DEVICE_WIDTH_RES, 1024);
		this.resolutionHeight = mPreferences.getInt(Constants.PREF_DEVICE_HEIGHT_RES, 768);
		instanceFactor();
	}

	/**
	 * Set the default factor for the views 
	 */
	private void instanceFactor() {
		factorY = screenHeight / resolutionHeight;
		factorX = screenWidth / resolutionWidth;
	}

	/**
	 * Scale the y-coordinate
	 * 
	 * @param y
	 * @return Scaled coordinate
	 */
	public double y(double y) {
		return (y * factorY);
	}

	/**
	 * Scale the x-coordinate
	 * 
	 * @param x
	 * @return Scaled coordinate
	 */
	public double x(double x) {
		return (x * factorX);
	}

	/**
	 * Scale the height
	 * 
	 * @param height
	 * @return Scaled height
	 */
	public int height(int height) {
		if (factorX < factorY) {
			return (int) (height * factorX);
		} else {
			return (int) (height * factorY);
		}
	}

	/**
	 * Scale the width
	 * 
	 * @param width
	 * @return Scaled width
	 */
	public int width(int width) {
		if (factorY < factorX) {
			return (int) (width * factorY);
		} else {
			return (int) (width * factorX);
		}
	}
}
