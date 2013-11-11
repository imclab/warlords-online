package com.giggs.apps.chaos.activities.fragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;

import com.giggs.apps.chaos.MyApplication;
import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.views.CustomRadioButton;

public class CreateGameDialog extends DialogFragment {

	private RadioGroup mRadioGroupArmy;
	private RadioGroup mRadioGroupNbPlayers;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen); // remove
		                                                                             // title
		                                                                             // from
		                                                                             // dialog
		                                                                             // fragment
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
		mRadioGroupArmy = (RadioGroup) view.findViewById(R.id.radioGroupArmy);
		for (ArmiesData army : ArmiesData.values()) {
			addArmyRadioButton(army);
		}
		// checks first radio button
		((CompoundButton) mRadioGroupArmy.getChildAt(0)).setChecked(true);

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

	/**
	 * Add a radio button for each available army.
	 * 
	 * @param army
	 */
	private void addArmyRadioButton(ArmiesData army) {
		CustomRadioButton radioBtn = (CustomRadioButton) getActivity().getLayoutInflater().inflate(R.layout.radio_army,
		        null);
		radioBtn.setId(army.ordinal());
		radioBtn.setText(army.getName());
		radioBtn.setCompoundDrawablesWithIntrinsicBounds(army.getFlagImage(), 0, 0, 0);
		LayoutParams params = new LayoutParams(getActivity(), null);
		params.setMargins(0, 0, 30, 0);
		radioBtn.setLayoutParams(params);
		mRadioGroupArmy.addView(radioBtn);
	}

	private void addNbPlayersRadioButton(int nbPlayers) {
		CustomRadioButton radioBtn = (CustomRadioButton) getActivity().getLayoutInflater().inflate(R.layout.radio_army,
		        null);
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
		extras.putInt("my_army", mRadioGroupArmy.getCheckedRadioButtonId());
		extras.putInt("nb_players", mRadioGroupNbPlayers.getCheckedRadioButtonId());
		intent.putExtras(extras);
		startActivity(intent);
	}

}
