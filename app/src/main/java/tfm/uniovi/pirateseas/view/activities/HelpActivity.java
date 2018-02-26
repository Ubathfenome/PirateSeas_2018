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
import android.widget.Button;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

public class HelpActivity extends Activity {
	
	private static final int HELP_PAGES = 1;
	
	Button btnNext, btnPrev, btnAbout;
	String versionName;
	
	int currentPage = 1;	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");
		
		btnNext = (Button) findViewById(R.id.btnBarNext);
		btnNext.setTypeface(customFont);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentPage == HELP_PAGES)
					finish();
				else {
					currentPage++;
					btnPrev.setVisibility(View.VISIBLE);
				}
			}
		});
		
		btnPrev = (Button) findViewById(R.id.btnBarPrev);
		btnPrev.setTypeface(customFont);
		btnPrev.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentPage == 2) 
					v.setVisibility(View.INVISIBLE);
				currentPage--;				
			}
		});
		
		btnAbout = (Button) findViewById(R.id.btnBarAbout);
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
