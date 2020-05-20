package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity to manage the app settings
 */
public class SettingsActivity extends PreferenceActivity {

	SharedPreferences mPreferences;
	private SharedPreferences.Editor mEditor;
	private PreferenceCompanion pCompanion;
	private Context mContext;

	private List<AppSensorEvent> sensorEvents;

	// private static final int SETTING_START_ACTIVITY = 0;

	private boolean mDebugMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		Intent data = getIntent();
		sensorEvents = data.getParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS);

		mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);

		mDebugMode = mPreferences.getBoolean(Constants.TAG_EXE_MODE, false);

		pCompanion = new PreferenceCompanion();
		getFragmentManager().beginTransaction().replace(android.R.id.content, new AppPreferenceFragment()).commit();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}

	public PreferenceCompanion getCompanion() {
		return pCompanion;
	}

	class PreferenceCompanion {

		private Preference.OnPreferenceChangeListener mPreferenceChangeListener;
		private Preference.OnPreferenceClickListener mPreferenceClickListener;

		PreferenceCompanion(){

			mPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object o) {

					String stringValue = o.toString();

					if(preference instanceof ListPreference){
						preference.setSummary(((ListPreference) preference).getEntry());
					} else {
						preference.setSummary(stringValue);
					}

					return true;
				}
			};

			mPreferenceClickListener = new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					switch (preference.getKey()) {
						case Constants.PREF_WIPE_MEMORY:
							showResetPreferencesDialog();
							break;
						case Constants.PREF_SENSORS_EVENTS:
							launchSensorActivity();
							break;
					}
					return true;
				}
			};
		}

		private void showResetPreferencesDialog() {
			ResetPreferencesDialogFragment restoreDialog = new ResetPreferencesDialogFragment();
			restoreDialog.show(getFragmentManager(),
					"RestorePreferencesDialog");
		}

		private void launchSensorActivity() {
			Intent sensorIntent = new Intent(mContext, SensorActivity.class);
			sensorIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
			startActivity(sensorIntent);
		}

		void bindPreferenceSummaryToValue(Preference preference){
			// Set the listener to watch for value changes
			preference.setOnPreferenceChangeListener(mPreferenceChangeListener);
			preference.setOnPreferenceClickListener(mPreferenceClickListener);

			mPreferenceChangeListener.onPreferenceChange(preference,
					PreferenceManager
							.getDefaultSharedPreferences(preference.getContext())
							.getString(preference.getKey(), Constants.EMPTY_STRING));
		}
	}

	public static class AppPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);

			PreferenceCompanion pCompanion = ((SettingsActivity)getActivity()).getCompanion();

			pCompanion.bindPreferenceSummaryToValue(findPreference(Constants.PREF_SHIP_CONTROL_MODE));
			pCompanion.bindPreferenceSummaryToValue(findPreference(Constants.PREF_AMMO_CONTROL_MODE));
			pCompanion.bindPreferenceSummaryToValue(findPreference(Constants.PREF_SHOOT_CONTROL_MODE));
			// pCompanion.bindPreferenceSummaryToValue(findPreference(Constants.PREF_VOLUME_VALUE));
			pCompanion.bindPreferenceSummaryToValue(findPreference(Constants.PREF_SENSORS_EVENTS));
			pCompanion.bindPreferenceSummaryToValue(findPreference(Constants.PREF_WIPE_MEMORY));


		}
	}

	@SuppressLint("ValidFragment")
	/*
	 * Class to show a Dialog that asks the user if he/she really want to reset all preferences
	 */
	public static class ResetPreferencesDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity, R.style.Dialog_No_Border);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			Button btnNegative = view.findViewById(R.id.btnNegative);
			txtTitle.setText(getResources().getString(R.string.settings_restore_dialog_title));
			txtMessage.setText(getResources().getString(R.string.settings_restore_dialog_message));
			btnPositive.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String message = ((SettingsActivity)getActivity()).resetPreferences() ? getString(R.string.reset_preferences_ok)
							: getString(R.string.reset_preferences_error);
					Toast.makeText(getActivity(), message,
							Toast.LENGTH_SHORT).show();
					dismiss();
				}
			});
			btnNegative.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dismiss();
				}
			});
			builder.setView(view);
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	/**
	 * Method that resets all saved preferences
	 * @return true if the preferences got reset, false otherwise
	 */
	private boolean resetPreferences() {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.clear();
		editor.putBoolean(Constants.TAG_EXE_MODE, mDebugMode);
		editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_IS_ACTIVE);
		editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_IS_ACTIVE);
		editor.putBoolean(Constants.PREF_SHOOT_CONTROL_MODE, Constants.PREF_IS_ACTIVE);
		return editor.commit();
	}

}
