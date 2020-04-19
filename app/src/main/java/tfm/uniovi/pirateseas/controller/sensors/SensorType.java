package tfm.uniovi.pirateseas.controller.sensors;

import tfm.uniovi.pirateseas.R;

/**
 * Enum with all possible sensors of an Android device
 */
public enum SensorType {
	//TYPE_ALL(-1),
	TYPE_ACCELEROMETER(1, R.string.sensor_accelerometer),
	TYPE_MAGNETIC_FIELD(2, R.string.sensor_magnetic_field),
	//TYPE_ORIENTATION(3, R.string.sensor_orientation), // Deprecated since API 8
	TYPE_GYROSCOPE(4, R.string.sensor_gyroscope),
	TYPE_LIGHT(5, R.string.sensor_light),
	TYPE_PRESSURE(6, R.string.sensor_pressure),
	//TYPE_TEMPERATURE(7, R.string.sensor_temperature),  // Deprecated since API 8
	TYPE_PROXIMITY(8, R.string.sensor_proximity),
	TYPE_GRAVITY(9, R.string.sensor_gravity),
	TYPE_LINEAR_ACCELERATION(10, R.string.sensor_linear_acceleration),
	TYPE_ROTATION_VECTOR(11, R.string.sensor_rotation_vector),
	TYPE_RELATIVE_HUMIDITY(12, R.string.sensor_relative_humidity),
	TYPE_AMBIENT_TEMPERATURE(13, R.string.sensor_ambient_temperature);
	
	private final int code;
	private final int name;

	/**
	 * Constructor
	 * @param code SensorType code
	 * @param name SensorType name
	 */
	SensorType(int code, int name){
		this.code = code;
		this.name = name;
	}

	/**
	 * Return the SensorType code
	 * @return SensorType code
	 */
	public int getCode(){
		return code;
	}

	/**
	 * Return the SensorType name
	 * @return SensorType name
	 */
	public int getName() {
		return name;
	}
}