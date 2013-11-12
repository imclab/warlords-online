package com.giggs.apps.chaos.game.logic;

import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;

public class GameLogic {

    public static void runTurn(Battle battle) {
        updateFogsOfWar(battle, 0);
    }

    public static void updateFogsOfWar(Battle battle, int myArmyIndex) {
        Map map = battle.getMap();
        // hide all tiles
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                map.getTiles()[y][x].setVisible(myArmyIndex, false);
            }
        }

        // show tiles
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = map.getTiles()[y][x];
                // do I own this tile ?
                if (tile.getOwner() == myArmyIndex || tile.getContent().size() > 0
                        && tile.getContent().get(0).getArmyIndex() == myArmyIndex) {
                    tile.setVisible(myArmyIndex, true);

                    int vision = 1;// control zone vision
                    if (tile.getContent().size() > 0) {
                        // get unit's vision
                        vision = tile.getContent().get(0).getVision();
                    }

                    // make adjacent tiles visible
                    for (Tile adjacentTile : MapLogic.getAdjacentTiles(map, tile, vision)) {
                        if (!adjacentTile.isVisible()) {

                            // add forest exception : hidden in diagonal
                            if (adjacentTile.getTerrain() == TerrainData.forest) {
                                int distanceBetween = MapLogic.getDistance(tile, adjacentTile);

                                if (vision == 1 && distanceBetween == 2 || vision == 2 && distanceBetween == 4) {
                                    continue;
                                }
                            }

                            adjacentTile.setVisible(myArmyIndex, true);
                        }
                    }
                }
            }
        }
    }
}
