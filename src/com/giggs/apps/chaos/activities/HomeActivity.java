package com.giggs.apps.chaos.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHandler;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHandler.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHandler.EventCategory;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GameUtils.MusicState;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.WWActivity;

public class HomeActivity extends WWActivity implements OnClickListener {

	private static enum ScreenState {
		HOME, SETTINGS
	}

	private SharedPreferences mSharedPrefs;
	private ScreenState mScreenState = ScreenState.HOME;

	private Animation mFadeOutAnimation, mFadeInAnimation;
	private Button mSoloButton, mMultiplayerButton, mHelpButton, mSettingsButton, mAboutButton, mRateAppButton;
	private ViewGroup mMainButtonsLayout, mSettingsLayout;
	private View mBackButton;
	private RadioGroup mRadioMusicvolume;
	private Dialog mAboutDialog = null;
	private Animation mButtonsInAnimation;
	private Runnable mStormEffect;
	private ImageView mStormBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		setupUI();

		ApplicationUtils.showRateDialogIfNeeded(this);
		showMainHomeButtons();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// init storm effect
		mStormBackground = (ImageView) findViewById(R.id.stormBackground);
		mStormEffect = ApplicationUtils.addStormBackgroundAtmosphere(mStormBackground);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mStormBackground.removeCallbacks(mStormEffect);
		if (mAboutDialog != null) {
			mAboutDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.isShown()) {
			switch (v.getId()) {
			case R.id.soloButton:
				goToSoloGameScreen();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "play_solo");
				break;
			case R.id.multiplayerButton:
				goToMultiplayerScreen();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "play_multi");
				break;
			case R.id.helpButton:
				goToHelpScreen();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_help");
				break;
			case R.id.settingsButton:
				showSettings();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_settings");
				break;
			case R.id.backButton:
				onBackPressed();
				break;
			case R.id.aboutButton:
				openAboutDialog();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_about_dialog");
				break;
			case R.id.rateButton:
				ApplicationUtils.rateTheApp(this);
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "rate_app_button");
				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		switch (mScreenState) {
		case HOME:
			super.onBackPressed();
			break;
		case SETTINGS:
			showMainHomeButtons();
			GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
			        EventAction.button_press, "back_pressed");
			break;
		}
	}

	private void setupUI() {
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		mMainButtonsLayout = (ViewGroup) findViewById(R.id.mainButtonsLayout);
		mSettingsLayout = (ViewGroup) findViewById(R.id.settingsLayout);

		mButtonsInAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_in);

		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

		mSoloButton = (Button) findViewById(R.id.soloButton);
		mSoloButton.setOnClickListener(this);

		mMultiplayerButton = (Button) findViewById(R.id.multiplayerButton);
		mMultiplayerButton.setOnClickListener(this);

		mHelpButton = (Button) findViewById(R.id.helpButton);
		mHelpButton.setOnClickListener(this);

		mSettingsButton = (Button) findViewById(R.id.settingsButton);
		mSettingsButton.setOnClickListener(this);

		mBackButton = (Button) findViewById(R.id.backButton);
		mBackButton.setOnClickListener(this);

		mRadioMusicvolume = (RadioGroup) findViewById(R.id.musicVolume);

		// update radio buttons states according to the music preference
		int musicVolume = mSharedPrefs.getInt(GameUtils.GAME_PREFS_KEY_MUSIC_VOLUME, 0);
		((RadioButton) mRadioMusicvolume.getChildAt(musicVolume)).setChecked(true);
		mRadioMusicvolume.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// update game difficulty in preferences
				MusicState newMusicState = null;
				Editor editor = mSharedPrefs.edit();
				switch (checkedId) {
				case R.id.musicOff:
					newMusicState = MusicState.off;
					break;
				case R.id.musicOn:
					newMusicState = MusicState.on;
					break;
				}
				editor.putInt(GameUtils.GAME_PREFS_KEY_MUSIC_VOLUME, newMusicState.ordinal());
				editor.commit();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "music_" + newMusicState.name());
			}
		});

		mAboutButton = (Button) findViewById(R.id.aboutButton);
		mAboutButton.setOnClickListener(this);

		mRateAppButton = (Button) findViewById(R.id.rateButton);
		mRateAppButton.setOnClickListener(this);
	}

	private void openAboutDialog() {
		mAboutDialog = new Dialog(this, R.style.Dialog);
		mAboutDialog.setCancelable(true);
		mAboutDialog.setContentView(R.layout.dialog_about);
		// activate the dialog links
		TextView creditsTV = (TextView) mAboutDialog.findViewById(R.id.aboutCredits);
		creditsTV.setMovementMethod(LinkMovementMethod.getInstance());
		TextView blogTV = (TextView) mAboutDialog.findViewById(R.id.aboutBlog);
		blogTV.setMovementMethod(LinkMovementMethod.getInstance());
		TextView contactTV = (TextView) mAboutDialog.findViewById(R.id.aboutContact);
		contactTV.setMovementMethod(LinkMovementMethod.getInstance());
		mAboutDialog.show();
	}

	private void showMainHomeButtons() {
		mScreenState = ScreenState.HOME;
		mMainButtonsLayout.startAnimation(mButtonsInAnimation);
		mMainButtonsLayout.setVisibility(View.VISIBLE);
		mSettingsLayout.startAnimation(mFadeOutAnimation);
		mSettingsLayout.setVisibility(View.GONE);
		mBackButton.startAnimation(mFadeOutAnimation);
		mBackButton.setVisibility(View.GONE);
	}

	private void showSettings() {
		mScreenState = ScreenState.SETTINGS;
		mMainButtonsLayout.startAnimation(mFadeOutAnimation);
		mMainButtonsLayout.setVisibility(View.GONE);
		mSettingsLayout.startAnimation(mFadeInAnimation);
		mSettingsLayout.setVisibility(View.VISIBLE);
		mBackButton.startAnimation(mFadeInAnimation);
		mBackButton.setVisibility(View.VISIBLE);
	}

	// private void showResumeSoloGameDialog(final Battle savedGame) {
	// // ask user if he wants to resume a saved game
	// Dialog dialog = new CustomAlertDialog(this, R.style.Dialog,
	// getString(R.string.resume_saved_battle,
	// getString(savedGame.getName())), new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// if (which == R.id.okButton) {
	// // load saved solo game
	// // TODO
	// Intent i = new Intent(HomeActivity.this, GameActivity.class);
	// Bundle extras = new Bundle();
	// extras.putLong("game_id", savedGame.getId());
	// i.putExtras(extras);
	// dialog.dismiss();
	// startActivity(i);
	// finish();
	// } else {
	// // create new battle
	// dialog.dismiss();
	// goToSoloGameScreen();
	// }
	// }
	// });
	// dialog.show();
	// }

	private void goToSoloGameScreen() {
		// TODO
	}

	private void goToMultiplayerScreen() {
		// TODO Auto-generated method stub
	}

	private void goToHelpScreen() {
		startActivity(new Intent(this, HelpActivity.class));
		finish();
	}
}
