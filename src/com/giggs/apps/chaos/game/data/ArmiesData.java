package com.giggs.apps.chaos.game.data;

import com.giggs.apps.chaos.R;

public enum ArmiesData {
	HUMAN(R.string.human, R.drawable.un_infantry), ORCS(R.string.orcsArmy, R.drawable.un_orc), UNDEAD(R.string.undead,
	        R.drawable.un_skeleton), CHAOS(R.string.chaos, R.drawable.un_chaos_wizards);

	private final int name;
	private final int flagImage;

	ArmiesData(int name, int flagImage) {
		this.name = name;
		this.flagImage = flagImage;
	}

	public int getName() {
		return name;
	}

	public int getFlagImage() {
		return flagImage;
	}

}
