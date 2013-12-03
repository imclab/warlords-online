package com.giggs.apps.chaos.game.model.units.dwarf;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class GiantKiller extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public GiantKiller(int armyIndex) {
        super(R.string.dwarf_giant_killer, R.drawable.dwarf_giant_killer_image, "dwarf_giant_killer.png",
                ArmiesData.DWARF, armyIndex, 85, 500, false, WeaponType.normal, ArmorType.unarmored, 110, 2);
    }

    @Override
    public int getDamage(Unit target) {
        // beast killer !
        int damage = super.getDamage(target);
        if (target.getAttack() > 100) {
            damage *= 1.5;
        }
        return damage;
    }

}
