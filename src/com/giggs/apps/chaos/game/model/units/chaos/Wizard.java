package com.giggs.apps.chaos.game.model.units.chaos;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Wizard extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Wizard(int armyIndex) {
        super(R.string.chaos_wizards, R.drawable.chaos_wizard_image, "chaos_wizard.png", ArmiesData.CHAOS, armyIndex,
                110, 500, true, WeaponType.magic, ArmorType.unarmored, 80, 2);
    }

}
