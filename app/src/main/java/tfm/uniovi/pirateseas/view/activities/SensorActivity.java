package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.sensors.SensorType;
import tfm.uniovi.pirateseas.controller.sensors.events.EventDayNightCycle;
import tfm.uniovi.pirateseas.controller.sensors.events.EventShakeClouds;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherLight;
import tfm.uniovi.pirateseas.controller.sensors.events.EventWeatherMaelstrom;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity to retrieve the Devices's sensors
 */
public class SensorActivity extends Activity{
	
	public static final String TAG = "SensorActivity";
	
	private SharedPreferences mPreferences = null;
	private int[] preferenceSensorList = null;
	
	private SensorManager mSensorManager;
	private List<Integer> mDeviceSensorTypes;
	
	private TextView tv;
	private ImageView iv;
	
	private AnimationDrawable loadAnimation;
	private Animation enterAnimation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set animation layout while loading
		setContentView(R.layout.activity_sensors);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");
				
		iv = findViewById(R.id.img_load_animation);
		iv.setBackgroundResource(R.drawable.xml_loading_animation);
		loadAnimation = (AnimationDrawable) iv.getBackground();
		enterAnimation = AnimationUtils.loadAnimation(this, R.anim.xml_tween_animation);
		
		tv = findViewById(R.id.lbl_load_status);
		tv.setTypeface(customFont);
	
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mDeviceSensorTypes = new ArrayList<Integer>(); // Performance of Java's Lists @source: http://www.onjava.com/pub/a/onjava/2001/05/30/optimization.html
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    	mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME, Context.MODE_PRIVATE); 
		
		// Check preferences if its already called 
		preferenceSensorList = getPreferenceSensorList();
		if(hasValidValues(preferenceSensorList)){
			for(int i = 0; i < preferenceSensorList.length; i++){
				mDeviceSensorTypes.add(preferenceSensorList[i]); 
			}
			exitActivity(true);
		} else {
			// Call AsyncTask
			AsyncTask<Void,Integer,Boolean> listValidSensors = new ListSensors();
			listValidSensors.execute();
		}
	}
	
	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutSensors).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}

	/**
	 * Checks if the sensor array has any value different from 0
	 * @param sensorList
	 * @return true if there is any value different from 0, false otherwise
	 */
	private boolean hasValidValues(int[] sensorList){
		for(int i = 0; i < sensorList.length; i++){
			if(sensorList[i] != 0)
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
		for(int i = 0; i < valuesArray.length; i++){
			modifiedString += String.valueOf(valuesArray[i]) + ";";
		}
		
		return modifiedString;
	}

	/**
	 * Return to calling Activity with the resutls
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
		
		setResult(RESULT_OK, sensorListIntent);
		
		finish();
	}

	/**
	 * Async class to retrieve the device's sensors
	 */
	private class ListSensors extends AsyncTask<Void, Integer, Boolean>{
		List<SensorType> triggeredSensors = new ArrayList<SensorType>();
		
		@Override
		protected void onPreExecute(){
			// Set event generator' sensor list
			triggeredSensors.add(EventDayNightCycle.getSensorType());
			triggeredSensors.add(EventWeatherLight.getSensorType());
			triggeredSensors.add(EventWeatherMaelstrom.getSensorType());
			triggeredSensors.add(EventShakeClouds.getSensorType());
			
			// Start animation
			tv.setText(getResources().getStringArray(R.array.loading_messages)[0]);
			loadAnimation.start();
		}
		
		@Override
		protected Boolean doInBackground(Void... params){
			int count = SensorType.values().length;
			for(int i = 0; i < count; i++){
				SensorType type = SensorType.values()[i];
				if(mSensorManager.getDefaultSensor(type.getCode()) != null ){
					if(triggeredSensors.contains(type)) {
						mDeviceSensorTypes.add(type.getCode());
						Log.d(TAG, "Your device has a " + type + " sensor. Its event will be triggered.");
					} else
						Log.d(TAG, "Your device has a " + type + " sensor, but there is no event related to it.");
				} else {
					// Sorry, there is no 'type' sensor on your device.
					// The 'event' event will be disabled.
					if (triggeredSensors.contains(type))
						Log.d(TAG, "No " + type + " sensor detected on your device. Its event will be disabled.");
					else
						Log.d(TAG, "No " + type + " sensor detected on your device.");
				}
				publishProgress((int) ((i / (float) count) * 100));
				SystemClock.sleep(1500);
			}
			
			return !mDeviceSensorTypes.isEmpty();
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			// Update animation
			String[] messagesArray = getResources().getStringArray(R.array.loading_messages);
			tv.setText(messagesArray[progress[0]/messagesArray.length]);
			iv.startAnimation(enterAnimation);
		}
		
		protected void onPostExecute(Boolean result){
			loadAnimation.stop();
			exitActivity(result);
		}
	}
}