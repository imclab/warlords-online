package com.giggs.apps.chaos.activities;

import java.util.ArrayList;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;

import android.content.Intent;
import android.widget.TextView;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.GameGUI;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GraphicsFactory;
import com.giggs.apps.chaos.game.InputManager;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.graphics.SelectionCircle;
import com.giggs.apps.chaos.game.graphics.TileSprite;
import com.giggs.apps.chaos.game.logic.GameLogic;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.game.model.units.human.Soldier;
import com.giggs.apps.chaos.game.model.units.orc.Goblin;
import com.giggs.apps.chaos.game.model.units.orc.Orc;

public class TutorialActivity extends GameActivity {

    private int tutorialStep = 0;
    private TextView tutorialTV;
    private static final int[] TUTORIAL_STEPS_TEXT = { R.string.tutorial_0, R.string.tutorial_1, R.string.tutorial_2,
            R.string.tutorial_3, R.string.tutorial_4, R.string.tutorial_5, R.string.tutorial_6, R.string.tutorial_7,
            R.string.tutorial_8, R.string.tutorial_9, R.string.tutorial_10, R.string.tutorial_11, R.string.tutorial_12,
            R.string.tutorial_13 };

    @Override
    protected void initActivity() {
        mDbHelper = new DatabaseHelper(getApplicationContext());

        battle = new Battle();
        // setup players
        Player me = new Player("0", "Me", ArmiesData.HUMAN, 0, false);
        battle.getPlayers().add(me);
        Player bot = new Player("1", "Enemy Bot !", ArmiesData.ORCS, 1, false);
        battle.getPlayers().add(bot);

        // setup map
        Map map = new Map();
        Tile[][] tiles = new Tile[5][5];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                tiles[y][x] = new Tile(x, y, TerrainData.forest);
            }
        }
        tiles[2][1].setTerrain(TerrainData.grass);
        tiles[2][2].setTerrain(TerrainData.castle);
        tiles[2][2].setOwner(0);
        Unit unit = new Soldier(0);
        unit.setTilePosition(tiles[2][2]);
        tiles[2][3].setTerrain(TerrainData.grass);
        tiles[3][1].setTerrain(TerrainData.grass);
        tiles[3][2].setTerrain(TerrainData.farm);
        map.getFarms().add(tiles[3][2]);
        tiles[3][3].setTerrain(TerrainData.fort);
        map.getForts().add(tiles[3][3]);
        unit = new Goblin(1);
        unit.setTilePosition(tiles[3][3]);
        tiles[3][3].setOwner(1);
        tiles[4][4].setTerrain(TerrainData.castle);
        tiles[4][4].setOwner(1);
        unit = new Orc(1);
        unit.setTilePosition(tiles[4][4]);
        map.setTiles(tiles);
        battle.setMap(map);

        mGameGUI = new GameGUI(this);
        mGameGUI.setupGUI();
        mGameGUI.showConfirm = false;

        mMustSaveGame = false;

        // add tutorial layout
        tutorialTV = (TextView) findViewById(R.id.tutorialLayout);
        tutorialTV.setText(TUTORIAL_STEPS_TEXT[tutorialStep]);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_tutorial;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        // init game element factory
        mGameElementFactory = new GraphicsFactory(this, getVertexBufferObjectManager(), getTextureManager());
        mGameElementFactory.initGraphics(battle);

        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mScene = new Scene();

        mScene.setOnAreaTouchTraversalFrontToBack();

        mScene.setBackground(new Background(0, 0, 0));

        mInputManager = new InputManager(this, mCamera);
        this.mScene.setOnSceneTouchListener(mInputManager);
        this.mScene.setTouchAreaBindingOnActionDownEnabled(true);

        this.mCamera.setBounds(0, 0, battle.getMap().getHeight() * GameUtils.TILE_SIZE, battle.getMap().getWidth()
                * GameUtils.TILE_SIZE);
        this.mCamera.setBoundsEnabled(true);

        // add selection circle
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

        // add initial units to scene
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                for (Unit unit : tile.getContent()) {
                    unit.setTile(tile);
                    addUnitToScene(unit);
                }
                if (tile.getTerrain() == TerrainData.castle && battle.getMeSoloMode().getArmyIndex() == tile.getOwner()) {
                    castleTile = tile;
                }
                MapLogic.dispatchUnitsOnTile(tile);
            }
        }

        // init fogs of war
        GameLogic.updateFogsOfWar(battle, 0);

        wait(300);
        mGameGUI.hideLoadingScreen();

        pOnPopulateSceneCallback.onPopulateSceneFinished();

        startTutorial();
    }

    private void startTutorial() {
        mGameGUI.displayBigLabel(getString(R.string.welcome_tutorial), R.color.white);

        // center map on player's castle
        mCamera.setCenter(castleTile.getX() * GameUtils.TILE_SIZE, castleTile.getY() * GameUtils.TILE_SIZE);
    }

    @Override
    public void endGame(final Player winningPlayer) {
        mGameGUI.displayVictoryLabel(true);
    }

    @Override
    public void runTurn() {
        getEngine().stop();

        // update battle
        int winnerIndex = GameLogic.runTurn(battle);

        // add new units, remove dead ones
        for (Unit u : battle.getUnitsToRemove()) {
            removeUnit(u);
        }
        battle.setUnitsToRemove(new ArrayList<Unit>());
        for (Unit u : battle.getUnitsToAdd()) {
            addUnitToScene(u);
        }
        battle.setUnitsToAdd(new ArrayList<Unit>());

        // dispatch units properly on tiles
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                MapLogic.dispatchUnitsOnTile(tile);
            }
        }

        // update fogs of war
        GameLogic.updateFogsOfWar(battle, 0);

        getEngine().start();

        if (tutorialStep < TUTORIAL_STEPS_TEXT.length - 1) {
            if (tutorialStep == 5 && battle.getMap().getFarms().get(0).getOwner() >= 0 || tutorialStep == 8
                    && battle.getMap().getForts().get(0).getOwner() == 0 || tutorialStep != 5 && tutorialStep != 8) {
                tutorialStep++;
                tutorialTV.setText(TUTORIAL_STEPS_TEXT[tutorialStep]);
            }
        }

        // update my gold amount
        mGameGUI.updateGoldAmount(battle.getMeSoloMode().getGold());
        mGameGUI.updateEconomyBalance(battle.getMeSoloMode().getGameStats().getEconomy()
                .get(battle.getMeSoloMode().getGameStats().getEconomy().size() - 1));

        if (winnerIndex >= 0) {
            endGame(battle.getPlayers().get(winnerIndex));
            return;
        } else if (winnerIndex == GameLogic.SOLO_PLAYER_DEFEAT) {
            endGame(null);
        } else {
            // game continues !
            // show new turn count
            mGameGUI.displayBigLabel(getString(R.string.turn_count, battle.getTurnCount()), R.color.white);
        }
    }

    @Override
    public void goToReport(boolean victory) {
        // stop engine
        mEngine.stop();

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

}
