package com.giggs.apps.chaos.game.model.orders;

import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class BuyOrder extends Order {

	/**
     * 
     */
	private static final long serialVersionUID = -5452279368421461793L;
	private Tile tile;
	private Unit unit;

	public BuyOrder(Tile tile, Unit unit) {
		this.tile = tile;
		this.unit = unit;
	}

	public Tile getTile() {
		return tile;
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

}
