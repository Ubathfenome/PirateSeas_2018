package tfm.uniovi.pirateseas.model.canvasmodel.game.scene;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.model.canvasmodel.game.BasicModel;

/**
 * Class to manage the Sky object
 *
 * @see: https://softwyer.wordpress.com/2012/01/21/1009/
 * @see: http://en.wikipedia.org/wiki/Alpha_compositing
 *
 */
@SuppressWarnings("unused")
public class Sky extends BasicModel{
	private static final String TAG = "Sky";
	
	private Drawable mImageAux;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	/*
	 * Constructor
	 */
	public Sky(Context context, double x, double y, double mCanvasWidth,
            double mCanvasHeight){
		super(context, x, y, mCanvasWidth, mCanvasHeight, null);

		setImage(context.getResources().getDrawable(R.mipmap.txtr_sky_clear, null));
		mImageAux = context.getResources().getDrawable(R.mipmap.txtr_sky_clear, null);
	}
	
	@Override
	/*
	 * Draws on the screen the image of the model
	 */
	public void drawOnScreen(Canvas canvas){
		yUp = (int) y;
		xLeft = (int) x;
 
        mImage.setBounds(xLeft, yUp, (int) (xLeft + mCanvasWidth), (int) (yUp + mCanvasHeight));
        mImage.setColorFilter(filterValue * FILTER_MASK, PorterDuff.Mode.SRC_ATOP);
        mImageAux.setColorFilter(filterValue * FILTER_MASK, PorterDuff.Mode.SRC_ATOP);
        mImage.draw(canvas);
		
		// Si la xLeft no es cero 0 
		// Es necesario pintar un fondo auxiliar por la derecha o la izquierda. 
		if (xLeft < 0) { 
			mImageAux.setBounds((int) (xLeft + mCanvasWidth), yUp, (int) (xLeft + mCanvasWidth) + mWidth, yUp + mHeight);
			mImageAux.draw(canvas);
		} else if (xLeft > 0) { 
			mImageAux.setBounds((int) (xLeft - mCanvasWidth), yUp, (int) (xLeft - mCanvasWidth) + mWidth, yUp + mHeight);
			mImageAux.draw(canvas);
		}
	}

	/**
	 * Sets the filter to the sky brightness
	 * @param value Filter value
	 */
	public void setFilterValue(int value){
		this.filterValue = value;
	}

	@NonNull
	@Override
	/*
	 * toString
	 */
	public String toString() {
		return "Sky [filterValue=" + filterValue + "]";
	}
	
}
