package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

public class SettingsExtraActivity extends Activity {
	
	private Button btnRestore;
	private TextView txtSettingsLabel;
	private ToggleButton tglControlMode;
	private TextView txtControlMode;
	private ToggleButton tglChangeAmmo;
	private TextView txtAmmoMode;

	private boolean mDebugMode;

	SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings_extra);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		mPreferences = getSharedPreferences(Constants.TAG_PREF_NAME,
				Context.MODE_PRIVATE);

		mDebugMode = mPreferences.getBoolean(Constants.TAG_EXE_MODE, false);

		txtSettingsLabel = 	findViewById(R.id.txtSettingsLabel);
		txtSettingsLabel.setTypeface(customFont);
		
		tglControlMode = (ToggleButton) findViewById(R.id.tglControlMode);
		tglControlMode.setChecked(mPreferences.getBoolean(Constants.PREF_SHIP_CONTROL_MODE, Constants.PREF_GAME_TOUCH));
		tglControlMode.setTypeface(customFont);

		txtControlMode = findViewById(R.id.txtControlMode);
		txtControlMode.setTypeface(customFont);
		
		tglChangeAmmo = (ToggleButton) findViewById(R.id.tglChangeAmmo);
		tglChangeAmmo.setChecked(mPreferences.getBoolean(Constants.PREF_AMMO_CONTROL_MODE, Constants.PREF_GAME_TOUCH));
		tglChangeAmmo.setTypeface(customFont);

		txtAmmoMode = findViewById(R.id.txtAmmoMode);
		txtAmmoMode.setTypeface(customFont);

		btnRestore = (Button) findViewById(R.id.btnSettingsRestore);
		btnRestore.setTypeface(customFont);
		btnRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ResetPreferencesDialogFragment restoreDialog = new ResetPreferencesDialogFragment();
				restoreDialog.show(getFragmentManager(),
						"RestorePreferencesDialog");
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

	private void savePreferences() {
		// Save changes in preferences
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(Constants.PREF_SHIP_CONTROL_MODE, tglControlMode.isChecked());
		editor.putBoolean(Constants.PREF_AMMO_CONTROL_MODE, tglChangeAmmo.isChecked());
		editor.commit();
	}

	@SuppressLint("ValidFragment")
	public class ResetPreferencesDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			builder.setTitle(
					getResources().getString(
							R.string.settings_restore_dialog_title))
					.setMessage(R.string.settings_restore_dialog_message)
					.setPositiveButton(
							R.string.settings_restore_dialog_positive,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String message = resetPreferences() ? "Preferences sucessfully restored"
											: "Error while restoring preferences";
									Toast.makeText(getActivity(), message,
											Toast.LENGTH_SHORT).show();
								}
							})
					.setNegativeButton(R.string.exit_dialog_negative,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancels the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
	
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
