package com.glevel.wwii.activities;

import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.glevel.wwii.R;
import com.glevel.wwii.activities.adapters.UnitsArrayAdapter;
import com.glevel.wwii.database.DatabaseHelper;
import com.glevel.wwii.game.AI;
import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.SaveGameHelper;
import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.data.BattlesData;
import com.glevel.wwii.game.data.UnitsData;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.Player;
import com.glevel.wwii.game.model.units.Unit;
import com.glevel.wwii.utils.ApplicationUtils;
import com.glevel.wwii.utils.WWActivity;
import com.glevel.wwii.views.CustomAlertDialog;

public class ArmyBuilderActivity extends WWActivity {

    public static final String EXTRA_ARMY = "army_id", EXTRA_MAP = "map_id";

    private ArmiesData mPlayerArmy;
    private Battle mBattle;
    private Player mPlayer;
    private List<Unit> mAvailableUnits;

    private ListView mMyArmyList, mAvailableTroopsList;
    private TextView mRequisitionPointsTV;
    private Button mStartBattleBtn;
    private UnitsArrayAdapter mMyArmyAdapter;

    private DatabaseHelper dbHelper;

    /**
     * Callbacks
     */
    private OnClickListener onStartBattleClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPlayer.getUnits().size() == 0) {
                // the player has no units
                ApplicationUtils.showToast(ArmyBuilderActivity.this, R.string.no_troops, Toast.LENGTH_SHORT);
            } else if (mPlayer.getRequisition() > 0) {
                // the player has some requisition points left, show confirm
                // dialog
                Dialog dialog = new CustomAlertDialog(ArmyBuilderActivity.this, R.style.Dialog,
                        getString(R.string.confirm_battle_message), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == R.id.okButton) {
                                    startGame();
                                }
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            } else {
                startGame();
            }
        }
    };
    private OnItemClickListener onMyUnitClicked = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            if (position < mPlayer.getUnits().size()) {
                // if slot is not empty, ask to sell this unit
                openConfirmTransactionDialog(mPlayer.getUnits().get(position), true);
            }
        }
    };
    private OnItemClickListener onAvailableUnitClicked = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            Unit unit = mAvailableUnits.get(position);
            if (mPlayer.getUnits().size() == GameUtils.MAX_UNIT_PER_ARMY) {
                // no more unit slot available
                ApplicationUtils.showToast(ArmyBuilderActivity.this, R.string.no_slots_left, Toast.LENGTH_SHORT);
            } else if (mPlayer.getRequisition() < unit.getRealSellPrice(false)) {
                // unit is too expensive
                ApplicationUtils.showToast(ArmyBuilderActivity.this, R.string.not_enough_requisition,
                        Toast.LENGTH_SHORT);
            } else {
                // buy unit !
                openConfirmTransactionDialog(unit, false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // build battle with intent extras
        Intent intent = getIntent();
        mBattle = new Battle(BattlesData.values()[intent.getIntExtra(EXTRA_MAP, 0)]);
        mPlayerArmy = ArmiesData.values()[intent.getIntExtra(EXTRA_ARMY, ArmiesData.USA.ordinal())];

        // init human player
        mPlayer = new Player("Me", mPlayerArmy, 0, false, mBattle.getPlayerVictoryCondition(mPlayerArmy));
        mBattle.getPlayers().add(mPlayer);

        // create AI's player
        Player enemyPlayer = new Player("Enemy", mPlayerArmy.getEnemy(), mBattle.getPlayers().size(), true,
                mBattle.getPlayerVictoryCondition(mPlayerArmy.getEnemy()));
        mBattle.getPlayers().add(enemyPlayer);

        // create AI's player army
        AI.createArmy(enemyPlayer, mBattle);

        // get available units to hire
        mAvailableUnits = UnitsData.getAllUnits(mPlayerArmy);

        setContentView(R.layout.activity_army_builder);
        setupUI();

        dbHelper = new DatabaseHelper(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, BattleChooserActivity.class));
        finish();
    }

    private void setupUI() {
        // init requisition points value
        mRequisitionPointsTV = (TextView) findViewById(R.id.requisitionPoints);
        updateRequisitionPointsLeft(mBattle.getRequisition());

        // init army flag
        TextView viewTitle = (TextView) findViewById(R.id.title);
        viewTitle.setCompoundDrawablesWithIntrinsicBounds(mPlayerArmy.getFlagImage(), 0, 0, 0);

        // init start battle button
        mStartBattleBtn = (Button) findViewById(R.id.startBattle);
        mStartBattleBtn.setOnClickListener(onStartBattleClicked);

        // init my army list
        mMyArmyList = (ListView) findViewById(R.id.myArmyList);
        mMyArmyAdapter = new UnitsArrayAdapter(this, R.layout.army_list_item, mPlayer.getUnits(), true);
        mMyArmyList.setAdapter(mMyArmyAdapter);
        mMyArmyList.setOnItemClickListener(onMyUnitClicked);

        // init available troops list
        mAvailableTroopsList = (ListView) findViewById(R.id.availableTroopsList);
        UnitsArrayAdapter mAvailableTroopsAdapter = new UnitsArrayAdapter(this, R.layout.army_list_item,
                mAvailableUnits, false);
        mAvailableTroopsList.setAdapter(mAvailableTroopsAdapter);
        mAvailableTroopsList.setOnItemClickListener(onAvailableUnitClicked);
    }

    private void updateRequisitionPointsLeft(int newValue) {
        mPlayer.setRequisition(newValue);
        mRequisitionPointsTV.setText("" + mPlayer.getRequisition());
        // update text color if requisition pool is empty
        if (mPlayer.getRequisition() == 0) {
            mRequisitionPointsTV.setTextColor(getResources().getColor(R.color.bg_btn_green));
        } else {
            mRequisitionPointsTV.setTextColor(getResources().getColor(R.color.requisitionPoints));
        }
    }

    private void buyUnit(Unit unit) {
        // update requisition points
        updateRequisitionPointsLeft((int) (mPlayer.getRequisition() - unit.getRealSellPrice(false)));

        // create bought unit
        Unit newUnit = unit.copy();

        // add unit to army
        mPlayer.getUnits().add(newUnit);
        mMyArmyAdapter.notifyDataSetChanged();
    }

    private void sellUnit(Unit unit) {
        // update requisition points
        updateRequisitionPointsLeft((int) (mPlayer.getRequisition() + unit.getRealSellPrice(true)));
        // remove unit from slot
        mPlayer.getUnits().remove(unit);
        mMyArmyAdapter.notifyDataSetChanged();
    }

    private void openConfirmTransactionDialog(final Unit unit, final boolean isSelling) {
        String message = getString(R.string.confirm_transaction, (isSelling ? getString(R.string.sell)
                : getString(R.string.buy)), getString(unit.getName()), unit.getRealSellPrice(isSelling));
        Dialog dialog = new CustomAlertDialog(this, R.style.Dialog, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == R.id.okButton) {
                    if (isSelling) {
                        sellUnit(unit);
                    } else {
                        buyUnit(unit);
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void startGame() {
        long gameId = SaveGameHelper.saveGame(dbHelper, mBattle);
        Intent intent = new Intent(this, GameActivity.class);
        Bundle args = new Bundle();
        args.putLong("game_id", gameId);
        intent.putExtras(args);
        startActivity(intent);
        finish();
    }

}
