package com.giggs.apps.chaos.game;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;

import android.view.MotionEvent;

import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.game.graphics.UnitSprite;
import com.giggs.apps.chaos.game.logic.GameLogic;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.orders.Order;
import com.giggs.apps.chaos.game.model.units.Unit;

public class InputManager implements IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {

    private ZoomCamera mCamera;
    private float mPinchZoomStartedCameraZoomFactor;
    private SurfaceScrollDetector mScrollDetector;
    private PinchZoomDetector mPinchZoomDetector;
    private static final int ACTION_MOVE_THRESHOLD = 100;

    public boolean isLongPressTriggered = false;
    private boolean isDragged = false;

    public UnitSprite selectedElement = null;
    private GameActivity mGameActivity;
    private float lastX;
    private float lastY;

    public InputManager(GameActivity gameActivity, ZoomCamera camera) {
        mCamera = camera;
        this.mScrollDetector = new SurfaceScrollDetector(this);
        this.mPinchZoomDetector = new PinchZoomDetector(this);
        this.mGameActivity = gameActivity;
    }

    /**
     * Map scrolling
     */
    @Override
    public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX,
            final float pDistanceY) {
        final float zoomFactor = this.mCamera.getZoomFactor();
        this.mCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX,
            final float pDistanceY) {
        final float zoomFactor = this.mCamera.getZoomFactor();
        this.mCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX,
            final float pDistanceY) {
        final float zoomFactor = this.mCamera.getZoomFactor();
        this.mCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    /**
     * Map zooming
     */
    @Override
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
        this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
    }

    @Override
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent,
            final float pZoomFactor) {
        this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent,
            final float pZoomFactor) {
        this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }

    /**
     * Click on map
     */
    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

        if (this.mPinchZoomDetector.isZooming()) {
            this.mScrollDetector.setEnabled(false);
        } else {
            if (pSceneTouchEvent.isActionDown()) {
                this.mScrollDetector.setEnabled(true);
            }
            this.mScrollDetector.onTouchEvent(pSceneTouchEvent);

            switch (pSceneTouchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragged = false;
                lastX = pSceneTouchEvent.getX();
                lastY = pSceneTouchEvent.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(pSceneTouchEvent.getX() - lastX) + Math.abs(pSceneTouchEvent.getY() - lastY) > ACTION_MOVE_THRESHOLD) {
                    isDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isDragged) {
                    // simple tap
                    clickOnMap(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                }
                isDragged = false;
                break;
            }
        }
        return true;
    }

    private void clickOnMap(float x, float y) {
        if (selectedElement != null) {
            selectedElement.detachChild(mGameActivity.selectionCircle);
            selectedElement = null;
        }
    }

    public void onSelectElement(UnitSprite gameSprite) {
        if (selectedElement != null) {
            unselectElement(selectedElement);
        }

        // add selection circle
        selectedElement = gameSprite;
        gameSprite.setZIndex(10);
        gameSprite.attachChild(mGameActivity.selectionCircle);
        mGameActivity.selectionCircle.setZIndex(-10);

        // show move options
        for (Tile tile : GameLogic.getMoveOptions(mGameActivity.battle, selectedElement.getGameElement())) {
            if (((Unit) gameSprite.getGameElement()).canMove(tile)) {
                tile.setMoveOption(true);
            }
        }
    }

    private Tile hoveredTile = null;

    public void onTouchMove(float x, float y) {
        Tile tile = MapLogic.getTileAtCoordinates(mGameActivity.battle.getMap(), x, y);
        if (tile.isMoveOption()) {
            if (hoveredTile != null) {
                hoveredTile.setHovered(false);
            }

            hoveredTile = tile;
            tile.setHovered(true);
        } else if (hoveredTile != null) {
            hoveredTile.setHovered(false);
            hoveredTile = null;
        }
    }

    public void onActionUp(float x, float y) {
        if (selectedElement != null) {
            Tile tile = MapLogic.getTileAtCoordinates(mGameActivity.battle.getMap(), x, y);
            if (tile.isMoveOption()) {
                giveMoveOrderToUnit(tile);
            } else if (tile == selectedElement.getGameElement().getTilePosition()) {
                giveDefendOrder();
            }
            unselectElement(selectedElement);
        }
    }

    public void unselectElement(UnitSprite gameSprite) {
        // remove current selection circle
        selectedElement.detachChild(mGameActivity.selectionCircle);

        // hide move options
        for (Tile tile : GameLogic.getMoveOptions(mGameActivity.battle, selectedElement.getGameElement())) {
            tile.setMoveOption(false);
        }

        selectedElement = null;
    }

    public void giveDefendOrder() {
        Unit unit = (Unit) selectedElement.getGameElement();
        Order order = new DefendOrder(unit);
        mGameActivity.battle.getPlayers().get(unit.getArmyIndex()).giveOrder(order, unit.getOrder());
        unit.setOrder(order);
    }

    public void giveMoveOrderToUnit(Tile destination) {
        Unit unit = (Unit) selectedElement.getGameElement();
        Order order = new MoveOrder(unit, destination);
        mGameActivity.battle.getPlayers().get(unit.getArmyIndex()).giveOrder(order, unit.getOrder());
        unit.setOrder(order);
    }

    public void onBuyIconClicked(Tile mTile) {
        mGameActivity.mGameGUI.showBuyOptions(mTile);
    }

}
