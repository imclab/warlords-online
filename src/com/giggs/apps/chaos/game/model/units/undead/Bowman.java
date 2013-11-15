package com.giggs.apps.chaos.game.model.units.undead;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Bowman extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Bowman(int armyIndex) {
        super(R.string.undead_bowman, R.drawable.un_skbowmen, "undead_bowman.png", ArmiesData.UNDEAD, armyIndex, 70,
                600, true, WeaponType.piercing, ArmorType.light, 55, 2);
    }
}
