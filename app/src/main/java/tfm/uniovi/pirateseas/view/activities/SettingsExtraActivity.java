package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity to manage the extra settings available form the game
 */
public class SettingsExtraActivity extends Activity {

	private ToggleButton tglControlMode;
	private ToggleButton tglChangeAmmo;

	private boolean mDebugMode;

	SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings_extra);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);

		mDebugMode = mPreferences.getBoolean(Constants.TAG_EXE_MODE, false);

		TextView txtSettingsLabel = findViewById(R.id.txtSettingsLabel);
		txtSettingsLabel.setTypeface(customFont);
		
		tglControlMode = findViewById(R.id.tglControlMode);
		boolean controlCheck = mPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		tglControlMode.setChecked(controlCheck);
		tglControlMode.setTypeface(customFont);

		TextView txtControlMode = findViewById(R.id.txtControlMode);
		txtControlMode.setTypeface(customFont);
		
		tglChangeAmmo = findViewById(R.id.tglChangeAmmo);
		tglChangeAmmo.setChecked(mPreferences.getBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_GAME_TOUCH));
		tglChangeAmmo.setTypeface(customFont);

		TextView txtAmmoMode = findViewById(R.id.txtAmmoMode);
		txtAmmoMode.setTypeface(customFont);

		Button btnRestore = findViewById(R.id.btnSettingsRestore);
		btnRestore.setTypeface(customFont);
		btnRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ResetPreferencesDialogFragment restoreDialog = new ResetPreferencesDialogFragment();
				restoreDialog.show(getFragmentManager(),
						"RestorePreferencesDialog");
			}
		});

		Button btnBack = findViewById(R.id.btnSettingsBack);
		btnBack.setTypeface(customFont);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				savePreferences();
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		savePreferences();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		savePreferences();
		finish();
	}

	/**
	 * Save the modified settings at the preferences
	 */
	private void savePreferences() {
		// Save changes in preferences
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, tglControlMode.isChecked());
		editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, tglChangeAmmo.isChecked());
		editor.apply();
	}

	@SuppressLint("ValidFragment")
	/*
	 * Class to show a Dialog that asks the user if he/she really want to reset all preferences
	 */
	public static class ResetPreferencesDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			Button btnNegative = view.findViewById(R.id.btnNegative);
			txtTitle.setText(getResources().getString(R.string.settings_restore_dialog_title));
			txtMessage.setText(getResources().getString(R.string.settings_restore_dialog_message));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					String message = ((SettingsExtraActivity)getActivity()).resetPreferences() ? getString(R.string.reset_preferences_ok)
							: getString(R.string.reset_preferences_error);
					Toast.makeText(getActivity(), message,
							Toast.LENGTH_SHORT).show();
					dismiss();
				}
			});
			btnNegative.setOnClickListener(new OnClickListener() {
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
		editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		editor.putBoolean(Constants.PREF_LEVEL_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		editor.putBoolean(Constants.PREF_PAUSE_CONTROL_MODE, Constants.PREF_GAME_TOUCH);
		return editor.commit();
	}

}
