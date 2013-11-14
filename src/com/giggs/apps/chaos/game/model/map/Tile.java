package com.giggs.apps.chaos.game.model.map;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.graphics.TileSprite;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Tile {

    private final int x, y;
    private TerrainData terrain;
    private List<Unit> content = new ArrayList<Unit>();
    private int owner = -1;
    private TileSprite sprite;
    private boolean isVisible;

    private boolean isMoveOption = false;

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

    public void setTerrain(TerrainData terrain) {
        this.terrain = terrain;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public TileSprite getSprite() {
        return sprite;
    }

    public void setSprite(TileSprite sprite) {
        this.sprite = sprite;
    }

    public void updateTileOwner(int myArmyIndex, int newOwner) {
        setOwner(newOwner);
        // update buy button visibility
        if (terrain.isUnitFactory()) {
            sprite.getChildByTag(R.string.tag_buy_button).setVisible(newOwner == myArmyIndex);
        }

        // update owner's control zone color
        if (sprite.getChildByTag(R.string.tag_control_zone) != null) {
            sprite.getChildByTag(R.string.tag_control_zone).setVisible(true);
            sprite.getChildByTag(R.string.tag_control_zone).setColor(GameUtils.PLAYER_COLORS[newOwner]);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(int myArmyIndex, boolean isVisible) {
        this.isVisible = isVisible;
        // update visibility
        if (isVisible) {
            sprite.setAlpha(1.0f);
        } else {
            sprite.setAlpha(0.5f);
        }
        // update sprite children visibility
        if (sprite.getChildByTag(R.string.tag_control_zone) != null && owner >= 0) {
            sprite.getChildByTag(R.string.tag_control_zone).setVisible(isVisible);
        }

        // update buy button visibility
        if (terrain.isUnitFactory()) {
            sprite.getChildByTag(R.string.tag_buy_button).setVisible(owner == myArmyIndex && isVisible);
        }

        // update content visibility
        for (Unit unit : content) {
            // unit.getSprite().setVisible(isVisible);
        }
    }

    public boolean isMoveOption() {
        return isMoveOption;
    }

    public void setMoveOption(boolean isSet) {
        isMoveOption = isSet;
        updateColor();
    }

    private void updateColor() {
        if (isMoveOption) {
            sprite.setColor(0.0f, 0.6f, 0.0f);
        } else {
            sprite.setColor(1.0f, 1.0f, 1.0f);
        }
    }

    public void setHovered(boolean isHovered) {
        if (isHovered) {
            sprite.setColor(1.0f, 0.5f, 0.5f);
        } else {
            updateColor();
        }
    }

    public void updateWeather(boolean isWinter) {
        sprite.updateWeather(isWinter);
    }

    public int getGoldAmountGathered() {
        switch (terrain) {
        case farm:
            return 100;
        case castle:
            return 100;
        case fort:
            return 50;
        }
        return 0;
    }

}
