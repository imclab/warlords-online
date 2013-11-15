package com.giggs.apps.chaos.game.model.units.orc;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Troll extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Troll(int armyIndex) {
        super(R.string.orcs_troll, R.drawable.un_troll, "orcs_troll.png", ArmiesData.ORCS, armyIndex, 120, 800, false,
                WeaponType.normal, ArmorType.unarmored, 120, 6);
    }
}
