package com.giggs.apps.chaos.game.model;

import java.io.Serializable;

import org.andengine.util.color.Color;

import com.giggs.apps.chaos.game.graphics.UnitSprite;
import com.giggs.apps.chaos.game.model.map.Tile;

public abstract class GameElement implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5880458091427517171L;
    protected final int name;
    private final String spriteName;
    protected transient Tile tilePosition;
    protected transient UnitSprite sprite;
    private Rank rank;

    public static enum Rank {
        neutral, enemy, ally
    }

    public UnitSprite getSprite() {
        return sprite;
    }

    public void setSprite(UnitSprite sprite) {
        this.sprite = sprite;
    }

    public GameElement(int name, String spriteName) {
        this.name = name;
        this.spriteName = spriteName;
    }

    public int getName() {
        return name;
    }

    public Tile getTilePosition() {
        return tilePosition;
    }

    public void setTile(Tile tile) {
        this.tilePosition = tile;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public Color getSelectionColor() {
        switch (rank) {
        case enemy:
            return Color.RED;
        case ally:
            return Color.WHITE;
        }
        return Color.YELLOW;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
        if (rank == Rank.ally) {
            sprite.setCanBeDragged(true);
        } else {
            sprite.setCanBeDragged(false);
        }
    }

}
