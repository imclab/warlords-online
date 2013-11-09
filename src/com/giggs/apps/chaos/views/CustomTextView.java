package com.giggs.apps.chaos.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.giggs.apps.chaos.WWApplication.FONTS;

public class CustomTextView extends TextView {

	public CustomTextView(Context context) {
		super(context);
		setTypeface(FONTS.main);
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(FONTS.main);
	}

	public CustomTextView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
		setTypeface(FONTS.main);
	}

}
