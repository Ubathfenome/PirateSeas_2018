package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;
import tfm.uniovi.pirateseas.utils.persistence.GameHelper;

/**
 * Activity to manage the behaviour of the game pause button
 */
public class PauseActivity extends Activity {
	private static final String TAG = "PauseActivity";

	private Context context;

	private Ship nShip;
	private Player nPlayer;
	private Map nMap;

	private List<AppSensorEvent> sensorEvents;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pause);
		this.setFinishOnTouchOutside(false);
		
		context = this;

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		sensorEvents = new ArrayList<>();

		Intent data = getIntent();
		sensorEvents = data.getParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS);

        ImageButton btnResume = findViewById(R.id.btnPauseResume);
		btnResume.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				MusicManager.getInstance().changeSong(context, MusicManager.MUSIC_BATTLE);
				finish();
			}
		});

        ImageButton btnSettings = findViewById(R.id.btnPauseSettings);
		btnSettings.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent settingsIntent = new Intent(context, SettingsActivity.class);
				settingsIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
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

		ImageButton btnExit = findViewById(R.id.btnPauseExit);
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				LeaveGameDialogFragment exitDialog = new LeaveGameDialogFragment();
				exitDialog.show(getFragmentManager(), "LeaveGameDialog");
			}
		});

		nShip = data.getParcelableExtra(Constants.PAUSE_SHIP);
		nPlayer = data.getParcelableExtra(Constants.PAUSE_PLAYER);
		nMap = data.getParcelableExtra(Constants.PAUSE_MAP);

		TextView txtHealth = findViewById(R.id.txtHealth);
		txtHealth.setText(getString(R.string.current_max_value, nShip.getHealth(), nShip.getMaxHealth()));

		TextView txtGold = findViewById(R.id.txtGold);
		txtGold.setText(getString(R.string.generic_number, nPlayer.getGold()));

		TextView txtRange = findViewById(R.id.txtRange);
		txtRange.setText(getString(R.string.generic_number, Math.round(nShip.getRange() * Constants.DEFAULT_SHIP_BASIC_RANGE)));

		TextView txtXp = findViewById(R.id.txtXp);
		txtXp.setText(getString(R.string.current_level_xp, nPlayer.getLevel(), nPlayer.getExperience()));

		TextView txtPower = findViewById(R.id.txtPower);
		txtPower.setText(getString(R.string.generic_number, Math.round(nShip.getPower() * Constants.DEFAULT_SHOOT_DAMAGE)));

		TextView txtMap = findViewById(R.id.txtMap);
		txtMap.setText(getString(R.string.current_max_value, nPlayer.getMapPieces(), Constants.MAP_PIECES_LIMIT));

        MusicManager.getInstance().changeSong(context, MusicManager.MUSIC_GAME_PAUSED);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(dummyActivity, R.style.Dialog_No_Border);
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

					MusicManager.getInstance().changeSong(dummyActivity, MusicManager.MUSIC_GAME_MENU);

					((PauseActivity)dummyActivity).nMap.setActiveCell(((PauseActivity)dummyActivity).nMap.getLastActiveCell());
					GameHelper.saveGameAtPreferences(dummyActivity, ((PauseActivity)dummyActivity).nPlayer, ((PauseActivity)dummyActivity).nShip, ((PauseActivity)dummyActivity).nMap);

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
			AlertDialog d = builder.create();
			d.setView(view, 0,0,0,0);
			return d;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "Activity paused");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Activity destroyed");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "Activity resumed");
	}
}
