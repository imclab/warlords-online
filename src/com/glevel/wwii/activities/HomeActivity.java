package com.glevel.wwii.activities;

import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.glevel.wwii.R;
import com.glevel.wwii.activities.fragments.CampaignChooserFragment;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler.EventAction;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler.EventCategory;
import com.glevel.wwii.database.DatabaseHelper;
import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.GameUtils.DifficultyLevel;
import com.glevel.wwii.game.GameUtils.MusicState;
import com.glevel.wwii.game.SaveGameHelper;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.utils.ApplicationUtils;
import com.glevel.wwii.utils.WWActivity;
import com.glevel.wwii.views.CustomAlertDialog;

public class HomeActivity extends WWActivity implements OnClickListener {

	private static enum ScreenState {
		HOME, SOLO, SETTINGS
	}

	private SharedPreferences mSharedPrefs;
	private ScreenState mScreenState = ScreenState.HOME;
	private DatabaseHelper mDbHelper;

	private Animation mMainButtonAnimationRightIn, mMainButtonAnimationRightOut, mMainButtonAnimationLeftIn,
	        mMainButtonAnimationLeftOut;
	private Animation mFadeOutAnimation, mFadeInAnimation;
	private Button mSoloButton, mMultiplayerButton, mSettingsButton, mTutorialButton, mCampaignButton,
	        mBattleModeButton, mAboutButton, mRateAppButton;
	private ViewGroup mSoloButtonsLayout, mSettingsLayout;
	private View mBackButton;
	private RadioGroup mRadioMusicvolume, mRadioDifficulty;
	private VideoView mBackgroundVideoView;
	private Dialog mAboutDialog = null;

	/**
	 * Callbacks
	 */
	// remove background video sound - enable video looping
	private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer m) {
			try {
				if (m.isPlaying()) {
					m.stop();
					m.release();
					m = new MediaPlayer();
				}
				// disable sound
				m.setVolume(0f, 0f);
				// repeat video
				m.setLooping(true);
				m.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		setupUI();

		ApplicationUtils.showRateDialogIfNeeded(this);
		showMainHomeButtons();

		if (savedInstanceState != null) {
			// restart video where it had been stopped
			mBackgroundVideoView.seekTo(savedInstanceState.getInt("video_stop_position"));
		}
		mBackgroundVideoView.start();

		mDbHelper = new DatabaseHelper(getApplicationContext());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// store the stop position to restart the video at the correct position
		outState.putInt("video_stop_position", mBackgroundVideoView.getCurrentPosition());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBackgroundVideoView.pause();
		if (mAboutDialog != null) {
			mAboutDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.isShown()) {
			switch (v.getId()) {
			case R.id.soloButton:
				showSoloButtons();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_solo");
				break;
			case R.id.multiplayerButton:
				ApplicationUtils.showToast(this, R.string.coming_soon, Toast.LENGTH_SHORT);
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_multi");
				break;
			case R.id.settingsButton:
				showSettings();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "show_settings");
				break;
			case R.id.backButton:
				onBackPressed();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "back_button_soft");
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
			case R.id.campaignButton:
				showCampaignSelector();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "go_campaign");
				break;
			case R.id.battleButton:
				List<Battle> lstBattles = SaveGameHelper.getUnfinishedBattles(mDbHelper);
				if (lstBattles.size() > 0) {
					showResumeGameDialog(lstBattles.get(0));
				} else {
					goToBattleChooserActivity();
				}
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
		case SOLO:
			showMainHomeButtons();
			hideSoloButtons();
			GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
			        EventAction.button_press, "back_pressed");
			break;
		case SETTINGS:
			showMainHomeButtons();
			hideSettings();
			GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
			        EventAction.button_press, "back_pressed");
			break;
		}
	}

	private void setupUI() {
		mSharedPrefs = getSharedPreferences(GameUtils.GAME_PREFS_FILENAME, MODE_PRIVATE);

		mSoloButtonsLayout = (ViewGroup) findViewById(R.id.soloLayout);
		mSettingsLayout = (ViewGroup) findViewById(R.id.settingsLayout);

		mMainButtonAnimationRightIn = AnimationUtils.loadAnimation(this, R.anim.main_btn_right_in);
		mMainButtonAnimationLeftIn = AnimationUtils.loadAnimation(this, R.anim.main_btn_left_in);
		mMainButtonAnimationLeftIn.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mSoloButton.isShown()) {
					mSoloButtonsLayout.setVisibility(View.GONE);
				}
			}
		});
		mMainButtonAnimationRightOut = AnimationUtils.loadAnimation(this, R.anim.main_btn_right_out);
		mMainButtonAnimationLeftOut = AnimationUtils.loadAnimation(this, R.anim.main_btn_left_out);
		
		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

		mSoloButton = (Button) findViewById(R.id.soloButton);
		mSoloButton.setOnClickListener(this);

		mMultiplayerButton = (Button) findViewById(R.id.multiplayerButton);
		mMultiplayerButton.setOnClickListener(this);

		mSettingsButton = (Button) findViewById(R.id.settingsButton);
		mSettingsButton.setOnClickListener(this);

		mTutorialButton = (Button) findViewById(R.id.tutorialButton);
		mTutorialButton.setOnClickListener(this);

		mCampaignButton = (Button) findViewById(R.id.campaignButton);
		mCampaignButton.setOnClickListener(this);

		mBattleModeButton = (Button) findViewById(R.id.battleButton);
		mBattleModeButton.setOnClickListener(this);

		mBackButton = (Button) findViewById(R.id.backButton);
		mBackButton.setOnClickListener(this);

		mRadioDifficulty = (RadioGroup) findViewById(R.id.radioDifficulty);
		// update radio buttons states according to the game difficulty
		int gameDifficulty = mSharedPrefs.getInt(GameUtils.GAME_PREFS_KEY_DIFFICULTY,
		        GameUtils.DifficultyLevel.medium.ordinal());
		((RadioButton) mRadioDifficulty.getChildAt(gameDifficulty)).setChecked(true);
		mRadioDifficulty.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// update game difficulty in preferences
				DifficultyLevel newDifficultyLevel = null;
				Editor editor = mSharedPrefs.edit();
				switch (checkedId) {
				case R.id.easyRadioBtn:
					newDifficultyLevel = DifficultyLevel.easy;
					break;
				case R.id.mediumRadioBtn:
					newDifficultyLevel = DifficultyLevel.medium;
					break;
				case R.id.hardRadioBtn:
					newDifficultyLevel = DifficultyLevel.hard;
					break;
				}
				editor.putInt(GameUtils.GAME_PREFS_KEY_DIFFICULTY, newDifficultyLevel.ordinal());
				editor.commit();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "difficulty_" + newDifficultyLevel.name());
			}
		});

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

		// setup background video
		mBackgroundVideoView = (VideoView) findViewById(R.id.backgroundVideo);
		Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_bg_home);
		mBackgroundVideoView.setVideoURI(videoUri);
		mBackgroundVideoView.setOnPreparedListener(mPreparedListener);

		mAboutButton = (Button) findViewById(R.id.aboutButton);
		mAboutButton.setOnClickListener(this);

		mRateAppButton = (Button) findViewById(R.id.rateButton);
		mRateAppButton.setOnClickListener(this);
	}

	private void goToBattleChooserActivity() {
		startActivity(new Intent(this, BattleChooserActivity.class));
		finish();
		GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action, EventAction.button_press,
		        "go_battle");
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

	private void showButton(View view, boolean fromRight) {
		if (fromRight) {
			view.startAnimation(mMainButtonAnimationRightIn);
		} else {
			view.startAnimation(mMainButtonAnimationLeftIn);
		}
		view.setVisibility(View.VISIBLE);
		view.setEnabled(true);
	}

	private void hideButton(View view, boolean toRight) {
		if (toRight) {
			view.startAnimation(mMainButtonAnimationRightOut);
		} else {
			view.startAnimation(mMainButtonAnimationLeftOut);
		}
		view.setVisibility(View.GONE);
		view.setEnabled(false);
	}

	private void showSoloButtons() {
		mScreenState = ScreenState.SOLO;
		hideMainHomeButtons();
		showButton(mTutorialButton, true);
		showButton(mCampaignButton, false);
		showButton(mBattleModeButton, true);
		mSoloButtonsLayout.setVisibility(View.VISIBLE);
	}

	private void hideSoloButtons() {
		hideButton(mTutorialButton, true);
		hideButton(mCampaignButton, false);
		hideButton(mBattleModeButton, true);
	}

	private void showMainHomeButtons() {
		mScreenState = ScreenState.HOME;
		mBackButton.startAnimation(mFadeOutAnimation);
		mBackButton.setVisibility(View.GONE);
		showButton(mSoloButton, true);
		showButton(mMultiplayerButton, false);
		showButton(mSettingsButton, true);
	}

	private void hideMainHomeButtons() {
		mBackButton.setVisibility(View.VISIBLE);
		mBackButton.startAnimation(mFadeInAnimation);
		hideButton(mSoloButton, true);
		hideButton(mMultiplayerButton, false);
		hideButton(mSettingsButton, true);
	}

	private void showSettings() {
		mScreenState = ScreenState.SETTINGS;
		mSettingsLayout.setVisibility(View.VISIBLE);
		mSettingsLayout.startAnimation(mFadeInAnimation);
		hideMainHomeButtons();
	}

	private void hideSettings() {
		mSettingsLayout.setVisibility(View.GONE);
		mSettingsLayout.startAnimation(mFadeOutAnimation);
	}

	private void showResumeGameDialog(final Battle savedGame) {
		// ask user if he wants to resume a saved game
		Dialog dialog = new CustomAlertDialog(this, R.style.Dialog, getString(R.string.resume_saved_battle,
		        getString(savedGame.getName())), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == R.id.okButton) {
					// load game
					Intent i = new Intent(HomeActivity.this, GameActivity.class);
					Bundle extras = new Bundle();
					extras.putLong("game_id", savedGame.getId());
					i.putExtras(extras);
					dialog.dismiss();
					startActivity(i);
					finish();
				} else {
					// create new battle
					dialog.dismiss();
					goToBattleChooserActivity();
				}
			}
		});
		dialog.show();
	}

	private void showCampaignSelector() {
		ApplicationUtils.openDialogFragment(this, new CampaignChooserFragment(), null);
	}

}
