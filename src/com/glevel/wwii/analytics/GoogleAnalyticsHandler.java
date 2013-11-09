package com.glevel.wwii.analytics;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class GoogleAnalyticsHandler {

    public static enum EventCategory {
        ui_action, in_game
    }

    public static enum EventAction {
        button_press, end_game
    }

    public static void sendEvent(Context context, EventCategory category, EventAction action, String label) {
        EasyTracker easyTracker = EasyTracker.getInstance(context);
        // easyTracker.send(MapBuilder.createEvent(category.name(),
        // action.name(), label, null).build());
    }

    public static enum TimingCategory {
        resources, in_game
    }

    public static enum TimingName {
        load_game, deployment_time, game_time
    }

    public static void sendTiming(Context context, TimingCategory category, TimingName name, long loadTime) {
        Tracker easyTracker = EasyTracker.getInstance(context);
        // easyTracker.send(MapBuilder.createTiming(category.name(), loadTime,
        // name.name(), null).build());
    }

}
