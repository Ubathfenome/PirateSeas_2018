package tfm.uniovi.pirateseas.exceptions;

/**
 * Not enough gold exception thrown when the player tries to buy something too expensive for his/her gold purse
 */
public class NotEnoughGoldException extends Exception {

	private static final long serialVersionUID = -2421229729590696484L;

	/**
	 * Constructor
	 * @param message
	 */
	public NotEnoughGoldException(String message){
		super(message);
	}

}
