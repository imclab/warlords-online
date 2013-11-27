package com.giggs.apps.chaos.activities.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.MusicManager;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.BaseGameActivity;

public class MultiplayerFragment extends Fragment implements OnClickListener {

    public static final int RC_ARMY_CHOOSER = 1000;
    public static final int RC_INVITATION_INBOX = 2000;
    public static final int RC_SELECT_PLAYERS = 3000;
    public static final int RC_WAITING_ROOM = 4000;

    private Button mQuickGameButton, mInvitePlayersButton, mShowInvitationsButton, mRankingsButton;
    private View mBackButton;
    private Runnable mStormEffect;
    private ImageView mStormBackground;
    private Animation mButtonsInAnimation;

    public int mArmyChosen = 0;
    private int mNbPlayers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_multiplayer, null);
        mButtonsInAnimation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.bottom_in);

        mQuickGameButton = (Button) view.findViewById(R.id.quickGameButton);
        mQuickGameButton.setOnClickListener(this);

        mInvitePlayersButton = (Button) view.findViewById(R.id.invitePlayersButton);
        mInvitePlayersButton.setOnClickListener(this);

        mShowInvitationsButton = (Button) view.findViewById(R.id.showInvitationsButton);
        mShowInvitationsButton.setOnClickListener(this);

        mRankingsButton = (Button) view.findViewById(R.id.rankingsButton);
        mRankingsButton.setOnClickListener(this);

        mBackButton = (Button) view.findViewById(R.id.backButton);
        mBackButton.setOnClickListener(this);

        mStormBackground = (ImageView) view.findViewById(R.id.stormBackground);

        view.findViewById(R.id.mainButtonsLayout).startAnimation(mButtonsInAnimation);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // init storm effect
        mStormEffect = ApplicationUtils.addStormBackgroundAtmosphere(mStormBackground);
    }

    @Override
    public void onPause() {
        super.onPause();
        mStormBackground.removeCallbacks(mStormEffect);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.quickGameButton:
            if (((BaseGameActivity) getActivity()).getGamesClient().isConnected()) {
                MusicManager.playSound(getActivity().getApplicationContext(), R.raw.main_button);
                showJoinGameChooser();
            }
            break;
        case R.id.invitePlayersButton:
            if (((BaseGameActivity) getActivity()).getGamesClient().isConnected()) {
                MusicManager.playSound(getActivity().getApplicationContext(), R.raw.main_button);
                showCreateGameChooser();
            }
            break;
        case R.id.showInvitationsButton:
            if (((BaseGameActivity) getActivity()).getGamesClient().isConnected()) {
                MusicManager.playSound(getActivity().getApplicationContext(), R.raw.main_button);
                showInvitations();
            }
            break;
        case R.id.rankingsButton:
            if (((BaseGameActivity) getActivity()).getGamesClient().isConnected()) {
                MusicManager.playSound(getActivity().getApplicationContext(), R.raw.main_button);
                startActivityForResult(
                        ((BaseGameActivity) getActivity()).getGamesClient().getLeaderboardIntent(
                                getString(R.string.ranking_best_generals)), 1);
            }
            break;
        case R.id.backButton:
            MusicManager.playSound(getActivity().getApplicationContext(), R.raw.main_button);
            getActivity().onBackPressed();
            break;
        }
    }

    private void showJoinGameChooser() {
        DialogFragment fr = new CreateGameDialog();
        Bundle args = new Bundle();
        args.putInt(CreateGameDialog.ARGUMENT_GAME_TYPE, CreateGameDialog.MULTIPLAYER_GAME_TYPE);
        args.putBoolean(CreateGameDialog.ARGUMENT_IS_HOST, false);
        fr.setTargetFragment(this, 1000);
        ApplicationUtils.openDialogFragment(getActivity(), fr, args);
    }

    private void showCreateGameChooser() {
        DialogFragment fr = new CreateGameDialog();
        Bundle args = new Bundle();
        args.putInt(CreateGameDialog.ARGUMENT_GAME_TYPE, CreateGameDialog.MULTIPLAYER_GAME_TYPE);
        args.putBoolean(CreateGameDialog.ARGUMENT_IS_HOST, true);
        fr.setTargetFragment(this, 1000);
        ApplicationUtils.openDialogFragment(getActivity(), fr, args);
    }

    private void showInvitations() {
        Intent intent = ((BaseGameActivity) getActivity()).getGamesClient().getInvitationInboxIntent();
        startActivityForResult(intent, RC_INVITATION_INBOX);
        GoogleAnalyticsHelper.sendEvent(getActivity().getApplicationContext(), EventCategory.ui_action,
                EventAction.button_press, "show_invitations");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ARMY_CHOOSER) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();

                mArmyChosen = extras.getInt("army");

                int nbPlayers = extras.getInt("nb_players", -1);
                if (nbPlayers > 0) {
                    mNbPlayers = nbPlayers;
                    invitePlayersToMyGame();
                } else {
                    mNbPlayers = 4;
                    startQuickGame();
                }
            }
        }
    }

    private void startQuickGame() {
        // auto-match criteria to invite 1 random auto-match opponent.
        Bundle am = RoomConfig.createAutoMatchCriteria(1, mNbPlayers - 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = ((GameActivity) getActivity()).makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        ((BaseGameActivity) getActivity()).getGamesClient().createRoom(roomConfig);

        GoogleAnalyticsHelper.sendEvent(getActivity().getApplicationContext(), EventCategory.ui_action,
                EventAction.button_press, "quick_game");
    }

    private void invitePlayersToMyGame() {
        // launch the player selection screen
        Intent intent = ((BaseGameActivity) getActivity()).getGamesClient().getSelectPlayersIntent(mNbPlayers - 1,
                mNbPlayers - 1);
        getActivity().startActivityForResult(intent, RC_SELECT_PLAYERS);
        GoogleAnalyticsHelper.sendEvent(getActivity().getApplicationContext(), EventCategory.ui_action,
                EventAction.button_press, "invite_players");
    }

}
