package com.giggs.apps.chaos.game.model.units.chaos;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Wizard extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Wizard(int armyIndex) {
        super(R.string.chaos_wizards, R.drawable.chaos_wizard_image, "chaos_wizard.png", ArmiesData.CHAOS, armyIndex,
                130, 500, true, WeaponType.magic, ArmorType.unarmored, 70, 2);
    }

    @Override
    public void initTurn(Map map) {
        // despair atmosphere
        for (Tile t : MapLogic.getAdjacentTiles(map, tilePosition, 1, false)) {
            if (t.isEnemyOnIt(armyIndex)) {
                for (Unit unit : t.getContent()) {
                    unit.updateHealth(-30);
                }
            }
        }
        super.initTurn(map);
    }
}
