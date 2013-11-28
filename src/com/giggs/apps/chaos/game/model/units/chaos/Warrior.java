package com.giggs.apps.chaos.game.model.units.chaos;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Warrior extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Warrior(int armyIndex) {
        super(R.string.chaos_warrior, R.drawable.chaos_warrior_image, "chaos_warrior.png", ArmiesData.CHAOS, armyIndex,
                70, 600, false, WeaponType.normal, ArmorType.heavy, 70, 6);
    }

}
