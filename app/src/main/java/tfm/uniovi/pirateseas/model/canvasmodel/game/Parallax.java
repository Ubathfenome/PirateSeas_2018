package tfm.uniovi.pirateseas.model.canvasmodel.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Class to manage the behaviour of objects that have 2 images to represent
 */
public class Parallax {
	
	private Drawable imageBase = null, imageTop = null;
	public static final float SPEED_BASE = 1.2f;
	public static final float SPEED_TOP = 8f;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	/*
	 * Constructor
	 */
	public Parallax(Context context, int resourceBase, int resourceTop){
		if(resourceBase != 0)
			imageBase = context.getResources().getDrawable(resourceBase, null);
		if(resourceTop != 0)
			imageTop = context.getResources().getDrawable(resourceTop, null);
	}

	/**
	 * Return Drawable array with the 2 images of the Parallax
	 * @return Drawable array with the 2 images of the Parallax
	 */
	public Drawable[] getLayers(){
		Drawable[] layers = new Drawable[2];
		layers[0] = imageBase;
		layers[1] = imageTop;
		return layers;
	}

	/**
	 * Sets the alpha value for the parallax
	 * @param alpha alpha
	 */
	public void setAlpha(int alpha) {
		imageBase.setAlpha(alpha);
		imageTop.setAlpha(alpha);
	}

	/**
	 * Get the height of the taller image on the Parallax
	 * @return Height
	 */
	int getMaxHeight(){
		int hBase = imageBase.getIntrinsicHeight();
		int hTop = imageTop.getIntrinsicHeight();
		return hBase >= hTop ? hBase : hTop;
	}

	/**
	 * Get the top width of the wider image on the Parallax
	 * @return Width
	 */
	int getMaxWidth(){
		int wBase = imageBase.getIntrinsicWidth();
		int wTop = imageTop.getIntrinsicWidth();
		return wBase >= wTop ? wBase : wTop;
	}

}
