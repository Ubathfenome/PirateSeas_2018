package tfm.uniovi.pirateseas.exceptions;

/**
 * No Ammo exception thrown when a player tries to shoot without any ammunition left
 */
public class NoAmmoException extends Exception {
	
	private static final long serialVersionUID = -7086092562775180996L;

	/**
	 * Constructor
	 * @param message
	 */
	public NoAmmoException(String message) {
		super(message);
	}
}
