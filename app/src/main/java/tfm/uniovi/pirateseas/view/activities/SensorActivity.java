package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
import tfm.uniovi.pirateseas.controller.sensors.events.SensorEventAdapter;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity to retrieve the Devices's sensors
 */
public class SensorActivity extends Activity{
	
	public static final String TAG = "SensorActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set animation layout while loading
		setContentView(R.layout.activity_sensors_list);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		List<AppSensorEvent> sensorEvents;

		Intent data = getIntent();
		sensorEvents = data.getParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS);

		TextView txtSensorListTitle = findViewById(R.id.txtSensorListTitle);
		txtSensorListTitle.setTypeface(customFont);

		ListView lstSensorEventList = findViewById(R.id.lstSensorEventsList);
		SensorEventAdapter mAdapter = new SensorEventAdapter(this, R.layout.list_item_sensor_event, sensorEvents);
		lstSensorEventList.setAdapter(mAdapter);

		Button btnBack = findViewById(R.id.btnBack);
		btnBack.setTypeface(customFont);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutSensorsList).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}
}