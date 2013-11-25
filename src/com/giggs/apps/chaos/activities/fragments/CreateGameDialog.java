package com.giggs.apps.chaos.activities.fragments;

import java.util.List;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.LayoutParams;

import com.giggs.apps.chaos.MyApplication;
import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.activities.MultiplayerActivity;
import com.giggs.apps.chaos.activities.interfaces.OnBillingServiceConnectedListener;
import com.giggs.apps.chaos.billing.InAppBillingHelper;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.utils.MusicManager;
import com.giggs.apps.chaos.views.CustomRadioButton;

public class CreateGameDialog extends DialogFragment {

    public static final String ARGUMENT_GAME_TYPE = "game_type";
    public static final String ARGUMENT_IS_HOST = "is_host";
    public static final int SOLO_GAME_TYPE = 100;
    public static final int MULTIPLAYER_GAME_TYPE = 200;

    private GridLayout mRadioGroupArmy;
    private RadioGroup mRadioGroupNbPlayers;
    private int mGameType;
    private int mSelectedArmy = 0;
    private boolean mIsHost = true;
    private InAppBillingHelper mInAppBillingHelper;

    /**
     * Callbacks
     */
    private OnBillingServiceConnectedListener mBillingServiceConnectionCallback = new OnBillingServiceConnectedListener() {
        @Override
        public void onBillingServiceConnected() {
            updateAvailableArmies();
        }
    };
    private OnClickListener onAvailableArmyButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSelectedArmy = (Integer) v.getTag();
            for (int n = 0; n < mRadioGroupArmy.getChildCount(); n++) {
                CustomRadioButton view = (CustomRadioButton) mRadioGroupArmy.getChildAt(n);
                view.setChecked(false);
            }
            ((CustomRadioButton) v).setChecked(true);
        }
    };
    private OnClickListener onNonAvailableArmyButtonClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = (Integer) v.getTag();
            String productId = null;
            if (index == ArmiesData.CHAOS.ordinal()) {
                productId = "com.glevel.warlords.chaos.army";
            } else if (index == ArmiesData.DWARF.ordinal()) {
                productId = "com.glevel.warlords.dwarf.army";
            }
            mInAppBillingHelper.purchaseItem(productId);
            ((CustomRadioButton) v).setChecked(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mInAppBillingHelper = new InAppBillingHelper(getActivity(), mBillingServiceConnectionCallback);

        Bundle args = getArguments();
        mGameType = args.getInt(ARGUMENT_GAME_TYPE);

        if (mGameType == MULTIPLAYER_GAME_TYPE) {
            mIsHost = args.getBoolean(ARGUMENT_IS_HOST);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null)
            return;

        // set the animations to use on showing and hiding the dialog
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_new_game, container, false);

        // init armies chooser
        mRadioGroupArmy = (GridLayout) view.findViewById(R.id.radioGroupArmy);
        for (int n = 0; n < mRadioGroupArmy.getChildCount(); n++) {
            if (n == ArmiesData.HUMAN.ordinal()) {
                ((CustomRadioButton) mRadioGroupArmy.getChildAt(n)).setChecked(true);
            }
            if (n <= ArmiesData.UNDEAD.ordinal()) {
                mRadioGroupArmy.getChildAt(n).setOnClickListener(onAvailableArmyButtonClicked);
            }
            mRadioGroupArmy.getChildAt(n).setTag(n);
        }

        mRadioGroupNbPlayers = (RadioGroup) view.findViewById(R.id.radioGroupNbPlayers);
        if (mIsHost) {
            // init nb players chooser
            for (int nbPlayers : GameUtils.NB_PLAYERS_IN_GAME) {
                addNbPlayersRadioButton(nbPlayers);
            }
            // checks first radio button
            ((CompoundButton) mRadioGroupNbPlayers.getChildAt(2)).setChecked(true);
        } else {
            mRadioGroupNbPlayers.setVisibility(View.GONE);
            view.findViewById(R.id.titleNbPlayers).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.okButton)).setText(R.string.go);
        }

        // cancel button
        view.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.playSound(getActivity().getApplicationContext(), R.raw.main_button);
                dismiss();
            }
        });

        // ok button
        view.findViewById(R.id.okButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.playSound(getActivity().getApplicationContext(), R.raw.main_button);
                createGame();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == InAppBillingHelper.BILLING_REQUEST_CODE) {
            if (resultCode == 0) {
                updateAvailableArmies();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInAppBillingHelper.onDestroy();
    }

    private void addNbPlayersRadioButton(int nbPlayers) {
        CustomRadioButton radioBtn = (CustomRadioButton) getActivity().getLayoutInflater().inflate(
                R.layout.radio_nb_players, null);
        radioBtn.setId(nbPlayers);
        radioBtn.setText("" + nbPlayers);
        radioBtn.setTypeface(MyApplication.FONTS.text);
        LayoutParams params = new LayoutParams(getActivity(), null);
        params.setMargins(0, 0, 40, 0);
        radioBtn.setLayoutParams(params);
        mRadioGroupNbPlayers.addView(radioBtn);
    }

    public void updateAvailableArmies() {
        // get available armies
        List<Integer> lstAvailableArmies = mInAppBillingHelper
                .getAvailableArmies(getActivity().getApplicationContext());
        for (int n = 0; n < mRadioGroupArmy.getChildCount(); n++) {
            boolean isArmyAvailable = lstAvailableArmies.indexOf(n) >= 0;
            mRadioGroupArmy.getChildAt(n).setOnClickListener(
                    isArmyAvailable ? onAvailableArmyButtonClicked : onNonAvailableArmyButtonClicked);
            if (!isArmyAvailable) {
                ((CustomRadioButton) mRadioGroupArmy.getChildAt(n))
                        .setBackgroundResource(R.drawable.bg_radio_btn_disabled);
            }
        }
    }

    private void createGame() {
        if (mGameType == SOLO_GAME_TYPE) {
            Intent intent = new Intent(getActivity(), GameActivity.class);
            Bundle extras = new Bundle();
            extras.putInt("my_army", mSelectedArmy);
            extras.putInt("nb_players", mRadioGroupNbPlayers.getCheckedRadioButtonId());
            intent.putExtras(extras);
            startActivity(intent);
            getActivity().finish();
        } else if (mGameType == MULTIPLAYER_GAME_TYPE) {
            ((MultiplayerActivity) getActivity()).onArmyChosen(mSelectedArmy,
                    mIsHost ? mRadioGroupNbPlayers.getCheckedRadioButtonId() : -1);
            dismiss();
        }
    }

}
