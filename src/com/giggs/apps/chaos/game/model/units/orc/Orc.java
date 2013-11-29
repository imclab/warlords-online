package com.giggs.apps.chaos.game.model.units.orc;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Orc extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Orc(int armyIndex) {
        super(R.string.orcs_orc, R.drawable.orcs_orc_image, "orcs_orc.png", ArmiesData.ORCS, armyIndex, 70, 900, false,
                WeaponType.normal, ArmorType.medium, 60, 6);
    }

}
