package com.giggs.apps.chaos.game;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class GameCreation {

    public static Battle createSoloGame(int nbPlayers, int myArmy) {
        Battle battle = new Battle();

        // init players
        List<Player> lstPlayers = new ArrayList<Player>();
        for (int n = 0; n < nbPlayers; n++) {
            int army = n > 0 ? (int) (Math.random() * ArmiesData.values().length) : myArmy;
            Player p = new Player("" + n, "Bobby", ArmiesData.values()[0], n, n > 0);
            lstPlayers.add(p);
        }
        battle.setPlayers(lstPlayers);

        // create random map
        battle.setMap(createRandomMap(lstPlayers));

        return battle;
    }

    private static Map createRandomMap(List<Player> lstPlayers) {
        Map map = new Map();
        int mapSize = getMapSize(lstPlayers.size());
        Tile[][] tiles = new Tile[mapSize][mapSize];

        // create terrain
        buildRandomMap(tiles);
        map.setTiles(tiles);

        // setup player zones
        addPlayersZones(map, lstPlayers);

        return map;
    }

    private static void buildRandomMap(Tile[][] tiles) {
        int terrainQuantitySum = 0;
        for (TerrainData terrain : TerrainData.values()) {
            terrainQuantitySum += terrain.getQuantityFactor();
        }

        for (int x = 0; x < tiles[0].length; x++) {
            for (int y = 0; y < tiles.length; y++) {
                tiles[y][x] = new Tile(x, y, getRandomTerrain(terrainQuantitySum));
            }
        }
    }

    private static TerrainData getRandomTerrain(int terrainQuantitySum) {
        double random = Math.random();
        double threshold = 0;
        for (int n = 0; n < TerrainData.values().length; n++) {
            threshold += (double) TerrainData.values()[n].getQuantityFactor() / terrainQuantitySum;
            if (random < threshold) {
                return TerrainData.values()[n];
            }
        }
        return TerrainData.grass;
    }

    private static void addPlayersZones(Map map, List<Player> lstPlayers) {
        List<Player> lstPlayersCopy = new ArrayList<Player>(lstPlayers);
        switch (lstPlayers.size()) {
        case 2:
            addPlayerZoneToMap(map, 0, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 3, 3, lstPlayersCopy);
            break;
        case 3:
            addPlayerZoneToMap(map, 0, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 4, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 2, 4, lstPlayersCopy);
            break;
        case 4:
            addPlayerZoneToMap(map, 0, 3, lstPlayersCopy);
            addPlayerZoneToMap(map, 6, 3, lstPlayersCopy);
            addPlayerZoneToMap(map, 3, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 3, 6, lstPlayersCopy);
            break;
        case 8:
            addPlayerZoneToMap(map, 0, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 4, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 8, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 0, 4, lstPlayersCopy);
            addPlayerZoneToMap(map, 8, 4, lstPlayersCopy);
            addPlayerZoneToMap(map, 0, 8, lstPlayersCopy);
            addPlayerZoneToMap(map, 4, 8, lstPlayersCopy);
            addPlayerZoneToMap(map, 8, 8, lstPlayersCopy);
            break;
        }
    }

    private static void addPlayerZoneToMap(Map map, int i, int j, List<Player> lstPlayers) {
        // add a farm
        map.getTiles()[i + (int) (Math.random() * 2)][j + (int) (Math.random() * 2)].setTerrain(TerrainData.farm);

        // add a castle
        Tile castleTile = null;
        do {
            castleTile = map.getTiles()[i + (int) (Math.random() * 2)][j + (int) (Math.random() * 2)];
        } while (castleTile.getTerrain() == TerrainData.farm);
        castleTile.setTerrain(TerrainData.castle);

        // add initial units
        Player player = lstPlayers.get((int) (Math.random() * lstPlayers.size()));
        List<Unit> initialUnits = UnitsData.getInitialUnits(player.getArmy(), player.getArmyIndex());
        for (Unit u : initialUnits) {
            u.setTilePosition(castleTile);
        }
        castleTile.setOwner(player.getArmyIndex());
        lstPlayers.remove(player);
    }

    private static int getMapSize(int nbPlayers) {
        switch (nbPlayers) {
        case 2:
            return 5;
        case 3:
            return 7;
        case 4:
            return 8;
        case 8:
            return 12;
        }
        return 0;
    }

}
