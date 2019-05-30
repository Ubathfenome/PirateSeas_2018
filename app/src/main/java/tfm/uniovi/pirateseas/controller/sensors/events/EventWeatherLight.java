package tfm.uniovi.pirateseas.controller.sensors.events;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.WindowManager;

import tfm.uniovi.pirateseas.controller.sensors.SensorType;

/**
 * Event for the screen brightness adjustments
 */
public class EventWeatherLight {

	/**
	 * Return SensorType used on this event
	 * @return SensorType code
	 */
	public static SensorType getSensorType() {
		return SensorType.TYPE_LIGHT;
	}

	/**
	 * Modify the screen brightness on certain device sensor behaviour
	 * @param context
	 * @param brightnessLevel Brightness sensor new value
	 */
	public static void adjustScreenBrightness(Context context, float brightnessLevel){
		if (brightnessLevel < 8)
			brightnessLevel = 8;
		else if (brightnessLevel > 100)
			brightnessLevel = 100;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(Settings.System.canWrite(context)) {
				Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
				Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) brightnessLevel);
			}
		}

		WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
		lp.screenBrightness = brightnessLevel/100.0f; 
		((Activity) context).getWindow().setAttributes(lp);
	}
	
}