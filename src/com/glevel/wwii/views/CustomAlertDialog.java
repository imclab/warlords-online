package com.glevel.wwii.views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.glevel.wwii.R;

public class CustomAlertDialog extends Dialog implements OnClickListener {

    private OnClickListener mOnClickListener;

    public CustomAlertDialog(Context context, int style, String message,
            android.content.DialogInterface.OnClickListener onClickListener) {
        super(context, style);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.custom_alert_dialog);
        ((TextView) findViewById(R.id.message)).setText(message);
        ((Button) findViewById(R.id.okButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.cancelButton)).setOnClickListener(this);
        mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View v) {
        mOnClickListener.onClick(this, v.getId());
    }

}
