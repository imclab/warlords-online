package com.glevel.wwii.game.graphics;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class DeploymentZone extends Rectangle {

    private static final float ALPHA_ANIMATION_SPEED = 0.001f;
    private static final float ALPHA_ANIMATION_LIMIT = 0.05f;
    private static final float INITIAL_ALPHA = 0.1f;

    private boolean mIsGrowing = true;

    public DeploymentZone(float pX, float pY, float pWidth, float pHeight,
            VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY, pWidth, pHeight, vertexBufferObjectManager);
        setColor(0.0f, 1.0f, 0.0f, INITIAL_ALPHA);
    }

    @Override
    protected void onManagedUpdate(final float pSecondsElapsed) {
        // alpha animation
        this.setAlpha(getAlpha() + ALPHA_ANIMATION_SPEED * (mIsGrowing ? 1 : -1));
        if (Math.abs(getAlpha() - INITIAL_ALPHA) > ALPHA_ANIMATION_LIMIT) {
            mIsGrowing = !mIsGrowing;
        }
        super.onManagedUpdate(pSecondsElapsed);
    }

}
