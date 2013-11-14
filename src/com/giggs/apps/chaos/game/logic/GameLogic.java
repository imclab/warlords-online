package com.giggs.apps.chaos.game.logic;

import java.util.List;

import com.giggs.apps.chaos.R;
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
        if (battle.isWinter() && Math.random() < 0.2) {
            // summer !
            updateWeather(battle, false);
        } else if (!battle.isWinter() && Math.random() < 0.1) {
            // winter !
            updateWeather(battle, true);
        }

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
                        // add unit
                        buyOrder.getUnit().setTilePosition(tile);
                        gameActivity.addUnitToScene(buyOrder.getUnit());
                    }
                } else if (order instanceof MoveOrder) {
                    MoveOrder moveOrder = (MoveOrder) order;
                    moveOrder.getUnit().updateTilePosition(moveOrder.getDestination());
                }
            }
        }

        // process battles
        // TODO

        // update places' owners
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                if (tile.getContent().size() > 0 && tile.getContent().get(0).getArmyIndex() != tile.getOwner()) {
                    tile.updateTileOwner(battle.getPlayers().get(0).getArmyIndex(), tile.getContent().get(0)
                            .getArmyIndex());
                }
            }
        }

        // gather resources + supply units + check if players are defeated
        // set all players to defeated
        for (Player player : battle.getPlayers()) {
            player.setDefeated(true);
        }
        int nbPlayersInGame = 0;
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                // gather resources
                if (tile.getOwner() >= 0) {
                    Player player = battle.getPlayers().get(tile.getOwner());

                    player.updateGold(tile.getGoldAmountGathered());

                    // this player is still alive !
                    if (player.isDefeated()) {
                        player.setDefeated(false);
                        nbPlayersInGame++;
                    }
                }
                // supply units
                for (Unit unit : tile.getContent()) {
                    battle.getPlayers().get(unit.getArmyIndex())
                            .updateGold(-unit.getPrice() * unit.getHealth() / unit.getMaxHealth());

                    // reset unit order
                    unit.setOrder(null);
                }
            }
        }

        // check economic crisis !
        for (Player player : battle.getPlayers()) {
            if (player.getGold() < 0) {
                // player is in deficit
                for (int y = 0; y < battle.getMap().getHeight(); y++) {
                    for (int x = 0; x < battle.getMap().getWidth(); x++) {
                        Tile tile = battle.getMap().getTiles()[y][x];
                        if (tile.getContent().size() > 0
                                && tile.getContent().get(0).getArmyIndex() == player.getArmyIndex()) {
                            for (Unit unit : tile.getContent()) {
                                // units are not paid anymore, they lose morale
                                // !
                                unit.updateMorale(player.getGold() / 4);
                                if (unit.getMorale() < 40) {
                                    // units are deserting...
                                    boolean isDead = unit.updateHealth(-7 * (50 - unit.getMorale()));
                                    if (isDead) {
                                        gameActivity.removeUnit(unit);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // update fogs of war
        updateFogsOfWar(battle, 0);

        // dispatch units properly on tiles
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                MapLogic.dispatchUnitsOnTile(tile);
            }
        }

        // init players for new turn
        for (Player player : battle.getPlayers()) {
            player.initNewTurn();
        }

        // update my gold amount
        gameActivity.mGameGUI.updateGoldAmount(battle.getPlayers().get(0).getGold());

        // check if game is ended
        if (nbPlayersInGame < 2) {
            Player winner = null;
            for (Player player : battle.getPlayers()) {
                if (!player.isDefeated()) {
                    winner = player;
                }
            }
            // winner can be null if game is a draw
            gameActivity.endGame(winner);
            return;
        }

        // show new turn count
        gameActivity.mGameGUI.displayBigLabel(gameActivity.getString(R.string.turn_count, battle.getTurnCount()),
                R.color.white);
    }

    private static void updateWeather(Battle battle, boolean isWinter) {
        battle.setWinter(isWinter);
        for (Tile[] h : battle.getMap().getTiles()) {
            for (Tile t : h) {
                t.updateWeather(isWinter);
            }
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
