package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

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
	
	private float getLength(Point origin, Point destiny){
		return (float) Math.hypot(destiny.x - origin.x, destiny.y - origin.y);
	}
	
	public int getDamage(){
		return mDamage;
	}
	
	public void setDamage(int damage){
		this.mDamage = damage;
	}
	
	public float getPathLength() {
		return pathLength;
	}

	public void setPathLength(float pathLength) {
		this.pathLength = pathLength;
		this.setFlyingTime(Constants.FLYING_TIME_MULTIPLIER * pathLength);
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
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
	
	public void setShotStatus(int shotStatus) {
		mShotStatus = shotStatus;
	}

	public int getShotStatus() {
		return mShotStatus;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(long mTimestamp) {
		this.mTimestamp = mTimestamp;
	}
	
	public Point getEndPoint() {
		return endPoint;
	}

	public float getFlyingTime() {
		return mFlyingTime;
	}

	public void setFlyingTime(float mFlyingTime) {
		this.mFlyingTime = mFlyingTime;
	}

	public float getCurrentTime() {
		return mCurrentTime;
	}

	public void setCurrentTime(float mCurrentTime) {
		this.mCurrentTime = mCurrentTime;
	}

	@Override
	public String toString() {
		return "Shot [EntityOrigin=" + (startPoint.y<5?"Player":"Enemy") + ", startPoint=" + startPoint + ", endPoint=" + endPoint
				+ ", pathLength=" + pathLength + ", mDamage=" + mDamage
				+ ", mStatus=" + mShotStatus +  ", entityDirection=" + entityDirection
				+ ", entityCoordinates=" + entityCoordinates
				+ ", TimeStamp=" + mTimestamp + "]";
	}

	public boolean isInBounds(float initialHeight) {
		return x >= 0 && x + mWidth <= mCanvasWidth && y >= initialHeight && y + mHeight <= mCanvasHeight;
	}

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