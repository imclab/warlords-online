package com.glevel.wwii.game.graphics;

import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.units.Unit;
import com.glevel.wwii.game.model.weapons.Weapon;

public class Crosshair extends Sprite {

    private static final float ROTATION_SPEED = 0.2f;
    private static final float SCALE_ANIMATION_SPEED = 0.005f;
    private static final float SCALE_ANIMATION_LIMIT = 0.1f;
    private static final float INITIAL_SCALE = 0.9f;

    private static final int DISTANCE_TEXT_X_OFFSET = -40, DISTANCE_TEXT_Y_OFFSET = -40;

    private Text mDistanceText = null;
    private boolean mIsGrowing = true;

    public Crosshair(final TextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager,
            Text distanceText) {
        super(0, 0, pTextureRegion, pVertexBufferObjectManager);
        setScale(INITIAL_SCALE);
        this.mDistanceText = distanceText;
    }

    public Crosshair(final TextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(0, 0, pTextureRegion, pVertexBufferObjectManager);
        setScale(INITIAL_SCALE);
    }

    @Override
    public void setPosition(float pX, float pY) {
        if (mDistanceText != null) {
            mDistanceText.setPosition(pX + DISTANCE_TEXT_X_OFFSET, pY + DISTANCE_TEXT_Y_OFFSET);
        }
        super.setPosition(pX, pY);
    }

    @Override
    public void setVisible(boolean pVisible) {
        if (mDistanceText != null) {
            mDistanceText.setVisible(pVisible);
        }
        super.setVisible(pVisible);
    }

    @Override
    protected void onManagedUpdate(final float pSecondsElapsed) {
        this.setRotation(getRotation() + ROTATION_SPEED);
        this.setScale(getScaleX() + SCALE_ANIMATION_SPEED * (mIsGrowing ? 1 : -1));
        if (Math.abs(getScaleX() - INITIAL_SCALE) > SCALE_ANIMATION_LIMIT) {
            mIsGrowing = !mIsGrowing;
        }
        super.onManagedUpdate(pSecondsElapsed);
    }

    public void updateDistanceLabel(int distance, Battle battle, Unit unit, Unit target) {
        mDistanceText.setText(distance / GameUtils.PIXEL_BY_METER + "m");
        if (target == null) {
            // move color
            mDistanceText.setColor(Color.WHITE);
        } else {
            Weapon w = unit.getBestWeapon(battle, target);
            if (w != null) {
                mDistanceText.setColor(w.getDistanceColor(unit, target));
            } else {
                // can't hurt target
                mDistanceText.setColor(Color.BLACK);
            }
        }
    }

}
