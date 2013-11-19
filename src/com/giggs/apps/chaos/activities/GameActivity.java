package com.giggs.apps.chaos.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import android.content.Intent;
import android.os.Bundle;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.TimingCategory;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.TimingName;
import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.GameCreation;
import com.giggs.apps.chaos.game.GameGUI;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GraphicsFactory;
import com.giggs.apps.chaos.game.InputManager;
import com.giggs.apps.chaos.game.SaveGameHelper;
import com.giggs.apps.chaos.game.andengine.custom.CustomZoomCamera;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.graphics.SelectionCircle;
import com.giggs.apps.chaos.game.graphics.TileSprite;
import com.giggs.apps.chaos.game.graphics.UnitSprite;
import com.giggs.apps.chaos.game.logic.GameLogic;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.Order;
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

        mDbHelper = new DatabaseHelper(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // new game
            int myArmy = extras.getInt("my_army", 0);
            int nbPlayers = extras.getInt("nb_players", 4);
            battle = GameCreation.createSoloGame(nbPlayers, myArmy);
            SaveGameHelper.deleteSavedBattles(mDbHelper);
            GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.nb_players, ""
                    + nbPlayers);
            GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game,
                    EventAction.solo_player_army, ArmiesData.values()[myArmy].name());
        } else {
            // load game
            battle = mDbHelper.getBattleDao().get(null, null, null, null).get(0);
        }

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

        pOnCreateResourcesCallback.onCreateResourcesFinished();

        GoogleAnalyticsHelper.sendTiming(getApplicationContext(), TimingCategory.resources, TimingName.load_game,
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

        // add selection circle
        selectionCircle = new SelectionCircle(GraphicsFactory.mGfxMap.get("selection.png"),
                getVertexBufferObjectManager());

        // add minimap
        // minimap = new Rectangle(0, 0, 30, 30,
        // getVertexBufferObjectManager());
        // minimap.setColor(Color.WHITE);
        // minimap.setAlpha(0.6f);
        // mScene.registerTouchArea(minimap);
        // mScene.attachChild(minimap);

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
                if (battle.isWinter()) {
                    tileSprite.updateWeather(true);
                }
                mScene.attachChild(tileSprite);
                // update tile owner
                if (tile.getOwner() >= 0) {
                    tile.updateTileOwner(0, tile.getOwner());
                }
            }
        }

        // add initial units
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                for (Unit unit : tile.getContent()) {
                    unit.setTile(tile);
                    addUnitToScene(unit);
                }
                if (tile.getTerrain() == TerrainData.farm) {
                    battle.getMap().getFarms().add(tile);
                } else if (tile.getTerrain() == TerrainData.castle) {
                    battle.getMap().getCastles().add(tile);
                } else if (tile.getTerrain() == TerrainData.fort) {
                    battle.getMap().getForts().add(tile);
                }
                MapLogic.dispatchUnitsOnTile(tile);
            }
        }

        // init fogs of war
        GameLogic.updateFogsOfWar(battle, 0);

        wait(500);
        mGameGUI.hideLoadingScreen();

        if (battle.getId() >= 0L) {
            initLoadedGame();
        } else {
            initNewGame();
        }

        pOnPopulateSceneCallback.onPopulateSceneFinished();
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
            SaveGameHelper.saveGame(mDbHelper, battle);
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
        mGameGUI.displayBigLabel(getString(R.string.turn_count, battle.getTurnCount()), R.color.white);
    }

    private void initNewGame() {
        startGame();
    }

    private void initLoadedGame() {
        for (Player player : battle.getPlayers()) {
            player.setLstTurnOrders(new ArrayList<Order>());
        }

        List<Integer> economyHistory = battle.getMeSoloMode().getGameStats().getEconomy();
        if (economyHistory.size() > 0) {
            mGameGUI.updateEconomyBalance(economyHistory.get(economyHistory.size() - 1));
        }

        startGame();
    }

    public void addUnitToScene(Unit unit) {
        UnitSprite s = new UnitSprite(unit, mInputManager, GameUtils.TILE_SIZE * unit.getTilePosition().getX(),
                GameUtils.TILE_SIZE * unit.getTilePosition().getY(), GraphicsFactory.mTiledGfxMap.get(unit
                        .getSpriteName()), getVertexBufferObjectManager());
        s.setCanBeDragged(unit.getArmyIndex() == battle.getMeSoloMode().getArmyIndex());
        unit.setSprite(s);
        unit.updateMorale(0);
        unit.updateExperience(0);
        mScene.registerTouchArea(s);
        mScene.attachChild(s);
    }

    public void removeUnit(final Unit unit) {
        this.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                mScene.detachChild(unit.getSprite());
            }
        });
        unit.getTilePosition().getContent().remove(unit);
    }

    public void endGame(final Player winningPlayer) {
        if (mGameStartTime > 0L) {
            GoogleAnalyticsHelper.sendTiming(getApplicationContext(), TimingCategory.in_game, TimingName.game_time,
                    (System.currentTimeMillis() - mGameStartTime) / 1000);
        }

        GoogleAnalyticsHelper.sendTiming(getApplicationContext(), TimingCategory.in_game, TimingName.game_nb_turn,
                battle.getTurnCount());
        GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.against_AI,
                winningPlayer == battle.getMeSoloMode() ? "victory" : "defeat");

        mGameGUI.displayVictoryLabel(winningPlayer == battle.getMeSoloMode());
    }

    public void goToReport(boolean victory) {
        // stop engine
        mEngine.stop();

        Intent intent = new Intent(GameActivity.this, BattleReportActivity.class);
        startActivity(intent);
        finish();
    }

}
