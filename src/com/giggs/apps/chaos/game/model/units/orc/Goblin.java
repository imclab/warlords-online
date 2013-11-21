package com.giggs.apps.chaos.game.model.units.orc;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Goblin extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Goblin(int armyIndex) {
        super(R.string.orcs_goblin, R.drawable.orcs_goblin_image, "orcs_goblin.png", ArmiesData.ORCS, armyIndex, 30,
                600, false, WeaponType.piercing, ArmorType.light, 30, 2);
    }

}
