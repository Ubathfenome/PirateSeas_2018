package tfm.uniovi.pirateseas.exceptions;

/**
 * Cannon reloading exception thrown when a player tries to shoot his/her cannons and they are reloading
 */
public class CannonReloadingException extends Exception {

	private static final long serialVersionUID = -2975124763994247851L;

	/**
	 * Constructor
	 * @param message
	 */
	public CannonReloadingException(String message){
		super(message);
	}

}
