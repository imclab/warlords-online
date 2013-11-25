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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.giggs.apps.chaos.game.andengine.custom.CustomLayoutGameActivity;
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
import com.giggs.apps.chaos.game.multiplayer.Message;
import com.giggs.apps.chaos.game.multiplayer.Message.MessageType;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

public class GameActivity extends CustomLayoutGameActivity implements RealTimeMessageReceivedListener,
        RealTimeReliableMessageSentListener, RoomUpdateListener, RoomStatusUpdateListener {

    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;

    private long mGameStartTime = 0L;
    protected DatabaseHelper mDbHelper;
    protected boolean mMustSaveGame = true;

    public Scene mScene;
    protected ZoomCamera mCamera;
    public GameGUI mGameGUI;
    protected GraphicsFactory mGameElementFactory;
    protected InputManager mInputManager;

    public Sprite selectionCircle;

    public Battle battle;
    protected Tile castleTile = null;
    public int myArmyIndex = 0;
    public boolean isMultiplayerGame = false;
    private String roomId;

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
        initActivity();
    }

    protected void initActivity() {
        mDbHelper = new DatabaseHelper(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            int myArmy = extras.getInt("my_army", 0);
            if (extras.getString("room_id") != null) {
                // new multiplayer game
                isMultiplayerGame = true;
                mMustSaveGame = false;
                roomId = extras.getString("room_id");
                myArmyIndex = extras.getInt("army_index");
                battle = extras.getParcelable("battle");
            } else {
                // new solo game
                int nbPlayers = extras.getInt("nb_players", 4);
                battle = GameCreation.createSoloGame(nbPlayers, myArmy, 0, null);
                SaveGameHelper.deleteSavedBattles(mDbHelper);

                GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.nb_players,
                        "" + nbPlayers);
            }
            GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game,
                    EventAction.solo_player_army, ArmiesData.values()[myArmy].name());
        } else {
            // load solo game
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

        // add initial units to scene
        for (int y = 0; y < battle.getMap().getHeight(); y++) {
            for (int x = 0; x < battle.getMap().getWidth(); x++) {
                Tile tile = battle.getMap().getTiles()[y][x];
                for (Unit unit : tile.getContent()) {
                    unit.setTile(tile);
                    addUnitToScene(unit);
                }
                if (tile.getTerrain() == TerrainData.castle && myArmyIndex == tile.getOwner()) {
                    castleTile = tile;
                }
                MapLogic.dispatchUnitsOnTile(tile);
            }
        }

        // init fogs of war
        GameLogic.updateFogsOfWar(battle, myArmyIndex);

        wait(300);
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

        // center map on player's castle
        mCamera.setCenter(castleTile.getX() * GameUtils.TILE_SIZE, castleTile.getY() * GameUtils.TILE_SIZE);
    }

    private void initNewGame() {
        startGame();
    }

    private void initLoadedGame() {
        for (Player player : battle.getPlayers()) {
            player.setLstTurnOrders(new ArrayList<Order>());
        }

        List<Integer> economyHistory = battle.getMe(myArmyIndex).getGameStats().getEconomy();
        if (economyHistory.size() > 0) {
            mGameGUI.updateEconomyBalance(economyHistory.get(economyHistory.size() - 1));
        }

        startGame();
    }

    public void addUnitToScene(Unit unit) {
        UnitSprite s = new UnitSprite(unit, mInputManager, GameUtils.TILE_SIZE * unit.getTilePosition().getX(),
                GameUtils.TILE_SIZE * unit.getTilePosition().getY(), GraphicsFactory.mTiledGfxMap.get(unit
                        .getSpriteName()), getVertexBufferObjectManager());
        s.setCanBeDragged(unit.getArmyIndex() == myArmyIndex);
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
                mScene.unregisterTouchArea(unit.getSprite());
                mScene.detachChild(unit.getSprite());
            }
        });
    }

    public void endGame(final Player winningPlayer) {
        if (mGameStartTime > 0L) {
            GoogleAnalyticsHelper.sendTiming(getApplicationContext(), TimingCategory.in_game, TimingName.game_time,
                    (System.currentTimeMillis() - mGameStartTime) / 1000);
        }

        GoogleAnalyticsHelper.sendTiming(getApplicationContext(), TimingCategory.in_game, TimingName.game_nb_turn,
                battle.getTurnCount());
        GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.against_AI,
                winningPlayer == battle.getMe(myArmyIndex) ? "victory" : "defeat");

        mGameGUI.displayVictoryLabel(winningPlayer == battle.getMe(myArmyIndex));
    }

    public void goToReport() {
        // stop engine
        mEngine.stop();

        Intent intent = new Intent(GameActivity.this, BattleReportActivity.class);
        Bundle args = new Bundle();
        args.putInt("army_index", myArmyIndex);
        args.putParcelable("battle", battle);
        intent.putExtras(args);
        startActivity(intent);
        finish();
    }

    public void runTurn() {
        getEngine().stop();

        // update battle
        int winnerIndex = GameLogic.runTurn(battle, myArmyIndex);

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
        GameLogic.updateFogsOfWar(battle, myArmyIndex);

        getEngine().start();

        // update my gold amount
        mGameGUI.updateGoldAmount(battle.getMe(myArmyIndex).getGold());
        mGameGUI.updateEconomyBalance(battle.getMe(myArmyIndex).getGameStats().getEconomy()
                .get(battle.getMe(myArmyIndex).getGameStats().getEconomy().size() - 1));

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

    public void updateUnitProduction(final TileSprite sprite, final TextureRegion texture) {
        runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                sprite.updateUnitProduction(texture);
            }
        });
    }

    public void sendOrders() {
        for (Player p : battle.getPlayers()) {
            if (p.getArmyIndex() != myArmyIndex) {
                getGamesClient().sendReliableRealTimeMessage(
                        this,
                        new Message(myArmyIndex, MessageType.TURN_ORDERS, SaveGameHelper.toByte(
                                battle.getPlayers().get(myArmyIndex).getLstTurnOrders()).toByteArray()).toByte(),
                        roomId, p.getId());
            }
        }

    }

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onSignInSucceeded() {
        if (myArmyIndex == 0) {
            RoomConfig.Builder rtmConfigBuilder = makeBasicRoomConfigBuilder();
            rtmConfigBuilder.setSocketCommunicationEnabled(true);
            ArrayList<String> ids = new ArrayList<String>();
            for (Player p : battle.getPlayers()) {
                ids.add(p.getId());
            }
            rtmConfigBuilder.addPlayersToInvite(ids);
            getGamesClient().createRoom(rtmConfigBuilder.build());
        }
    }

    @Override
    public void onRealTimeMessageSent(int arg0, int arg1, String arg2) {
    }

    public int nbOrdersReceived = 0;
    public boolean hasSendOrders = false;

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        Message message = SaveGameHelper.getMessageFromByte(rtm.getMessageData());
        Log.d("message", message.getType().name());
        switch (message.getType()) {
        case TURN_ORDERS:
            battle.getPlayers().get(message.getSenderIndex())
                    .setLstTurnOrders((List<Order>) SaveGameHelper.getObjectFromByte(message.getContent()));
            onNewOrders();
            break;
        }
    }

    public void onNewOrders() {
        nbOrdersReceived++;
        int nbOrdersNeeded = 0;
        for (Player p : battle.getPlayers()) {
            if (!p.isDefeated()) {
                nbOrdersNeeded++;
            }
        }
        if (nbOrdersReceived == nbOrdersNeeded) {
            nbOrdersReceived = 0;
            runTurn();
            mGameGUI.mSendOrdersButton.setVisibility(View.VISIBLE);
            hasSendOrders = false;
        }
    }

    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this).setMessageReceivedListener(this).setRoomStatusUpdateListener(this);
    }

    @Override
    public void onJoinedRoom(int arg0, Room arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLeftRoom(int arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomConnected(int arg0, Room arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesClient.STATUS_OK) {
            // show error message, return to main screen.
            ApplicationUtils.showToast(getApplicationContext(), R.string.error_room_creation, Toast.LENGTH_SHORT);
            return;
        }
        
        roomId = room.getRoomId();
    }

    @Override
    public void onConnectedToRoom(Room arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDisconnectedFromRoom(Room arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerDeclined(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerInvitedToRoom(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerJoined(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerLeft(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeersConnected(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeersDisconnected(Room arg0, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomAutoMatching(Room arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomConnecting(Room arg0) {
        // TODO Auto-generated method stub

    }

}
