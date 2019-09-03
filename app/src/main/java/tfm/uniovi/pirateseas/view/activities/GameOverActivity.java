package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.controller.audio.MusicManager;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity that is called when the player looses a battle
 */
public class GameOverActivity extends Activity {
	
	Player  p = null;
	
	TextView lblGameOver, txtDays, txtScore, lblRestartHint;

	// OnCreate
	//: Display results	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_gameover);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// GetIntent Extras
		Intent intent = getIntent();
		p = intent.getParcelableExtra(Constants.TAG_GAME_OVER_PLAYER);
		int clearedMapCells = intent.getIntExtra(Constants.TAG_GAME_OVER_MAP, 1);

		lblGameOver = findViewById(R.id.lblGameOver);
		lblGameOver.setTypeface(customFont);
		txtDays = findViewById(R.id.txtDays);
		txtDays.setTypeface(customFont);
		txtScore = findViewById(R.id.txtScore);
		txtScore.setTypeface(customFont);
		lblRestartHint = findViewById(R.id.lblRestartHint);
		lblRestartHint.setTypeface(customFont);
		
		int score = p.getLevel() * p.getExperience() + p.getGold();
		if(score == 0)
			score = p.getExperience() + p.getGold();
		
		txtDays.setText(getString(R.string.generic_number,clearedMapCells));
		txtScore.setText(getString(R.string.generic_number,score));
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

	@Override
	protected void onPause() {
		try {
			MusicManager.getInstance().pauseBackgroundMusic();
		} catch(IllegalStateException e){
			MusicManager.getInstance().resetPlayer();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		try {
			MusicManager.getInstance().stopBackgroundMusic();
		} catch(IllegalStateException e){
			MusicManager.getInstance().resetPlayer();
		}
		super.onStop();
	}

	// OnTouch
	@SuppressLint("NewApi")
	//: DESTROY EVERYTHING!! Muahahaha!! >:D
	//: Start Main Menu Activity
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			Transition mFadeTransition = new Fade();
			TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.rootLayoutGameOver), mFadeTransition);

			try {
				MusicManager.getInstance().stopBackgroundMusic();
			} catch(IllegalStateException e){
				MusicManager.getInstance().resetPlayer();
			}
			
			Intent newGameTaskIntent = new Intent(this, MainMenuActivity.class);
			newGameTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(newGameTaskIntent);
			finish();
		}
		
		return true;
	}
	
	
	
	
	
}
