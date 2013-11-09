package com.giggs.apps.chaos.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.giggs.apps.chaos.MyApplication.FONTS;
import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.WWActivity;

public class SplashActivity extends WWActivity {

	private static final int DELAY_AFTER_ANIMATION = 300, DELAY_BEFORE_ANIMATION = 200;

	private ViewGroup mTitleLayout;
	private Animation mLetterAnimation, mBounceAnimation;
	private int mCurrentAnimationPlaying = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// increase number of launches
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		int nbLaunches = prefs.getInt(ApplicationUtils.PREFS_NB_LAUNCHES, 0);
		prefs.edit().putInt(ApplicationUtils.PREFS_NB_LAUNCHES, ++nbLaunches);

		// do not show splashscreen after a few launches
		if (nbLaunches >= ApplicationUtils.NB_LAUNCHES_WITH_SPLASHSCREEN) {
			goToHomeScreen();
		}

		setContentView(R.layout.activity_splashscreen);
		setupUI();

		prepareSplashScreenTitle();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startNextFallingLetterAnimation();
			}
		}, DELAY_BEFORE_ANIMATION);
	}

	private void setupUI() {
		mTitleLayout = (ViewGroup) findViewById(R.id.title);

		mBounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_effect);

		mLetterAnimation = AnimationUtils.loadAnimation(this, R.anim.falling_letter);
		mLetterAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// start next letter animation and the general bounce effect
				startNextFallingLetterAnimation();
				mTitleLayout.startAnimation(mBounceAnimation);
			}
		});
	}

	private void prepareSplashScreenTitle() {
		// create text views for each letter of the title
		String appPublisher = getString(R.string.app_publisher);
		for (int n = 0; n < appPublisher.length(); n++) {
			char c = appPublisher.charAt(n);
			TextView letterTV = new TextView(this);
			letterTV.setTextAppearance(this, R.style.SplashScreenTitle);
			letterTV.setText("" + c);
			letterTV.setVisibility(View.GONE);
			letterTV.setTypeface(FONTS.splash);
			mTitleLayout.addView(letterTV);
		}
	}

	private void startNextFallingLetterAnimation() {
		if (mCurrentAnimationPlaying < mTitleLayout.getChildCount()) {
			// reset previous view's animation
			if (mCurrentAnimationPlaying > 0) {
				mTitleLayout.getChildAt(mCurrentAnimationPlaying - 1).setAnimation(null);
			}
			// play next animation
			View nextLetter = mTitleLayout.getChildAt(mCurrentAnimationPlaying);
			nextLetter.setVisibility(View.VISIBLE);
			nextLetter.startAnimation(mLetterAnimation);
			mCurrentAnimationPlaying++;
		} else {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					goToHomeScreen();
				}
			}, DELAY_AFTER_ANIMATION);
		}
	}

	private void goToHomeScreen() {
		Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
		startActivity(intent);
		finish();
	}

}
