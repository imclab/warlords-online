package com.giggs.apps.chaos.game.model.orders;

import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class MoveOrder extends Order {

    /**
     * 
     */
    private static final long serialVersionUID = -2227867419405002169L;
    private final Unit unit;
    private final Tile destination, origin;
    private final int unitIndex;

    public MoveOrder(Unit unit, Tile destination, int unitIndex) {
        this.unit = unit;
        this.origin = unit.getTilePosition();
        this.destination = destination;
        this.unitIndex = unitIndex;
    }

    public Tile getDestination() {
        return destination;
    }

    public Tile getOrigin() {
        return origin;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getUnitIndex() {
        return unitIndex;
    }

}
