package com.giggs.apps.chaos.game.model.units.dwarf;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class CrossBowman extends Unit {

	/**
     * 
     */
	private static final long serialVersionUID = 1018681662969655381L;

	public CrossBowman(int armyIndex) {
		super(R.string.dwarf_crossbowman, R.drawable.dwarf_crossbowman_image, "dwarf_crossbowman.png",
		        ArmiesData.DWARF, armyIndex, 75, 600, true, WeaponType.piercing, ArmorType.heavy, 60, 8);
	}
}
