package com.giggs.apps.chaos.game.model.units.undead;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Necromancer extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Necromancer(int armyIndex) {
        super(R.string.undead_necromancer, R.drawable.undead_necromancer_image, "undead_necromancer.png",
                ArmiesData.UNDEAD, armyIndex, 120, 500, true, WeaponType.magic, ArmorType.unarmored, 80, 2);
    }

    @Override
    public int getDamage(Unit target) {
        // terror !
        int damage = super.getDamage(target);
        if (target.getArmy() != ArmiesData.UNDEAD) {
            target.updateMorale(damage / 10);

            // cast undead
            for (Unit unit : tilePosition.getContent()) {
                if (unit.getArmyIndex() == armyIndex) {
                    unit.updateHealth(damage);
                    unit.updateMorale(damage / 10);
                }
            }
        }

        return damage;
    }
    
}
