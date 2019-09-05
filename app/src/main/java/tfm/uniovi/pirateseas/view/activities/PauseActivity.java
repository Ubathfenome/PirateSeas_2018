package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;

/**
 * Activity to manage the behaviour of the game pause button
 */
public class PauseActivity extends Activity {
	private static final String TAG = "PauseActivity";

	private Context context;

    private TextView txtTooltip;

    private ProgressBar pgrHealth;
	private ProgressBar pgrPower;
	private ProgressBar pgrRange;

	private Ship nShip;
	private Player nPlayer;
	private Map nMap;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pause);
		this.setFinishOnTouchOutside(false);
		
		context = this;

        TextView txtTitleLabel = findViewById(R.id.txtPauseLabel);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");
		txtTitleLabel.setTypeface(customFont);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent data = getIntent();

        Button btnResume = findViewById(R.id.btnPauseResume);
		btnResume.setTypeface(customFont);
		btnResume.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				try {
					MusicManager.getInstance().stopBackgroundMusic();
				} catch(IllegalStateException e){
					MusicManager.getInstance().resetPlayer();
				}
				MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
				finish();
			}
		});

        ImageButton btnSettings = findViewById(R.id.btnPauseSettings);
		btnSettings.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent settingsIntent = new Intent(context, SettingsActivity.class);
				startActivity(settingsIntent);
			}
		});

        ImageButton btnHelp = findViewById(R.id.btnPauseHelp);
		btnHelp.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent helpIntent = new Intent(context, HelpActivity.class);
				startActivity(helpIntent);
			}
		});

        Button btnExit = findViewById(R.id.btnPauseExit);
		btnExit.setTypeface(customFont);
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				LeaveGameDialogFragment exitDialog = new LeaveGameDialogFragment();
				exitDialog.show(getFragmentManager(), "LeaveGameDialog");
			}
		});

		txtTooltip = findViewById(R.id.txtTooltip);
		txtTooltip.setTypeface(customFont);

		nShip = data.getParcelableExtra(Constants.PAUSE_SHIP);
		nPlayer = data.getParcelableExtra(Constants.PAUSE_PLAYER);
		nMap = data.getParcelableExtra(Constants.PAUSE_MAP);

		pgrHealth = findViewById(R.id.pgrsHealth);
		pgrHealth.setMax(nShip.getMaxHealth());
		pgrHealth.setProgress(nShip.getHealth());

		pgrPower = findViewById(R.id.pgrsPower);
		pgrPower.setMax(Constants.SHIP_MAX_POWER);
		int pProgress = Math.round(nShip.getPower() * Constants.DEFAULT_SHOOT_DAMAGE);
		pgrPower.setProgress(pProgress);

		pgrRange = findViewById(R.id.pgrsRange);
		pgrRange.setMax(Constants.SHIP_MAX_RANGE);
		int rProgress = Math.round(nShip.getRange() * Constants.DEFAULT_SHIP_BASIC_RANGE);
		pgrRange.setProgress(rProgress);

        ImageView imgHealth = findViewById(R.id.imgHealth);
        imgHealth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				txtTooltip.setText(getString(R.string.pause_hint_health, pgrHealth.getProgress(), pgrHealth.getMax()));
			}
		});
        ImageView imgPower = findViewById(R.id.imgPower);
        imgPower.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				txtTooltip.setText(getString(R.string.pause_hint_power, pgrPower.getProgress(), pgrPower.getMax()));
			}
		});
        ImageView imgRange = findViewById(R.id.imgRange);
        imgRange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				txtTooltip.setText(getString(R.string.pause_hint_range, pgrRange.getProgress(), pgrRange.getMax()));
			}
		});

		try {
			MusicManager.getInstance().stopBackgroundMusic();
		} catch(IllegalStateException e){
			MusicManager.getInstance().resetPlayer();
		}
		MusicManager.getInstance(context, MusicManager.MUSIC_GAME_PAUSED).playBackgroundMusic();

		Log.d(TAG,"PauseActivity Ship H=" + pgrHealth.getProgress() + "/" + pgrHealth.getMax() + " P=" + pgrPower.getProgress() + "/" + pgrPower.getMax() + " R=" + pgrRange.getProgress() + "/" + pgrRange.getMax());

	}

	@Override
	public void onBackPressed() {
		// Pop up messageBox asking if the user is sure to leave
		LeaveGameDialogFragment exitDialog = new LeaveGameDialogFragment();
		exitDialog.show(getFragmentManager(), "LeaveGameDialog");
	}

	/**
	 * Class to create a Dialog that asks the player if he/she is sure of leaving the game activity
	 */
	public static class LeaveGameDialogFragment extends DialogFragment {
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
			txtTitle.setText(getResources().getString(R.string.exit_dialog_title));
			txtMessage.setText(getResources().getString(R.string.exit_dialog_message));
			btnPositive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// Exit
					// Exit game. Return to main menu
					Intent mainMenuIntent = new Intent(dummyActivity, MainMenuActivity.class);
					try {
						MusicManager.getInstance().stopBackgroundMusic();
					} catch(IllegalStateException e){
						MusicManager.getInstance().resetPlayer();
					}

					((PauseActivity)dummyActivity).nMap.setActiveCell(((PauseActivity)dummyActivity).nMap.getLastActiveCell());
					GameHelper.saveGameAtPreferences(dummyActivity, ((PauseActivity)dummyActivity).nPlayer, ((PauseActivity)dummyActivity).nShip, ((PauseActivity)dummyActivity).nMap);

					MusicManager.getInstance(dummyActivity, MusicManager.MUSIC_GAME_MENU).playBackgroundMusic();
					mainMenuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(mainMenuIntent);
					dummyActivity.finish();
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
}
