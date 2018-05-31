package tfm.uniovi.pirateseas.model.canvasmodel.game.scene;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.model.canvasmodel.game.BasicModel;

/**
 * Class to manage the sea behaviour
 */
public class Sea extends BasicModel{
	
	private Drawable mImageAux;
	private static int startingHeight;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
    /**
     * Constructor
     */
	public Sea(Context context, double x, double y, double mCanvasWidth,
            double mCanvasHeight){
		super(context, x, y, mCanvasWidth, mCanvasHeight, null);
		
		startingHeight = (int) y;
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			setImage(context.getResources().getDrawable(R.mipmap.txtr_water, null));
			mImageAux = context.getResources().getDrawable(R.mipmap.txtr_water, null);
		} else {
			setImage(context.getResources().getDrawable(R.mipmap.txtr_water));
			mImageAux = context.getResources().getDrawable(R.mipmap.txtr_water);
		}
	}
	
	@Override
    /**
     * Draws on the screen the image of the model
     */
	public void drawOnScreen(Canvas canvas){
		yUp = (int) y;
		xLeft = (int) x;
 
        mImage.setBounds(xLeft, yUp, (int)(xLeft + mCanvasWidth), (int)(yUp + mCanvasHeight));
        mImage.draw(canvas);
		
		// Si la xLeft no es cero
		// Es necesario pintar un fondo auxiliar por la derecha o la izquierda. 
		if (xLeft < 0) { 
			mImageAux.setBounds((int) (xLeft + mCanvasWidth), yUp, (int) (xLeft + mCanvasWidth) + mWidth, yUp + mHeight);
			mImageAux.draw(canvas);
		} else if (xLeft > 0) { 
			mImageAux.setBounds((int) (xLeft - mCanvasWidth), yUp, (int) (xLeft - mCanvasWidth) + mWidth, yUp + mHeight);
			mImageAux.draw(canvas);
		}

		// Si la yUp no es startingHeight
		// Es necesario pintar un fondo auxiliar por arriba o por abajo.
		if (yUp < startingHeight) { 
			mImageAux.setBounds(xLeft, yUp, (int) mCanvasWidth, (int) mCanvasHeight);
			mImageAux.draw(canvas);
		} else if (yUp > startingHeight) { 
			mImageAux.setBounds(xLeft, startingHeight, (int) mCanvasWidth, (int) mCanvasHeight);
			mImageAux.draw(canvas);
		}
	}
		
	@Override
    /**
     * Move the sea image
     */
	public void move(double xLength, double yLength, boolean bounce){
		x = x - xLength;
		y = y + Math.abs(yLength);
		
		// Horizontal behavior
		if ( x > mCanvasWidth){
			x = 0;
		} 
		if ( x < 0){
			x = mCanvasWidth;
		}
		
		// Vertical behavior
		if( y > mCanvasHeight || y < startingHeight){
			y = startingHeight;
		}
	}

    /**
     * Sets the filter to the sea brightness
     * @param value Filter value
     */
	public void setFilterValue(int value){
		this.filterValue = value;
	}
}
