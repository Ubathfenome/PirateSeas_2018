package tfm.uniovi.pirateseas.model.canvasmodel.game.entity;

import android.content.Context;
import android.graphics.Point;

import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.BasicModel;

/**
 * Class to add living attributes to the in-game objects
 */
public class Entity extends BasicModel{

	int entityLength;
	
	int entityDirection; // 0..359 degrees
	
	Point entityCoordinates;
	
	private int mStatus = Constants.STATE_DEAD;
		
	// Common attribs
	int mHealthPoints = 0;
	int mMaxHealth = 0;

	/**
	 * Constructor
	 * @param context Context
	 * @param x X Coordinate of the image
	 * @param y Y Coordinate of the image
	 * @param canvasWidth Canvas width
	 * @param canvasHeight Canvas height
	 * @param eCoords Entity coordinates
	 * @param eDirection Entity direction
	 * @param eLength Entity length
	 */
	Entity(Context context, double x, double y, double canvasWidth, double canvasHeight, Point eCoords, int eDirection, int eLength){
		super(context, x, y, canvasWidth, canvasHeight, null);

		// Entity Attribs
		this.entityLength = eLength;
		
		this.entityCoordinates = eCoords;
		
		if(eDirection < 0)
			entityDirection = 360 - eDirection;
		else if(eDirection > 360)
			entityDirection = eDirection - 360;
		else
			entityDirection = eDirection;
	}

	/**
	 * Method to retrieve if this Entity is within bounds of another entity
	 * @param other The other entity
	 * @return true if is in-bounds, false otherwise
	 */
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

	/**
	 * Method to add health to the entity
	 * @param points Health points to add to the entity
	 */
	public void gainHealth(int points){
		if(points >= 0){
			if(mHealthPoints + points <= mMaxHealth)
				mHealthPoints += points;
			else if (mHealthPoints + points > mMaxHealth)
				mHealthPoints = mMaxHealth;
		}else
			throw new IllegalArgumentException("Encontrado valor de puntos invalido al modificar HealthPoints");
	}

	/**
	 * Method to substract health from the entity
	 * @param points Health points to be subtracted
	 */
	public void looseHealth(int points){
		if(points > 0)
			mHealthPoints -= points;
		else
			throw new IllegalArgumentException("Encontrado valor de puntos negativo al modificar HealthPoints");
	}

	/**
	 * Get the entity's health points
	 * @return Health points
	 */
	public int getHealth(){
		return mHealthPoints;
	}

	/**
	 * Get if the entity is alive or not
	 * @return true if the entity has any health point, false otherwise
	 */
	public boolean isAlive(){
		return mHealthPoints > 0;
	}

	/**
	 * Get the entity's coordinates
	 * @return Point with the entity's coordinates
	 */
	public Point getCoordinates(){
		return entityCoordinates;
	}

	/**
	 * Set the entity's coordinates
	 * @param point New coordinates
	 */
	public void setCoordinates(Point point){
		this.entityCoordinates = point;
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
}
