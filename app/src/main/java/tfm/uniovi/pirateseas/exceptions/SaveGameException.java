package tfm.uniovi.pirateseas.exceptions;

/**
 * Save Game Exception thrown when an error occurs while saving the game
 */
public class SaveGameException extends Exception {

	private static final long serialVersionUID = 6885789959140243028L;

	/**
	 * Constructor
	 * @param message
	 */
	public SaveGameException(String message){
		super(message);
	}

}
