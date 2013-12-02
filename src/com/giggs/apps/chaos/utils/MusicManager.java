package com.giggs.apps.chaos.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.preference.PreferenceManager;
import android.util.Log;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GameUtils.MusicState;

public class MusicManager {
	private static final String TAG = "MusicManager";
	public static final int MUSIC_PREVIOUS = -1;
	public static final int MUSIC_MENU = 0;
	public static final int MUSIC_GAME = 1;
	public static final int MUSIC_END_GAME = 2;

	private static Map<Integer, MediaPlayer> players = new HashMap<Integer, MediaPlayer>();
	private static int currentMusic = -1;
	private static int previousMusic = -1;

	public static void start(Context context, int music) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPrefs.getInt(GameUtils.GAME_PREFS_KEY_MUSIC_VOLUME, MusicState.off.ordinal()) == MusicState.on
		        .ordinal()) {
			start(context, music, false);
		}
	}

	public static void start(Context context, int music, boolean force) {
		if (!force && currentMusic > -1) {
			// already playing some music and not forced to change
			return;
		}
		if (music == MUSIC_PREVIOUS) {
			Log.d(TAG, "Using previous music [" + previousMusic + "]");
			music = previousMusic;
		}
		if (currentMusic == music) {
			// already playing this music
			return;
		}
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			Log.d(TAG, "Previous music was [" + previousMusic + "]");
			// playing some other music, pause it and change
			pause();
		}
		currentMusic = music;
		Log.d(TAG, "Current music is now [" + currentMusic + "]");
		MediaPlayer mp = players.get(music);
		if (mp != null) {
			if (!mp.isPlaying()) {
				mp.start();
			}
		} else {
			if (music == MUSIC_MENU) {
				mp = MediaPlayer.create(context, R.raw.main_music);
			} else if (music == MUSIC_GAME) {
				mp = MediaPlayer.create(context, R.raw.main_music);
			} else if (music == MUSIC_END_GAME) {
				mp = MediaPlayer.create(context, R.raw.main_music);
			} else {
				Log.e(TAG, "unsupported music number - " + music);
				return;
			}
			players.put(music, mp);
			if (mp == null) {
				Log.e(TAG, "player was not created successfully");
			} else {
				try {
					mp.setLooping(true);
					mp.start();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}

	public static void pause() {
		Collection<MediaPlayer> mps = players.values();
		for (MediaPlayer p : mps) {
			if (p.isPlaying()) {
				p.pause();
			}
		}
		// previousMusic should always be something valid
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			Log.d(TAG, "Previous music was [" + previousMusic + "]");
		}
		currentMusic = -1;
		Log.d(TAG, "Current music is now [" + currentMusic + "]");
	}

	public static void release() {
		Log.d(TAG, "Releasing media players");
		Collection<MediaPlayer> mps = players.values();
		for (MediaPlayer mp : mps) {
			try {
				if (mp != null) {
					if (mp.isPlaying()) {
						mp.stop();
					}
					mp.release();
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		mps.clear();
		if (currentMusic != -1) {
			previousMusic = currentMusic;
			Log.d(TAG, "Previous music was [" + previousMusic + "]");
		}
		currentMusic = -1;
		Log.d(TAG, "Current music is now [" + currentMusic + "]");
	}

	public static void playSound(Context context, int sound) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPrefs.getInt(GameUtils.GAME_PREFS_KEY_MUSIC_VOLUME, MusicState.off.ordinal()) == MusicState.on
		        .ordinal()) {
			MediaPlayer mp = MediaPlayer.create(context, sound);
			mp.start();
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.release();
					mp = null;
				}
			});
		}
	}

}
