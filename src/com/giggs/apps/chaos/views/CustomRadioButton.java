package com.giggs.apps.chaos.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.giggs.apps.chaos.MyApplication.FONTS;

public class CustomRadioButton extends RadioButton {

    public CustomRadioButton(Context context) {
        super(context);
        setTypeface(FONTS.main);
    }

    public CustomRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(FONTS.main);
    }

}
