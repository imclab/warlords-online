package com.giggs.apps.chaos.game;

import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.activities.HomeActivity;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;
import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.BuyOrder;
import com.giggs.apps.chaos.game.model.orders.Order;
import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.views.CustomAlertDialog;

public class GameGUI {

    private GameActivity mActivity;
    private Dialog mLoadingScreen;
    private TextView mBigLabel;
    private Animation mBigLabelAnimation;
    private Dialog mGameMenuDialog;
    private Animation mBuyLayoutAnimationIn, mBuyLayoutAnimationOut;
    private ViewGroup mBuyLayout;
    private TextView mGoldAmount;
    private Button mSendOrdersButton;
    private Animation mGoldDeficitAnimation;
    private TextView economyBalanceTV;

    public GameGUI(GameActivity activity) {
        this.mActivity = activity;
    }

    public void setupGUI() {
        // setup loading screen
        mLoadingScreen = new Dialog(mActivity, R.style.FullScreenDialog);
        mLoadingScreen.setContentView(R.layout.dialog_game_loading);
        mLoadingScreen.setCancelable(false);
        mLoadingScreen.setCanceledOnTouchOutside(false);
        // animate loading dots
        Animation loadingDotsAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.loading_dots);
        ((TextView) mLoadingScreen.findViewById(R.id.loadingDots)).startAnimation(loadingDotsAnimation);
        mLoadingScreen.show();

        // setup big label
        mBigLabelAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.big_label_in_game);
        mBigLabel = (TextView) mActivity.findViewById(R.id.bigLabel);

        // allow user to change the music volume with his phone's buttons
        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // setup buy layout
        mBuyLayoutAnimationIn = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_in);
        mBuyLayoutAnimationOut = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_out);
        mBuyLayout = (ViewGroup) mActivity.findViewById(R.id.buyLayout);
        Button mBuyReset = (Button) mActivity.findViewById(R.id.buyReset);
        mBuyReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTile != null) {
                    // remove order
                    for (Order order : mActivity.battle.getMeSoloMode().getLstTurnOrders()) {
                        if (order instanceof BuyOrder && ((BuyOrder) order).getTile() == selectedTile) {
                            mActivity.battle.getMeSoloMode().removeOrder(order);
                            break;
                        }
                    }
                    selectedTile.getSprite().updateUnitProduction(null);
                    hideBuyOptions();
                }
            }
        });
        // init units buy buttons
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(15, 0, 0, 0);
        List<Unit> lstAvailableUnits = UnitsData.getUnits(mActivity.battle.getMeSoloMode().getArmy(), 0);
        for (int n = 0; n < lstAvailableUnits.size(); n++) {
            final Unit unit = lstAvailableUnits.get(n);
            View button = mActivity.getLayoutInflater().inflate(R.layout.buy_unit_button, null);
            button.setTag(n);
            // set name
            TextView unitName = (TextView) button.findViewById(R.id.name);
            unitName.setText(unit.getName());
            unitName.setCompoundDrawablesWithIntrinsicBounds(unit.getImage(), 0, 0, 0);
            // set price
            TextView unitPrice = (TextView) button.findViewById(R.id.price);
            unitPrice.setText("" + unit.getPrice());
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedTile != null) {
                        // remove order
                        for (Order order : mActivity.battle.getMeSoloMode().getLstTurnOrders()) {
                            if (order instanceof BuyOrder && ((BuyOrder) order).getTile() == selectedTile) {
                                mActivity.battle.getMeSoloMode().removeOrder(order);
                            }
                        }
                        mActivity.battle
                                .getPlayers()
                                .get(0)
                                .getLstTurnOrders()
                                .add(new BuyOrder(selectedTile, UnitsData.getUnits(unit.getArmy(),
                                        mActivity.battle.getMeSoloMode().getArmyIndex()).get((Integer) v.getTag())));
                        selectedTile.getSprite().updateUnitProduction(
                                GraphicsFactory.mGfxMap.get(unit.getSpriteName().replace(".png", "") + "_image.png"));
                        hideBuyOptions();
                    }
                }
            });
            button.setLayoutParams(layoutParams);
            mBuyLayout.addView(button);
        }

        // setup gold amount
        mGoldAmount = (TextView) mActivity.findViewById(R.id.gold);
        mGoldDeficitAnimation = (Animation) AnimationUtils.loadAnimation(mActivity.getApplicationContext(),
                R.anim.gold_deficit);
        updateGoldAmount(0);

        // setup send orders button
        mSendOrdersButton = (Button) mActivity.findViewById(R.id.sendOrders);
        mSendOrdersButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSendOrders();
            }
        });

        // setup players layout
        ViewGroup playersLayout = (ViewGroup) mActivity.findViewById(R.id.players);
        for (Player player : mActivity.battle.getPlayers()) {
            View layout = mActivity.getLayoutInflater().inflate(R.layout.in_game_player_layout, null);
            // set name
            TextView playerName = (TextView) layout.findViewById(R.id.name);
            playerName.setText(player.getName());
            playerName.setCompoundDrawablesWithIntrinsicBounds(GameUtils.PLAYER_BLASONS[player.getArmyIndex()], 0,
                    (player.isAI() ? R.drawable.ic_wooden_sword : 0), 0);

            // chat button
            ImageView chatButton = (ImageView) layout.findViewById(R.id.chat);
            if (!player.isAI() && player.getArmyIndex() != 0) {
                chatButton.setVisibility(View.VISIBLE);
                chatButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openChatSession();
                    }
                });
            }

            // show economy
            if (player.getArmyIndex() == 0) {
                economyBalanceTV = (TextView) layout.findViewById(R.id.economy);
                economyBalanceTV.setVisibility(View.VISIBLE);
                updateEconomyBalance(0);
            }

            // defeated players
            if (player.isDefeated()) {
                layout.setAlpha(0.5f);
            }

            playersLayout.addView(layout);
        }

    }

    public void displayBigLabel(final String text, final int color) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBigLabel.setVisibility(View.VISIBLE);
                mBigLabel.setText("" + text);
                mBigLabel.setTextColor(mActivity.getResources().getColor(color));
                mBigLabel.startAnimation(mBigLabelAnimation);
            }
        });
    }

    public void hideLoadingScreen() {
        mLoadingScreen.dismiss();
    }

    public void openGameMenu() {
        mGameMenuDialog = new Dialog(mActivity, R.style.FullScreenDialog);
        mGameMenuDialog.setContentView(R.layout.dialog_game_menu);
        mGameMenuDialog.setCancelable(true);
        Animation menuButtonAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_in);
        // surrender button
        mGameMenuDialog.findViewById(R.id.surrenderButton).setAnimation(menuButtonAnimation);
        mGameMenuDialog.findViewById(R.id.surrenderButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog confirmDialog = new CustomAlertDialog(mActivity, R.style.Dialog, mActivity
                        .getString(R.string.confirm_surrender_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == R.id.okButton) {
                            mActivity.battle.getMeSoloMode().setDefeated(true);
                            GoogleAnalyticsHelper.sendEvent(mActivity.getApplicationContext(), EventCategory.ui_action,
                                    EventAction.button_press, "surrender_game");
                            mActivity.goToReport(false);
                        }
                        dialog.dismiss();
                    }
                });
                confirmDialog.show();
            }
        });
        // resume game button
        mGameMenuDialog.findViewById(R.id.resumeGameButton).setAnimation(menuButtonAnimation);
        mGameMenuDialog.findViewById(R.id.resumeGameButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGameMenuDialog.dismiss();
            }
        });
        // exit button
        mGameMenuDialog.findViewById(R.id.exitButton).setAnimation(menuButtonAnimation);
        mGameMenuDialog.findViewById(R.id.exitButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startActivity(new Intent(mActivity, HomeActivity.class));
                mActivity.finish();
                GoogleAnalyticsHelper.sendEvent(mActivity.getApplicationContext(), EventCategory.ui_action,
                        EventAction.button_press, "exit_game");
            }
        });
        mGameMenuDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mActivity.resumeGame();
            }
        });
        mGameMenuDialog.show();
        menuButtonAnimation.start();
    }

    public void onPause() {
        if (mGameMenuDialog != null) {
            mGameMenuDialog.dismiss();
        }

        if (mLoadingScreen != null) {
            mLoadingScreen.dismiss();
        }
    }

    private Tile selectedTile = null;

    public void showBuyOptions(Tile tile) {
        selectedTile = tile;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // can I afford these units ?
                List<Unit> lstUnits = UnitsData.getUnits(mActivity.battle.getMeSoloMode().getArmy(), 0);
                for (int n = 0; n < lstUnits.size(); n++) {
                    mBuyLayout.getChildAt(n + 1).setEnabled(
                            lstUnits.get(n).getPrice() <= mActivity.battle.getMeSoloMode().getGold());
                }
                mBuyLayout.startAnimation(mBuyLayoutAnimationIn);
                mBuyLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideBuyOptions() {
        selectedTile = null;
        if (mBuyLayout.isShown()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBuyLayout.startAnimation(mBuyLayoutAnimationOut);
                    mBuyLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    public void updateGoldAmount(int goldAmount) {
        if (goldAmount < 0) {
            // in bankroot
            mGoldAmount.setTextColor(mActivity.getResources().getColor(R.color.red));
            mGoldAmount.startAnimation(mGoldDeficitAnimation);
            goldAmount = 0;
        } else {
            mGoldAmount.setTextColor(mActivity.getResources().getColor(R.color.gold));
            mGoldAmount.setAnimation(null);
        }
        mGoldAmount.setText("" + goldAmount);
    }

    private void confirmSendOrders() {
        // show confirm dialog if no orders for this turn
        if (mActivity.battle.getMeSoloMode().getLstTurnOrders().size() == 0) {
            Dialog dialog = new CustomAlertDialog(mActivity, R.style.Dialog,
                    mActivity.getString(R.string.confirm_no_orders), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == R.id.okButton) {
                                // send orders to the troops !
                                sendOrders();
                            }
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        } else {
            sendOrders();
        }
    }

    private void sendOrders() {
        // TODO multi
        hideBuyOptions();
        mActivity.runTurn();
    }

    private void openChatSession() {
        // TODO Auto-generated method stub
    }

    public void updateEconomyBalance(final int balance) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                economyBalanceTV.setText((balance < 0 ? "" : "+") + balance);
                if (balance >= 0) {
                    economyBalanceTV.setTextColor(mActivity.getResources().getColor(R.color.green));
                } else {
                    economyBalanceTV.setTextColor(mActivity.getResources().getColor(R.color.red));
                }
            }
        });
    }

    public void displayVictoryLabel(final boolean isVictory) {
        // show battle report when big label animation is over
        mBigLabelAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mActivity.goToReport(isVictory);
            }
        });

        // show victory / defeat big label
        if (isVictory) {
            // victory
            displayBigLabel(mActivity.getString(R.string.victory), R.color.green);
        } else {
            // defeat
            displayBigLabel(mActivity.getString(R.string.defeat), R.color.red);
        }
    }

}
