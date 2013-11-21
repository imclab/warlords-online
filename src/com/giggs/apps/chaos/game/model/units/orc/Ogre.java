package com.giggs.apps.chaos.game.model.units.orc;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Ogre extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Ogre(int armyIndex) {
        super(R.string.orcs_ogre, R.drawable.orcs_ogre_image, "orcs_ogre.png", ArmiesData.ORCS, armyIndex, 120, 600,
                true, WeaponType.normal, ArmorType.medium, 160, 8);
    }

    @Override
    public boolean canMove(Tile tile) {
        // can't go on mountain tiles !
        if (tile.getTerrain() == TerrainData.mountain) {
            return false;
        }
        return super.canMove(tile);
    }

    @Override
    public void initTurn(Map map) {
        // ogres are eating allied units close to them !
        for (Unit unit : tilePosition.getContent()) {
            if (this != unit) {
                unit.updateHealth(-50);
            }
        }
        super.initTurn(map);
    }

    @Override
    public int getDamage(Unit target) {
        int damage = super.getDamage(target);
        // critical hit !
        if (Math.random() < 0.15) {
            damage *= 2;
        }
        return damage;
    }

}
