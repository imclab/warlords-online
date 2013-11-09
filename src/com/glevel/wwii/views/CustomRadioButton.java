package com.glevel.wwii.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.glevel.wwii.WWApplication.FONTS;

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
