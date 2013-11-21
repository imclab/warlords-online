package com.giggs.apps.chaos.game.model.units.orc;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Troll extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Troll(int armyIndex) {
        super(R.string.orcs_troll, R.drawable.orcs_troll_image, "orcs_troll.png", ArmiesData.ORCS, armyIndex, 110, 800,
                false, WeaponType.normal, ArmorType.unarmored, 100, 4);
    }

    @Override
    public void initTurn(Map map) {
        // trolls regeneration
        updateHealth(150);
        super.initTurn(map);
    }

}
