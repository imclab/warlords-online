package com.giggs.apps.chaos.game.model.units.undead;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Zombie extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Zombie(int armyIndex) {
        super(R.string.undead_zombie, R.drawable.undead_zombie_image, "undead_zombie.png", ArmiesData.UNDEAD,
                armyIndex, 90, 900, false, WeaponType.normal, ArmorType.medium, 60, 20);
    }

    @Override
    public int getDamage(Unit target) {
        // terror !
        int damage = super.getDamage(target);
        if (target.getArmy() != ArmiesData.UNDEAD) {
            target.updateMorale(damage / 10);

            // vampiric effect
            updateHealth(damage);
        }

        return damage;
    }

    @Override
    public void applyDamage(int damage) {
        // goes back to dust when lack of magic
        super.applyDamage(damage);
        if (morale < 40) {
            updateHealth(-(40 - morale) * 10);
        }
    }

}
