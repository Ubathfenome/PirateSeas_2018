package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.sensors.SensorType;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
import tfm.uniovi.pirateseas.controller.sensors.events.EventDayNightCycle;
import tfm.uniovi.pirateseas.controller.sensors.events.EventShakeClouds;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherLight;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherMaelstrom;
import tfm.uniovi.pirateseas.controller.sensors.events.NoEvent;
import tfm.uniovi.pirateseas.controller.sensors.events.SensorEventAdapter;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity to retrieve the Devices's sensors
 */
public class SensorActivity extends Activity{
	
	public static final String TAG = "SensorActivity";
	
	private SharedPreferences mPreferences = null;

	private List<AppSensorEvent> sensorEvents;
	private List<Integer> mDeviceSensorTypes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set animation layout while loading
		setContentView(R.layout.activity_sensors_list);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mDeviceSensorTypes = new ArrayList<>(); // Performance of Java's Lists @source: http://www.onjava.com/pub/a/onjava/2001/05/30/optimization.html
		sensorEvents = new ArrayList<>();
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    	mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE);


		TextView txtSensorListTitle = findViewById(R.id.txtSensorListTitle);
		txtSensorListTitle.setTypeface(customFont);

		// Set event generator' sensor list
		sensorEvents.add(new EventWeatherMaelstrom(
				EventWeatherMaelstrom.class.getSimpleName(),
				SensorType.TYPE_ACCELEROMETER,
				R.mipmap.img_event_whirlpool,
				R.mipmap.img_event_whirlpool_thumb,
				R.mipmap.img_sensor_accelerometer_thumb,
				R.string.event_whirlpool_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_ACCELEROMETER.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_AMBIENT_TEMPERATURE,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_ambient_temperature_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_AMBIENT_TEMPERATURE.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_GRAVITY,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_gravity_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_GRAVITY.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_GYROSCOPE,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_gyroscope_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_GYROSCOPE.getCode())!= null));
		sensorEvents.add(new EventWeatherLight(
				EventWeatherLight.class.getSimpleName(),
				SensorType.TYPE_LIGHT,
				R.mipmap.img_event_light,
				R.mipmap.img_event_light_thumb,
				R.mipmap.img_sensor_light_thumb,
				R.string.event_light_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_LIGHT.getCode())!= null));
		sensorEvents.add(new EventShakeClouds(
				EventShakeClouds.class.getSimpleName(),
				SensorType.TYPE_LINEAR_ACCELERATION,
				R.mipmap.img_movement_spawn,
				R.mipmap.img_event_clouds_thumb,
				R.mipmap.img_sensor_linear_acceleration_thumb,
				R.string.event_clouds_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_LINEAR_ACCELERATION.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_MAGNETIC_FIELD,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_magnetic_field_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_MAGNETIC_FIELD.getCode())!= null));
		sensorEvents.add(new EventDayNightCycle(
				EventDayNightCycle.class.getSimpleName(),
				SensorType.TYPE_PRESSURE,
				R.mipmap.img_event_day_night_cycle,
				R.mipmap.img_event_day_night_thumb,
				R.mipmap.img_sensor_pressure_thumb,
				R.string.event_day_night_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_PRESSURE.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_PROXIMITY,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_proximity_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_PROXIMITY.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_RELATIVE_HUMIDITY,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_relative_humidity_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_RELATIVE_HUMIDITY.getCode())!= null));
		sensorEvents.add(new NoEvent(
				NoEvent.class.getSimpleName(),
				SensorType.TYPE_ROTATION_VECTOR,
				R.drawable.img_none,
				R.mipmap.img_none_thumb,
				R.mipmap.img_sensor_rotation_vector_thumb,
				R.string.event_no_event_message,
				mSensorManager.getDefaultSensor(SensorType.TYPE_ROTATION_VECTOR.getCode())!= null));

		ListView lstSensorEventList = findViewById(R.id.lstSensorEventsList);
		SensorEventAdapter mAdapter = new SensorEventAdapter(this, R.layout.list_item_sensor_event, sensorEvents);
		lstSensorEventList.setAdapter(mAdapter);

		Button btnCancel = findViewById(R.id.btnCancel);
		btnCancel.setTypeface(customFont);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				exitActivity(false);
			}
		});

		Button btnContinue = findViewById(R.id.btnContinue);
		btnContinue.setTypeface(customFont);
		btnContinue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				exitActivity(true);
			}
		});
		
		// Check preferences if its already called 
        int[] preferenceSensorList = getPreferenceSensorList();
		if(hasValidValues(preferenceSensorList)){
			for (int aPreferenceSensorList : preferenceSensorList) {
				mDeviceSensorTypes.add(aPreferenceSensorList);
			}
			exitActivity(true);
		}
		/*
		else {
			for(AppSensorEvent appSensorEvent : sensorEvents){
				if(mSensorManager.getDefaultSensor(appSensorEvent.getSensorType().getCode()) != null){
					mDeviceSensorTypes.add(appSensorEvent.getSensorType().getCode());
					appSensorEvent.setActive(true);
					Log.d(TAG, "Your device has a " + appSensorEvent.getSensorType().name() + " sensor. " +
							"The event " + appSensorEvent.getEventName() + " will be triggered.");
				} else {
					appSensorEvent.setActive(false);
					Log.d(TAG, "No " + appSensorEvent.getSensorType().name() + " sensor detected on your device. " +
							"The event " + appSensorEvent.getEventName() + " will be disabled.");
				}
			}
			int count = SensorType.values().length;
			for(int i = 0; i < count; i++){
				SensorType type = SensorType.values()[i];
				if(mSensorManager.getDefaultSensor(type.getCode()) != null ){
					if(hasEventWithSensorType(type)) {
						mDeviceSensorTypes.add(type.getCode());
						Log.d(TAG, "Your device has a " + type + " sensor. Its event will be triggered.");
					}else{
						Log.d(TAG, "Your device has a " + type + " sensor, but there is no event related to it.");
					}
				} else {
					// Sorry, there is no 'type' sensor on your device.
					// The 'event' event will be disabled.
					if(hasEventWithSensorType(type)) {
						Log.d(TAG, "No " + type + " sensor detected on your device. Its event will be disabled.");
					} else {
						Log.d(TAG, "No " + type + " sensor detected on your device.");
					}
				}
			}
		}
		*/
	}

	/**
	 * Checks if there is an event with the SensorType receiver as param
	 * @param type SensorType to check
	 * @return true if there is an AppSensorEvent with that SensorType, false otherwise
	 */
	private boolean hasEventWithSensorType(SensorType type) {
		for(AppSensorEvent appSensorEvent : sensorEvents){
			if(appSensorEvent.getSensorName().equals(type.name()))
				return true;
		}
		return false;
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutSensorsList).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}

	/**
	 * Checks if the sensor array has any value different from 0
	 * @param sensorList Sensors list
	 * @return true if there is any value different from 0, false otherwise
	 */
	private boolean hasValidValues(int[] sensorList){
		for (int aSensorList : sensorList) {
			if (aSensorList != 0)
				return true;
		}
		return false;
	}

	/**
	 * Retrieve the sensor array stored in Preferences (if any)
	 * @return Sensor array
	 */
	private int[] getPreferenceSensorList(){
		String prefStringSensorList = "";
		String[] stuff = null;
		int prefLength = 0;
		prefStringSensorList = mPreferences.getString(Constants.PREF_SENSOR_LIST,Constants.EMPTY_STRING);
		if(prefStringSensorList.length() > 0){
			stuff = prefStringSensorList.split(";");
			prefLength = stuff.length;
		}
		
		int[] preferenceIntArray = new int[SensorType.values().length];
		for(int i = 0; ((i < preferenceIntArray.length) && (i < prefLength)); i++){
			int value = Integer.valueOf(stuff[i]);
			preferenceIntArray[i] = value;
		}
		
		return preferenceIntArray;
	}

	/**
	 * Save the sensor array at the preferences
	 * @return Sensor's array values as concatenated String
	 */
	private String putPreferenceSensorList(){
		String modifiedString = "";
		Object[] valuesArray =  mDeviceSensorTypes.toArray();
		if(valuesArray != null) {
			StringBuilder builder = new StringBuilder();
			for (Object aValuesArray : valuesArray) {
				builder.append(aValuesArray);
				builder.append(";");
			}
			modifiedString = builder.toString();
		}
		
		return modifiedString;
	}

	/**
	 * Return to calling Activity with the results
	 * @param result true if everything was correct, false otherwise
	 */
	private void exitActivity(boolean result){
		Intent sensorListIntent = new Intent();
		
		Object[] tmpObjectArray = mDeviceSensorTypes.toArray();
		int[] extraIntArray = new int[tmpObjectArray.length];
		
		for(int i = 0; i < tmpObjectArray.length; i++){
			extraIntArray[i] = Integer.valueOf(tmpObjectArray[i].toString());
		}
		
		sensorListIntent.putExtra(Constants.TAG_SENSOR_LIST, extraIntArray);
		
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(Constants.PREF_SENSOR_LIST, putPreferenceSensorList());
		editor.apply();

		setResult(result?RESULT_OK:RESULT_CANCELED, sensorListIntent);
		
		finish();
	}
}