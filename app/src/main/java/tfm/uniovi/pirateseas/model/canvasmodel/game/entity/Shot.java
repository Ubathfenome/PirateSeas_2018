package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.utils.approach2d.DrawableHelper;

/**
 * Class to manage the Shot's behaviour
 */
public class Shot extends Entity{

	private static final int ANIMATION_STEPS = 13;

	private Context mContext;
	
	private Point startPoint;
	private Point endPoint;
	
	private float pathLength;

    private long mTimestamp;
	
	private int mDamage;
	
	private int mShotStatus;

	private boolean animationHasEnded;

	private int frameWidth;
	private int frameHeight;
	private int currentFrame;
	private Rect frameToDraw = new Rect(
			0,
			0,
			frameWidth,
			frameHeight);

	private Bitmap bitmap;

	// What time was it when we last changed frames
	private long lastFrameChangeTime = 0;

	static int shotWidth, shotHeight;

	private int yCollision;

	/**
	 * Constructor
	 * @param context: Activity where the object is loaded
	 * @param screenX: x coordinate of the object image
	 * @param screenY: y coordinate of the object image
	 * @param mCanvasWidth: Object's image width in pixels
	 * @param mCanvasHeight: Object's image height in pixels
	 * @param entityBeginning: Object's initial coordinate point
	 * @param entityDestiny: Object's ending coordinate point
	 * @param eDirection: Direction of the object in degrees (0..360)
	 * @param power: Damage the shot will deal
	 * @param timestampLastShot: Timestamp of the previous shot
	 */
	Shot(Context context, double screenX, double screenY, double mCanvasWidth, double mCanvasHeight, Point entityBeginning, Point entityDestiny, int eDirection, int power, long timestampLastShot){
		super(context, screenX, screenY, mCanvasWidth, mCanvasHeight, entityBeginning, eDirection, 1);
		
		mContext = context;
		
		startPoint = entityBeginning;
		endPoint = entityDestiny;
		
		mShotStatus = Constants.SHOT_FIRED;

		setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_smoke, null));
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.explosion_sprite);
		Drawable bDrawable = new BitmapDrawable(context.getResources(), bitmap);
		frameHeight = bDrawable.getIntrinsicHeight();
		frameWidth = bDrawable.getIntrinsicWidth() / ANIMATION_STEPS;
		currentFrame = 0;
		animationHasEnded = false;

		shotWidth = mWidth;
		shotHeight = mHeight;
		
		setPathLength(getLength(startPoint, endPoint));
		
		mDamage = power;
		setTimestamp(timestampLastShot);

		mHealthPoints = 1;
		setStatus(Constants.STATE_ALIVE);
	}

	public static int getCannonImageWidth(Context context) {
		return context.getResources().getDrawable(R.mipmap.txtr_shot_cannonball, null).getIntrinsicWidth();
	}

	/**
	 * Get the length between the two Points
	 * @param origin  Start Point
	 * @param destiny End Point
	 * @return Distance between the Points
	 */
	private float getLength(Point origin, Point destiny){
		return (float) Math.hypot(destiny.x - origin.x, destiny.y - origin.y);
	}

	/**
	 * Get the shot's damage
	 * @return Damage value
	 */
	public int getDamage(){
		return mDamage;
	}

	/**
	 * Set the path's length
	 * @param pathLength New path's length
	 */
    private void setPathLength(float pathLength) {
		this.pathLength = pathLength;
	}
	
	@SuppressLint("NewApi")
	/*
	 * Draw the Shot on the screen Canvas
	 */
	public void drawOnScreen(Canvas canvas) {
		switch(mShotStatus){
			case Constants.SHOT_FIRED:
				setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_smoke, null));
				break;
			case Constants.SHOT_FLYING:
				setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_cannonball, null));
				break;
			case Constants.SHOT_HIT:
				getCurrentFrame();
				// Set the correct frame
				setImage(DrawableHelper.getFrameFromBitmap(mContext.getResources(), bitmap, frameToDraw.left, frameToDraw.top, frameWidth, frameHeight));
				if(getEntityDirection() == Constants.DIRECTION_UP){
					y = yCollision - frameHeight;
				} else if(getEntityDirection() == Constants.DIRECTION_DOWN){
					y = yCollision + frameHeight;
				}
				break;
			case Constants.SHOT_MISSED:
				setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_miss, null));
				break;
		}
		super.drawOnScreen(canvas);
	}

	/**
	 * Set Shot status
	 * @param shotStatus New status
	 */
	public void setShotStatus(int shotStatus) {
		mShotStatus = shotStatus;
	}

	/**
	 * Get Shot status
	 * @return Shot status
	 */
	public int getShotStatus() {
		return mShotStatus;
	}

	/**
	 * Get the starting timestamp of the Shot
	 * @return timestamp
	 */
	public long getTimestamp() {
		return mTimestamp;
	}

	/**
	 * Set the timestamp of the Shot
	 * @param mTimestamp timestamp
	 */
	public void setTimestamp(long mTimestamp) {
		this.mTimestamp = mTimestamp;
	}

	/**
	 * Get the End Point
	 * @return End Point
	 */
	public Point getEndPoint() {
		return endPoint;
	}

	/**
	 * Set the next frame to draw
	 */
	public void getCurrentFrame(){
		long timestamp = System.currentTimeMillis();
		// How long should each frame last
		int frameLengthInMilliseconds = 100;
		if ( timestamp > lastFrameChangeTime + frameLengthInMilliseconds) {
			lastFrameChangeTime = timestamp;
			currentFrame++;
			if (currentFrame >= ANIMATION_STEPS) {
				animationHasEnded = true;
				// currentFrame = 0; // Use this if the animation should be restarted
			}
		}

		//update the left and right values of the source of
		//the next frame on the sprite sheet
		frameToDraw.left = currentFrame * frameWidth;
		frameToDraw.right = frameToDraw.left + frameWidth;

	}

	public boolean getAnimationHasEnded(){
		return animationHasEnded;
	}

	@NonNull
    @Override
	/*
	 * toString
	 */
	public String toString() {
		return "Shot [EntityOrigin=" + (startPoint.y<5?"Player":"Enemy") + ", startPoint=" + startPoint + ", endPoint=" + endPoint
				+ ", pathLength=" + pathLength + ", mDamage=" + mDamage
				+ ", mStatus=" + mShotStatus +  ", entityDirection=" + entityDirection
				+ ", entityCoordinates=" + entityCoordinates
				+ ", TimeStamp=" + mTimestamp + "]";
	}

	/**
	 * Check if the Shot is in-bounds of the screen
	 * @param initialHeight Initial height
	 * @return true if is within bounds, false otherwise
	 */
	public boolean isInBounds(float initialHeight) {
		return x >= 0 &&
				x + mWidth <= mCanvasWidth &&
				y >= initialHeight &&
				y + mHeight <= mCanvasHeight;
	}

	/**
	 * Move the Shot closer to its destiny Point
	 * @param destiny Destiny Point
	 * @param xDelta X delta
	 * @param yDelta Y delta
	 */
	public void moveShotEntity(Point destiny, int xDelta, int yDelta){
		int xDiff = 0;
		int yDiff = 0;
		int nextX = 0;
		int nextY = 0;

		// Get current Point
		Point curr = new Point(entityCoordinates.x, entityCoordinates.y);

		// Set difference with destiny Point
		if(destiny.x > curr.x){			// Destiny to the right
			xDiff = destiny.x - curr.x;	// Get the positive needed amount to reach the destiny
			nextX = curr.x + 1;			// Next point moved 1 position to the side
			x += xDelta;		// Move Bitmap coordinates relative to next Point movement
		} else if(destiny.x < curr.x) {	// Destiny to the left
			xDiff = curr.x - destiny.x;	// Get the positive needed amount to reach the destiny
			nextX = curr.x - 1;			// Next point moved 1 position to the side
			x -= xDelta;		// Move Bitmap coordinates relative to next Point movement
		}
		if(destiny.y > curr.y){			// Destiny to the front
			yDiff = destiny.y - curr.y;	// Get the positive needed amount to reach the destiny
			nextY = curr.y + 1;			// Next point moved 1 position to the front
			y += yDelta;		// Move Bitmap coordinates relative to next Point movement
		} else if(destiny.y < curr.y) { // Destiny to the back
			yDiff = curr.y - destiny.y;	// Get the positive needed amount to reach the destiny
			nextY = curr.y - 1;			// Next point moved 1 position to the back
			y += yDelta;		// Move Bitmap coordinates relative to next Point movement
		}

		// Set next Point coordinates
		Point next = new Point(xDiff > 0 ? nextX : curr.x, yDiff > 0 ? nextY : curr.y);
		entityCoordinates = new Point(next.x, next.y);
	}

	public void setYCollision(double y) {
		this.yCollision = (int) y;
	}
}