package tfm.uniovi.pirateseas.model.canvasmodel.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Class to represent values over images
 */
public class UIDisplayElement extends View {
	
	private int mValue;
	private boolean mReloading;
	private Paint paint;
	private Drawable mImage;
	private Drawable mImageCancel;
	private TypedArray mArray;
	private Typeface customFont;
	
	@SuppressLint("NewApi")
	/*
	 * Constructor
	 */
	public UIDisplayElement(Context context, int drawableResource, int value){
		super(context);

		customFont = Typeface.createFromAsset(context.getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTypeface(customFont);
		this.mValue = value;
		this.mReloading = false;
		mImage = context.getResources().getDrawable(drawableResource, null);
		mImageCancel = context.getResources().getDrawable(R.mipmap.ico_cancel, null);
	}

	/**
	 * Constructor
	 * @param context Context context
	 */
	public UIDisplayElement(Context context) {
		this(context, null);
	}

	/**
	 * Constructor
	 * @param context Context context
	 * @param attrs AttributeSet attrs
	 */
	public UIDisplayElement(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Constructor
	 * @param context Context context
	 * @param attrs AttributeSet attrs
	 * @param defStyle int default style
	 */
	public UIDisplayElement(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		
		mArray = context.obtainStyledAttributes(attrs, R.styleable.UIDisplayElement, defStyle, 0);
		customFont = Typeface.createFromAsset(context.getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");
		
		init();
		
		mArray.recycle();
	}
	
	@SuppressLint("NewApi")
	/*
	 * Initialize the UIElement
	 */
	private void init(){
		paint = new Paint();
		paint.setColor(Color.RED);
		paint.setTextSize(65f);
		paint.setTypeface(customFont);
		paint.setStyle(Style.FILL_AND_STROKE);
		
		this.mValue = mArray.getInteger(R.styleable.UIDisplayElement_value, 0);
		this.mImage = getBackground();
		this.mImageCancel = getResources().getDrawable(R.mipmap.ico_cancel, null);
	}

	/**
	 * Get the UIElement value
	 * @return Element value
	 */
	public int getElementValue(){
		return mValue;
	}

	/**
	 * Set the UIElement value
	 * @param value value
	 */
	public void setElementValue(int value){
		this.mValue = value;
	}
	
	/**
	 * @return the mReloading
	 */
	public boolean isReloading() {
		return mReloading;
	}

	/**
	 * @param mReloading the mReloading to set
	 */
	public void setReloading(boolean mReloading) {
		this.mReloading = mReloading;
	}

	@Override
	/*
	 * Draws on the screen the image of the model
	 */
	public void onDraw(Canvas canvas){
		mImage.draw(canvas);
		if(mReloading)
			mImageCancel.draw(canvas);
		//noinspection IntegerDivisionInFloatingPointContext
		canvas.drawText(String.valueOf(mValue), mImage.getIntrinsicWidth() / 16, mImage.getIntrinsicHeight() / 2 + 10, paint);
	}

	@NonNull
	@Override
	/*
	 * toString
	 */
	public String toString() {
		return "UIDisplayElement [name = " + this.getClass().getName() + ", mValue=" + mValue + "]";
	}
	
	
}