package com.giggs.apps.chaos.game.model.orders;

import com.giggs.apps.chaos.game.model.map.Tile;

public class MoveOrder extends Order {

	/**
     * 
     */
	private static final long serialVersionUID = -2227867419405002169L;
	private Tile destination, origin;

	public MoveOrder(Tile destination, Tile origin) {
		this.destination = destination;
		this.origin = origin;
	}

	public Tile getDestination() {
		return destination;
	}

	public void setDestination(Tile destination) {
		this.destination = destination;
	}

	public Tile getOrigin() {
		return origin;
	}

	public void setOrigin(Tile origin) {
		this.origin = origin;
	}

}
