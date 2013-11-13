package com.giggs.apps.chaos.activities;

import java.util.HashMap;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.LayoutGameActivity;

import android.os.Bundle;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHandler;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHandler.TimingCategory;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHandler.TimingName;
import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.GameCreation;
import com.giggs.apps.chaos.game.GameGUI;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GraphicsFactory;
import com.giggs.apps.chaos.game.InputManager;
import com.giggs.apps.chaos.game.andengine.custom.CustomZoomCamera;
import com.giggs.apps.chaos.game.graphics.SelectionCircle;
import com.giggs.apps.chaos.game.graphics.TileSprite;
import com.giggs.apps.chaos.game.graphics.UnitSprite;
import com.giggs.apps.chaos.game.logic.GameLogic;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;

public class GameActivity extends LayoutGameActivity {

    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;

    private long mGameStartTime = 0L;
    private DatabaseHelper mDbHelper;
    private boolean mMustSaveGame = true;

    public Scene mScene;
    private ZoomCamera mCamera;
    public GameGUI mGameGUI;
    private GraphicsFactory mGameElementFactory;
    private InputManager mInputManager;

    public Sprite selectionCircle;

    public Battle battle;

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.mCamera = new CustomZoomCamera(100, 100, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                new FillResolutionPolicy(), mCamera);
        return engineOptions;
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // load battle
        // TODO
        // Bundle extras = getIntent().getExtras();
        // int nbPlayers = extras.getInt("nb_players", 4);
        // int myArmy = extras.getInt("my_army", 0);
        battle = GameCreation.createSoloGame(4, 0);

        mDbHelper = new DatabaseHelper(getApplicationContext());
        mGameGUI = new GameGUI(this);
        mGameGUI.setupGUI();
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_game;
    }

    @Override
    protected int getRenderSurfaceViewID() {
        return R.id.surfaceView;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        long startLoadingTime = System.currentTimeMillis();
        // init game element factory
        mGameElementFactory = new GraphicsFactory(this, getVertexBufferObjectManager(), getTextureManager());
        mGameElementFactory.initGraphics(battle);

        mGameGUI.hideLoadingScreen();
        pOnCreateResourcesCallback.onCreateResourcesFinished();

        GoogleAnalyticsHandler.sendTiming(getApplicationContext(), TimingCategory.resources, TimingName.load_game,
                (System.currentTimeMillis() - startLoadingTime) / 1000);
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mScene = new Scene();

        mScene.setOnAreaTouchTraversalFrontToBack();

        mScene.setBackground(new Background(0, 0, 0));

        mInputManager = new InputManager(this, mCamera);
        this.mScene.setOnSceneTouchListener(mInputManager);
        this.mScene.setTouchAreaBindingOnActionDownEnabled(true);

        /* Make the camera not exceed the bounds of the TMXEntity. */
        this.mCamera.setBounds(0, 0, battle.getMap().getHeight() * GameUtils.TILE_SIZE, battle.getMap().getWidth()
                * GameUtils.TILE_SIZE);
        this.mCamera.setBoundsEnabled(true);
        this.mCamera.setCenter(GameUtils.TILE_SIZE * battle.getMap().getWidth() / 2, GameUtils.TILE_SIZE
                * battle.getMap().getWidth() / 2);

        selectionCircle = new SelectionCircle(GraphicsFactory.mGfxMap.get("selection.png"),
                getVertexBufferObjectManager());

        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {

        // display map
        TileSprite tileSprite = null;
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                tileSprite = new TileSprite(x * GameUtils.TILE_SIZE, y * GameUtils.TILE_SIZE,
                        GraphicsFactory.mGfxMap.get(tile.getTerrain().getSpriteName()), getVertexBufferObjectManager(),
                        mScene, mInputManager, tile);
                tile.setSprite(tileSprite);
                mScene.attachChild(tileSprite);
                // update tile owner
                if (tile.getOwner() >= 0) {
                    tile.updateTileOwner(0, tile.getOwner());
                }

                // add units
                for (Unit unit : tile.getContent()) {
                    addUnitToScene(unit);
                }

                MapLogic.dispatchUnitsOnTile(tile);

            }
        }
        // init fogs of war
        GameLogic.updateFogsOfWar(battle, 0);

        pOnPopulateSceneCallback.onPopulateSceneFinished();
        startGame();
    }

    @Override
    public void onBackPressed() {
        pauseGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameGUI.onPause();
        GraphicsFactory.mGfxMap = new HashMap<String, TextureRegion>();
        GraphicsFactory.mTiledGfxMap = new HashMap<String, TiledTextureRegion>();
        if (mMustSaveGame) {
            // TODO save game
            // SaveGameHelper.saveGame(mDbHelper, battle);
        }
    }

    private void pauseGame() {
        mGameGUI.openGameMenu();
        mEngine.stop();
    }

    public void resumeGame() {
        mEngine.start();
    }

    private void startGame() {
        mGameStartTime = System.currentTimeMillis();
        mGameGUI.displayBigLabel(R.string.go, R.color.bg_btn_green);
    }

    private void endGame(final Player winningPlayer) {
        if (mGameStartTime > 0L) {
            GoogleAnalyticsHandler.sendTiming(getApplicationContext(), TimingCategory.in_game, TimingName.game_time,
                    (System.currentTimeMillis() - mGameStartTime) / 1000);
        }

        // stop engine
        mEngine.stop();

        // show battle report when big label animation is over
        // bigLabelAnimation.setAnimationListener(new AnimationListener() {
        // @Override
        // public void onAnimationStart(Animation animation) {
        // }
        //
        // @Override
        // public void onAnimationRepeat(Animation animation) {
        // }
        //
        // @Override
        // public void onAnimationEnd(Animation animation) {
        // goToReport(winningPlayer == battle.getMe());
        // }
        // });
        //
        // // show victory / defeat big label
        // if (winningPlayer == battle.getMe()) {
        // // victory
        // displayBigLabel(R.string.victory, R.color.green);
        // } else {
        // // defeat
        // displayBigLabel(R.string.defeat, R.color.red);
        // }

        // GoogleAnalyticsHandler.sendEvent(getApplicationContext(),
        // EventCategory.in_game, EventAction.end_game,
        // winningPlayer == battle.getMe() ? "victory" : "defeat");
    }

    private void goToReport(boolean victory) {
        // TODO
        // mMustSaveGame = false;
        // long battleId = SaveGameHelper.saveGame(mDbHelper, battle);
        // Intent i = new Intent(GameActivity.this, BattleReportActivity.class);
        // Bundle extras = new Bundle();
        // extras.putLong("game_id", battleId);
        // extras.putBoolean("victory", victory);
        // i.putExtras(extras);
        // startActivity(i);
        // finish();
    }

    public void addUnitToScene(Unit unit) {
        UnitSprite s = new UnitSprite(unit, mInputManager, GameUtils.TILE_SIZE * unit.getTilePosition().getX(),
                GameUtils.TILE_SIZE * unit.getTilePosition().getY(), GraphicsFactory.mTiledGfxMap.get(unit
                        .getSpriteName()), getVertexBufferObjectManager());
        s.setCanBeDragged(unit.getArmyIndex() == battle.getPlayers().get(0).getArmyIndex());
        unit.setSprite(s);
        mScene.registerTouchArea(s);
        mScene.attachChild(s);
    }
}
