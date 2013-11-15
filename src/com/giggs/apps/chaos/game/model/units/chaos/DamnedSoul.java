package com.giggs.apps.chaos.game.model.units.chaos;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class DamnedSoul extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public DamnedSoul(int armyIndex) {
        super(R.string.chaos_damned_soul, R.drawable.un_bowmen, "chaos_damned_soul.png", ArmiesData.CHAOS, armyIndex,
                60, 600, true, WeaponType.piercing, ArmorType.light, 60, 2);
    }
}
