package com.giggs.apps.chaos.game.model.units.human;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Monk extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Monk(int armyIndex) {
        super(R.string.human_monk, R.drawable.un_knight, "human_monk.png", ArmiesData.HUMAN, armyIndex, 80, 500, false,
                WeaponType.magic, ArmorType.unarmored, 40, 3);
    }
}
