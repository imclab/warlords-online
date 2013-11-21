package com.giggs.apps.chaos.game.model.units.human;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Monk extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Monk(int armyIndex) {
        super(R.string.human_monk, R.drawable.human_monk_image, "human_monk.png", ArmiesData.HUMAN, armyIndex, 80, 500,
                false, WeaponType.magic, ArmorType.unarmored, 40, 3);
    }

    @Override
    public void initTurn(Map map) {
        // monks are healing closed units !
        for (Unit unit : tilePosition.getContent()) {
            unit.updateHealth(200);
        }
        super.initTurn(map);
    }

}
