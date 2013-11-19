package com.giggs.apps.chaos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.GameStats;
import com.giggs.apps.chaos.game.model.Player;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class BattleReportActivity extends BaseGameActivity {

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onSignInSucceeded() {
    }

    private DatabaseHelper mDbHelper;
    private boolean mIsVictory = false;
    private Battle battle;

    private Button mLeaveReportBtn;

    /**
     * Callbacks
     */
    private OnClickListener onLeaveReportClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            leaveReport();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new DatabaseHelper(getApplicationContext());

        // get battle info
        battle = mDbHelper.getBattleDao().get(null, null, null, null).get(0);
        mIsVictory = !battle.getPlayers().get(0).isDefeated();

        // erase saved games from database
        // SaveGameHelper.deleteSavedBattles(mDbHelper);

        setContentView(R.layout.activity_battle_report);
        setupUI();
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
        // format axis labels
        // CustomLabelFormatter labelFormatter = new CustomLabelFormatter() {
        // @Override
        // public String formatLabel(double value, boolean isValueX) {
        // return "" + (int) value;
        // }
        // };
        String[] horizontalLabels = new String[] { "", "", "time" };
        graphViewPop.setHorizontalLabels(horizontalLabels);
        graphViewPop.setVerticalLabels(new String[] { "high", "", "low" });
        popGraphLayout.addView(graphViewPop);
        graphViewEconomy.setHorizontalLabels(horizontalLabels);
        economyGraphLayout.addView(graphViewEconomy);
    }

    private void leaveReport() {
        startActivity(new Intent(BattleReportActivity.this, HomeActivity.class));
        finish();
    }

}
