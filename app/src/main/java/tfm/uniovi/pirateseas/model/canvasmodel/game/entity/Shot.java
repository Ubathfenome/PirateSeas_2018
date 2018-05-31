package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Class to manage the Shot's behaviour
 */
public class Shot extends Entity{
	
	private Context mContext;
	
	private Point startPoint;
	private Point endPoint;
	
	private float pathLength;
	private float mFlyingTime;
	private float mCurrentTime;
	
	private long mTimestamp;
	
	private int mDamage;
	
	private int mShotStatus;

	protected static int shotWidth, shotHeight;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	/**
	 * Constructor
	 * @param Context context: Activity where the object is loaded
	 * @param double screenX: x coordinate of the object image
	 * @param double screenY: y coordinate of the object image
	 * @param double mCanvasWidth: Object's image width in pixels
	 * @param double mCanvasHeight: Object's image height in pixels
	 * @param Point entityBeginning: Object's initial coordinate point
	 * @param Point entityDestiny: Object's ending coordinate point
	 * @param int eDirection: Direction of the object in degrees (0..360)
	 * @param int power: Damage the shot will deal
	 * @param long timestampLastShot: Timestamp of the previous shot
	 */
	public Shot(Context context, double screenX, double screenY, double mCanvasWidth, double mCanvasHeight, Point entityBeginning, Point entityDestiny, int eDirection, int power, long timestampLastShot){
		super(context, screenX, screenY, mCanvasWidth, mCanvasHeight, entityBeginning, eDirection, 1, 1, 1);
		
		mContext = context;
		
		startPoint = entityBeginning;
		endPoint = entityDestiny;
		
		mShotStatus = Constants.SHOT_FIRED;
	
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_smoke, null));
		} else {
			setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_smoke));
		}
		
		shotWidth = mWidth;
		shotHeight = mHeight;
		
		setPathLength(getLength(startPoint, endPoint));
		
		mDamage = power;
		setTimestamp(timestampLastShot);

		mHealthPoints = 1;
		if(mHealthPoints > 0)
			setStatus(Constants.STATE_ALIVE);
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
	public void setPathLength(float pathLength) {
		this.pathLength = pathLength;
		this.setFlyingTime(Constants.FLYING_TIME_MULTIPLIER * pathLength);
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	/**
	 * Draw the Shot on the screen Canvas
	 */
	public void drawOnScreen(Canvas canvas) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			switch(mShotStatus){
				case Constants.SHOT_FIRED:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_smoke, null));
					break;
				case Constants.SHOT_FLYING:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_cannonball, null));
					break;
				case Constants.SHOT_HIT:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_hit, null));
					break;
				case Constants.SHOT_MISSED:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_miss, null));
					break;
			}
		} else {
			switch(mShotStatus){
				case Constants.SHOT_FIRED:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_smoke));
					break;
				case Constants.SHOT_FLYING:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_cannonball));
					break;
				case Constants.SHOT_HIT:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_hit));
					break;
				case Constants.SHOT_MISSED:
					setImage(mContext.getResources().getDrawable(R.mipmap.txtr_shot_miss));
					break;
			}
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
	 * @return
	 */
	public long getTimestamp() {
		return mTimestamp;
	}

	/**
	 * Set the timestamp of the Shot
	 * @param mTimestamp
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
	 * Set the flying time for the shot
	 * @param mFlyingTime Flying time in seconds
	 */
	public void setFlyingTime(float mFlyingTime) {
		this.mFlyingTime = mFlyingTime;
	}

	@Override
	/**
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
	 * @param initialHeight
	 * @return true if is within bounds, false otherwise
	 */
	public boolean isInBounds(float initialHeight) {
		return x >= 0 && x + mWidth <= mCanvasWidth && y >= initialHeight && y + mHeight <= mCanvasHeight;
	}

	/**
	 * Move the Shot closer to its destiny Point
	 * @param destiny
	 * @param xDelta
	 * @param yDelta
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
		} else if(destiny.x < curr.x) {	// Destiny to the left
			xDiff = curr.x - destiny.x;	// Get the positive needed amount to reach the destiny
		}
		if(destiny.y > curr.y){			// Destiny to the front
			yDiff = destiny.y - curr.y;	// Get the positive needed amount to reach the destiny
		} else if(destiny.y < curr.y) { // Destiny to the back
			yDiff = curr.y - destiny.y;	// Get the positive needed amount to reach the destiny
		}

		// Calculate next Point coordinates
		if(xDiff > 0){
			if(destiny.x > curr.x){			// Destiny to the right
				nextX = curr.x + 1;			// Next point moved 1 position to the side
				x += xDelta;		// Move Bitmap coordinates relative to next Point movement
			} else if(destiny.x < curr.x) {	// Destiny to the left
				nextX = curr.x - 1;			// Next point moved 1 position to the side
				x -= xDelta;		// Move Bitmap coordinates relative to next Point movement
			}
		}
		if(yDiff > 0){
			if(destiny.y > curr.y){			// Destiny to the front
				nextY = curr.y + 1;			// Next point moved 1 position to the front
				y -= yDelta;		// Move Bitmap coordinates relative to next Point movement
			} else if(destiny.y < curr.y) {	// Destiny to the back
				nextY = curr.y - 1;			// Next point moved 1 position to the back
				y += yDelta;		// Move Bitmap coordinates relative to next Point movement
			}
		}

		// Set next Point coordinates
		Point next = new Point(xDiff > 0 ? nextX : curr.x, yDiff > 0 ? nextY : curr.y);
		entityCoordinates = new Point(next.x, next.y);
	}
	
}