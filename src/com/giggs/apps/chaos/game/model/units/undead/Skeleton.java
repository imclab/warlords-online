package com.giggs.apps.chaos.game.model.units.undead;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Skeleton extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Skeleton(int armyIndex) {
        super(R.string.undead_skeleton, R.drawable.un_skeleton, "undead_skeleton.png", ArmiesData.UNDEAD, armyIndex,
                30, 900, false, WeaponType.normal, ArmorType.light, 35, 3);
    }
}
