/*******************************************************************************
 * Copyright 2012 Gunnar Karlsson.
 * ******************************************************************************/

package com.bluebitapps.fbclientbase.layout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class LoadingView extends RelativeLayout {

	public LoadingView(Context context) {
		super(context);
		initialize(context);
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public LoadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	private void initialize(Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String selection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);

		if (context != null) {

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			addView(inflater.inflate(R.layout.loading, null));
/*
			ImageView img = (ImageView) findViewById(com.bluebitapps.fbclientbase.R.id.loadingImageView);
			img.setBackgroundResource(R.drawable.loading_animation);
			AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
			frameAnimation.start();
*/
			setBackgroundResource(R.drawable.roundcorner_shape);
			GradientDrawable shape = (GradientDrawable)getBackground();
			int color = 0x000000;
			if(Constants.THEME_IS_COLOR_PICKER_COLOR.equals(selection)){				
				color= prefs.getInt(Constants.COLOR_PICKER_CHOICE_SLIDING_MENU, 0x000000);
			}else{
				color = getResources().getColor(ThemeFactory.getSlidingMenuColor(context, selection));				
			}
			shape.setColor(color);

		}

	}
}
