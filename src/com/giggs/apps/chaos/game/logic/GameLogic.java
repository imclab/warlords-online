package com.giggs.apps.chaos.game.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;

import com.giggs.apps.chaos.game.AI;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GameUtils.Direction;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.GameElement;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.BuyOrder;
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.orders.Order;
import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.game.model.units.orc.Goblin;

@SuppressLint("UseSparseArrays")
public class GameLogic {

    public static enum WeaponType {
        normal, piercing, siege, magic;
    }

    public static enum ArmorType {
        unarmored, light, medium, heavy, building;
    }

    public static final float[][] WEAPONS_EFFICIENCY = { { 1, 1, 1.5f, 1, 0.4f }, { 1.5f, 1.5f, 0.5f, 0.5f, 0.2f },
            { 1.5f, 1, 0.5f, 0.5f, 2 }, { 1.5f, 1.5f, 1.5f, 1.5f, 0.1f } };

    public static final int SOLO_PLAYER_DEFEAT = -10;

    /**
     * 
     * @param battle
     * @return winner index
     */
    public static int runTurn(Battle battle) {

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

        // units are resting
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                for (Unit unit : tile.getContent()) {
                    unit.initTurn(battle.getMap());
                }
            }
        }

        // AI
        for (Player player : battle.getPlayers()) {
            if (player.isAI() && !player.isDefeated()) {
                AI.generateTurnOrders(battle, player);
            }
        }

        // process orders
        for (Player player : battle.getPlayers()) {
            List<Order> ordersCopy = new ArrayList<Order>(player.getLstTurnOrders());
            for (int n = 0; n < ordersCopy.size(); n++) {
                Order order = ordersCopy.get(n);
                if (order instanceof BuyOrder) {
                    BuyOrder buyOrder = (BuyOrder) order;
                    // check if there is enough space on the tile
                    Tile tile = buyOrder.getTile();
                    if (tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                        // add unit
                        buyOrder.getUnit().setTilePosition(tile);
                        battle.getUnitsToAdd().add(buyOrder.getUnit());
                        // game stats
                        player.getGameStats().incrementNbUnitsCreated(1);
                    }
                    tile.updateUnitProduction(null);
                } else if (order instanceof MoveOrder) {
                    MoveOrder moveOrder = (MoveOrder) order;
                    boolean canMove = true;
                    // check units crossing
                    double random = Math.random();
                    for (Unit u : moveOrder.getDestination().getContent()) {
                        if (u.getOrder() != null && u.getOrder() instanceof MoveOrder
                                && ((MoveOrder) u.getOrder()).getDestination() == moveOrder.getOrigin()) {
                            // resolve units crossing
                            if (random < 0.5) {
                                battle.getPlayers().get(u.getArmyIndex()).removeOrder(u.getOrder());
                                u.setOrder(null);
                            } else {
                                canMove = false;
                                for (Unit allyUnit : moveOrder.getOrigin().getContent()) {
                                    if (allyUnit.getOrder() != null
                                            && allyUnit.getOrder() instanceof MoveOrder
                                            && ((MoveOrder) allyUnit.getOrder()).getDestination() == moveOrder
                                                    .getDestination()) {
                                        player.removeOrder(allyUnit.getOrder());
                                        allyUnit.setOrder(null);
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if (canMove) {
                        moveOrder.getUnit().updateTilePosition(moveOrder.getDestination());
                    }
                }
            }
        }

        // process battles
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                if (tile.getContent().size() > 1) {
                    for (int n = 1; n < tile.getContent().size(); n++) {
                        if (tile.getContent().get(0).getArmyIndex() != tile.getContent().get(n).getArmyIndex()) {
                            processBattle(battle, tile);
                            break;
                        }
                    }
                }
            }
        }

        // check if over-numbered
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                if (tile.getContent().size() > GameUtils.MAX_UNITS_PER_TILE) {
                    List<Unit> unitsCopy = new ArrayList<Unit>(tile.getContent());
                    // move back units
                    for (int n = 0; n < unitsCopy.size(); n++) {
                        if (tile.getContent().size() > GameUtils.MAX_UNITS_PER_TILE) {
                            Unit u = unitsCopy.get(n);
                            if (u.getOrder() != null && u.getOrder() instanceof MoveOrder) {
                                MoveOrder m = (MoveOrder) u.getOrder();
                                if (u.canFleeHere(m.getOrigin())) {
                                    // unit goes back
                                    u.updateTilePosition(m.getOrigin());
                                } else {
                                    // unit has no more space, it is removed
                                    battle.getUnitsToRemove().add(u);
                                    u.getTilePosition().getContent().remove(u);
                                }
                            }
                        }
                    }
                    if (tile.getContent().size() > GameUtils.MAX_UNITS_PER_TILE) {
                        for (int n = 0; n < unitsCopy.size(); n++) {
                            if (tile.getContent().size() > GameUtils.MAX_UNITS_PER_TILE) {
                                Unit u = unitsCopy.get(n);
                                battle.getUnitsToRemove().add(u);
                                u.getTilePosition().getContent().remove(u);
                            }
                        }
                    }
                }
            }
        }

        // gather resources + supply units + check if players are defeated
        // set all players to defeated
        for (Player player : battle.getPlayers()) {
            player.setDefeated(true);
        }
        int nbPlayersInGame = 0;
        int[] goldsBeforeTurn = new int[battle.getPlayers().size()];
        for (Player player : battle.getPlayers()) {
            goldsBeforeTurn[player.getArmyIndex()] = player.getGold();
        }
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];

                // update places' owners
                if (tile.getTerrain().canBeControlled() && tile.getContent().size() > 0
                        && tile.getContent().get(0).getArmyIndex() != tile.getOwner()) {
                    tile.updateTileOwner(battle.getPlayers().get(0).getArmyIndex(), tile.getContent().get(0)
                            .getArmyIndex());
                }

                // gather resources
                if (tile.getOwner() >= 0) {
                    Player player = battle.getPlayers().get(tile.getOwner());

                    int goldAmount = (int) (tile.getGoldAmountGathered() * (battle.isWinter() ? GameUtils.WINTER_GATHERING_MODIFIER
                            : 1.0f));
                    player.updateGold(goldAmount);

                    // game stats
                    player.getGameStats().incrementGold(goldAmount);

                    // this player is still alive !
                    if (player.isDefeated() && tile.getTerrain() == TerrainData.castle) {
                        player.setDefeated(false);
                        nbPlayersInGame++;
                    }
                }
                // supply units
                for (Unit unit : tile.getContent()) {
                    if (unit instanceof Goblin
                            && (unit.getTilePosition().getTerrain() == TerrainData.forest || unit.getTilePosition()
                                    .getTerrain() == TerrainData.mountain)) {
                        // goblins are auto-supplied in mountains and forests !
                    } else {
                        battle.getPlayers().get(unit.getArmyIndex())
                                .updateGold(-unit.getPrice() * unit.getHealth() / unit.getMaxHealth());
                    }

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
                            List<Unit> content = new ArrayList<Unit>(tile.getContent());
                            for (int n = 0; n < content.size(); n++) {
                                // units are not paid anymore, they lose morale
                                // !
                                Unit unit = content.get(n);
                                unit.updateMorale(player.getGold() / 4);
                                if (unit.getMorale() < 40) {
                                    // units are deserting...
                                    boolean isDead = unit.updateHealth(-7 * (50 - unit.getMorale()));
                                    if (isDead) {
                                        battle.getUnitsToRemove().add(unit);
                                        tile.getContent().remove(unit);
                                    }
                                }
                            }

                        }
                    }
                }
                player.setGold(0);
            }
        }

        float[] populations = new float[battle.getPlayers().size()];
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                List<Unit> content = new ArrayList<Unit>(tile.getContent());
                for (int n = 0; n < content.size(); n++) {
                    Unit unit = content.get(n);
                    // game stats
                    populations[unit.getArmyIndex()] += (float) (unit.getHealth() / unit.getMaxHealth());

                    // remove dead
                    if (unit.isDead()) {
                        battle.getUnitsToRemove().add(unit);
                        tile.getContent().remove(unit);
                    }
                }
            }
        }

        // game stats
        for (Player player : battle.getPlayers()) {
            player.getGameStats().getPopulation().add(populations[player.getArmyIndex()]);
            player.getGameStats().getEconomy().add(player.getGold() - goldsBeforeTurn[player.getArmyIndex()]);
        }

        // init players for new turn
        for (Player player : battle.getPlayers()) {
            player.initNewTurn();
        }

        // check if game is ended
        if (nbPlayersInGame < 2) {
            Player winner = null;
            for (Player player : battle.getPlayers()) {
                if (!player.isDefeated()) {
                    winner = player;
                }
            }
            return winner.getArmyIndex();
        } else if (!battle.getMeSoloMode().isAI() && battle.getMeSoloMode().isDefeated()) {
            return SOLO_PLAYER_DEFEAT;
        } else {
            return -1;
        }
    }

    private static void processBattle(Battle battle, Tile tile) {

        // split in armies
        HashMap<Integer, List<Unit>> lstArmies = new HashMap<Integer, List<Unit>>();
        for (Unit u : tile.getContent()) {
            if (lstArmies.get(u.getArmyIndex()) == null) {
                lstArmies.put(u.getArmyIndex(), new ArrayList<Unit>());
            }
            lstArmies.get(u.getArmyIndex()).add(u);
        }

        int nbRounds = 0;

        do {
            nbRounds++;

            // ranged attacks have initiative
            for (Unit unit : tile.getContent()) {
                if (unit.isRangedAttack()) {
                    boolean isKilled = unit.attack(getTarget(lstArmies, unit));
                    if (isKilled) {
                        // game stats
                        battle.getPlayers().get(unit.getArmyIndex()).getGameStats().incrementNbUnitsKilled(1);
                    }
                }
            }

            // then contact units
            for (Unit unit : tile.getContent()) {
                if (!unit.isRangedAttack()) {
                    boolean isKilled = unit.attack(getTarget(lstArmies, unit));
                    if (isKilled) {
                        // game stats
                        battle.getPlayers().get(unit.getArmyIndex()).getGameStats().incrementNbUnitsKilled(1);
                    }
                }
            }

            // check if an army is defeated
            HashMap<Integer, List<Unit>> lstArmiesCopy = new HashMap<Integer, List<Unit>>(lstArmies);
            Iterator<Entry<Integer, List<Unit>>> it = lstArmiesCopy.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, List<Unit>> entry = it.next();
                List<Unit> army = entry.getValue();

                // calculate average morale and remove dead units
                int totalMorale = 0;
                for (int n = 0; n < army.size(); n++) {
                    Unit unit = army.get(n);
                    totalMorale += unit.getMorale();
                    if (unit.getHealth() == 0) {
                        battle.getUnitsToRemove().add(unit);
                        unit.getTilePosition().getContent().remove(unit);
                        army.remove(n);
                        n--;
                    }
                }
                if (army.size() == 0 || army.get(0).getArmy() != ArmiesData.UNDEAD
                        && totalMorale / army.size() < GameUtils.MORALE_THRESHOLD_ROUTED) {
                    // an army is defeated
                    for (Unit unit : army) {
                        giveBattleReward(unit, false, nbRounds);
                        boolean canFlee = unit.flee(battle);
                        if (!canFlee) {
                            battle.getUnitsToRemove().add(unit);
                            unit.getTilePosition().getContent().remove(unit);
                        }
                    }
                    lstArmies.remove(entry.getKey());
                }
            }
        } while (lstArmies.size() > 1 && nbRounds < 100);

        // battle is a draw
        if (nbRounds >= 100) {
            Iterator<Entry<Integer, List<Unit>>> it = lstArmies.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, List<Unit>> entry = it.next();
                List<Unit> army = entry.getValue();
                for (Unit unit : army) {
                    giveBattleReward(unit, false, nbRounds);
                    if (unit.getOrder() == null || unit.getOrder() instanceof DefendOrder) {

                    } else {
                        boolean canFlee = unit.flee(battle);
                        if (!canFlee) {
                            battle.getUnitsToRemove().add(unit);
                            unit.getTilePosition().getContent().remove(unit);
                        }
                    }
                }
            }
        }

        // give rewards to the winners
        if (lstArmies.size() == 1) {
            List<Unit> lstUnits = lstArmies.get(lstArmies.keySet().iterator().next());
            for (Unit unit : lstUnits) {
                giveBattleReward(unit, true, nbRounds);
            }
            // game stats
            battle.getPlayers().get(lstUnits.get(0).getArmyIndex()).getGameStats().incrementBattlesWon();
        }
    }

    private static void giveBattleReward(Unit unit, boolean isVictory, int nbRounds) {
        unit.updateExperience((int) (GameUtils.EXPERIENCE_POINTS_PER_BATTLE_ROUND * nbRounds * (isVictory ? 2.0f : 1.0f)));
        if (isVictory) {
            // morale bonus for winners
            unit.updateMorale(20);
        }
    }

    private static Unit getTarget(HashMap<Integer, List<Unit>> lstArmies, Unit unit) {
        // get opposing armies
        HashMap<Integer, List<Unit>> opposingArmies = new HashMap<Integer, List<Unit>>(lstArmies);
        opposingArmies.remove(unit.getArmyIndex());

        // pick a random opponent unit
        Integer armyIndex = (Integer) opposingArmies.keySet().toArray()[(int) ((opposingArmies.size() - 1) * Math
                .random())];
        return opposingArmies.get(armyIndex).get((int) ((opposingArmies.get(armyIndex).size() - 1) * Math.random()));

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
