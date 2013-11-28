package com.giggs.apps.chaos.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.fragments.MultiplayerFragment;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.TimingCategory;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.TimingName;
import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.GameConverterHelper;
import com.giggs.apps.chaos.game.GameCreation;
import com.giggs.apps.chaos.game.GameGUI;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.GraphicsFactory;
import com.giggs.apps.chaos.game.InputManager;
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
import com.giggs.apps.chaos.game.model.orders.BuyOrder;
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.orders.Order;
import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.game.multiplayer.ChatMessage;
import com.giggs.apps.chaos.game.multiplayer.Message;
import com.giggs.apps.chaos.game.multiplayer.Message.MessageType;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig.Builder;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

public class GameActivity extends CustomLayoutGameActivity implements RoomUpdateListener,
        RealTimeMessageReceivedListener, RoomStatusUpdateListener, RealTimeReliableMessageSentListener {

    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;

    private long mGameStartTime = 0L;
    protected DatabaseHelper mDbHelper;
    public Battle battle = null;
    protected Tile castleTile = null;

    /**
     * By default, solo game
     */
    protected boolean mMustSaveGame = true;
    public int myArmyIndex = -1;
    public boolean mIsMultiplayerGame = false;

    public Scene mScene;
    protected ZoomCamera mCamera;
    public GameGUI mGameGUI;
    protected GraphicsFactory mGameElementFactory;
    protected InputManager mInputManager;
    public Sprite selectionCircle;

    private MultiplayerFragment multiplayerFragment;
    private OnCreateResourcesCallback pOnCreateResourcesCallback;

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
        initGameActivity();

        // If the device goes to sleep during handshake or gameplay, the player
        // will be disconnected from the room
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void initGameActivity() {
        mDbHelper = new DatabaseHelper(getApplicationContext());
        timer = new Timer();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int myArmy = extras.getInt("my_army", 0);

            if (extras.getBoolean("multiplayer")) {
                // new multiplayer game
                initMultiplayerGame();
            } else {
                // new solo game
                int nbPlayers = extras.getInt("nb_players", 4);
                myArmyIndex = 0;
                battle = GameCreation.createSoloGame(nbPlayers, myArmy, 0, null);
                // init GUI
                mGameGUI = new GameGUI(this);
                GameConverterHelper.deleteSavedBattles(mDbHelper);
                // analytics
                GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.nb_players,
                        "" + nbPlayers);
            }
            // analytics
            GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game,
                    EventAction.solo_player_army, ArmiesData.values()[myArmy].name());
        } else {
            // load solo game
            myArmyIndex = 0;
            battle = mDbHelper.getBattleDao().get(null, null, null, null).get(0);
            // init GUI
            mGameGUI = new GameGUI(this);
        }
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
        this.pOnCreateResourcesCallback = pOnCreateResourcesCallback;
        if (battle != null) {
            long startLoadingTime = System.currentTimeMillis();
            // init game element factory
            mGameElementFactory = new GraphicsFactory(this, getVertexBufferObjectManager(), getTextureManager());
            mGameElementFactory.initGraphics(battle);
            // analytics
            GoogleAnalyticsHelper.sendTiming(getApplicationContext(), TimingCategory.resources, TimingName.load_game,
                    (System.currentTimeMillis() - startLoadingTime) / 1000);
            pOnCreateResourcesCallback.onCreateResourcesFinished();
        }
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        // prepare scene
        mScene = new Scene();
        mScene.setOnAreaTouchTraversalFrontToBack();
        mScene.setBackground(new Background(0, 0, 0));
        mInputManager = new InputManager(this, mCamera);
        mScene.setOnSceneTouchListener(mInputManager);
        mScene.setTouchAreaBindingOnActionDownEnabled(true);

        // make the camera not exceed the bounds of the bounds
        mCamera.setBounds(0, 0, battle.getMap().getHeight() * GameUtils.TILE_SIZE, battle.getMap().getWidth()
                * GameUtils.TILE_SIZE);
        mCamera.setBoundsEnabled(true);
        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
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

        pOnPopulateSceneCallback.onPopulateSceneFinished();
        startGame();
    }

    private void startGame() {
        // add tiles to scene
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

        // add units to scene
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

        if (battle.getId() >= 0L) {
            // for loaded games...
            for (Player player : battle.getPlayers()) {
                player.setLstTurnOrders(new ArrayList<Order>());
                player.setChatMessages(new ArrayList<ChatMessage>());
            }
            List<Integer> economyHistory = battle.getMe(myArmyIndex).getGameStats().getEconomy();
            if (economyHistory.size() > 0) {
                mGameGUI.updateEconomyBalance(economyHistory.get(economyHistory.size() - 1));
            }
        }

        // analytics
        mGameStartTime = System.currentTimeMillis();

        // center map on player's castle
        mCamera.setCenter(castleTile.getX() * GameUtils.TILE_SIZE, castleTile.getY() * GameUtils.TILE_SIZE);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // wait a bit to avoid loading screen to not appear because the
                // game is
                // so awesome and loads so fast...
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                // hide loading screen
                mGameGUI.hideLoadingScreen();

                // show turn number
                mGameGUI.displayBigLabel(getString(R.string.turn_count, battle.getTurnCount()), R.color.white);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (battle == null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            pauseGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGameGUI != null) {
            mGameGUI.onPause();
        }
        if (mGameElementFactory != null) {
            mGameElementFactory.onPause();
        }
        if (mMustSaveGame && !mIsMultiplayerGame) {
            GameConverterHelper.saveGame(mDbHelper, battle);
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pauseGame() {
        mGameGUI.openGameMenu();
        mEngine.stop();
    }

    public void resumeGame() {
        mEngine.start();
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
        if (winningPlayer != null) {
            GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.winner_army,
                    winningPlayer.getArmy().name());
        }
        if (!mIsMultiplayerGame) {
            GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.against_AI,
                    winningPlayer == battle.getMe(myArmyIndex) ? "victory" : "defeat");
        }

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

        mGameGUI.updatePlayersNameColor(battle);

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

        if (mIsMultiplayerGame) {
            restartChrono();
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

    /**
     * 
     * Multiplayer stuff
     * 
     */
    public RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this).setMessageReceivedListener(this).setRoomStatusUpdateListener(this);
    }

    private int[] lstMultiplayerArmies = null;
    private Room mRoom;

    public int nbOrdersReceived = 0;
    public boolean hasSendOrders = false;
    private int receivedArmies = 1;

    private int chrono;
    private Timer timer;

    private List<String> lstParticipantIds = null;
    private String mMyParticipantId;

    public void sendOrdersOnline() {
        for (Player p : battle.getPlayers()) {
            if (p.getArmyIndex() != myArmyIndex) {
                getGamesClient().sendReliableRealTimeMessage(
                        this,
                        new Message(myArmyIndex, MessageType.TURN_ORDERS, GameConverterHelper.toByte(
                                battle.getPlayers().get(myArmyIndex).getLstTurnOrders()).toByteArray()).toByte(),
                        mRoom.getRoomId(), p.getId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        Message message = GameConverterHelper.getMessageFromByte(rtm.getMessageData());
        Log.d("message", message.getType().name());
        switch (message.getType()) {
        case PARTICIPANT_ID:
            String playerParticipantId = (String) GameConverterHelper.getObjectFromByte(message.getContent());
            lstParticipantIds.add(playerParticipantId);
            // if I have received all the players id, let's distribute army
            // indexes
            if (lstParticipantIds.size() == mRoom.getParticipants().size()) {
                Collections.sort(lstParticipantIds);
                // am I the game host ?
                if (lstParticipantIds.get(0).equals(mMyParticipantId)) {
                    myArmyIndex = 0;
                    lstMultiplayerArmies = new int[mRoom.getParticipants().size()];
                    lstMultiplayerArmies[0] = multiplayerFragment.mArmyChosen;
                    // send the army indexes to the others !
                    for (int n = 1; n < lstParticipantIds.size(); n++) {
                        getGamesClient().sendReliableRealTimeMessage(
                                this,
                                new Message(myArmyIndex, MessageType.ARMY_INDEX, GameConverterHelper.toByte(n)
                                        .toByteArray()).toByte(), mRoom.getRoomId(), lstParticipantIds.get(n));
                    }
                }
            }
            break;
        case ARMY_INDEX:
            myArmyIndex = (Integer) GameConverterHelper.getObjectFromByte(message.getContent());
            // send my army to the game host
            getGamesClient().sendReliableRealTimeMessage(
                    this,
                    new Message(myArmyIndex, MessageType.WHICH_ARMY, GameConverterHelper.toByte(
                            multiplayerFragment.mArmyChosen).toByteArray()).toByte(), mRoom.getRoomId(),
                    rtm.getSenderParticipantId());
            break;
        case WHICH_ARMY:
            Integer army = (Integer) GameConverterHelper.getObjectFromByte(message.getContent());
            lstMultiplayerArmies[message.getSenderIndex()] = army;
            receivedArmies++;
            if (receivedArmies == mRoom.getParticipants().size()) {
                // let's create the game
                battle = GameCreation.createMultiplayerMap(lstParticipantIds, mRoom.getParticipants(),
                        lstMultiplayerArmies, getGamesClient().getCurrentPlayer().getDisplayName());
                // send the game object to the other players
                for (int n = 1; n < lstParticipantIds.size(); n++) {
                    getGamesClient().sendReliableRealTimeMessage(
                            this,
                            new Message(myArmyIndex, MessageType.INIT_BATTLE, GameConverterHelper.toByte(battle)
                                    .toByteArray()).toByte(), mRoom.getRoomId(), lstParticipantIds.get(n));
                }
                startMultiplayerGame();
            }
            break;
        case INIT_BATTLE:
            // init game data
            battle = GameConverterHelper.getBattleFromLoadGame(message.getContent());
            Log.d("message", "game received : " + battle.getPlayers().size() + " players");
            startMultiplayerGame();
            break;
        case TURN_ORDERS:
            List<Order> lstOrders = (List<Order>) GameConverterHelper.getObjectFromByte(message.getContent());
            // format received orders
            for (Order o : lstOrders) {
                if (o instanceof MoveOrder) {
                    MoveOrder moveOrder = (MoveOrder) o;
                    Tile tile = battle.getMap().getTiles()[moveOrder.getOrigin().getY()][moveOrder.getOrigin().getX()];
                    Unit unit = tile.getContent().get(moveOrder.getUnitIndex());
                    MoveOrder formattedOrder = new MoveOrder(unit, battle.getMap().getTiles()[moveOrder
                            .getDestination().getY()][moveOrder.getDestination().getX()], moveOrder.getUnitIndex());
                    unit.setOrder(formattedOrder, false);
                    battle.getPlayers().get(message.getSenderIndex()).getLstTurnOrders().add(formattedOrder);
                } else if (o instanceof DefendOrder) {
                    DefendOrder defendOrder = (DefendOrder) o;
                    Tile tile = battle.getMap().getTiles()[defendOrder.getTile().getY()][defendOrder.getTile().getX()];
                    Unit unit = tile.getContent().get(defendOrder.getUnitIndex());
                    DefendOrder formattedOrder = new DefendOrder(unit, tile, defendOrder.getUnitIndex());
                    battle.getPlayers().get(message.getSenderIndex()).getLstTurnOrders().add(formattedOrder);
                    unit.setOrder(formattedOrder, false);
                } else if (o instanceof BuyOrder) {
                    BuyOrder buyOrder = (BuyOrder) o;
                    battle.getPlayers()
                            .get(message.getSenderIndex())
                            .getLstTurnOrders()
                            .add(new BuyOrder(battle.getMap().getTiles()[buyOrder.getTile().getY()][buyOrder.getTile()
                                    .getX()], buyOrder.getUnit()));
                }
            }
            onNewOrders();
            break;
        case CHAT:
            onReceiveChatMessage(message.getSenderIndex(),
                    (ChatMessage) GameConverterHelper.getObjectFromByte(message.getContent()));
            break;
        case START_BATTLE_NOW:
            if (lstParticipantIds == null) {
                mWaitingRoomFinishedFromCode = true;
                finishActivity(MultiplayerFragment.RC_WAITING_ROOM);
                mMyParticipantId = mRoom.getParticipantId(getGamesClient().getCurrentPlayerId());
                prepareGame(mRoom);
            }
            break;
        }
    }

    private void initMultiplayerGame() {
        mIsMultiplayerGame = true;
        timer = new Timer();

        if (battle == null) {
            // add multiplayer fragment
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            multiplayerFragment = new MultiplayerFragment();
            ft.add(R.id.fragment_container, multiplayerFragment);
            ft.commit();
        }
    }

    public void startMultiplayerGame() {
        mGameGUI = new GameGUI(this);
        getFragmentManager().beginTransaction().remove(multiplayerFragment).commit();
        try {
            getEngine().stop();
            onCreateResources(pOnCreateResourcesCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        restartChrono();
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

    @Override
    public void onSignInSucceeded() {
        if (getInvitationId() != null) {
            Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.setInvitationIdToAccept(getInvitationId());
            getGamesClient().joinRoom(roomConfigBuilder.build());
        }
    }

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
    }

    @Override
    public void onPeerLeft(Room room, List<String> arg1) {
    }

    @Override
    public void onRealTimeMessageSent(int arg0, int arg1, String arg2) {
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        // leave the room
        getGamesClient().leaveRoom(this, room.getRoomId());
    }

    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        if (battle != null) {
            int nbPlayersLeft = 0;
            for (Player p : battle.getPlayers()) {
                if (peers.indexOf(p.getId()) >= 0) {
                    p.setDefeated(true);
                }
                if (!p.isDefeated()) {
                    nbPlayersLeft++;
                }
            }
            if (nbPlayersLeft == 1) {
                endGame(battle.getMe(myArmyIndex));
                return;
            }
        }
    }

    @Override
    public void onRoomAutoMatching(Room room) {
    }

    @Override
    public void onRoomConnecting(Room room) {
    }

    @Override
    public void onLeftRoom(int statusCode, String arg1) {
    }

    private boolean mWaitingRoomFinishedFromCode = false;

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        if (request == MultiplayerFragment.RC_INVITATION_INBOX) {
            if (response != Activity.RESULT_OK) {
                // canceled
                return;
            }

            // get the selected invitation
            Bundle extras = data.getExtras();
            Invitation invitation = extras.getParcelable(GamesClient.EXTRA_INVITATION);

            // accept it!
            RoomConfig roomConfig = makeBasicRoomConfigBuilder().setInvitationIdToAccept(invitation.getInvitationId())
                    .build();
            getGamesClient().joinRoom(roomConfig);
        } else if (request == MultiplayerFragment.RC_SELECT_PLAYERS) {

            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            final ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);

            // get automatch criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            getGamesClient().createRoom(roomConfig);
        } else if (request == MultiplayerFragment.RC_WAITING_ROOM) {

            if (mWaitingRoomFinishedFromCode)
                return;

            if (response == Activity.RESULT_OK) {
                // start game
                mMyParticipantId = mRoom.getParticipantId(getGamesClient().getCurrentPlayerId());

                for (Participant p : mRoom.getParticipants()) {
                    if (!p.getParticipantId().equals(mMyParticipantId)) {
                        getGamesClient().sendReliableRealTimeMessage(this,
                                new Message(myArmyIndex, MessageType.START_BATTLE_NOW, null).toByte(),
                                mRoom.getRoomId(), p.getParticipantId());
                    }
                }

                prepareGame(mRoom);
            } else if (response == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning
                // of this
                // action is up to the game. You may choose to leave the room
                // and cancel the
                // match, or do something else like minimize the waiting room
                // and
                // continue to connect in the background.

                // in this example, we take the simple approach and just leave
                // the room:
                getGamesClient().leaveRoom(this, mRoom.getRoomId());
            } else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                getGamesClient().leaveRoom(this, mRoom.getRoomId());
            }
        }
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesClient.STATUS_OK) {
            // show error message, return to main screen.
            ApplicationUtils.showToast(getApplicationContext(), R.string.error_room_join, Toast.LENGTH_SHORT);
            return;
        }

        // get waiting room intent
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, 10);
        startActivityForResult(i, MultiplayerFragment.RC_WAITING_ROOM);
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesClient.STATUS_OK) {
            // show error message, return to main screen.
            ApplicationUtils.showToast(getApplicationContext(), R.string.error_room_connection, Toast.LENGTH_SHORT);
            return;
        }
        mRoom = room;
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesClient.STATUS_OK) {
            // show error message, return to main screen.
            ApplicationUtils.showToast(getApplicationContext(), R.string.error_room_creation, Toast.LENGTH_SHORT);
            return;
        }

        // get waiting room intent
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, 2);
        startActivityForResult(i, MultiplayerFragment.RC_WAITING_ROOM);
        mRoom = room;
    }

    @Override
    public void onConnectedToRoom(Room room) {
        mRoom = room;
    }

    private void prepareGame(Room room) {
        // send my participant id to all others players
        receivedArmies = 1;
        lstParticipantIds = new ArrayList<String>();
        lstParticipantIds.add(mMyParticipantId);

        for (Participant p : room.getParticipants()) {
            if (!p.getParticipantId().equals(mMyParticipantId)) {
                getGamesClient().sendReliableRealTimeMessage(
                        this,
                        new Message(-1, MessageType.PARTICIPANT_ID, GameConverterHelper.toByte(mMyParticipantId)
                                .toByteArray()).toByte(), room.getRoomId(), p.getParticipantId());
            }
        }
    }

    public void sendChatMessage(int recipientIndex, String messageContent) {
        if (getGamesClient() != null && getGamesClient().isConnected()) {
            ChatMessage chatMessage = new ChatMessage(getGamesClient().getCurrentPlayer().getDisplayName(),
                    messageContent, true);
            // add message to UI
            mGameGUI.addMessageToChatDialog(chatMessage);
            mGameGUI.scrollToChatBottom();
            // add message to player object
            battle.getPlayers().get(recipientIndex).getChatMessages().add(chatMessage);
            // send message !
            getGamesClient().sendReliableRealTimeMessage(this,
                    new Message(myArmyIndex, MessageType.CHAT, chatMessage.toByte()).toByte(), mRoom.getRoomId(),
                    battle.getPlayers().get(recipientIndex).getId());
        }
    }

    public void onReceiveChatMessage(int senderIndex, ChatMessage receivedMessage) {
        // add message to player object
        ChatMessage chatMessage = new ChatMessage(receivedMessage.getSenderName(), receivedMessage.getContent(), false);
        battle.getPlayers().get(senderIndex).getChatMessages().add(chatMessage);
        // notify player
        mGameGUI.onReceiveChatMessage(senderIndex, chatMessage);
    }

    private void restartChrono() {
        chrono = GameUtils.MULTIPLAYER_TURN_MAX_TIME;
        ((TextView) findViewById(R.id.chrono)).setText("" + chrono);
        findViewById(R.id.chrono).setVisibility(View.VISIBLE);
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // send orders after 1 minute
                if (chrono == 0) {
                    timer.cancel();
                    timer.purge();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mGameGUI.sendOrders();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // update chrono UI
                            ((TextView) findViewById(R.id.chrono)).setText("" + chrono);
                        }
                    });
                    chrono--;
                }
            }
        }, 0, 1000);
    }

}
