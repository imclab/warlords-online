package com.giggs.apps.chaos.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.giggs.apps.chaos.MyApplication;
import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;

public class ApplicationUtils {

    public static final String PREFS_NB_LAUNCHES = "nb_launches";
    public static final String PREFS_RATE_DIALOG_IN = "rate_dialog_in";
    public static final int NB_LAUNCHES_WITH_SPLASHSCREEN = 8;
    public static final int NB_LAUNCHES_RATE_DIALOG_APPEARS = 5;

    private static final int STORM_EFFECT_INITIAL_ALPHA = 150;
    private static final int STORM_EFFECT_ALPHA = 50;

    public static void showRateDialogIfNeeded(final Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        if (prefs.getInt(PREFS_RATE_DIALOG_IN, NB_LAUNCHES_RATE_DIALOG_APPEARS) == 0) {
            final Editor editor = prefs.edit();

            AlertDialog dialog = new AlertDialog.Builder(activity, R.style.Dialog)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setMessage(activity.getString(R.string.rate_message, activity.getString(R.string.app_name)))
                    .setPositiveButton(R.string.rate_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putInt(PREFS_RATE_DIALOG_IN, -1);
                            editor.commit();
                            rateTheApp(activity);
                            dialog.dismiss();
                            GoogleAnalyticsHelper.sendEvent(activity, EventCategory.ui_action,
                                    EventAction.button_press, "rate_app_yes");
                        }
                    }).setNegativeButton(R.string.rate_dont_want, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putInt(PREFS_RATE_DIALOG_IN, -1);
                            editor.commit();
                            dialog.dismiss();
                            GoogleAnalyticsHelper.sendEvent(activity, EventCategory.ui_action,
                                    EventAction.button_press, "rate_app_no");
                        }
                    }).setNeutralButton(R.string.rate_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putInt(PREFS_RATE_DIALOG_IN, 5);
                            dialog.dismiss();
                            GoogleAnalyticsHelper.sendEvent(activity, EventCategory.ui_action,
                                    EventAction.button_press, "rate_app_later");
                        }
                    }).create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            stylifyDefaultAlertDialog(dialog);
        }
    }

    public static void rateTheApp(Activity activity) {
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.giggs.apps.chaos"));
        activity.startActivity(goToMarket);
    }

    public static void contactSupport(Context context) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { context.getString(R.string.mail_address) });
        i.putExtra(Intent.EXTRA_SUBJECT,
                context.getString(R.string.contact_title) + context.getString(R.string.app_name));
        try {
            context.startActivity(Intent.createChooser(i, context.getString(R.string.contact_support_via)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, context.getString(R.string.no_mail_client), Toast.LENGTH_LONG).show();
        }
        GoogleAnalyticsHelper.sendEvent(context, EventCategory.ui_action, EventAction.button_press, "contact_support");
    }

    public static void stylifyDefaultAlertDialog(AlertDialog dialog) {
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        // center message and update font
        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
        messageView.setTypeface(MyApplication.FONTS.main);
        messageView.setBackgroundResource(R.drawable.bg_alert_dialog_btn);

        // update buttons background and font
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setBackgroundResource(R.drawable.bg_alert_dialog_btn);
        negativeButton.setTypeface(MyApplication.FONTS.main);
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setBackgroundResource(R.drawable.bg_alert_dialog_btn);
        positiveButton.setTypeface(MyApplication.FONTS.main);
        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setBackgroundResource(R.drawable.bg_alert_dialog_btn);
        neutralButton.setTypeface(MyApplication.FONTS.main);
    }

    public static void showToast(Context context, int textResourceId, int duration) {
        // setup custom toast view
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(textResourceId);

        Toast toast = new Toast(context);
        // toast.setGravity(Gravity.CE, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    public static void openDialogFragment(Activity activity, DialogFragment dialog, Bundle bundle) {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        dialog.setArguments(bundle);
        dialog.show(ft, "dialog");
    }

    public static Runnable addStormBackgroundAtmosphere(final ImageView backgroundView) {
        // Storm atmosphere
        backgroundView.setColorFilter(Color.argb(STORM_EFFECT_INITIAL_ALPHA, 0, 0, 0));
        Runnable stormEffectRunnable = new Runnable() {

            private boolean isThunder = false;

            public void run() {
                if (Math.random() < 0.03) {
                    // thunderstruck !
                    isThunder = true;
                    backgroundView.setColorFilter(Color.argb(STORM_EFFECT_ALPHA, 0, 0, 0));
                } else if (isThunder) {
                    isThunder = false;
                    backgroundView.setColorFilter(Color.argb(STORM_EFFECT_INITIAL_ALPHA, 0, 0, 0));
                }
                backgroundView.postDelayed(this, 100);
            }
        };
        backgroundView.postDelayed(stormEffectRunnable, 100);
        return stormEffectRunnable;
    }

}
