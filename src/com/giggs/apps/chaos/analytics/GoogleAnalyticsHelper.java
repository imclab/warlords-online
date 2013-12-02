package com.giggs.apps.chaos.analytics;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class GoogleAnalyticsHelper {

    public static enum EventCategory {
        ui_action, game_stats
    }

    public static enum EventAction {
        button_press, end_game, winner_army, nb_players_chosen_solo, army_chosen_solo, result_against_AI, game_nb_turn
    }

    public static void sendEvent(Context context, EventCategory category, EventAction action, String label) {
        EasyTracker easyTracker = EasyTracker.getInstance(context);
        easyTracker.send(MapBuilder.createEvent(category.name(), action.name(), label, null).build());
    }

    public static enum TimingCategory {
    }

    public static enum TimingName {
    }

    public static void sendTiming(Context context, TimingCategory category, TimingName name, long loadTime) {
        Tracker easyTracker = EasyTracker.getInstance(context);
        easyTracker.send(MapBuilder.createTiming(category.name(), loadTime, name.name(), null).build());
    }

}
