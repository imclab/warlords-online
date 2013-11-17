package com.giggs.apps.chaos.game.model.units.human;

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
        super(R.string.human_bowman, R.drawable.human_bowman_image, "human_bowman.png", ArmiesData.HUMAN, armyIndex, 60, 600, true,
                WeaponType.piercing, ArmorType.light, 60, 2);
    }
}
