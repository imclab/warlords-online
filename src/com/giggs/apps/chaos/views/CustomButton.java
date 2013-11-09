package com.giggs.apps.chaos.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.giggs.apps.chaos.WWApplication.FONTS;

public class CustomButton extends Button {

    public CustomButton(Context context) {
        super(context);
        initCustomButton();
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomButton();
    }

    private void initCustomButton() {
        setTypeface(FONTS.main);
        // disable button default sound when clicked
        setSoundEffectsEnabled(false);
    }

}
