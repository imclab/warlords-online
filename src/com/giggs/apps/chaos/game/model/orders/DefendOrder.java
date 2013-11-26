package com.giggs.apps.chaos.game.model.orders;

import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class DefendOrder extends Order {

    /**
     * 
     */
    private static final long serialVersionUID = 4397286734981789298L;

    private final Unit unit;
    private final Tile tile;

    public DefendOrder(Unit unit, Tile tile) {
        this.unit = unit;
        this.tile = tile;
    }

    public Unit getUnit() {
        return unit;
    }

    public Tile getTile() {
        return tile;
    }

}
