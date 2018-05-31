package tfm.uniovi.pirateseas.model.canvasmodel.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Class to represent objects on the screen canvas
 */
public class BasicModel{
	protected static final int FILTER_MASK = (int) Math.pow(2, (2 * 8 + 8));
	
	protected Context context;
    protected double mCanvasHeight;
    protected double mCanvasWidth;

	// Model properties
    protected double x;
    protected double y;
    protected int mHeight;
    protected int mWidth;
    protected Drawable mImage;
    protected Parallax mParallax;
	
    protected int yUp;
    protected int xLeft;
    
    protected int filterValue = 1;

    /**
     * Constructor
     * @param context
     * @param x
     * @param y
     * @param mCanvasWidth
     * @param mCanvasHeight
     * @param parallax
     */
	public BasicModel(Context context, double x, double y, double mCanvasWidth,
            double mCanvasHeight, Parallax parallax){
		
		this.context = context;
        this.x = x; 
        this.y = y;
        this.mCanvasHeight = mCanvasHeight;
        this.mCanvasWidth = mCanvasWidth;
        
        setParallax(parallax);
	}

    /**
     * Move the object the specified pixels on the specified coordinate
     * @param xLength
     * @param yLength
     * @param bounce  true if the object should bounce on the screen's edge, false otherwise
     */
	public void move(double xLength, double yLength, boolean bounce){
		x = x - xLength;
		y = y - yLength;
		
		if (bounce) {
			if (x + mWidth > mCanvasWidth) {
				x = mCanvasWidth - mWidth;
			}
			if (x < 0) {
				x = 0;
			}

			if (y > mCanvasHeight) {
				y = mCanvasHeight - yLength;
			}
			if (y < 0) {
				y = 0;
			}
		} else {
			if (x > mCanvasWidth) {
				x = 0;
			}
			if (x < 0) {
				x = mCanvasWidth;
			}

			if (y > mCanvasHeight) {
				y = 0;
			}
			if (y < 0) {
				y = mCanvasHeight;
			}
		}
	}
	
	/**
     * Draws on the screen the image of the model
     * 
     * @param canvas
     */
    public void drawOnScreen(Canvas canvas) {
        yUp = (int) y;
        xLeft = (int) x;
 
        if (mParallax == null){
        	mImage.setBounds(xLeft, yUp, xLeft + mWidth, yUp + mHeight);
        	mImage.draw(canvas);
        } else {
        	Drawable[] parallaxLayers = mParallax.getLayers();
        	for(int i = 0; i < 2; i++){
        		if(parallaxLayers[i] != null){
        			parallaxLayers[i].setBounds(xLeft, yUp, xLeft + mWidth, yUp + mHeight);
        			parallaxLayers[i].draw(canvas);
        		}
        	}
        }
    }

    /**
     * Get the object's drawable
     * @return
     */
	public Drawable getImage() {
        return mImage;
    }

    /**
     * Set the object's drawable
     * @param image
     */
    public void setImage(Drawable image) {
        this.mImage = image;
        this.mWidth = image.getIntrinsicWidth();
        this.mHeight = image.getIntrinsicHeight();
    }

    /**
     * Get the object's parallax (if any)
     * @return Object's Parallax
     */
    public Parallax getParallax(){
    	return this.mParallax;
    }

    /**
     * Set the object's parallax
     * @param parallax Parallax
     */
    public void setParallax(Parallax parallax){
    	this.mParallax = parallax;
    	if(parallax != null){
	    	this.mWidth = parallax.getMaxWidth();
	    	this.mHeight = parallax.getMaxHeight();
    	}
    }

    /**
     * Get the left x value of the object
     * @return xValue
     */
	public double getX() {
        return x;
    }

    /**
     * Get the top y value of the object
     * @return yValue
     */
    public double getY() {
        return y;
    }

    /**
     * Set the left x value of the object
     * @param x new xValue
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Set the top y value of the object
     * @param y new yValue
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Get the image height of the object
     * @return Height value
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Set the object's height
     * @param height
     */
    public void setHeight(int height) {
        this.mHeight = height;
    }

    /**
     *  Get the image width of the object
     * @return
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Set the object's width
     * @param width
     */
    public void setWidth(int width) {
        this.mWidth = width;
    }   
    
}