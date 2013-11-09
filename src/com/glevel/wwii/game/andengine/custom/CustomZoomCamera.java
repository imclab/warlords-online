package com.glevel.wwii.game.andengine.custom;

import org.andengine.engine.camera.ZoomCamera;

public class CustomZoomCamera extends ZoomCamera {

    // add zooming limits
    private static final float ZOOM_MIN_LIMIT = 0.5f, ZOOM_MAX_LIMIT = 1.5f;

    public CustomZoomCamera(final float pX, final float pY, final float pWidth, final float pHeight) {
        super(pX, pY, pWidth, pHeight);
    }

    @Override
    public void setZoomFactor(final float pZoomFactor) {
        if (pZoomFactor >= ZOOM_MIN_LIMIT && pZoomFactor <= ZOOM_MAX_LIMIT) {
            this.mZoomFactor = pZoomFactor;

            if (this.mBoundsEnabled) {
                this.ensureInBounds();
            }
        }
    }

}
