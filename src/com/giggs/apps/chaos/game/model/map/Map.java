package com.giggs.apps.chaos.game.model.map;

import java.io.Serializable;

public class Map implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 323109981895240844L;
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
