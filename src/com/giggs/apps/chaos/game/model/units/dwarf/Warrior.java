package com.giggs.apps.chaos.game.model.units.dwarf;

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
        super(R.string.dwarf_warrior, R.drawable.dwarf_warrior_image, "dwarf_warrior.png", ArmiesData.DWARF, armyIndex,
                50, 700, false, WeaponType.normal, ArmorType.heavy, 50, 8);
    }

}
