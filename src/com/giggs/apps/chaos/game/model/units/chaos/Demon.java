package com.giggs.apps.chaos.game.model.units.chaos;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Demon extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Demon(int armyIndex) {
        super(R.string.chaos_demons, R.drawable.chaos_demon_image, "chaos_demon.png", ArmiesData.CHAOS, armyIndex, 110,
                800, false, WeaponType.normal, ArmorType.heavy, 100, 10);
    }

    @Override
    public int getDamage(Unit target) {
        int damage = super.getDamage(target);
        double random = GameLogic.random.nextDouble();
        // critical hit !
        if (random < 0.15) {
            damage *= 2;
        }
        return damage;
    }

    @Override
    public void initTurn(Map map) {
        // demons regeneration
        updateHealth(100);
        super.initTurn(map);
    }

}
