package com.giggs.apps.chaos.activities;

import java.util.Iterator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.GameConverterHelper;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.GameStats;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.MusicManager;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.OnLeaderboardScoresLoadedListener;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class BattleReportActivity extends BaseGameActivity implements OnLeaderboardScoresLoadedListener {

    private DatabaseHelper mDbHelper;
    private Battle battle;
    private boolean mIsVictory = false;
    private boolean mIsSoloGame = true;

    private Button mLeaveReportBtn;

    /**
     * Callbacks
     */
    private OnClickListener onLeaveReportClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            MusicManager.playSound(getApplicationContext(), R.raw.main_button);
            leaveReport();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getIntent().getExtras();
        int myArmyIndex = args.getInt("army_index");

        mDbHelper = new DatabaseHelper(getApplicationContext());

        // get battle info
        battle = args.getParcelable("battle");
        mIsVictory = !battle.getMe(myArmyIndex).isDefeated();
        for (Player p : battle.getPlayers()) {
            if (p != battle.getMe(myArmyIndex) && !p.isAI()) {
                mIsSoloGame = false;
            }
        }

        // erase saved games from database
        GameConverterHelper.deleteSavedBattles(mDbHelper);

        setContentView(R.layout.activity_battle_report);
        setupUI();

        mMusic = MusicManager.MUSIC_END_GAME;
    }

    @Override
    public void onBackPressed() {
        leaveReport();
    }

    private void setupUI() {
        // setup background victory label
        TextView victoryLabel = (TextView) findViewById(R.id.victoryLabel);
        if (mIsVictory) {
            victoryLabel.setText(R.string.victory);
            victoryLabel.setTextColor(getResources().getColor(R.color.green));
        } else {
            victoryLabel.setText(R.string.defeat);
            victoryLabel.setTextColor(getResources().getColor(R.color.red));
        }

        // init leave report button
        mLeaveReportBtn = (Button) findViewById(R.id.leaveReportButton);
        mLeaveReportBtn.setOnClickListener(onLeaveReportClicked);

        // setup players figures
        TableLayout statsTable = (TableLayout) findViewById(R.id.playersStats);
        for (Player player : battle.getPlayers()) {
            TableRow statsRow = (TableRow) getLayoutInflater().inflate(R.layout.player_stats_row, null);
            TextView playerName = (TextView) statsRow.findViewById(R.id.name);
            playerName.setText("" + player.getName());
            playerName.setCompoundDrawablesWithIntrinsicBounds(player.getArmy().getImage(), 0, 0, 0);
            int playerColor = GameUtils.PLAYER_COLORS[player.getArmyIndex()].getARGBPackedInt();
            playerName.setTextColor(playerColor);
            TextView goldGathered = (TextView) statsRow.findViewById(R.id.gold);
            goldGathered.setText("" + player.getGameStats().getGold());
            TextView unitsCreated = (TextView) statsRow.findViewById(R.id.unitsCreated);
            unitsCreated.setText("" + player.getGameStats().getNbUnitsCreated());
            TextView unitsKilled = (TextView) statsRow.findViewById(R.id.unitsKilled);
            unitsKilled.setText("" + player.getGameStats().getNbUnitsKilled());
            TextView battlesWon = (TextView) statsRow.findViewById(R.id.battlesWon);
            battlesWon.setText("" + player.getGameStats().getNbBattlesWon());

            statsTable.addView(statsRow);
        }

        // setup charts
        ViewGroup popGraphLayout = (ViewGroup) findViewById(R.id.popGraph);
        ViewGroup economyGraphLayout = (ViewGroup) findViewById(R.id.economyGraph);
        GraphView graphViewPop = new LineGraphView(this, "");
        GraphView graphViewEconomy = new LineGraphView(this, "");
        for (Player player : battle.getPlayers()) {
            GameStats gameStats = player.getGameStats();
            GraphViewData[] dataEconomy = new GraphViewData[gameStats.getEconomy().size()];
            GraphViewData[] dataPop = new GraphViewData[gameStats.getPopulation().size()];
            for (int n = 0; n < player.getGameStats().getPopulation().size(); n++) {
                dataPop[n] = new GraphViewData(n + 1, player.getGameStats().getPopulation().get(n));
                dataEconomy[n] = new GraphViewData(n + 1, player.getGameStats().getEconomy().get(n));
            }
            graphViewPop.addSeries(new GraphViewSeries(player.getName(), new GraphViewSeriesStyle(
                    GameUtils.PLAYER_COLORS[player.getArmyIndex()].getARGBPackedInt(), 5), dataPop));
            graphViewEconomy.addSeries(new GraphViewSeries(player.getName(), new GraphViewSeriesStyle(
                    GameUtils.PLAYER_COLORS[player.getArmyIndex()].getARGBPackedInt(), 5), dataEconomy));
        }
        String[] horizontalLabels = new String[] { "", "", "time" };
        graphViewPop.setHorizontalLabels(horizontalLabels);
        graphViewPop.setVerticalLabels(new String[] { "high", "", "low" });
        popGraphLayout.addView(graphViewPop);
        graphViewEconomy.setHorizontalLabels(horizontalLabels);
        economyGraphLayout.addView(graphViewEconomy);
    }

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onSignInSucceeded() {
        if (battle.getTurnCount() >= 5) {
            GamesClient gameClient = getGamesClient();
            if (mIsVictory) {
                if (mIsSoloGame) {
                    gameClient.incrementAchievement(getString(R.string.achievement_ai_killer), 1);
                    gameClient.incrementAchievement(getString(R.string.achievement_morpheus), 1);
                } else {
                    gameClient.incrementAchievement(getString(R.string.achievement_novice), 1);
                    gameClient.incrementAchievement(getString(R.string.achievement_sergeant), 1);
                    gameClient.incrementAchievement(getString(R.string.achievement_captain), 1);
                    gameClient.incrementAchievement(getString(R.string.achievement_warlord), 1);
                    gameClient.incrementAchievement(getString(R.string.achievement_professor_chaos), 1);
                    gameClient.loadPlayerCenteredScores(this,
                            getResources().getString(R.string.ranking_best_generals), 0, 0, 10);
                    return;
                }
            }
        } else {
            // if battle was too short, no score update !
            ApplicationUtils.showToast(getApplicationContext(), R.string.game_was_too_short, Toast.LENGTH_SHORT);
        }
    }

    private void leaveReport() {
        startActivity(new Intent(BattleReportActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void onLeaderboardScoresLoaded(int arg0, LeaderboardBuffer arg1, LeaderboardScoreBuffer arg2) {
        Iterator<LeaderboardScore> it = arg2.iterator();
        while (it.hasNext()) {
            LeaderboardScore temp = it.next();
            if (temp.getScoreHolder().getPlayerId().equals(getGamesClient().getCurrentPlayerId())) {
                getGamesClient().submitScore(getString(R.string.ranking_best_generals), temp.getRawScore() + 1);
                break;
            }
        }
    }

}
