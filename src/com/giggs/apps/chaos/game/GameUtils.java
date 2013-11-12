package com.giggs.apps.chaos.game;

import org.andengine.util.color.Color;

import com.giggs.apps.chaos.game.graphics.ControlZone;

public class GameUtils {

    public static final String GAME_PREFS_KEY_MUSIC_VOLUME = "game_music_volume";

    public static enum MusicState {
        off, on
    }

    public static final int[] NB_PLAYERS_IN_GAME = { 2, 3, 4, 8 };

    public static final Color[] PLAYER_COLORS = { new Color(0.0f, 0.0f, 1.0f, ControlZone.INITIAL_ALPHA),
            new Color(1.0f, 0.0f, 0.0f, ControlZone.INITIAL_ALPHA),
            new Color(0.0f, 1.0f, 0.0f, ControlZone.INITIAL_ALPHA),
            new Color(1.0f, 1.0f, 0.0f, ControlZone.INITIAL_ALPHA),
            new Color(0.0f, 0.0f, 0.0f, ControlZone.INITIAL_ALPHA),
            new Color(1.0f, 1.0f, 1.0f, ControlZone.INITIAL_ALPHA),
            new Color(0.0f, 0.5f, 1.0f, ControlZone.INITIAL_ALPHA),
            new Color(0.5f, 0.0f, 1.0f, ControlZone.INITIAL_ALPHA) };

    public static final int TILE_SIZE = 256;// in pixels

}
