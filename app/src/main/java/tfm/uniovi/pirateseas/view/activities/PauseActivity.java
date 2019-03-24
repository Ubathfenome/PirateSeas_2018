package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.model.canvasmodel.game.entity.Ship;

/**
 * Activity to manage the behaviour of the game pause button
 */
public class PauseActivity extends Activity {
	private static final String TAG = "PauseActivity";

	private Context context;
	
	private TextView txtTitleLabel;
	private TextView txtTooltip;
	private Button btnResume;
	private Button btnExit;
	private ImageButton btnSettings;
	private ImageButton btnHelp;

	private ProgressBar pgrHealth;
	private ProgressBar pgrPower;
	private ProgressBar pgrRange;

	private ImageView imgHealth;
	private ImageView imgPower;
	private ImageView imgRange;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pause);
		this.setFinishOnTouchOutside(false);
		
		context = this;

		txtTitleLabel = findViewById (R.id.txtPauseLabel);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");
		txtTitleLabel.setTypeface(customFont);

		Intent data = getIntent();

		btnResume = findViewById(R.id.btnPauseResume);
		btnResume.setTypeface(customFont);
		btnResume.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				MusicManager.getInstance().stopBackgroundMusic();
				MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
				finish();
			}
		});
		
		btnSettings = findViewById(R.id.btnPauseSettings);
		btnSettings.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent settingsIntent = new Intent(context, SettingsActivity.class);
				startActivity(settingsIntent);
			}
		});
		
		btnHelp = findViewById(R.id.btnPauseHelp);
		btnHelp.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent helpIntent = new Intent(context, HelpActivity.class);
				startActivity(helpIntent);
			}
		});
		
		btnExit = findViewById(R.id.btnPauseExit);
		btnExit.setTypeface(customFont);
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// Exit game. Return to main menu
				Intent mainMenuIntent = new Intent(context, MainMenuActivity.class);
				MusicManager.getInstance().stopBackgroundMusic();
				MusicManager.getInstance(context, MusicManager.MUSIC_BATTLE).playBackgroundMusic();
				mainMenuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mainMenuIntent);
				finish();
			}
		});

		txtTooltip = findViewById(R.id.txtTooltip);
		txtTooltip.setTypeface(customFont);

		Ship ship = data.getParcelableExtra(Constants.PAUSE_SHIP);

		pgrHealth = findViewById(R.id.pgrsHealth);
		pgrHealth.setMax(ship.getMaxHealth());
		pgrHealth.setProgress(ship.getHealth());

		pgrPower = findViewById(R.id.pgrsPower);
		pgrPower.setMax(Constants.SHIP_MAX_POWER);
		int pProgress = Math.round(ship.getPower() * Constants.DEFAULT_SHOOT_DAMAGE);
		pgrPower.setProgress(pProgress);

		pgrRange = findViewById(R.id.pgrsRange);
		pgrRange.setMax(Constants.SHIP_MAX_RANGE);
		int rProgress = Math.round(ship.getRange() * Constants.DEFAULT_SHIP_BASIC_RANGE);
		pgrRange.setProgress(rProgress);

        imgHealth = findViewById(R.id.imgHealth);
        imgHealth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				txtTooltip.setText(getString(R.string.pause_hint_health, pgrHealth.getProgress(), pgrHealth.getMax()));
			}
		});
        imgPower = findViewById(R.id.imgPower);
        imgPower.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				txtTooltip.setText(getString(R.string.pause_hint_power, pgrPower.getProgress(), pgrPower.getMax()));
			}
		});
        imgRange = findViewById(R.id.imgRange);
        imgRange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				txtTooltip.setText(getString(R.string.pause_hint_range, pgrRange.getProgress(), pgrRange.getMax()));
			}
		});

		MusicManager.getInstance().stopBackgroundMusic();
		MusicManager.getInstance(context, MusicManager.MUSIC_GAME_PAUSED).playBackgroundMusic();

		Log.d(TAG,"PauseActivity Ship H=" + pgrHealth.getProgress() + "/" + pgrHealth.getMax() + " P=" + pgrPower.getProgress() + "/" + pgrPower.getMax() + " R=" + pgrRange.getProgress() + "/" + pgrRange.getMax());

	}	
	
}
