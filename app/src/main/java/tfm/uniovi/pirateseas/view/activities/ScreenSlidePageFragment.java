/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tfm.uniovi.pirateseas.view.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tfm.uniovi.pirateseas.R;
import tfm.uniovi.pirateseas.global.Constants;
import tfm.uniovi.pirateseas.utils.approach2d.DrawableHelper;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 */
public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;

    ViewGroup rootView;
    int imageReference;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int pageNumber) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
				
		imageReference = 0;
				
        // Inflate the layout containing a title and body text.
        rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page, container, false);

        // Set the title view to show the page number.
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                getString(R.string.title_template_step, mPageNumber + 1) + " / " + Constants.TUTORIAL_NUM_PAGES);
				
		switch(mPageNumber){
			case 0:
				imageReference = R.mipmap.img_game_activity;
				rootView.findViewById(R.id.imgLeftArrow).setAlpha(0f);
				((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_1));
				break;
			case 1:
				imageReference = R.mipmap.img_screen_selection;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
				((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_2));
				break;
			case 2:
				imageReference = R.mipmap.img_movement_spawn;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
				((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_3));
				break;
			case 3:
				imageReference = R.mipmap.img_movement_shoot;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
				((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_4));
				break;
            case 4:
                imageReference = R.mipmap.img_movement_shoot_voice;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
                ((TextView) rootView.findViewById(R.id.text2)).setText(
                        getResources().getString(R.string.tutorial_5,
                                getResources().getString(R.string.command_fire),
                                getResources().getString(R.string.command_shoot),
                                getResources().getString(R.string.command_go)));
                break;
            case 5:
                imageReference = R.mipmap.img_ammunition;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
                ((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_6));
                break;
            case 6:
                imageReference = R.mipmap.img_enemy_defeated;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
                ((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_7));
                break;
            case 7:
                imageReference = R.mipmap.enemy_types;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
                ((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_8));
                break;
            case 8:
                imageReference = R.mipmap.img_pause;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
                ((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_9));
                break;
            case 9:
                imageReference = R.mipmap.img_gameover;
                rootView.findViewById(R.id.imgLeftArrow).setAlpha(1f);
                ((TextView) rootView.findViewById(R.id.text2)).setText(getResources().getString(R.string.tutorial_10));
                break;
		}

		ImageView background = rootView.findViewById(R.id.imgDisplayed);
		Context context = getActivity();
		background.setImageBitmap(DrawableHelper.decodeBitmapFromResource(getResources(),imageReference, DrawableHelper.getScreenWidth(context), DrawableHelper.getScreenHeight(context)));

        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
}
