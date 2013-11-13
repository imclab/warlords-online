package com.giggs.apps.chaos.game.graphics;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.giggs.apps.chaos.game.InputManager;
import com.giggs.apps.chaos.game.model.map.Tile;

public class BuySprite extends Sprite {

    private InputManager mInputManager;
    private Tile mTile;
    private boolean mIsSelected;

    public BuySprite(float pX, float pY, TextureRegion pTextureRegion,
            VertexBufferObjectManager pSpriteVertexBufferObject, InputManager inputManager, Tile tile) {
        super(pX, pY, pTextureRegion, pSpriteVertexBufferObject);
        this.mInputManager = inputManager;
        this.mTile = tile;
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,
            final float pTouchAreaLocalY) {
        // zoom has priority
        if (pSceneTouchEvent.getMotionEvent().getPointerCount() > 1) {
            return false;
        }

        if (isVisible()) {
            switch (pSceneTouchEvent.getAction()) {
            case TouchEvent.ACTION_DOWN:
                // element is selected
                mInputManager.onBuyIconClicked(mTile);
                setAlpha(0.5f);
                break;
            case TouchEvent.ACTION_MOVE:
                break;
            case TouchEvent.ACTION_UP:
                setAlpha(1.0f);
                break;
            }
        }
        return true;

    }

}
