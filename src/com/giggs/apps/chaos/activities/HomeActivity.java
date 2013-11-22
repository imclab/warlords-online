package com.giggs.apps.chaos.activities;

import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.fragments.CreateGameDialog;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;
import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GameUtils.MusicState;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.MusicManager;
import com.giggs.apps.chaos.views.CustomAlertDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.example.games.basegameutils.BaseGameActivity;

public class HomeActivity extends BaseGameActivity implements OnClickListener {

	private static enum ScreenState {
		HOME, SETTINGS
	}

	private SharedPreferences mSharedPrefs;
	private ScreenState mScreenState = ScreenState.HOME;

	private Animation mFadeOutAnimation, mFadeInAnimation;
	private Button mSoloButton, mMultiplayerButton, mHelpButton, mSettingsButton, mAboutButton, mRateAppButton;
	private ViewGroup mMainButtonsLayout, mSettingsLayout, mLoginLayout;
	private View mBackButton, mAppNameView;
	private RadioGroup mRadioMusicvolume;
	private Dialog mAboutDialog = null;
	private Animation mButtonsInAnimation;
	private Runnable mStormEffect;
	private ImageView mStormBackground;
	private DatabaseHelper mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		setupUI();

		mDbHelper = new DatabaseHelper(getApplicationContext());

		ApplicationUtils.showRateDialogIfNeeded(this);
		showMainHomeButtons();
		
		mContinueMusic = true;
	}

	private void setupUI() {
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		mMainButtonsLayout = (ViewGroup) findViewById(R.id.mainButtonsLayout);
		mSettingsLayout = (ViewGroup) findViewById(R.id.settingsLayout);
		mLoginLayout = (ViewGroup) findViewById(R.id.loginLayout);

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

		mAppNameView = (View) findViewById(R.id.appName);

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
					MusicManager.start(HomeActivity.this, mMusic);
					break;
				}
				editor.putInt(GameUtils.GAME_PREFS_KEY_MUSIC_VOLUME, newMusicState.ordinal());
				editor.commit();
				GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "music_" + newMusicState.name());
			}
		});

		mAboutButton = (Button) findViewById(R.id.aboutButton);
		mAboutButton.setOnClickListener(this);

		mRateAppButton = (Button) findViewById(R.id.rateButton);
		mRateAppButton.setOnClickListener(this);

		// login / logout buttons
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);

		mStormBackground = (ImageView) findViewById(R.id.stormBackground);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// init storm effect
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
	protected void onStart() {
		super.onStart();
		// analytics
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// analytics
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.isShown()) {
			switch (v.getId()) {
			case R.id.soloButton:
				if (mSharedPrefs.getInt(GameUtils.TUTORIAL_DONE, 0) == 0) {
					showTutorialDialog();
				} else {
					List<Battle> savedGames = mDbHelper.getBattleDao().get(null, null, null, null);
					if (savedGames.size() > 0) {
						showResumeSoloGameDialog(savedGames.get(0));
					} else {
						goToSoloGameScreen();
					}
					GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action,
					        EventAction.button_press, "play_solo");
				}
				break;
			case R.id.multiplayerButton:
				if (isSignedIn()) {
					goToMultiplayerScreen();
				} else {
					ApplicationUtils.showToast(getApplicationContext(), R.string.log_in_to_play_multi,
					        Toast.LENGTH_SHORT);
				}
				GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "play_multi");
				break;
			case R.id.helpButton:
				goToHelpScreen();
				GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_help");
				break;
			case R.id.settingsButton:
				showSettings();
				GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_settings");
				break;
			case R.id.backButton:
				onBackPressed();
				break;
			case R.id.aboutButton:
				openAboutDialog();
				GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_about_dialog");
				break;
			case R.id.rateButton:
				ApplicationUtils.rateTheApp(this);
				GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "rate_app_button");
				break;
			case R.id.sign_in_button:
				beginUserInitiatedSignIn();
				break;
			case R.id.sign_out_button:
				// sign out.
				signOut();

				// show sign-in button, hide the sign-out button
				findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
				findViewById(R.id.sign_out_button).setVisibility(View.GONE);
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
			GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action, EventAction.button_press,
			        "back_pressed");
			break;
		}
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
		mAppNameView.startAnimation(mFadeInAnimation);
		mAppNameView.setVisibility(View.VISIBLE);
		mLoginLayout.startAnimation(mFadeInAnimation);
		mLoginLayout.setVisibility(View.VISIBLE);
	}

	private void showSettings() {
		mScreenState = ScreenState.SETTINGS;
		mMainButtonsLayout.startAnimation(mFadeOutAnimation);
		mMainButtonsLayout.setVisibility(View.GONE);
		mSettingsLayout.startAnimation(mFadeInAnimation);
		mSettingsLayout.setVisibility(View.VISIBLE);
		mBackButton.startAnimation(mFadeInAnimation);
		mBackButton.setVisibility(View.VISIBLE);
		mAppNameView.startAnimation(mFadeOutAnimation);
		mAppNameView.setVisibility(View.GONE);
		mLoginLayout.startAnimation(mFadeOutAnimation);
		mLoginLayout.setVisibility(View.GONE);
	}

	private void showResumeSoloGameDialog(final Battle savedGame) {
		// ask user if he wants to resume a saved game
		Dialog dialog = new CustomAlertDialog(this, R.style.Dialog, getString(R.string.resume_saved_battle, savedGame
		        .getPlayers().size()), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == R.id.okButton) {
					// load saved solo game
					Intent intent = new Intent(HomeActivity.this, GameActivity.class);
					dialog.dismiss();
					startActivity(intent);
					finish();
				} else {
					// create new battle
					dialog.dismiss();
					goToSoloGameScreen();
				}
			}
		});
		dialog.show();
	}

	private void showTutorialDialog() {
		// ask user if he wants to do the tutorial as he is a noob
		Dialog dialog = new CustomAlertDialog(this, R.style.Dialog, getString(R.string.ask_tutorial),
		        new DialogInterface.OnClickListener() {
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
				        if (which == R.id.okButton) {
					        // go to tutorial
					        Intent intent = new Intent(HomeActivity.this, TutorialActivity.class);
					        dialog.dismiss();
					        startActivity(intent);
					        finish();
				        } else {
					        // create new battle
					        dialog.dismiss();
					        goToSoloGameScreen();
				        }
			        }
		        });
		dialog.show();
		mSharedPrefs.edit().putInt(GameUtils.TUTORIAL_DONE, 1).commit();
	}

	private void goToSoloGameScreen() {
		ApplicationUtils.openDialogFragment(this, new CreateGameDialog(), null);
	}

	private void goToMultiplayerScreen() {
		// TODO Auto-generated method stub
	}

	private void goToHelpScreen() {
		startActivity(new Intent(this, HelpActivity.class));
		finish();
	}

	@Override
	public void onSignInSucceeded() {
		// show sign-out button, hide the sign-in button
		findViewById(R.id.sign_in_button).setVisibility(View.GONE);
		findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
	}

	@Override
	public void onSignInFailed() {
		// Sign in has failed. So show the user the sign-in button.
		findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		findViewById(R.id.sign_out_button).setVisibility(View.GONE);
	}

}
