package com.glevel.wwii.game.model;

import java.io.Serializable;

import org.andengine.util.color.Color;

import com.glevel.wwii.game.model.map.Tile;

public abstract class GameElement implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5880458091427517171L;
    protected final int name;
    private final String spriteName;
    private transient Tile tilePosition;
    protected transient GameSprite sprite;
    private Rank rank;
    private boolean isVisible = false;

    public static enum Rank {
        neutral, enemy, ally
    }

    public GameSprite getSprite() {
        return sprite;
    }

    public void setSprite(GameSprite sprite) {
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

    public void setTilePosition(Tile tilePosition) {
        if (this.tilePosition != null) {
            this.tilePosition.setContent(null);
        }
        this.tilePosition = tilePosition;
        this.tilePosition.setContent(this);
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
            setVisible(true);
            sprite.setCanBeDragged(true);
        } else {
            setVisible(false);
            sprite.setCanBeDragged(false);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        getSprite().setVisible(isVisible);
    }
}
