package com.giggs.apps.chaos.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.fragments.CreateGameDialog;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;
import com.giggs.apps.chaos.game.GameCreation;
import com.giggs.apps.chaos.game.SaveGameHelper;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.multiplayer.Message;
import com.giggs.apps.chaos.game.multiplayer.Message.MessageType;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.MusicManager;
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
import com.google.example.games.basegameutils.BaseGameActivity;

public class MultiplayerActivity extends BaseGameActivity implements OnClickListener, RoomUpdateListener,
        RealTimeMessageReceivedListener, RoomStatusUpdateListener, RealTimeReliableMessageSentListener {

    private static final int RC_INVITATION_INBOX = 20000;
    private static final int RC_SELECT_PLAYERS = 30000;
    private static final int RC_WAITING_ROOM = 40000;

    private Button mQuickGameButton, mInvitePlayersButton, mShowInvitationsButton, mRankingsButton;
    private View mBackButton;
    private Animation mButtonsInAnimation;
    private Runnable mStormEffect;
    private ImageView mStormBackground;

    private int mNbPlayersNeeded = 2;
    private int mArmyChosen = 0;
    private int myArmyIndex;
    private int[] lstMultiplayerArmies;
    private Room mRoom;

    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this).setMessageReceivedListener(this).setRoomStatusUpdateListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multiplayer);
        setupUI();

        mContinueMusic = true;

        // If the device goes to sleep during handshake or gameplay, the player
        // will be disconnected from the room
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupUI() {
        mButtonsInAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_in);

        mQuickGameButton = (Button) findViewById(R.id.quickGameButton);
        mQuickGameButton.setOnClickListener(this);

        mInvitePlayersButton = (Button) findViewById(R.id.invitePlayersButton);
        mInvitePlayersButton.setOnClickListener(this);

        mShowInvitationsButton = (Button) findViewById(R.id.showInvitationsButton);
        mShowInvitationsButton.setOnClickListener(this);

        mRankingsButton = (Button) findViewById(R.id.rankingsButton);
        mRankingsButton.setOnClickListener(this);

        mBackButton = (Button) findViewById(R.id.backButton);
        mBackButton.setOnClickListener(this);

        mStormBackground = (ImageView) findViewById(R.id.stormBackground);

        findViewById(R.id.mainButtonsLayout).startAnimation(mButtonsInAnimation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // init storm effect
        mStormEffect = ApplicationUtils.addStormBackgroundAtmosphere(mStormBackground);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStormBackground.removeCallbacks(mStormEffect);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.quickGameButton:
            MusicManager.playSound(getApplicationContext(), R.raw.main_button);
            showJoinGameChooser();
            break;
        case R.id.invitePlayersButton:
            MusicManager.playSound(getApplicationContext(), R.raw.main_button);
            showCreateGameChooser();
            break;
        case R.id.showInvitationsButton:
            MusicManager.playSound(getApplicationContext(), R.raw.main_button);
            showInvitations();
            break;
        case R.id.rankingsButton:
            MusicManager.playSound(getApplicationContext(), R.raw.main_button);
            startActivityForResult(getGamesClient().getLeaderboardIntent(getString(R.string.ranking_best_generals)), 1);
            break;
        case R.id.backButton:
            MusicManager.playSound(getApplicationContext(), R.raw.main_button);
            onBackPressed();
            break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void showJoinGameChooser() {
        DialogFragment fr = new CreateGameDialog();
        Bundle args = new Bundle();
        args.putInt(CreateGameDialog.ARGUMENT_GAME_TYPE, CreateGameDialog.MULTIPLAYER_GAME_TYPE);
        args.putBoolean(CreateGameDialog.ARGUMENT_IS_HOST, false);
        ApplicationUtils.openDialogFragment(this, fr, args);
    }

    private void startQuickGame() {
        // auto-match criteria to invite 1 random auto-match opponent.
        Bundle am = RoomConfig.createAutoMatchCriteria(1, mNbPlayersNeeded, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        getGamesClient().createRoom(roomConfig);

        GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action, EventAction.button_press,
                "quick_game");
    }

    private void showCreateGameChooser() {
        DialogFragment fr = new CreateGameDialog();
        Bundle args = new Bundle();
        args.putInt(CreateGameDialog.ARGUMENT_GAME_TYPE, CreateGameDialog.MULTIPLAYER_GAME_TYPE);
        args.putBoolean(CreateGameDialog.ARGUMENT_IS_HOST, true);
        ApplicationUtils.openDialogFragment(this, fr, args);
    }

    public void onArmyChosen(int army, int nbPlayers) {
        mArmyChosen = army;
        if (nbPlayers > 0) {
            // create game
            mNbPlayersNeeded = nbPlayers - 1;
            invitePlayersToMyGame();
        } else {
            mNbPlayersNeeded = 3;
            startQuickGame();
        }
    }

    private void invitePlayersToMyGame() {
        // launch the player selection screen
        Intent intent = getGamesClient().getSelectPlayersIntent(mNbPlayersNeeded, mNbPlayersNeeded);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
        GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action, EventAction.button_press,
                "invite_players");
    }

    private void showInvitations() {
        Intent intent = getGamesClient().getInvitationInboxIntent();
        startActivityForResult(intent, RC_INVITATION_INBOX);
        GoogleAnalyticsHelper.sendEvent(getApplicationContext(), EventCategory.ui_action, EventAction.button_press,
                "show_invitations");
    }

    private void prepareGame(Room room) {
        if (myArmyIndex == 0) {
            lstMultiplayerArmies = new int[room.getParticipants().size()];
            lstMultiplayerArmies[0] = mArmyChosen;
        } else {
            getGamesClient().sendReliableRealTimeMessage(
                    this,
                    new Message(myArmyIndex, MessageType.SEND_ARMY, SaveGameHelper.toByte(myArmyIndex).toByteArray())
                            .toByte(), mRoom.getRoomId(), room.getParticipants().get(0).getParticipantId());
        }
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        if (request == RC_INVITATION_INBOX) {
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
        } else if (request == RC_SELECT_PLAYERS) {
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
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
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
        } else if (request == RC_WAITING_ROOM) {
            if (response == Activity.RESULT_OK) {
                // start game
                Room room = data.getParcelableExtra(GamesClient.EXTRA_ROOM);
                prepareGame(room);
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
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, Integer.MAX_VALUE);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onLeftRoom(int statusCode, String arg1) {
        // TODO Auto-generated method stub
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
        Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, Integer.MAX_VALUE);
        startActivityForResult(i, RC_WAITING_ROOM);
        mRoom = room;
    }

    @Override
    public void onConnectedToRoom(Room room) {
        for (int n = 0; n < room.getParticipants().size(); n++) {
            Participant p = room.getParticipants().get(n);
            if (p.getParticipantId().equals(mRoom.getParticipantId(getGamesClient().getCurrentPlayerId()))) {
                myArmyIndex = n;
                break;
            }
        }
        mRoom = room;
        mNbPlayersNeeded = 1;
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        // leave the room
        getGamesClient().leaveRoom(this, room.getRoomId());
    }

    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomAutoMatching(Room room) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRoomConnecting(Room room) {
        // TODO Auto-generated method stub

    }

    private int receivedArmies = 1;

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        Message message = SaveGameHelper.getMessageFromByte(rtm.getMessageData());
        Log.d("message", message.getType().name());
        switch (message.getType()) {
        case SEND_ARMY:
            Integer army = (Integer) SaveGameHelper.getObjectFromByte(message.getContent());
            lstMultiplayerArmies[message.getSenderIndex()] = army;
            receivedArmies++;
            if (receivedArmies == mRoom.getParticipants().size()) {
                // let's create the game
                Battle battle = GameCreation.createMultiplayerMap(mRoom.getParticipants(), lstMultiplayerArmies);
                // send the game object to the other players
                for (int n = 0; n < mRoom.getParticipants().size(); n++) {
                    if (myArmyIndex != n) {
                        getGamesClient().sendReliableRealTimeMessage(
                                this,
                                new Message(myArmyIndex, MessageType.INIT_BATTLE, SaveGameHelper.toByte(battle)
                                        .toByteArray()).toByte(), mRoom.getRoomId(),
                                mRoom.getParticipants().get(n).getParticipantId());
                    }
                }
                startBattle(battle);
            }
            break;
        case INIT_BATTLE:
            // init game data
            Battle battle = SaveGameHelper.getBattleFromLoadGame(message.getContent());
            Log.d("message", "battle received : " + battle.getPlayers().size());
            startBattle(battle);
            break;
        }
    }

    private void startBattle(Battle battle) {
        Intent intent = new Intent(this, GameActivity.class);
        Bundle extras = new Bundle();
        extras.putString("room_id", mRoom.getRoomId());
        extras.putInt("army_index", myArmyIndex);
        extras.putParcelable("battle", battle);
        intent.putExtras(extras);
        startActivity(intent);
        finish();
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
        // TODO Auto-generated method stub
    }

    @Override
    public void onRealTimeMessageSent(int arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub
    }

}
