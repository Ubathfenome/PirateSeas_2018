package tfm.uniovi.pirateseas.model.canvasmodel.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.BasicModel;

/**
 * Class to represent bars on the screen
 */
public class StatBar extends BasicModel {
	
	private int mType;
	
	private int maxValue;
	private int currentValue;

	private int barLength;

	/**
	 * Constructor
	 * @param context Context context
	 * @param x  Coordinate x
	 * @param y  Coordinate y
	 * @param mCanvasWidth Canvas width
	 * @param mCanvasHeight Canvas Height
	 * @param maxValue max value
	 * @param currentValue current value
	 * @param type StatBar type
	 */
	public StatBar(Context context, double x, double y, double mCanvasWidth, double mCanvasHeight, int maxValue, int currentValue, int type){
		super(context, x, y, mCanvasWidth, mCanvasHeight, null);
		this.maxValue = maxValue;
		this.currentValue = currentValue;
		this.barLength = (int)((mCanvasWidth - x) - x);
		
		this.mType = type;
	}

	@Override
	/*
	 * Draws on the screen the image of the model
	 */
	public void drawOnScreen(Canvas canvas){
		int startXPoint = (int) x;
		int yValue = (int) y;
		int endXPoint = startXPoint + barLength;
		int separation = 2;
		
		Paint mPaintLine = new Paint();
		
		canvas.save();
		
		// Bar background
		mPaintLine.setColor(Color.BLACK);
		mPaintLine.setStrokeWidth(15);
		canvas.drawLine(startXPoint, yValue, endXPoint, yValue, mPaintLine);
		
		// Bar max value
		if(mType == Constants.BAR_HEALTH)
			mPaintLine.setColor(Color.RED);
		else if (mType == Constants.BAR_EXPERIENCE)
			mPaintLine.setColor(Color.YELLOW);
		mPaintLine.setStrokeWidth(10);
		canvas.drawLine(startXPoint + separation, yValue, endXPoint - separation, yValue, mPaintLine);
		
		// Bar current value
		if(mType == Constants.BAR_HEALTH)
			mPaintLine.setColor(Color.BLUE);
		else if (mType == Constants.BAR_EXPERIENCE)
			mPaintLine.setColor(Color.GREEN);
		mPaintLine.setStrokeWidth(10);
		
		double unitBar = barLength / 100.0f;
		double completionPercentage = (currentValue * 100.0f) / maxValue;
		double value = (startXPoint + (unitBar * completionPercentage)) - separation;
		canvas.drawLine(startXPoint + separation, yValue, (float) value, yValue, mPaintLine);
		
		canvas.restore();
	}

	/**
	 * Set the current value of the bar
	 * @param currentValue Update current value
	 */
	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	/**
	 * Get bar type (HEALTH or EXPERIENCE)
	 * @return bar type
	 */
	public int getType() {
		return mType;
	}

	/**
	 * Get the bar max value
	 * @return Max value
	 */
	public int getMaxValue() {
		return maxValue;
	}

	@NonNull
	@Override
	/*
	 * toString
	 */
	public String toString() {
		return "StatBar [name=" + this.getClass().getName() + ", mType=" + mType + ", maxValue=" + maxValue
				+ ", currentValue=" + currentValue + "]";
	}

	/**
	 * Set the bar's max value
	 * @param nextLevelThreshold Bar's max value
	 */
	public void setMaxValue(int nextLevelThreshold) {
		this.maxValue = nextLevelThreshold;		
	}
	
}