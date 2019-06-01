package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity that shows help about the game
 */
public class HelpActivity extends Activity {

	Button btnNext, btnAbout;
	String versionName;

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		TextView txtHelp1 = findViewById(R.id.txtHelp1);
		txtHelp1.setTypeface(customFont);
		TextView txtHelp2 = findViewById(R.id.txtHelp2);
		txtHelp2.setTypeface(customFont);
		TextView txtHelp3 = findViewById(R.id.txtHelp3);
		txtHelp3.setTypeface(customFont);
		TextView txtHelp4 = findViewById(R.id.txtHelp4);
		txtHelp4.setTypeface(customFont);
		TextView txtHelp5 = findViewById(R.id.txtHelp5);
		txtHelp5.setTypeface(customFont);
		TextView txtHelp6 = findViewById(R.id.txtHelp6);
		txtHelp6.setTypeface(customFont);
		TextView txtHelp7 = findViewById(R.id.txtHelp7);
		txtHelp7.setTypeface(customFont);
		TextView txtHelp8 = findViewById(R.id.txtHelp8);
		txtHelp8.setTypeface(customFont);
		TextView txtHelp9 = findViewById(R.id.txtHelp9);
		txtHelp9.setTypeface(customFont);

		btnNext = findViewById(R.id.btnBarNext);
		btnNext.setTypeface(customFont);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					finish();
			}
		});
		
		btnAbout = findViewById(R.id.btnBarAbout);
		btnAbout.setTypeface(customFont);
		btnAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DisplayInfoDialogFragment displayDialog = new DisplayInfoDialogFragment();
				displayDialog.show(getFragmentManager(), "DisplayInfoDialogFragment");
			}
		});

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = packageInfo.versionName;
		} catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}
	}
	
	@SuppressLint("ValidFragment")
	public class DisplayInfoDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			builder.setTitle(
					getResources().getString(R.string.about_dialog_title))
					.setMessage(
							getResources().getString(
									R.string.about_dialog_message)
									+ "\n"
									+ versionName)
					.setPositiveButton(R.string.about_dialog_positive,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Show info
									dismiss();
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutHelp).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}
}
