package com.giggs.apps.chaos;

import android.app.Application;
import android.graphics.Typeface;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // fonts caching
        loadFonts();
    }

    /**
     * Loads the required fonts.
     */
    private void loadFonts() {
        FONTS.splash = Typeface.createFromAsset(getAssets(), "fonts/font_splash.ttf");
        FONTS.main = Typeface.createFromAsset(getAssets(), "fonts/font_main.ttf");
        FONTS.text = Typeface.createFromAsset(getAssets(), "fonts/font_text.otf");
    }

    public static class FONTS {
        public static Typeface splash, main, text;
    }

}
