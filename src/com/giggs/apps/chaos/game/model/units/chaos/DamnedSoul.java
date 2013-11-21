package com.giggs.apps.chaos.game.model.units.chaos;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class DamnedSoul extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public DamnedSoul(int armyIndex) {
        super(R.string.chaos_damned_soul, R.drawable.chaos_damned_soul_image, "chaos_damned_soul.png",
                ArmiesData.CHAOS, armyIndex, 160, 600, false, WeaponType.piercing, ArmorType.light, 80, 2);
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
    public int getDamage(Unit target) {
        int damage = super.getDamage(target);
        // strike attacks !
        for (Unit unit : tilePosition.getContent()) {
            if (unit.getArmyIndex() != armyIndex) {
                unit.setHealth(-damage);
                unit.setMorale(damage / 10);
            }
        }
        return damage;
    }

}
