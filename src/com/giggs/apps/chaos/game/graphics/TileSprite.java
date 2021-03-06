package com.giggs.apps.chaos.game.graphics;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.GraphicsFactory;
import com.giggs.apps.chaos.game.InputManager;
import com.giggs.apps.chaos.game.model.map.Tile;

public class TileSprite extends Sprite {

    private Tile tile;
    private BuySprite buySprite;
    private boolean isWinter = false;

    public TileSprite(float pX, float pY, TextureRegion pTextureRegion,
            VertexBufferObjectManager pSpriteVertexBufferObject, Scene mScene, InputManager inputManager, Tile tile) {
        super(pX, pY, pTextureRegion, pSpriteVertexBufferObject);
        this.tile = tile;

        // add control zone
        if (tile.getTerrain().canBeControlled()) {
            ControlZone controlZone = new ControlZone(GraphicsFactory.mGfxMap.get("control_zone.png"),
                    getVertexBufferObjectManager());
            controlZone.setTag(R.string.tag_control_zone);
            controlZone.setVisible(false);
            attachChild(controlZone);
        }

        // add buy button
        if (tile.getTerrain().isUnitFactory()) {
            buySprite = new BuySprite(getWidth() - 64, 0, GraphicsFactory.mGfxMap.get("buy.png"),
                    getVertexBufferObjectManager(), inputManager, tile);
            mScene.registerTouchArea(buySprite);
            buySprite.setTag(R.string.tag_buy_button);
            buySprite.setVisible(false);
            attachChild(buySprite);
        }
    }

    public void updateWeather(boolean isWinter) {
        this.isWinter = isWinter;
        if (isWinter) {
            setTextureRegion(GraphicsFactory.mGfxMap.get(tile.getTerrain().getSpriteName().replace(".png", "")
                    + "_winter.png"));
        } else {
            setTextureRegion(GraphicsFactory.mGfxMap.get(tile.getTerrain().getSpriteName()));
        }
    }

    public void updateUnitProduction(TextureRegion texture) {
        if (texture == null) {
            buySprite.setTextureRegion(GraphicsFactory.mGfxMap.get("buy.png"));
            buySprite.setX(getWidth() - 64);
        } else {
            buySprite.setTextureRegion(texture);
            buySprite.setX(getWidth() - 59);
        }
    }

    public boolean isWinter() {
        return isWinter;
    }

}
