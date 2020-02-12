package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
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

	Button btnNext, btnFinish, btnAbout;
	String versionName;

	TextView txtHelp1, txtHelp2,
			txtHelp3, txtHelp4,
			txtHelp5, txtHelp6,
			txtHelp7, txtHelp8,
			txtHelp9;

	boolean startPage = true;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		txtHelp1 = findViewById(R.id.txtHelp1);
		txtHelp1.setTypeface(customFont);
		txtHelp2 = findViewById(R.id.txtHelp2);
		txtHelp2.setTypeface(customFont);
		txtHelp3 = findViewById(R.id.txtHelp3);
		txtHelp3.setTypeface(customFont);
		txtHelp4 = findViewById(R.id.txtHelp4);
		txtHelp4.setTypeface(customFont);
		txtHelp5 = findViewById(R.id.txtHelp5);
		txtHelp5.setTypeface(customFont);
		txtHelp6 = findViewById(R.id.txtHelp6);
		txtHelp6.setTypeface(customFont);
		txtHelp7 = findViewById(R.id.txtHelp7);
		txtHelp7.setTypeface(customFont);
		txtHelp8 = findViewById(R.id.txtHelp8);
		txtHelp8.setTypeface(customFont);
		txtHelp9 = findViewById(R.id.txtHelp9);
		txtHelp9.setTypeface(customFont);

		btnNext = findViewById(R.id.btnBarNext);
		btnNext.setTypeface(customFont);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startPage = !startPage;
				showStartItems(startPage);
			}
		});

		btnFinish = findViewById(R.id.btnBarFinish);
		btnFinish.setTypeface(customFont);
		btnFinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
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

	private void showStartItems(boolean startPage) {
		if(startPage){
			txtHelp1.setVisibility(View.VISIBLE);
			txtHelp2.setVisibility(View.VISIBLE);
			txtHelp3.setVisibility(View.VISIBLE);
			txtHelp4.setVisibility(View.VISIBLE);
			txtHelp5.setVisibility(View.VISIBLE);

			txtHelp6.setVisibility(View.GONE);
			txtHelp7.setVisibility(View.GONE);
			txtHelp8.setVisibility(View.GONE);
			txtHelp9.setVisibility(View.GONE);
		} else {
			txtHelp1.setVisibility(View.GONE);
			txtHelp2.setVisibility(View.GONE);
			txtHelp3.setVisibility(View.GONE);
			txtHelp4.setVisibility(View.GONE);
			txtHelp5.setVisibility(View.GONE);

			txtHelp6.setVisibility(View.VISIBLE);
			txtHelp7.setVisibility(View.VISIBLE);
			txtHelp8.setVisibility(View.VISIBLE);
			txtHelp9.setVisibility(View.VISIBLE);
		}
	}

	@SuppressLint("ValidFragment")
	public static class DisplayInfoDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity dummyActivity = getActivity();
			String message = getResources().getString(
					R.string.about_dialog_message)
					+ "\n"
					+ ((HelpActivity)getActivity()).versionName;
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity);
			LayoutInflater inflater = dummyActivity.getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_positive_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			txtTitle.setText(getResources().getString(R.string.about_dialog_title));
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			txtMessage.setText(message);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			btnPositive.setOnClickListener(new OnClickListener() {
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

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutHelp).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}
}
