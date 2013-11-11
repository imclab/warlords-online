package com.giggs.apps.chaos.game.model.map;

public class Map {

	private Tile[][] tiles;

	public Tile[][] getTiles() {
		return tiles;
	}

	public void setTiles(Tile[][] tiles) {
		this.tiles = tiles;
	}

	public int getWidth() {
		return tiles[0].length;
	}

	public int getHeight() {
		return tiles.length;
	}

}
