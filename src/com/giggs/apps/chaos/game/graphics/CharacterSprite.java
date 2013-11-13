package com.giggs.apps.chaos.game.graphics;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.giggs.apps.chaos.game.GameUtils.Direction;

public class CharacterSprite extends AnimatedSprite {

    public CharacterSprite(float pX, float pY, ITiledTextureRegion pTiledTextureRegion,
            VertexBufferObjectManager mVertexBufferObjectManager) {
        super(pX, pY, pTiledTextureRegion, mVertexBufferObjectManager);
        stand();
    }

    public void walk(Direction direction) {
        animate(new long[] { 300, 300, 300 }, direction.ordinal() * 3, direction.ordinal() * 3 + 2, true);
    }

    public void stand() {
        animate(new long[] { 100, 0 }, Direction.south.ordinal() * 3 + 1, Direction.south.ordinal() * 3 + 2, false);
    }

}
