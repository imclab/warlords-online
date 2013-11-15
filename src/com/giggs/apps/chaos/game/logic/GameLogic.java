package com.giggs.apps.chaos.game.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
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
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.orders.Order;
import com.giggs.apps.chaos.game.model.units.Unit;

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

    public static void runTurn(GameActivity gameActivity, Battle battle) {
        gameActivity.getEngine().stop();

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
                    unit.initTurn();
                }
            }
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
                    tile.getSprite().updateUnitProduction(null);
                } else if (order instanceof MoveOrder) {
                    MoveOrder moveOrder = (MoveOrder) order;
                    // check units crossing
                    double random = Math.random();
                    for (Unit u : ((MoveOrder) order).getDestination().getContent()) {
                        if (u.getOrder() != null && u.getOrder() instanceof MoveOrder
                                && ((MoveOrder) u.getOrder()).getDestination() == moveOrder.getOrigin()) {
                            // resolve units crossing
                            if (random < 0.5) {
                                u.setOrder(null);
                            } else {
                                for (Unit allyUnit : moveOrder.getOrigin().getContent()) {
                                    if (allyUnit.getOrder() != null
                                            && allyUnit.getOrder() instanceof MoveOrder
                                            && ((MoveOrder) allyUnit.getOrder()).getDestination() == moveOrder
                                                    .getDestination()) {
                                        u.setOrder(null);
                                    }
                                }
                                break;
                            }
                        }
                    }
                    moveOrder.getUnit().updateTilePosition(moveOrder.getDestination());
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
                            processBattle(gameActivity, battle, tile);
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
                    for (Unit u : tile.getContent()) {
                        if (u.getOrder() != null && u.getOrder() instanceof MoveOrder) {
                            MoveOrder m = (MoveOrder) u.getOrder();
                            if (m.getOrigin().getContent().size() == 0
                                    || m.getOrigin().getContent().get(0).getArmyIndex() == u.getArmyIndex()
                                    && m.getOrigin().getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                                u.setTilePosition(m.getOrigin());
                                if (tile.getContent().size() <= GameUtils.MAX_UNITS_PER_TILE) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // update places' owners
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                if (tile.getTerrain().canBeControlled() && tile.getContent().size() > 0
                        && tile.getContent().get(0).getArmyIndex() != tile.getOwner()) {
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
        int myGoldBeforeTurn = battle.getPlayers().get(0).getGold();
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                // gather resources
                if (tile.getOwner() >= 0) {
                    Player player = battle.getPlayers().get(tile.getOwner());

                    player.updateGold((int) (tile.getGoldAmountGathered() * (battle.isWinter() ? GameUtils.WINTER_GATHERING_MODIFIER
                            : 1.0f)));

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

        // update my gold amount
        gameActivity.mGameGUI.updateGoldAmount(battle.getPlayers().get(0).getGold());
        gameActivity.mGameGUI.updateEconomyBalance(battle.getPlayers().get(0).getGold() - myGoldBeforeTurn);

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
                                        gameActivity.removeUnit(unit);
                                    }
                                }
                            }
                        }
                    }
                }
                player.setGold(0);
            }
        }

        // dispatch units properly on tiles
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                MapLogic.dispatchUnitsOnTile(tile);
            }
        }

        // update fogs of war
        updateFogsOfWar(battle, 0);

        // init players for new turn
        for (Player player : battle.getPlayers()) {
            player.initNewTurn();
        }
        // dispatch units properly on tiles
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                MapLogic.dispatchUnitsOnTile(tile);
            }
        }

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

        gameActivity.getEngine().start();
    }

    private static void processBattle(GameActivity gameActivity, Battle battle, Tile tile) {

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
                    unit.attack(getTarget(lstArmies, unit));
                }
            }

            // then contact units
            for (Unit unit : tile.getContent()) {
                if (!unit.isRangedAttack()) {
                    unit.attack(getTarget(lstArmies, unit));
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
                        gameActivity.removeUnit(unit);
                        army.remove(n);
                        n--;
                    }
                }
                if (army.size() == 0 || totalMorale / army.size() < GameUtils.MORALE_THRESHOLD_ROUTED) {
                    // an army is defeated
                    for (Unit unit : army) {
                        giveBattleReward(unit, false, nbRounds);
                        unit.flee(battle);
                    }
                    lstArmies.remove(entry.getKey());
                }
            }

        } while (lstArmies.size() > 1);

        // give rewards for the winners
        if (lstArmies.size() == 1) {
            for (Unit unit : lstArmies.get(lstArmies.keySet().iterator().next())) {
                giveBattleReward(unit, true, nbRounds);
            }
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

    public static int getDamage(Unit attacker, Unit target) {
        float attackFactor = WEAPONS_EFFICIENCY[attacker.getWeaponType().ordinal()][target.getArmorType().ordinal()];
        int damage = (int) Math.max(0,
                attacker.getDamage() * attackFactor * (1 + 0.2 * Math.random()) - target.getArmor());

        // terrain modifier
        if (target.getTilePosition().getTerrain() == TerrainData.castle
                || target.getTilePosition().getTerrain() == TerrainData.fort) {
            damage *= 0.7;
        }

        // order modifier
        if (target.getOrder() != null && target.getOrder() instanceof DefendOrder) {
            damage *= 0.8;
        }

        return damage;
    }

}
