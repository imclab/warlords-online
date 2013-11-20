package com.giggs.apps.chaos.game;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static Battle createSoloGame(int nbPlayers, int myArmy, int myArmyIndex, int[] lstArmies) {
        Battle battle = new Battle();

        // init players
        List<Player> lstPlayers = new ArrayList<Player>();
        List<String> lstAINames = new ArrayList<String>(Arrays.asList(GameUtils.AI_NAMES));
        for (int n = 0; n < nbPlayers; n++) {
            int army;
            if (lstArmies == null) {
                army = n != myArmyIndex ? (int) (Math.random() * ArmiesData.values().length) : myArmy;
            } else {
                army = lstArmies[n];
            }
            int nameIndex = (int) (Math.random() * lstAINames.size());
            String playerName = n == 0 ? "Me" : lstAINames.get(nameIndex);
            lstAINames.remove(nameIndex);
            Player p = new Player("" + n, playerName, ArmiesData.values()[army], n, n != myArmyIndex);
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
        buildRandomMap(map, tiles);
        map.setTiles(tiles);

        // setup player zones
        addPlayersZones(map, lstPlayers);

        // build zones list
        for (int x = 0; x < tiles[0].length; x++) {
            for (int y = 0; y < tiles.length; y++) {
                Tile t = tiles[y][x];
                if (t.getTerrain() == TerrainData.farm) {
                    map.getFarms().add(t);
                } else if (t.getTerrain() == TerrainData.fort) {
                    map.getForts().add(t);
                } else if (t.getTerrain() == TerrainData.castle) {
                    map.getCastles().add(t);
                }
            }
        }

        return map;
    }

    private static void buildRandomMap(Map map, Tile[][] tiles) {
        int terrainQuantitySum = 0;
        for (TerrainData terrain : TerrainData.values()) {
            terrainQuantitySum += terrain.getQuantityFactor();
        }

        for (int x = 0; x < tiles[0].length; x++) {
            for (int y = 0; y < tiles.length; y++) {
                Tile newTile = new Tile(x, y, getRandomTerrain(terrainQuantitySum));
                tiles[y][x] = newTile;
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
            addPlayerZoneToMap(map, map.getHeight() - 2, map.getWidth() - 2, lstPlayersCopy);
            break;
        case 3:
            addPlayerZoneToMap(map, 0, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, map.getHeight() - 2, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 2, map.getWidth() - 2, lstPlayersCopy);
            break;
        case 4:
            addPlayerZoneToMap(map, 0, 3, lstPlayersCopy);
            addPlayerZoneToMap(map, map.getHeight() - 2, 3, lstPlayersCopy);
            addPlayerZoneToMap(map, 3, 0, lstPlayersCopy);
            addPlayerZoneToMap(map, 3, map.getWidth() - 2, lstPlayersCopy);
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
            return 6;
        case 3:
            return 7;
        case 4:
            return 8;
        case 8:
            return 11;
        }
        return 0;
    }

}
