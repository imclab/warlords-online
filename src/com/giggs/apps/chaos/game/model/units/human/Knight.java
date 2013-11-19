package com.giggs.apps.chaos.game.model.units.human;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Knight extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = 1018681662969655381L;

    public Knight(int armyIndex) {
        super(R.string.human_knight, R.drawable.human_knight_image, "human_knight.png", ArmiesData.HUMAN, armyIndex,
                100, 900, false, WeaponType.normal, ArmorType.heavy, 90, 10);
    }

    @Override
    public boolean canMove(Tile tile) {
        // can't go on mountain tiles !
        if (tile.getTerrain() == TerrainData.mountain) {
            return false;
        }
        return super.canMove(tile);
    }

}
