package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.controller.sensors.events.AppSensorEvent;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity that is called when the player looses a battle
 */
public class GameOverActivity extends Activity {
	
	Player  p = null;
	Context context;
	
	TextView lblGameOver, txtDays, txtScore, lblRestartHint;

	private List<AppSensorEvent> sensorEvents;
	protected int[] sensorTypes = null;

	// OnCreate
	//: Display results	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_gameover);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		context = this;
		
		// GetIntent Extras
		Intent intent = getIntent();
		p = intent.getParcelableExtra(Constants.TAG_GAME_OVER_PLAYER);
		int clearedMapCells = intent.getIntExtra(Constants.TAG_GAME_OVER_MAP, 1);
		sensorEvents = intent.getParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS);
		sensorTypes = intent.getIntArrayExtra(Constants.TAG_SENSOR_LIST);

		lblGameOver = findViewById(R.id.lblGameOver);
		txtDays = findViewById(R.id.txtDays);
		txtScore = findViewById(R.id.txtScore);
		
		int score = p.getLevel() * p.getExperience() + p.getGold();
		if(score == 0)
			score = p.getExperience() + p.getGold();
		
		txtDays.setText(getString(R.string.generic_number, clearedMapCells));
		txtScore.setText(getString(R.string.generic_number,score));

		Button btnLoadGame = findViewById(R.id.btn_load_game);
		btnLoadGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Transition mFadeTransition = new Fade();
				TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.rootLayoutGameOver), mFadeTransition);

				MusicManager.getInstance().changeSong(context, MusicManager.MUSIC_GAME_MENU);

				Intent screenIntent = new Intent(context, ScreenSelectionActivity.class);
				screenIntent.putParcelableArrayListExtra(Constants.TAG_SENSOR_EVENTS, (ArrayList<? extends Parcelable>) sensorEvents);
				screenIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
				screenIntent.putExtra(Constants.TAG_LOAD_GAME, true);
				screenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(screenIntent);
				finish();
			}
		});

		Button btnMainMenu = findViewById(R.id.btn_main_menu);
		btnMainMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Transition mFadeTransition = new Fade();
				TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.rootLayoutGameOver), mFadeTransition);

				MusicManager.getInstance().changeSong(context, MusicManager.MUSIC_GAME_MENU);

				Intent newGameTaskIntent = new Intent(context, MainMenuActivity.class);
				newGameTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(newGameTaskIntent);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutGameOver).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		try {
			MusicManager.getInstance().playBackgroundMusic();
		} catch(IllegalStateException e){
			MusicManager.getInstance().resetPlayer();
		}
		super.onResume();
	}

	// OnTouch
	@SuppressLint("NewApi")
	//: DESTROY EVERYTHING!! Muahahaha!! >:D
	//: Start Main Menu Activity
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){

		}
		
		return true;
	}
	
	
	
	
	
}
