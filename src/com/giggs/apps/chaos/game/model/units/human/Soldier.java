package com.giggs.apps.chaos.game.model.units.human;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Soldier extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Soldier(int armyIndex) {
        super(R.string.human_soldier, R.drawable.human_soldier_image, "human_soldier.png", ArmiesData.HUMAN, armyIndex,
                50, 900, false, WeaponType.normal, ArmorType.medium, 40, 4);
    }

}
