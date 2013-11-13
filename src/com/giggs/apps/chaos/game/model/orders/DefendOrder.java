package com.giggs.apps.chaos.game.model.orders;

import com.giggs.apps.chaos.game.model.units.Unit;

public class DefendOrder extends Order {

    /**
     * 
     */
    private static final long serialVersionUID = 4397286734981789298L;

    private final Unit unit;

    public DefendOrder(Unit unit) {
        this.unit = unit;
    }

    public Unit getUnit() {
        return unit;
    }

}
