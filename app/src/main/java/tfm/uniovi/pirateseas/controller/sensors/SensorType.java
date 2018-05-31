package tfm.uniovi.pirateseas.controller.sensors;

/**
 * Enum with all possible sensors of an Android device
 */
public enum SensorType {
	//TYPE_ALL(-1),
	TYPE_ACCELEROMETER(1),
	TYPE_MAGNETIC_FIELD(2),
	//TYPE_ORIENTATION(3), // Deprecated since API 8
	TYPE_GYROSCOPE(4),
	TYPE_LIGHT(5),
	TYPE_PRESSURE(6),
	//TYPE_TEMPERATURE(7),  // Deprecated since API 8
	TYPE_PROXIMITY(8),
	TYPE_GRAVITY(9),
	TYPE_LINEAR_ACCELERATION(10),
	TYPE_ROTATION_VECTOR(11),
	TYPE_RELATIVE_HUMIDITY(12),	
	TYPE_AMBIENT_TEMPERATURE(13);
	
	private final int code;

	/**
	 * Constructor
	 * @param code SensorType code
	 */
	SensorType(int code){
		this.code = code;
	}

	/**
	 * Return the SensorType code
	 * @return SensorType code
	 */
	public int getCode(){
		return code;
	}
}