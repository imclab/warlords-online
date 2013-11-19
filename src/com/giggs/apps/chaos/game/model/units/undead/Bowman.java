package com.giggs.apps.chaos.game.model.units.undead;

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
        super(R.string.undead_bowman, R.drawable.undead_bowman_image, "undead_bowman.png", ArmiesData.UNDEAD,
                armyIndex, 70, 600, true, WeaponType.piercing, ArmorType.light, 55, 2);
    }

    @Override
    public int getDamage(Unit target) {
        // terror !
        int damage = super.getDamage(target);
        if (target.getArmy() != ArmiesData.UNDEAD) {
            target.updateMorale(damage / 10);
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
