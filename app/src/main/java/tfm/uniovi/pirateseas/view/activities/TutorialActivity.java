package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

/**
 * Tutorial activity that shows the basics of the game. Can be shown the first time the game is launched or anytime from the Main menu
 */
public class TutorialActivity extends FragmentActivity {

	Context context;
	
	int[] sensorTypes = null;
	boolean returnToMain = false;
	boolean loadGame = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		context = this;
        
        Intent data = getIntent();
		sensorTypes = data.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		loadGame = data.getBooleanExtra(Constants.TAG_LOAD_GAME, true);

		if(sensorTypes==null || emptySensors(sensorTypes))
			returnToMain=true;

		// Instantiate a ViewPager and a PagerAdapter.
		/*
		  The pager widget, which handles animation and allows swiping horizontally to access previous
		  and next wizard steps.
		 */
		ViewPager mPager = findViewById(R.id.pager);
		/*
		 * The pager adapter, which provides the pages to the view pager widget.
		 */
		PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			int currentPosition = Constants.ZERO_INT;
			int currentState = ViewPager.SCROLL_STATE_IDLE;
			
            @Override
            public void onPageSelected(int position) {
				currentPosition = position;
            }

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
				if (position == Constants.TUTORIAL_NUM_PAGES - 1 && positionOffsetPixels == Constants.ZERO_INT && currentState == ViewPager.SCROLL_STATE_DRAGGING)
					currentPosition = Constants.TUTORIAL_NUM_PAGES;

			}

			@Override
			public void onPageScrollStateChanged(int state){
				if (currentPosition == Constants.TUTORIAL_NUM_PAGES && currentState == ViewPager.SCROLL_STATE_DRAGGING && state == ViewPager.SCROLL_STATE_IDLE)
					checkEndTutorial();
				currentState = state;
			}
        });
    }

	/**
	 * Check if the sensorTypes array is empty (sensors have not been loaded yet)
	 * @param sensorTypes Set the sensor values as an array to be handled
	 * @return true if sensors have not been loaded, false otherwise
	 */
	private boolean emptySensors(int[] sensorTypes) {
		boolean empty = true;
		for(int sensor : sensorTypes){
			if(sensor != 0)
				empty = false;
		}
		return empty;
	}

	@Override
	protected void onResume() {
		findViewById(R.id.pager).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}

	/**
	 * Checks if the current fragment is the last fragment of the tutorial
	 */
	private void checkEndTutorial(){
		LeaveTutorialDialogFragment exitDialog = new LeaveTutorialDialogFragment();
		exitDialog.show(getFragmentManager(),"LeaveTutorialDialog");
	}
	
	@SuppressLint("ValidFragment")
	/*
	 * Class to show a Dialog that asks the user if he/she really want to leave the tutorial
	 */
	public static class LeaveTutorialDialogFragment extends DialogFragment {



	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.custom_dialog_layout, null);
			TextView txtTitle = view.findViewById(R.id.txtTitle);
			TextView txtMessage = view.findViewById(R.id.txtMessage);
			Button btnPositive = view.findViewById(R.id.btnPositive);
			Button btnNegative = view.findViewById(R.id.btnNegative);

	        if(((TutorialActivity)getActivity()).returnToMain) {

				txtTitle.setText(getResources().getString(R.string.exit_tutorial_dialog_title));

				txtMessage.setText(getResources().getString(R.string.exit_tutorial_main_menu_message));

				btnPositive.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						((TutorialActivity) getActivity()).returnToMainMenu();
					}
				});

				btnNegative.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						dismiss();
					}
				});

			} else {

				txtTitle.setText(getResources().getString(R.string.exit_tutorial_dialog_title));

				txtMessage.setText(getResources().getString(R.string.exit_tutorial_dialog_message));

				btnPositive.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						((TutorialActivity) getActivity()).startGame();
					}
				});

				btnNegative.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						dismiss();
					}
				});
			}
			builder.setView(view);
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}

	/**
	 * Launch the Main menu
	 */
	private void returnToMainMenu(){
		Intent mainMenuIntent = new Intent(context, MainMenuActivity.class);
		mainMenuIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		finish();
	}

	/**
	 * Launch the game activity
	 */
	private void startGame(){
		Intent startGameIntent = new Intent(context, ScreenSelectionActivity.class);
		startGameIntent.putExtra(Constants.TAG_SENSOR_LIST, sensorTypes);
		startGameIntent.putExtra(Constants.TAG_LOAD_GAME, loadGame);
		startActivity(startGameIntent);
		finish();
	}
	
	/**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return ScreenSlidePageFragment.create(position);
        }

        @Override
        public int getCount() {
            return Constants.TUTORIAL_NUM_PAGES;
        }
    }
}