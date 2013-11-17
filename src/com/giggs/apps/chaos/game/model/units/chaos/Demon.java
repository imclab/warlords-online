package com.giggs.apps.chaos.game.model.units.chaos;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Demon extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Demon(int armyIndex) {
        super(R.string.chaos_demons, R.drawable.chaos_demon_image, "chaos_demon.png", ArmiesData.CHAOS, armyIndex, 100, 900, false,
                WeaponType.normal, ArmorType.heavy, 90, 10);
    }
}
