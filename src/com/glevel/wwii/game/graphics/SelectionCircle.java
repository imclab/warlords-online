package com.glevel.wwii.game.graphics;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class SelectionCircle extends Sprite {

    public SelectionCircle(final TextureRegion pTextureRegion,
            final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(0, 0, pTextureRegion, pVertexBufferObjectManager);
    }

    @Override
    protected void onManagedUpdate(final float pSecondsElapsed) {
        this.setRotation(getRotation() + 0.8f);
        super.onManagedUpdate(pSecondsElapsed);
    }

}
