package com.giggs.apps.chaos.game.model.map;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Tile {

	private final int x, y;
	private final TerrainData terrain;
	private List<Unit> content = new ArrayList<Unit>();

	public Tile(int x, int y, TerrainData terrain) {
		this.x = x;
		this.y = y;
		this.terrain = terrain;
	}

	public List<Unit> getContent() {
		return content;
	}

	public void setContent(List<Unit> content) {
		this.content = content;
	}

	public TerrainData getTerrain() {
		return terrain;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
