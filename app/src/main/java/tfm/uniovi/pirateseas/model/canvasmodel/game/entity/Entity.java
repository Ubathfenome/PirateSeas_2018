package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import android.content.Context;
import android.graphics.Point;

import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.BasicModel;

public class Entity extends BasicModel{
		
	// Entity Attribs
	protected int entityWidth;
	protected int entityHeight;
	protected int entityLength;
	
	protected int entityDirection; // 0..359 degrees
	
	protected Point entityCoordinates;
	
	private int mStatus = Constants.STATE_DEAD;
		
	// Common attribs
	protected int mHealthPoints = 0;
	protected int mMaxHealth = 0;
	protected int mSpeedXLevel = 0;
	protected int mSpeedY = 0;
		
	public Entity(Context context, double x, double y, double canvasWidth, double canvasHeight, Point eCoords, int eDirection, int eWidth, int eHeight, int eLength){
		super(context, x, y, canvasWidth, canvasHeight, null);
		
		this.entityWidth = eWidth;
		this.entityHeight = eHeight;
		this.entityLength = eLength;
		
		this.entityCoordinates = eCoords;
		
		if(eDirection < 0)
			entityDirection = 360 - eDirection;
		else if(eDirection > 360)
			entityDirection = eDirection - 360;
		else
			entityDirection = eDirection;
		
		mSpeedXLevel = 0;
		mSpeedY = 0;
	}

	public boolean intersection(Entity other){
		boolean intersection = false;
		double otherLeft = other.getX();
		double otherRight = other.getX() + other.getWidth();
		double otherUp = other.getY();
		double otherDown = other.getY() + other.getHeight();
		double thisLeft = x;
		double thisRight = x + mWidth;
		double thisUp = y;
		double thisDown = y + mHeight;

		if(thisRight >= otherLeft
			&& thisLeft <= otherRight
			&& thisDown >= otherUp
			&& thisUp < otherDown)
			intersection = true;
		return intersection;
	}
	
	public boolean intersectionWithEntity(Entity other){
		boolean intersection = false;
		boolean horizontalInt = false, verticalInt = false;
		
		if (intersectionToRight(other) || intersectionToLeft(other))
			horizontalInt = true;
		
		if (intersectionToFront(other) || intersectionToBack(other))
			verticalInt = true;
		
		if(horizontalInt && verticalInt)
			intersection = true;
		
		return intersection;
	}
	
	private boolean intersectionToBack(Entity other) {
		return ((entityCoordinates.y + entityHeight / 2) >= (other.entityCoordinates.y - other.entityHeight / 2)) && ((entityCoordinates.y - entityWidth / 2) < (other.entityCoordinates.y - other.entityHeight / 2));
	}

	private boolean intersectionToFront(Entity other) {
		return ((entityCoordinates.y - entityHeight / 2) <= (other.entityCoordinates.y + other.entityHeight / 2))
                && ((entityCoordinates.y + entityWidth / 2) > (other.entityCoordinates.y + other.entityHeight / 2));
	}

	private boolean intersectionToRight(Entity other) {
		return ((entityCoordinates.x + entityWidth / 2) >= (other.entityCoordinates.x - other.entityWidth / 2))
                && ((entityCoordinates.x - entityWidth / 2) < (other.entityCoordinates.x - other.entityWidth / 2));
	}

	private boolean intersectionToLeft(Entity other) {
		return ((entityCoordinates.x - entityWidth / 2) <= (other.entityCoordinates.x + other.entityWidth / 2))
                && ((entityCoordinates.x + entityWidth / 2) > (other.entityCoordinates.x + other.entityWidth / 2));
	}

	public void gainHealth(int points){
		if(points >= 0){
			if(mHealthPoints + points <= mMaxHealth)
				mHealthPoints += points;
			else if (mHealthPoints + points > mMaxHealth)
				mHealthPoints = mMaxHealth;
		}else
			throw new IllegalArgumentException("Encontrado valor de puntos invalido al modificar HealthPoints");
	}
	
	public void looseHealth(int points){
		if(points > 0)
			mHealthPoints -= points;
		else
			throw new IllegalArgumentException("Encontrado valor de puntos negativo al modificar HealthPoints");
	}
	
	public int getHealth(){
		return mHealthPoints;
	}
	
	public boolean isAlive(){
		return mHealthPoints > 0;
	}
	
	public Point getCoordinates(){
		return entityCoordinates;
	}
	
	public void setCoordinates(Point point){
		this.entityCoordinates = point;
	}
	
	public boolean isMoving(){
		return mSpeedXLevel > 0;
	}
	
	/**
	 * @return the mStatus
	 */
	public int getStatus() {
		return mStatus;
	}

	/**
	 * @param mStatus the mStatus to set
	 */
	public void setStatus(int mStatus) {
		this.mStatus = mStatus;
	}

	/**
	 * @return the entityDirection
	 */
	public int getEntityDirection() {
		return entityDirection;
	}

	/**
	 * @param entityDirection the entityDirection to set
	 */
	public void setEntityDirection(int entityDirection) {
		this.entityDirection = entityDirection;
	}

	/**
	 * @return the mSpeedXLevel
	 */
	public int getSpeedXLevel() {
		return mSpeedXLevel;
	}
	
	public void resetSpeedLevel(){
		this.mSpeedXLevel = 0;
	}
}
