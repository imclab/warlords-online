package com.giggs.apps.chaos.game.graphics;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class ControlZone extends Sprite {

    public static final float INITIAL_ALPHA = 0.4f;

    public ControlZone(final TextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(0, 0, pTextureRegion, pVertexBufferObjectManager);
    }

}
