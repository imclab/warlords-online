package com.giggs.apps.chaos.game.graphics;

import java.util.ArrayList;
import java.util.List;

import org.andengine.entity.primitive.Line;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GameUtils.Direction;
import com.giggs.apps.chaos.game.GraphicsFactory;
import com.giggs.apps.chaos.game.InputManager;
import com.giggs.apps.chaos.game.model.GameElement;
import com.giggs.apps.chaos.game.model.units.Unit;

public class UnitSprite extends Sprite {

    private final GameElement mGameElement;
    private InputManager mInputManager;
    private boolean mIsSelected = false;

    private static final int UNIT_SIZE = 100;

    private boolean canBeDragged;
    private Line experienceLine;
    private Line moraleLine;
    private List<CharacterSprite> lstCharacters = new ArrayList<CharacterSprite>();

    public UnitSprite(GameElement gameElement, InputManager inputManager, float pX, float pY,
            ITiledTextureRegion pTiledTextureRegion, VertexBufferObjectManager mVertexBufferObjectManager) {
        super(pX, pY, GraphicsFactory.mGfxMap.get("transparent.png") != null ? GraphicsFactory.mGfxMap
                .get("transparent.png") : GraphicsFactory.mGfxMap.get("transparent2.png"), mVertexBufferObjectManager);
        mGameElement = gameElement;
        mInputManager = inputManager;
        // add characters
        for (int n = 0; n < 1 + ((Unit) getGameElement()).getHealth() / 101; n++) {
            CharacterSprite characterSprite = new CharacterSprite((n % GameUtils.NUMBER_CHARACTERS_IN_ROW) * UNIT_SIZE
                    / GameUtils.NUMBER_CHARACTERS_IN_ROW, (n / GameUtils.NUMBER_CHARACTERS_IN_ROW) * UNIT_SIZE
                    / GameUtils.NUMBER_CHARACTERS_IN_ROW, pTiledTextureRegion, getVertexBufferObjectManager());
            lstCharacters.add(characterSprite);
            attachChild(characterSprite);
        }
        setSize(UNIT_SIZE, UNIT_SIZE);

        // add orders
        Sprite moveOrderSprite = new Sprite(UNIT_SIZE * 0.3f, UNIT_SIZE * 0.2f,
                GraphicsFactory.mGfxMap.get("move_order.png"), getVertexBufferObjectManager());
        moveOrderSprite.setTag(R.string.tag_move_order);
        moveOrderSprite.setVisible(false);
        attachChild(moveOrderSprite);
        Sprite defendOrderSprite = new Sprite(UNIT_SIZE * 0.2f, UNIT_SIZE * 0.2f,
                GraphicsFactory.mGfxMap.get("defend_order.png"), getVertexBufferObjectManager());
        defendOrderSprite.setTag(R.string.tag_defend_order);
        defendOrderSprite.setVisible(false);
        attachChild(defendOrderSprite);

        // add morale and experience
        moraleLine = new Line(40, 0, 90, 0, getVertexBufferObjectManager());
        moraleLine.setColor(0.0f, 0.0f, 1.0f, 0.7f);
        moraleLine.setLineWidth(6.0f);
        attachChild(moraleLine);
        experienceLine = new Line(40, 6, 90, 6, getVertexBufferObjectManager());
        experienceLine.setColor(0.0f, 1.0f, 0.0f, 0.7f);
        experienceLine.setLineWidth(6.0f);
        experienceLine.setScaleX(0.0f);
        attachChild(experienceLine);

        // add flag
        Sprite flagSprite = new Sprite(-20, -30, GraphicsFactory.mGfxMap.get("blason_"
                + (((Unit) getGameElement()).getArmyIndex() + 1) + ".png"), getVertexBufferObjectManager());
        flagSprite.setScale(0.5f);
        attachChild(flagSprite);

        stand();
    }

    public void walk(Direction direction) {
        for (int n = 0; n < getChildCount(); n++) {
            if (getChildByIndex(n) instanceof CharacterSprite) {
                ((CharacterSprite) getChildByIndex(n)).walk(direction);
            }
        }
        // update orders sprite
        Sprite moveOrderSprite = getMoveOrderSprite();
        moveOrderSprite.setRotation(90.0f * direction.ordinal());
        moveOrderSprite.setVisible(true);
        getDefendOrderSprite().setVisible(false);
    }

    public void stand() {
        for (int n = 0; n < getChildCount(); n++) {
            if (getChildByIndex(n) instanceof CharacterSprite) {
                ((CharacterSprite) getChildByIndex(n)).stand();
            }
        }
        hideOrders();
    }

    public void defend() {
        stand();
        // update orders sprite
        getMoveOrderSprite().setVisible(false);
        getDefendOrderSprite().setVisible(true);
    }

    private Sprite getMoveOrderSprite() {
        return (Sprite) getChildByTag(R.string.tag_move_order);
    }

    private Sprite getDefendOrderSprite() {
        return (Sprite) getChildByTag(R.string.tag_defend_order);
    }

    public void hideOrders() {
        getMoveOrderSprite().setVisible(false);
        getDefendOrderSprite().setVisible(false);
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX,
            final float pTouchAreaLocalY) {
        // zoom has priority
        if (pSceneTouchEvent.getMotionEvent().getPointerCount() > 1) {
            return false;
        }

        switch (pSceneTouchEvent.getAction()) {
        case TouchEvent.ACTION_DOWN:
            if (canBeDragged) {
                // element is selected
                mInputManager.onSelectElement(this);
                mIsSelected = true;
            }
            break;
        case TouchEvent.ACTION_MOVE:
            if (mIsSelected && canBeDragged) {
                mInputManager.onTouchMove(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
            }
            break;
        case TouchEvent.ACTION_UP:
            if (mIsSelected) {
                mInputManager.onActionUp(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                mIsSelected = false;
            }
            break;
        }
        return true;
    }

    public GameElement getGameElement() {
        return mGameElement;
    }

    public void setCanBeDragged(boolean canBeDragged) {
        this.canBeDragged = canBeDragged;
    }

    public void updateMorale(int newMorale) {
        moraleLine.setScaleX(newMorale / 100.0f);
    }

    public void updateExperience(int newExperience) {
        experienceLine.setScaleX(newExperience / 100.0f);
    }

    public void updateHealth(int health) {
        for (int n = 0; n < lstCharacters.size(); n++) {
            if (health < 100) {
                // when only one character left, show the centered one
                lstCharacters.get(n).setVisible(n == 1);
            } else {
                lstCharacters.get(n).setVisible(health / 100.0f >= n);
            }
        }
    }
}
