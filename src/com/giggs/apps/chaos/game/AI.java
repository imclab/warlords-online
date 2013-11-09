package com.giggs.apps.chaos.game;

import java.util.List;

import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.orders.FireOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.units.Unit;

public class AI {

    public static void updateUnitOrder(Battle battle, Unit unit) {
        // TODO
        for (Unit u : battle.getEnemies(unit)) {
            if (!u.isDead() && GameUtils.getDistanceBetween(unit, u) < 30 * GameUtils.PIXEL_BY_METER
                    && GameUtils.canSee(battle.getMap(), unit, u)) {
                unit.setOrder(new FireOrder(u));
                return;
            }
        }

        unit.setOrder(new MoveOrder((float) Math.random() * 1000, (float) Math.random() * 1000));

    }

    public static void createArmy(Player player, Battle battle) {
        // TODO
        List<Unit> availableUnits = UnitsData.getAllUnits(player.getArmy());
        for (int n = 0; n < 6; n++) {
            Unit randomUnit = availableUnits.get((int) (Math.random() * (availableUnits.size() - 1))).copy();
            player.getUnits().add(randomUnit);
        }
    }
}
