package com.giggs.apps.chaos.game.model.units.orc;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Ogre extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Ogre(int armyIndex) {
        super(R.string.orcs_ogre, R.drawable.un_bowmen, "orcs_ogre.png", ArmiesData.ORCS, armyIndex, 150, 600, true,
                WeaponType.piercing, ArmorType.medium, 150, 8);
    }
}
