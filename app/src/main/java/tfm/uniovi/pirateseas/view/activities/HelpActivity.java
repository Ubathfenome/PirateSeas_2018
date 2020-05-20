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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.utils.persistence.FontAdapter;

/**
 * Activity that shows help about the game
 */
public class HelpActivity extends Activity {

	Button btnFinish, btnAbout;
	String versionName;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		ListView listView = findViewById(R.id.lstHelpItems);
		ArrayAdapter<String> adapter = new FontAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.help_messages), customFont);

		listView.setAdapter(adapter);

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
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity, R.style.Dialog_No_Border);
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
