package com.giggs.apps.chaos.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.giggs.apps.chaos.MyApplication.FONTS;

public class CustomButton extends Button {

    public CustomButton(Context context) {
        super(context);
        initCustomButton();
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomButton();
    }

    public CustomButton(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        initCustomButton();
    }

    private void initCustomButton() {
        setTypeface(FONTS.main);
        // disable button default sound when clicked
        setSoundEffectsEnabled(false);
    }

}
