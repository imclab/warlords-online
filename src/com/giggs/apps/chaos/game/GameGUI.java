package com.giggs.apps.chaos.game;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.GameActivity;
import com.giggs.apps.chaos.activities.HomeActivity;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventAction;
import com.giggs.apps.chaos.analytics.GoogleAnalyticsHelper.EventCategory;
import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.BuyOrder;
import com.giggs.apps.chaos.game.model.orders.Order;
import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.game.multiplayer.ChatMessage;
import com.giggs.apps.chaos.views.CustomAlertDialog;

public class GameGUI {

    private GameActivity mGameActivity;
    private Dialog mLoadingScreen, mGameMenuDialog, mChatDialog;
    private TextView mBigLabel;
    private Animation mBigLabelAnimation;
    private Animation mBuyLayoutAnimationIn, mBuyLayoutAnimationOut;
    private ViewGroup mBuyLayout;
    private TextView mGoldAmount;
    public Button mSendOrdersButton;
    private Animation mGoldDeficitAnimation;
    private TextView economyBalanceTV;
    private ViewGroup mPlayersLayout;
    private EditText chatInput;
    private View mChatNotification;

    private Tile selectedTile = null;
    public boolean showConfirm = true;
    private int mOpenChatPlayerIndex;
    private int mChatNotificationPlayerIndex = -1;

    public GameGUI(GameActivity activity) {
        this.mGameActivity = activity;
        initGUI();
    }

    private void initGUI() {
        // setup loading screen
        mLoadingScreen = new Dialog(mGameActivity, R.style.LoadingDialog);
        mLoadingScreen.setContentView(R.layout.dialog_game_loading);
        mLoadingScreen.setCancelable(false);
        mLoadingScreen.setCanceledOnTouchOutside(false);
        // animate loading dots
        Animation loadingDotsAnimation = AnimationUtils.loadAnimation(mGameActivity, R.anim.loading_dots);
        ((TextView) mLoadingScreen.findViewById(R.id.loadingDots)).startAnimation(loadingDotsAnimation);
        mLoadingScreen.show();

        // setup big label
        mBigLabelAnimation = AnimationUtils.loadAnimation(mGameActivity, R.anim.big_label_in_game);
        mBigLabel = (TextView) mGameActivity.findViewById(R.id.bigLabel);

        // allow user to change the music volume with his phone's buttons
        mGameActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // setup buy layout
        mBuyLayoutAnimationIn = AnimationUtils.loadAnimation(mGameActivity, R.anim.bottom_in);
        mBuyLayoutAnimationOut = AnimationUtils.loadAnimation(mGameActivity, R.anim.bottom_out);
        mBuyLayout = (ViewGroup) mGameActivity.findViewById(R.id.buyLayout);
        Button mBuyReset = (Button) mGameActivity.findViewById(R.id.buyReset);
        mBuyReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTile != null) {
                    // remove order
                    for (Order order : mGameActivity.battle.getMe(mGameActivity.myArmyIndex).getLstTurnOrders()) {
                        if (order instanceof BuyOrder && ((BuyOrder) order).getTile() == selectedTile) {
                            mGameActivity.battle.getMe(mGameActivity.myArmyIndex).removeOrder(order);
                            break;
                        }
                    }
                    mGameActivity.updateUnitProduction(selectedTile.getSprite(), null);
                    hideBuyOptions();
                }
            }
        });
        // init units buy buttons
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(15, 0, 0, 0);
        List<Unit> lstAvailableUnits = UnitsData.getUnits(mGameActivity.battle.getMe(mGameActivity.myArmyIndex)
                .getArmy(), mGameActivity.myArmyIndex);
        for (int n = 0; n < lstAvailableUnits.size(); n++) {
            final Unit unit = lstAvailableUnits.get(n);
            View button = mGameActivity.getLayoutInflater().inflate(R.layout.buy_unit_button, null);
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
                        for (Order order : mGameActivity.battle.getMe(mGameActivity.myArmyIndex).getLstTurnOrders()) {
                            if (order instanceof BuyOrder && ((BuyOrder) order).getTile() == selectedTile) {
                                mGameActivity.battle.getMe(mGameActivity.myArmyIndex).removeOrder(order);
                            }
                        }
                        mGameActivity.battle
                                .getMe(mGameActivity.myArmyIndex)
                                .getLstTurnOrders()
                                .add(new BuyOrder(selectedTile, UnitsData.getUnits(unit.getArmy(),
                                        mGameActivity.battle.getMe(mGameActivity.myArmyIndex).getArmyIndex()).get(
                                        (Integer) v.getTag())));
                        mGameActivity.updateUnitProduction(selectedTile.getSprite(),
                                GraphicsFactory.mGfxMap.get(unit.getSpriteName().replace(".png", "") + "_image.png"));
                        hideBuyOptions();
                    }
                }
            });
            button.setLayoutParams(layoutParams);
            mBuyLayout.addView(button);
        }

        // setup gold amount
        mGoldAmount = (TextView) mGameActivity.findViewById(R.id.gold);
        mGoldDeficitAnimation = (Animation) AnimationUtils.loadAnimation(mGameActivity.getApplicationContext(),
                R.anim.gold_deficit);
        updateGoldAmount(0);

        // setup send orders button
        mSendOrdersButton = (Button) mGameActivity.findViewById(R.id.sendOrders);
        mSendOrdersButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSendOrders();
            }
        });

        // setup players layout
        mPlayersLayout = (ViewGroup) mGameActivity.findViewById(R.id.players);
        for (Player player : mGameActivity.battle.getPlayers()) {
            View layout = mGameActivity.getLayoutInflater().inflate(R.layout.in_game_player_layout, null);
            // set name
            TextView playerName = (TextView) layout.findViewById(R.id.name);
            playerName.setText(player.getName());
            playerName.setCompoundDrawablesWithIntrinsicBounds(GameUtils.PLAYER_BLASONS[player.getArmyIndex()], 0,
                    (player.isAI() ? R.drawable.ic_wooden_sword : 0), 0);

            // chat button
            ImageView chatButton = (ImageView) layout.findViewById(R.id.chat);
            if (!player.isAI() && player.getArmyIndex() != mGameActivity.myArmyIndex) {
                chatButton.setVisibility(View.VISIBLE);
                chatButton.setTag(player.getArmyIndex());
                chatButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openChatSession((Integer) v.getTag());
                    }
                });
            }

            // show economy
            if (player.getArmyIndex() == mGameActivity.myArmyIndex) {
                economyBalanceTV = (TextView) layout.findViewById(R.id.economy);
                economyBalanceTV.setVisibility(View.VISIBLE);
                updateEconomyBalance(0);
            }

            // defeated players
            if (player.isDefeated()) {
                layout.setAlpha(0.5f);
            }

            mPlayersLayout.addView(layout);
        }

        // chat dialog
        mChatDialog = new Dialog(mGameActivity, R.style.FullScreenDialog);
        mChatDialog.setContentView(R.layout.dialog_game_chat);
        mChatDialog.setCancelable(true);
        // message input
        chatInput = (EditText) mChatDialog.findViewById(R.id.chatInput);
        chatInput.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!v.getText().toString().isEmpty()
                        && (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || event != null
                                && event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    mGameActivity.sendChatMessage(mOpenChatPlayerIndex, v.getText().toString());
                    // reset input
                    chatInput.setText(null);
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) mGameActivity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        // back button
        mChatDialog.findViewById(R.id.backButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatDialog.dismiss();
            }
        });

        // chat notification
        mChatNotification = mGameActivity.findViewById(R.id.chatNotification);
        mChatNotification.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatNotificationPlayerIndex >= 0) {
                    openChatSession(mChatNotificationPlayerIndex);
                    checkChatNotificationPending();
                }
            }
        });
    }

    public void displayBigLabel(final String text, final int color) {
        mGameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBigLabel.setVisibility(View.VISIBLE);
                mBigLabel.setText("" + text);
                mBigLabel.setTextColor(mGameActivity.getResources().getColor(color));
                mBigLabel.startAnimation(mBigLabelAnimation);
            }
        });
    }

    public void hideLoadingScreen() {
        mLoadingScreen.dismiss();
    }

    public void openGameMenu() {
        mGameMenuDialog = new Dialog(mGameActivity, R.style.FullScreenDialog);
        mGameMenuDialog.setContentView(R.layout.dialog_game_menu);
        mGameMenuDialog.setCancelable(true);
        Animation menuButtonAnimation = AnimationUtils.loadAnimation(mGameActivity, R.anim.bottom_in);
        // surrender button
        mGameMenuDialog.findViewById(R.id.surrenderButton).setAnimation(menuButtonAnimation);
        mGameMenuDialog.findViewById(R.id.surrenderButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog confirmDialog = new CustomAlertDialog(mGameActivity, R.style.Dialog, mGameActivity
                        .getString(R.string.confirm_surrender_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == R.id.okButton) {
                            mGameActivity.battle.getMe(mGameActivity.myArmyIndex).setDefeated(true);
                            GoogleAnalyticsHelper.sendEvent(mGameActivity.getApplicationContext(),
                                    EventCategory.ui_action, EventAction.button_press, "surrender_game");
                            mGameActivity.goToReport();
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
                mGameActivity.startActivity(new Intent(mGameActivity, HomeActivity.class));
                mGameActivity.finish();
                GoogleAnalyticsHelper.sendEvent(mGameActivity.getApplicationContext(), EventCategory.ui_action,
                        EventAction.button_press, "exit_game");
            }
        });
        mGameMenuDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mGameActivity.resumeGame();
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
        if (mChatDialog != null) {
            mChatDialog.dismiss();
        }
    }

    public void showBuyOptions(Tile tile) {
        if (!mGameActivity.hasSendOrders) {
            selectedTile = tile;
            mGameActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // can I afford these units ?
                    List<Unit> lstUnits = UnitsData.getUnits(mGameActivity.battle.getMe(mGameActivity.myArmyIndex)
                            .getArmy(), mGameActivity.myArmyIndex);
                    for (int n = 0; n < lstUnits.size(); n++) {
                        mBuyLayout.getChildAt(n + 1).setEnabled(
                                lstUnits.get(n).getPrice() <= mGameActivity.battle.getMe(mGameActivity.myArmyIndex)
                                        .getGold());
                    }
                    mBuyLayout.startAnimation(mBuyLayoutAnimationIn);
                    mBuyLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void hideBuyOptions() {
        selectedTile = null;
        if (mBuyLayout.isShown()) {
            mGameActivity.runOnUiThread(new Runnable() {
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
            mGoldAmount.setTextColor(mGameActivity.getResources().getColor(R.color.red));
            mGoldAmount.startAnimation(mGoldDeficitAnimation);
            goldAmount = 0;
        } else {
            mGoldAmount.setTextColor(mGameActivity.getResources().getColor(R.color.gold));
            mGoldAmount.setAnimation(null);
        }
        mGoldAmount.setText("" + goldAmount);
    }

    private void confirmSendOrders() {
        // show confirm dialog if no orders for this turn
        if (showConfirm && mGameActivity.battle.getMe(mGameActivity.myArmyIndex).getLstTurnOrders().size() == 0) {
            Dialog dialog = new CustomAlertDialog(mGameActivity, R.style.Dialog,
                    mGameActivity.getString(R.string.confirm_no_orders), new DialogInterface.OnClickListener() {
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
        hideBuyOptions();
        if (mGameActivity.mIsMultiplayerGame) {
            // send orders
            mSendOrdersButton.setVisibility(View.GONE);
            mGameActivity.hasSendOrders = true;
            mGameActivity.sendOrdersOnline();
            mGameActivity.onNewOrders();
        } else {
            mGameActivity.runTurn();
        }
    }

    public void updateEconomyBalance(final int balance) {
        mGameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                economyBalanceTV.setText((balance < 0 ? "" : "+") + balance);
                if (balance >= 0) {
                    economyBalanceTV.setTextColor(mGameActivity.getResources().getColor(R.color.green));
                } else {
                    economyBalanceTV.setTextColor(mGameActivity.getResources().getColor(R.color.red));
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
                mGameActivity.goToReport();
            }
        });

        // show victory / defeat big label
        if (isVictory) {
            // victory
            displayBigLabel(mGameActivity.getString(R.string.victory), R.color.green);
        } else {
            // defeat
            displayBigLabel(mGameActivity.getString(R.string.defeat), R.color.red);
        }
    }

    public void updatePlayersNameColor(Battle battle) {
        for (int n = 0; n < battle.getPlayers().size(); n++) {
            Player p = battle.getPlayers().get(n);
            if (p.isDefeated()) {
                ((TextView) mPlayersLayout.getChildAt(n).findViewById(R.id.name)).setTextColor(mGameActivity
                        .getResources().getColor(R.color.red));
                if (!p.isAI()) {
                    mPlayersLayout.getChildAt(n).findViewById(R.id.chat).setVisibility(View.GONE);
                }
            }
        }
    }

    private void openChatSession(int playerIndex) {
        ((ViewGroup) mChatDialog.findViewById(R.id.messages)).removeAllViews();
        mOpenChatPlayerIndex = playerIndex;
        mChatDialog.show();
        for (ChatMessage msg : mGameActivity.battle.getPlayers().get(playerIndex).getChatMessages()) {
            addMessageToChatDialog(msg);
        }
        scrollToChatBottom();
        checkChatNotificationPending();
    }

    public void scrollToChatBottom() {
        // go to the bottom of the chat
        ((ScrollView) mChatDialog.findViewById(R.id.scroll)).post(new Runnable() {
            @Override
            public void run() {
                ((ScrollView) mChatDialog.findViewById(R.id.scroll)).fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void addMessageToChatDialog(ChatMessage msg) {
        // show messages from player
        LinearLayout messagesLayout = (LinearLayout) mChatDialog.findViewById(R.id.messages);
        View messageView = mGameActivity.getLayoutInflater().inflate(
                msg.isOut() ? R.layout.chat_message_out : R.layout.chat_message_in, null);

        // sent messages are aligned on the right
        if (!msg.isOut()) {
            TextView senderTV = (TextView) messageView.findViewById(R.id.sender);
            senderTV.setText(msg.getSenderName() + " :");
            senderTV.setVisibility(View.VISIBLE);
        }

        TextView contentTV = (TextView) messageView.findViewById(R.id.content);
        contentTV.setText("" + msg.getContent());

        // unread messages in light blue
        if (!msg.isRead()) {
            contentTV.setTextColor(mGameActivity.getResources().getColor(R.color.unread_message));
        }

        messagesLayout.addView(messageView);

        // update messages to read
        msg.setRead(true);
    }

    public void onReceiveChatMessage(int senderIndex, ChatMessage receivedMessage) {
        if (mChatDialog.isShowing() && mOpenChatPlayerIndex == senderIndex) {
            // if chat is already opened
            addMessageToChatDialog(receivedMessage);
            checkChatNotificationPending();
        } else if (mChatNotificationPlayerIndex == -1) {
            // if no notification, show notification
            showChatNotification(senderIndex);

        }
    }

    private void checkChatNotificationPending() {
        for (Player p : mGameActivity.battle.getPlayers()) {
            for (ChatMessage msg : p.getChatMessages()) {
                if (!msg.isRead() && (!mChatDialog.isShowing() || mOpenChatPlayerIndex != p.getArmyIndex())) {
                    showChatNotification(p.getArmyIndex());
                    return;
                }
            }
        }
        showChatNotification(-1);
    }

    private void showChatNotification(int chatNotificationPlayerIndex) {
        mChatNotificationPlayerIndex = chatNotificationPlayerIndex;
        if (chatNotificationPlayerIndex >= 0) {
            mChatNotification.startAnimation(mGoldDeficitAnimation);
            mChatNotification.setVisibility(View.VISIBLE);
        } else {
            mChatNotification.setAnimation(null);
            mChatNotification.setVisibility(View.GONE);
        }
    }

}
