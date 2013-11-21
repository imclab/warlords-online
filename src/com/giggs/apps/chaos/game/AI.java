package com.giggs.apps.chaos.game;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.BuyOrder;
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.units.Unit;

public class AI {

    public static void generateTurnOrders(Battle battle, Player player) {
        Map map = battle.getMap();

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
                    for (Unit u : t.getContent()) {
                        for (Tile insecure : insecureTiles) {
                            int distance = MapLogic.getDistance(t, insecure);
                            if (distance == 0) {
                                // defend
                                DefendOrder o = new DefendOrder(u);
                                u.setOrder(o);
                                player.getLstTurnOrders().add(o);
                                break;
                            } else if (distance <= 2) {
                                // go back to defend
                                Tile step = getOneStepCloser(map, u, insecure);
                                if (step != null) {
                                    MoveOrder o = new MoveOrder(u, step);
                                    u.setOrder(o);
                                    player.getLstTurnOrders().add(o);
                                }
                                break;
                            }
                        }

                        if (u.getOrder() == null) {
                            // conquer
                            Tile closestInterestingPlace = getClosestInterestingPlace(map, u);
                            if (closestInterestingPlace != null) {
                                Tile step = getOneStepCloser(map, u, closestInterestingPlace);
                                if (step != null) {
                                    MoveOrder o = new MoveOrder(u, step);
                                    u.setOrder(o);
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
        List<Unit> availableUnits = UnitsData.getUnits(player.getArmy(), player.getArmyIndex());

        List<Tile> lstBuyOrders = new ArrayList<Tile>();
        for (Tile tile : insecureTiles) {
            if (economyBalance <= 0) {
                break;
            }
            if (tile.getTerrain().isUnitFactory() && tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                for (int n = availableUnits.size() - 1; n >= 0; n--) {
                    if (economyBalance > availableUnits.get(n).getPrice()) {
                        player.getLstTurnOrders().add(new BuyOrder(tile, availableUnits.get(n)));
                        lstBuyOrders.add(tile);
                        economyBalance -= availableUnits.get(n).getPrice();
                        break;
                    }
                }
            }
        }

        for (Tile tile : map.getForts()) {
            if (economyBalance <= 0) {
                break;
            }
            if (lstBuyOrders.indexOf(tile) == -1 && tile.getOwner() == player.getArmyIndex()
                    && tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                for (int n = availableUnits.size() - 1; n >= 0; n--) {
                    if (economyBalance > availableUnits.get(n).getPrice()) {
                        player.getLstTurnOrders().add(new BuyOrder(tile, availableUnits.get(n)));
                        economyBalance -= availableUnits.get(n).getPrice();
                        break;
                    }
                }
            }
        }
        for (Tile tile : map.getCastles()) {
            if (economyBalance <= 0) {
                break;
            }
            if (lstBuyOrders.indexOf(tile) == -1 && tile.getOwner() == player.getArmyIndex()
                    && tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE) {
                for (int n = availableUnits.size() - 1; n >= 0; n--) {
                    if (economyBalance > availableUnits.get(n).getPrice()) {
                        player.getLstTurnOrders().add(new BuyOrder(tile, availableUnits.get(n)));
                        economyBalance -= availableUnits.get(n).getPrice();
                        break;
                    }
                }
            }
        }

    }

    private static int getThreat(Tile tile) {
        int threat = 0;
        for (Unit unit : tile.getContent()) {
            threat += 100 * unit.getHealth() / unit.getMaxHealth() * unit.getExperience()
                    * (unit.getAttack() + unit.getArmor());
        }
        return threat;
    }

    private static int getThreatAround(Battle battle, Player player, Tile tile, int distance, boolean allies) {
        int threat = 0;
        for (Tile tileAround : MapLogic.getAdjacentTiles(battle.getMap(), tile, distance, true)) {
            if (allies && tileAround.isAllyOnIt(player.getArmyIndex()) || !allies
                    && tileAround.isEnemyOnIt(player.getArmyIndex())) {
                if (!battle.getPlayers().get(tileAround.getContent().get(0).getArmyIndex()).isDefeated()) {
                    threat += getThreat(tileAround);
                }
            }
        }
        return threat;
    }

    private static Tile getOneStepCloser(Map map, Unit unit, Tile destination) {
        int dx = destination.getX() - unit.getTilePosition().getX();
        int dy = destination.getY() - unit.getTilePosition().getY();
        if (Math.abs(dx) > 0
                && (Math.abs(dx) > Math.abs(dy))
                || Math.abs(dx) > 0
                && Math.abs(dy) > 0
                && unit.canMove(map.getTiles()[unit.getTilePosition().getY() + dy / Math.abs(dy)][unit
                        .getTilePosition().getX()])) {
            return map.getTiles()[unit.getTilePosition().getY()][unit.getTilePosition().getX() + dx / Math.abs(dx)];
        } else if (Math.abs(dy) > 0) {
            return map.getTiles()[unit.getTilePosition().getY() + dy / Math.abs(dy)][unit.getTilePosition().getX()];
        }
        return null;
    }

    private static Tile getClosestInterestingPlace(Map map, Unit unit) {
        int distance = 100;
        Tile tile = null;
        for (Tile t : map.getCastles()) {
            int d = MapLogic.getDistance(unit.getTilePosition(), t);
            if (t.getOwner() != unit.getArmyIndex() && d < distance) {
                distance = d;
                tile = t;
            }
        }
        for (Tile t : map.getFarms()) {
            int d = MapLogic.getDistance(unit.getTilePosition(), t);
            if (t.getOwner() != unit.getArmyIndex() && d < distance) {
                distance = d;
                tile = t;
            }
        }
        for (Tile t : map.getForts()) {
            int d = MapLogic.getDistance(unit.getTilePosition(), t);
            if (t.getOwner() != unit.getArmyIndex() && d < distance) {
                distance = d;
                tile = t;
            }
        }
        return tile;
    }

}
