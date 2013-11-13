package com.giggs.apps.chaos.game.logic;

import java.util.List;

import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GameUtils.Direction;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.GameElement;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.BuyOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.orders.Order;
import com.giggs.apps.chaos.game.model.units.Unit;

public class GameLogic {

    public static enum WeaponType {
        normal, piercing, siege, magic;
    }

    public static enum ArmorType {
        unarmored, light, medium, heavy, building;
    }

    public static final float[][] WEAPONS_EFFICIENCY = { { 1, 1, 1.5f, 1, 0.4f }, { 1.5f, 1.5f, 0.5f, 0.5f, 0.2f },
            { 1.5f, 1, 0.5f, 0.5f, 2 }, { 1.5f, 1.5f, 1.5f, 1.5f, 0.1f } };

    public static void runTurn(GameActivity gameActivity, Battle battle) {
        // update turn count
        battle.setTurnCount(battle.getTurnCount() + 1);

        // update weather
        updateWeather(battle);

        // process buy orders
        for (Player player : battle.getPlayers()) {
            for (Order order : player.getLstTurnOrders()) {
                if (order instanceof BuyOrder) {
                    BuyOrder buyOrder = (BuyOrder) order;
                    // check if there is enough space on the tile
                    boolean isEnoughSpace = false;
                    Tile tile = buyOrder.getTile();
                    if (tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                        // slots are available
                        isEnoughSpace = true;
                    } else {
                        // check if one unit is leaving the tile
                        for (Unit u : tile.getContent()) {
                            if (u.getOrder() != null && u.getOrder() instanceof MoveOrder) {
                                // no slots but at least one unit is leaving
                                isEnoughSpace = true;
                                break;
                            }
                        }
                    }
                    if (isEnoughSpace) {
                        // buy unit
                        player.setGold(player.getGold() - buyOrder.getUnit().getPrice());

                        // add unit
                        tile.getContent().add(buyOrder.getUnit());
                        gameActivity.addUnitToScene(unit)
                    }
                }
            }
        }

        // process units orders

        // process battles

        // update places' owners

        // gather resources

        // supply units

        // check if some players are defeated

        // update fogs of war
        updateFogsOfWar(battle, 0);

        // init players for new turn
        for (Player player : battle.getPlayers()) {
            player.initNewTurn();
        }
    }

    private static void updateWeather(Battle battle) {
        if (battle.isWinter() && Math.random() < 0.15) {
            // summer !
            battle.setWinter(false);
        } else if (!battle.isWinter() && Math.random() < 0.1) {
            // winter !
            battle.setWinter(true);
        }
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
                    for (Tile adjacentTile : MapLogic.getAdjacentTiles(map, tile, vision, true)) {
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

    public static List<Tile> getMoveOptions(Battle battle, GameElement element) {
        return MapLogic.getAdjacentTiles(battle.getMap(), element.getTilePosition(), 1, false);
    }

    public static Direction getDirectionFromMoveOrder(MoveOrder moveOrder) {
        if (moveOrder.getDestination().getX() > moveOrder.getOrigin().getX()) {
            return Direction.east;
        } else if (moveOrder.getDestination().getX() < moveOrder.getOrigin().getX()) {
            return Direction.west;
        } else if (moveOrder.getDestination().getY() < moveOrder.getOrigin().getY()) {
            return Direction.north;
        } else {
            return Direction.south;
        }
    }

}
