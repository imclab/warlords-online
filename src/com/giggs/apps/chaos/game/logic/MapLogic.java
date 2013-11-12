package com.giggs.apps.chaos.game.logic;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;

public class MapLogic {

    public static List<Tile> getAdjacentTiles(Map map, Tile centerTile, int step) {
        List<Tile> adjacentTiles = new ArrayList<Tile>();

        for (int y = centerTile.getY() - step; y < centerTile.getY() + step + 1; y++) {
            for (int x = centerTile.getX() - step; x < centerTile.getX() + step + 1; x++) {
                if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()
                        && (x != centerTile.getX() || y != centerTile.getY())) {
                    Tile t = map.getTiles()[y][x];
                    adjacentTiles.add(t);
                }
            }
        }

        return adjacentTiles;
    }

    public static int getDistance(Tile tile1, Tile tile2) {
        return Math.abs(tile1.getX() - tile2.getX()) + Math.abs(tile1.getY() - tile2.getY());
    }
}
