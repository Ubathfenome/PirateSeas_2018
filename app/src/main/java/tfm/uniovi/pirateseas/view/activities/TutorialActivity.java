package tfm.uniovi.pirateseas.view.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;

public class TutorialActivity extends FragmentActivity{
	/**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
	
	/**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    
    Context context;
	
	int[] sensorTypes = null;
	boolean loadGame = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

		context = this;
        
        Intent data = getIntent();
		sensorTypes = data.getIntArrayExtra(Constants.TAG_SENSOR_LIST);
		loadGame = data.getBooleanExtra(Constants.TAG_LOAD_GAME, true);

		// Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			int tmpPosition = 0;
			
            @Override
            public void onPageSelected(int position) {
				tmpPosition = position;
            }
			
			@Override
			public void onPageScrollStateChanged(int state){
				if (state == ViewPager.SCROLL_STATE_DRAGGING && tmpPosition == (Constants.TUTORIAL_NUM_PAGES - 1))
					checkEndTutorial();
			}
        });
    }
	
	@Override
	protected void onResume() {
		findViewById(R.id.pager).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		super.onResume();
	}
	private void checkEndTutorial(){
		LeaveTutorialDialogFragment exitDialog = new LeaveTutorialDialogFragment();
		exitDialog.show(getFragmentManager(),"LeaveTutorialDialog");
	}
	
	@SuppressLint("ValidFragment")
	public class LeaveTutorialDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setTitle(getResources().getString(R.string.exit_tutorial_dialog_title))
				   .setMessage(R.string.exit_tutorial_dialog_message)
	               .setPositiveButton(R.string.exit_tutorial_dialog_positive, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       startGame();
	                   }
	               })
	               .setNegativeButton(R.string.exit_dialog_negative, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // User cancels the dialog
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
	
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
        public ScreenSlidePagerAdapter(FragmentManager fragmentManager) {
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