package com.giggs.apps.chaos.game.logic;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;

public class MapLogic {

	public static List<Tile> getAdjacentTiles(Map map, Tile centerTile, int step, boolean withDiagonal) {
		List<Tile> adjacentTiles = new ArrayList<Tile>();

		for (int y = centerTile.getY() - step; y < centerTile.getY() + step + 1; y++) {
			for (int x = centerTile.getX() - step; x < centerTile.getX() + step + 1; x++) {
				if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()
				        && (x != centerTile.getX() || y != centerTile.getY())) {
					Tile t = map.getTiles()[y][x];

					if (withDiagonal || getDistance(centerTile, t) <= step) {
						adjacentTiles.add(t);
					}
				}
			}
		}

		return adjacentTiles;
	}

	public static int getDistance(Tile tile1, Tile tile2) {
		return Math.abs(tile1.getX() - tile2.getX()) + Math.abs(tile1.getY() - tile2.getY());
	}

	private static final float CENTER_RATIO = 0.3f, FIRST_HALF_RATIO = 0.1f, SECOND_HALF_RATIO = 0.50f;

	public static void dispatchUnitsOnTile(Tile tile) {
		int nbUnits = tile.getContent().size();
		switch (nbUnits) {
		case 1:
			tile.getContent()
			        .get(0)
			        .getSprite()
			        .setPosition(GameUtils.TILE_SIZE * (tile.getX() + CENTER_RATIO),
			                GameUtils.TILE_SIZE * (tile.getY() + CENTER_RATIO));
			break;
		case 2:
			tile.getContent()
			        .get(0)
			        .getSprite()
			        .setPosition(GameUtils.TILE_SIZE * (tile.getX() + FIRST_HALF_RATIO),
			                GameUtils.TILE_SIZE * (tile.getY() + CENTER_RATIO));
			tile.getContent()
			        .get(1)
			        .getSprite()
			        .setPosition(GameUtils.TILE_SIZE * (tile.getX() + SECOND_HALF_RATIO),
			                GameUtils.TILE_SIZE * (tile.getY() + CENTER_RATIO));
			break;
		case 3:
			tile.getContent()
			        .get(0)
			        .getSprite()
			        .setPosition(GameUtils.TILE_SIZE * (tile.getX() + CENTER_RATIO),
			                GameUtils.TILE_SIZE * (tile.getY() + FIRST_HALF_RATIO));
			tile.getContent()
			        .get(1)
			        .getSprite()
			        .setPosition(GameUtils.TILE_SIZE * (tile.getX() + FIRST_HALF_RATIO),
			                GameUtils.TILE_SIZE * (tile.getY() + SECOND_HALF_RATIO));
			tile.getContent()
			        .get(2)
			        .getSprite()
			        .setPosition(GameUtils.TILE_SIZE * (tile.getX() + SECOND_HALF_RATIO),
			                GameUtils.TILE_SIZE * (tile.getY() + SECOND_HALF_RATIO));
			break;
		}
	}

	public static Tile getTileAtCoordinates(Map map, float x, float y) {
		if ((int) (y / GameUtils.TILE_SIZE) < map.getHeight() && (int) (x / GameUtils.TILE_SIZE) < map.getWidth()) {
			return map.getTiles()[(int) (y / GameUtils.TILE_SIZE)][(int) (x / GameUtils.TILE_SIZE)];
		} else {
			return null;
		}
	}

}
