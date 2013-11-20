package com.giggs.apps.chaos.game;

import org.andengine.util.color.Color;

import com.giggs.apps.chaos.R;
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
            new Color(0.5f, 0.0f, 1.0f, ControlZone.INITIAL_ALPHA),
            new Color(0.0f, 0.5f, 1.0f, ControlZone.INITIAL_ALPHA)};

    public static final int[] PLAYER_BLASONS = { R.drawable.blason_1, R.drawable.blason_2, R.drawable.blason_3,
            R.drawable.blason_4, R.drawable.blason_5, R.drawable.blason_6, R.drawable.blason_7, R.drawable.blason_8 };

    public static final int TILE_SIZE = 256;// in pixels

    public static enum Direction {
        north, east, south, west
    }

    public static final float WINTER_GATHERING_MODIFIER = 0.7f;

    public static final int NUMBER_CHARACTERS_IN_ROW = 3;

    public static final int MAX_UNITS_PER_TILE = 3;

    public static final int MORALE_THRESHOLD_ROUTED = 35;

    public static final int EXPERIENCE_POINTS_PER_BATTLE_ROUND = 3;

    public static final String[] AI_NAMES = { "Guildenstern", "Fractal", "Reindeer", "Lord Bobby", "Sigmund Fruit",
            "Ban Anna", "Optimus Lime", "Al Pacho", "Lemon Alisa", "Goliath", "Spongebob", "Nautilus", "Asterion",
            "Tron" };

}
