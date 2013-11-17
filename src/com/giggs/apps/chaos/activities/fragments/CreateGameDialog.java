package com.giggs.apps.chaos.activities.fragments;

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
import android.widget.RadioGroup.LayoutParams;

import com.giggs.apps.chaos.MyApplication;
import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.views.CustomRadioButton;

public class CreateGameDialog extends DialogFragment {

	private GridLayout mRadioGroupArmy;
	private RadioGroup mRadioGroupNbPlayers;
	private int selectedArmy = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen); // remove
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
			if (n == 0) {
				((CustomRadioButton) mRadioGroupArmy.getChildAt(n)).setChecked(true);
			}
			mRadioGroupArmy.getChildAt(n).setTag(n);
			mRadioGroupArmy.getChildAt(n).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					selectedArmy = (Integer) v.getTag();
					for (int n = 0; n < mRadioGroupArmy.getChildCount(); n++) {
						CustomRadioButton view = (CustomRadioButton) mRadioGroupArmy.getChildAt(n);
						view.setChecked(false);
					}
					((CustomRadioButton) v).setChecked(true);
				}
			});
		}

		// init nb players chooser
		mRadioGroupNbPlayers = (RadioGroup) view.findViewById(R.id.radioGroupNbPlayers);
		for (int nbPlayers : GameUtils.NB_PLAYERS_IN_GAME) {
			addNbPlayersRadioButton(nbPlayers);
		}
		// checks first radio button
		((CompoundButton) mRadioGroupNbPlayers.getChildAt(2)).setChecked(true);

		// cancel button
		view.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		// ok button
		view.findViewById(R.id.okButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createGame();
			}
		});

		return view;
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

	private void createGame() {
		Intent intent = new Intent(getActivity(), GameActivity.class);
		Bundle extras = new Bundle();
		extras.putInt("my_army", selectedArmy);
		extras.putInt("nb_players", mRadioGroupNbPlayers.getCheckedRadioButtonId());
		intent.putExtras(extras);
		startActivity(intent);
		getActivity().finish();
	}

}
