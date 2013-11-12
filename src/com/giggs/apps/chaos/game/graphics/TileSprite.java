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

    private InputManager mInputManager;

    public TileSprite(float pX, float pY, TextureRegion pTextureRegion,
            VertexBufferObjectManager pSpriteVertexBufferObject, Scene mScene, InputManager inputManager, Tile tile) {
        super(pX, pY, pTextureRegion, pSpriteVertexBufferObject);
        this.mInputManager = inputManager;

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
            BuySprite buySprite = new BuySprite(getWidth() - 64, 0, GraphicsFactory.mGfxMap.get("buy.png"),
                    getVertexBufferObjectManager(), inputManager, tile);
            mScene.registerTouchArea(buySprite);
            buySprite.setTag(R.string.tag_buy_button);
            buySprite.setVisible(false);
            attachChild(buySprite);
        }

    }

}
