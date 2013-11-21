package com.giggs.apps.chaos.game.model.units.dwarf;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class RuneMaster extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public RuneMaster(int armyIndex) {
        super(R.string.dwarf_rune_master, R.drawable.dwarf_rune_master_image, "dwarf_rune_master.png",
                ArmiesData.DWARF, armyIndex, 130, 400, true, WeaponType.magic, ArmorType.light, 90, 4);
    }

}
