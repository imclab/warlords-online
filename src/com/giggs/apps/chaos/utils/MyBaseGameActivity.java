package com.giggs.apps.chaos.utils;

import android.media.AudioManager;
import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.example.games.basegameutils.BaseGameActivity;

public abstract class MyBaseGameActivity extends BaseGameActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// allow user to change the music volume with the phone's hardware
		// buttons
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
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

}
