package com.giggs.apps.chaos.game;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.logic.pathfinding.AStar;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.BuyOrder;
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.units.Unit;

public class AI {

    /**
     * Generate turn orders for an AI player.
     * 
     * @param battle
     * @param player
     */
    public static void generateTurnOrders(Battle battle, Player player) {
        Map map = battle.getMap();

        int nbUnits = 0;

        // check if my buildings are secured
        List<Tile> insecureTiles = new ArrayList<Tile>();
        for (Tile castle : map.getCastles()) {
            if (castle.getOwner() == player.getArmyIndex()) {
                int threat = getThreatAround(battle, player, castle, 2, false);
                if (getThreatAround(battle, player, castle, 1, true) < threat) {
                    // this tile is insecure
                    insecureTiles.add(castle);
                }
            }
        }
        for (Tile tile : map.getForts()) {
            if (tile.getOwner() == player.getArmyIndex()) {
                int threat = getThreatAround(battle, player, tile, 1, false);
                if (getThreatAround(battle, player, tile, 1, true) < threat) {
                    // this tile is insecure
                    insecureTiles.add(tile);
                }
            }
        }
        for (Tile tile : map.getFarms()) {
            if (tile.getOwner() == player.getArmyIndex()) {
                int threat = getThreatAround(battle, player, tile, 1, false);
                if (getThreatAround(battle, player, tile, 1, true) < threat) {
                    // this tile is insecure
                    insecureTiles.add(tile);
                }
            }
        }

        // give move orders
        for (Tile[] h : map.getTiles()) {
            for (Tile t : h) {
                if (t.isAllyOnIt(player.getArmyIndex())) {
                    for (int n = 0; n < t.getContent().size(); n++) {
                        Unit u = t.getContent().get(n);
                        nbUnits++;
                        for (Tile insecure : insecureTiles) {
                            int distance = MapLogic.getDistance(t, insecure);
                            // unit is on an insecure tile
                            if (distance == 0) {
                                // defend
                                DefendOrder o = new DefendOrder(u, u.getTilePosition(), n);
                                u.setOrder(o, false);
                                player.getLstTurnOrders().add(o);
                                break;
                            } else if (distance <= 2) {
                                // unit is near an insecure tile : go back to
                                // defend
                                Tile step = getOneStepCloser(map, u, insecure);
                                if (step != null) {
                                    MoveOrder o = new MoveOrder(u, step, n);
                                    u.setOrder(o, false);
                                    player.getLstTurnOrders().add(o);
                                }
                                break;
                            }
                        }

                        if (u.getOrder() == null) {
                            // if no order yet, conquer the map !
                            Tile closestInterestingPlace = getClosestInterestingPlace(map, u);
                            if (closestInterestingPlace != null) {
                                Tile step = getOneStepCloser(map, u, closestInterestingPlace);
                                if (step != null) {
                                    MoveOrder o = new MoveOrder(u, step, n);
                                    u.setOrder(o, false);
                                    player.getLstTurnOrders().add(o);
                                }
                            }
                        }
                    }
                }
            }
        }

        // buy units
        int economyBalance = 0;
        if (player.getGameStats().getEconomy().size() > 0) {
            economyBalance = (int) (player.getGameStats().getEconomy()
                    .get(player.getGameStats().getEconomy().size() - 1) * (battle.isWinter() ? GameUtils.WINTER_GATHERING_MODIFIER
                    : 1.0f));
        }
        List<Unit> availableUnits;
        List<Tile> lstBuyOrders = new ArrayList<Tile>();
        // at first, buy units on insecure tiles
        for (Tile tile : insecureTiles) {
            if (economyBalance <= 0) {
                break;
            }
            if (tile.getTerrain().isUnitFactory() && tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                availableUnits = UnitsData.getUnits(player.getArmy(), player.getArmyIndex());
                Unit unitToBuy = chooseUnitToBuy(availableUnits, tile, economyBalance, false, nbUnits);
                if (unitToBuy != null) {
                    player.getLstTurnOrders().add(new BuyOrder(tile, unitToBuy));
                    lstBuyOrders.add(tile);
                    economyBalance -= unitToBuy.getPrice();
                }
            }
        }
        // then, buy units on forts
        for (Tile tile : map.getForts()) {
            if (economyBalance <= 0) {
                break;
            }
            if (lstBuyOrders.indexOf(tile) == -1 && tile.getOwner() == player.getArmyIndex()
                    && tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                availableUnits = UnitsData.getUnits(player.getArmy(), player.getArmyIndex());
                Unit unitToBuy = chooseUnitToBuy(availableUnits, tile, economyBalance, false, nbUnits);
                if (unitToBuy != null) {
                    player.getLstTurnOrders().add(new BuyOrder(tile, unitToBuy));
                    economyBalance -= unitToBuy.getPrice();
                }
            }
        }
        // then, buy units on castles
        for (Tile tile : map.getCastles()) {
            if (economyBalance <= 0) {
                break;
            }
            if (lstBuyOrders.indexOf(tile) == -1 && tile.getOwner() == player.getArmyIndex()
                    && tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                availableUnits = UnitsData.getUnits(player.getArmy(), player.getArmyIndex());
                Unit unitToBuy = chooseUnitToBuy(availableUnits, tile, economyBalance, false, nbUnits);
                if (unitToBuy != null) {
                    player.getLstTurnOrders().add(new BuyOrder(tile, unitToBuy));
                    economyBalance -= unitToBuy.getPrice();
                }
            }
        }

    }

    /**
     * Calculates a tile's threat.
     * 
     * @param tile
     * @return
     */
    private static int getTileThreat(Tile tile) {
        int threat = 0;
        for (Unit unit : tile.getContent()) {
            threat += unit.getThreat();
        }
        return threat;
    }

    /**
     * Calculates a zone's threat (ally or enemy).
     * 
     * @param battle
     * @param player
     * @param tile
     * @param distance
     * @param allies
     * @return
     */
    private static int getThreatAround(Battle battle, Player player, Tile tile, int distance, boolean allies) {
        int threat = 0;
        for (Tile tileAround : MapLogic.getAdjacentTiles(battle.getMap(), tile, distance, true)) {
            if (allies && tileAround.isAllyOnIt(player.getArmyIndex()) || !allies
                    && tileAround.isEnemyOnIt(player.getArmyIndex())) {
                if (!battle.getPlayers().get(tileAround.getContent().get(0).getArmyIndex()).isDefeated()) {
                    threat += getTileThreat(tileAround);
                }
            }
        }
        return threat;
    }

    /**
     * Uses A* algorithm to move one step closer to a target.
     * 
     * @param map
     * @param unit
     * @param destination
     * @return
     */
    private static Tile getOneStepCloser(Map map, Unit unit, Tile destination) {
        List<Tile> path = new AStar<Tile>().search(map.getTiles(), unit.getTilePosition(), destination, false, unit);
        if (path != null && path.size() > 1) {
            return path.get(1);
        }
        return null;
    }

    /**
     * Returns the closest most interesting zone to conquer (or null if none).
     * Castles have priority over farms, which have priority over forts.
     * 
     * @param map
     * @param unit
     * @return
     */
    private static Tile getClosestInterestingPlace(Map map, Unit unit) {
        int distance = 100;
        Tile tile = null;
        for (Tile t : map.getCastles()) {
            int d = MapLogic.getDistance(unit.getTilePosition(), t);
            if (d > 0 && t.getOwner() != unit.getArmyIndex() && d < distance) {
                distance = d;
                tile = t;
            }
        }
        for (Tile t : map.getFarms()) {
            int d = MapLogic.getDistance(unit.getTilePosition(), t);
            if (d > 0 && t.getOwner() != unit.getArmyIndex() && d < distance) {
                distance = d;
                tile = t;
            }
        }
        for (Tile t : map.getForts()) {
            int d = MapLogic.getDistance(unit.getTilePosition(), t);
            if (d > 0 && t.getOwner() != unit.getArmyIndex() && d < distance) {
                distance = d;
                tile = t;
            }
        }
        return tile;
    }

    /**
     * Returns the best unit to buy for a given economy balance and a given
     * offensive / defensive situation.
     * 
     * @param availableUnits
     * @param tile
     * @param economyBalance
     * @param defensiveBuy
     * @param nbUnits
     * @return
     */
    private static Unit chooseUnitToBuy(List<Unit> availableUnits, Tile tile, int economyBalance, boolean defensiveBuy,
            int nbUnits) {
        if (defensiveBuy || nbUnits > 3) {
            // in defense or in late game, get best possible units
            if (tile.getContent().size() > 0 && !tile.getContent().get(0).isRangedAttack()) {
                // if a contact unit is already on the tile, get a
                // ranged unit if possible
                Unit unitToBuy = getBestPossibleUnit(availableUnits, economyBalance, true);
                if (unitToBuy == null) {
                    return getBestPossibleUnit(availableUnits, economyBalance, false);
                }
            } else {
                // no unit on there : get the best unit possible
                return getBestPossibleUnit(availableUnits, economyBalance, false);
            }
        } else {
            // in attack in early game, get troops !
            return availableUnits.get(0);
        }
        return null;
    }

    /**
     * Returns best possible unit (either ranged unit or not).
     * 
     * @param availableUnits
     * @param economyBalance
     * @param isRangedUnit
     * @return
     */
    private static Unit getBestPossibleUnit(List<Unit> availableUnits, int economyBalance, boolean isRangedUnit) {
        for (int n = availableUnits.size() - 1; n >= 0; n--) {
            if ((isRangedUnit == availableUnits.get(n).isRangedAttack())
                    && economyBalance >= availableUnits.get(n).getPrice()) {
                return availableUnits.get(n);
            }
        }
        return null;
    }

}
