package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Map;
import tfm.uniovi.pirateseas.controller.androidGameAPI.Player;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Activity that is called when the player looses a battle
 */
public class GameOverActivity extends Activity {
	
	Player  p = null;
	Map m = null;
	
	TextView lblGameOver, txtDays, txtScore, lblRestartHint;

	// OnCreate
	//: Display results	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_gameover);
		Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/" + Constants.FONT_NAME + ".ttf");
		
		// GetIntent Extras
		Intent intent = getIntent();
		p = intent.getParcelableExtra(Constants.TAG_GAME_OVER_PLAYER);
		m = intent.getParcelableExtra(Constants.TAG_GAME_OVER_MAP);

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
		
		txtDays.setText(m.getClearedCells());
		txtScore.setText("" + score);
	}

	@Override
	protected void onResume() {
		findViewById(R.id.rootLayoutGameOver).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}
	// OnTouch
	@SuppressLint("NewApi")
	//: DESTROY EVERYTHING!! Muahahaha!! >:D
	//: Start Main Menu Activity
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				Transition mFadeTransition = new Fade();			
				TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.rootLayoutGameOver), mFadeTransition);
			}
			
			Intent newGameTaskIntent = new Intent(this, MainMenuActivity.class);
			newGameTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(newGameTaskIntent);
			finish();
		}
		
		return true;
	}
	
	
	
	
	
}
