package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;

public class SettingsActivity extends Activity {
	private TextView txtTitleLabel;
	private TextView txtVolumeLabel;
	private SeekBar skbVolume;

	private Button btnSettingsExtra;
	
	private Button btnSettingsAccept;
	private Button btnSettingsCancel;

	private float volumeValue = 0f;
	private String labelValue;

	SharedPreferences mPreferences;
	
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);
		
		context = this;

		txtTitleLabel = findViewById(R.id.txtSettingsLabel);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/"
				+ Constants.FONT_NAME + ".ttf");
		txtTitleLabel.setTypeface(customFont);
		
		volumeValue = (int) mPreferences.getFloat(
				Constants.PREF_DEVICE_VOLUME, MusicManager
				.getInstance(this).getDeviceVolume());

		txtVolumeLabel = findViewById(R.id.txtVolumeLabel);
		labelValue = (String) txtVolumeLabel.getText();
		txtVolumeLabel.setText(labelValue + " " + (int) volumeValue);

		skbVolume = findViewById(R.id.sbVolume);
		skbVolume.setProgress((int) volumeValue);
		skbVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser)
					volumeValue = progress;
				MusicManager.getInstance().setDeviceVolume(progress);
				txtVolumeLabel.setText(labelValue + " " + progress);
			}
		});
		
		btnSettingsExtra = findViewById(R.id.btnExtraSettings);
		btnSettingsExtra.setTypeface(customFont);
		btnSettingsExtra.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent extraSettingsIntent = new Intent(context, SettingsExtraActivity.class);
				startActivity(extraSettingsIntent);
			}
		});

		btnSettingsAccept = findViewById(R.id.btnSettingsAccept);
		btnSettingsAccept.setTypeface(customFont);
		btnSettingsAccept.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Save changes in preferences
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putFloat(Constants.PREF_DEVICE_VOLUME, volumeValue);
				editor.commit();

				finish();
			}
		});

		btnSettingsCancel = findViewById(R.id.btnSettingsCancel);
		btnSettingsCancel.setTypeface(customFont);
		btnSettingsCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutSettings).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);		
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		Toast.makeText(context, getResources().getString(R.string.settings_exit_message), Toast.LENGTH_SHORT).show();
	}

}
