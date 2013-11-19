package com.giggs.apps.chaos.game.model.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Used for AI
     */
    private final List<Tile> castles = new ArrayList<Tile>();
    private final List<Tile> forts = new ArrayList<Tile>();
    private final List<Tile> farms = new ArrayList<Tile>();

    public List<Tile> getCastles() {
        return castles;
    }

    public List<Tile> getForts() {
        return forts;
    }

    public List<Tile> getFarms() {
        return farms;
    }

}
