package com.glevel.wwii.game.graphics;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Protection extends Sprite {

    private static final float SCALE_ANIMATION_SPEED = 0.5f;
    private static final float SCALE_ANIMATION_LIMIT = 10.0f;

    private boolean mIsFromLeft = true;

    public Protection(final TextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(0, 0, pTextureRegion, pVertexBufferObjectManager);
    }

    @Override
    protected void onManagedUpdate(final float pSecondsElapsed) {
        this.setSkewY(getSkewY() + SCALE_ANIMATION_SPEED * (mIsFromLeft ? 1 : -1));
        if (Math.abs(getSkewY() - 1.0f) > SCALE_ANIMATION_LIMIT) {
            mIsFromLeft = !mIsFromLeft;
        }
        super.onManagedUpdate(pSecondsElapsed);
    }
}
