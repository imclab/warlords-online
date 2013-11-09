package com.glevel.wwii.activities;

import java.util.HashMap;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSCounter;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.LayoutGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.glevel.wwii.R;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler.EventAction;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler.EventCategory;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler.TimingCategory;
import com.glevel.wwii.analytics.GoogleAnalyticsHandler.TimingName;
import com.glevel.wwii.database.DatabaseHelper;
import com.glevel.wwii.game.AI;
import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.GraphicElementFactory;
import com.glevel.wwii.game.InputManager;
import com.glevel.wwii.game.SaveGameHelper;
import com.glevel.wwii.game.andengine.custom.CustomZoomCamera;
import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.graphics.Crosshair;
import com.glevel.wwii.game.graphics.DeploymentZone;
import com.glevel.wwii.game.graphics.Protection;
import com.glevel.wwii.game.graphics.SelectionCircle;
import com.glevel.wwii.game.interfaces.OnNewSpriteToDraw;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.Battle.Phase;
import com.glevel.wwii.game.model.GameElement.Rank;
import com.glevel.wwii.game.model.GameSprite;
import com.glevel.wwii.game.model.Player;
import com.glevel.wwii.game.model.map.Tile;
import com.glevel.wwii.game.model.orders.FireOrder;
import com.glevel.wwii.game.model.orders.MoveOrder;
import com.glevel.wwii.game.model.orders.Order;
import com.glevel.wwii.game.model.units.Soldier;
import com.glevel.wwii.game.model.units.Unit;
import com.glevel.wwii.game.model.weapons.Weapon;
import com.glevel.wwii.views.CustomAlertDialog;

public class GameActivity extends LayoutGameActivity implements OnNewSpriteToDraw {

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	public Scene mScene;
	private ZoomCamera mCamera;

	private Dialog mLoadingScreen;

	public TMXTiledMap mTMXTiledMap;

	public Sprite selectionCircle;
	public Line orderLine;
	private DeploymentZone deploymentZone;

	public Battle battle;
	public ViewGroup mSelectedUnitLayout;

	private TextView bigLabel;
	private Animation bigLabelAnimation;
	private Button finishDeploymentButton;
	private OnClickListener onFinishDeploymentClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finishDeploymentButton.setVisibility(View.GONE);
			startGame();
		}
	};

	private long mDeploymentStartTime = 0L;
	private long mGameStartTime = 0L;
	private DatabaseHelper mDbHelper;

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new CustomZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
		        new FillResolutionPolicy(), mCamera);
		return engineOptions;
	}

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		mDbHelper = new DatabaseHelper(getApplicationContext());

		// battle = GameUtils.createTestData();

		Bundle extras = getIntent().getExtras();
		long gameId = extras.getLong("game_id", 0);
		battle = mDbHelper.getBattleDao().getById(gameId);
		battle.setOnNewSprite(this);

		setupUI();

		// allow user to change the music volume with his phone's buttons
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void setupUI() {
		// setup loading screen
		mLoadingScreen = new Dialog(this, R.style.FullScreenDialog);
		mLoadingScreen.setContentView(R.layout.dialog_game_loading);
		mLoadingScreen.setCancelable(false);
		mLoadingScreen.setCanceledOnTouchOutside(false);
		// animate loading dots
		Animation loadingDotsAnimation = AnimationUtils.loadAnimation(this, R.anim.loading_dots);
		((TextView) mLoadingScreen.findViewById(R.id.loadingDots)).startAnimation(loadingDotsAnimation);
		mLoadingScreen.show();

		// setup selected unit layout
		mSelectedUnitLayout = (ViewGroup) findViewById(R.id.selectedUnit);

		// setup big label
		bigLabelAnimation = AnimationUtils.loadAnimation(this, R.anim.big_label_in_game);
		bigLabel = (TextView) findViewById(R.id.bigLabel);

		// setup finish deployment button
		finishDeploymentButton = (Button) findViewById(R.id.finishDeployment);
		finishDeploymentButton.setOnClickListener(onFinishDeploymentClicked);
	}

	public void updateSelectedElementLayout(final GameSprite selectedElement) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (selectedElement == null) {
					mSelectedUnitLayout.setVisibility(View.GONE);
					crosshair.setVisible(false);
					return;
				}
				Unit unit = (Unit) selectedElement.getGameElement();

				// hide enemies info
				updateUnitInfoVisibility(unit.getArmy() == battle.getMe().getArmy());

				// name
				if (unit instanceof Soldier) {
					// display real name
					((TextView) mSelectedUnitLayout.findViewById(R.id.unitName))
					        .setText(((Soldier) unit).getRealName());
				} else {
					((TextView) mSelectedUnitLayout.findViewById(R.id.unitName)).setText(unit.getName());
				}

				// health
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitName)).setTextColor(getResources().getColor(
				        unit.getHealth().getColor()));

				// experience
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitExperience)).setText(unit.getExperience().name());
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitExperience)).setTextColor(getResources()
				        .getColor(unit.getExperience().getColor()));

				// weapons
				// main weapon
				Weapon mainWeapon = unit.getWeapons().get(0);
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitMainWeaponName)).setText(mainWeapon.getName());
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitMainWeaponName))
				        .setCompoundDrawablesWithIntrinsicBounds(mainWeapon.getImage(), 0, 0, 0);
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitMainWeaponAP)).setBackgroundResource(mainWeapon
				        .getAPColorEfficiency());
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitMainWeaponAT)).setBackgroundResource(mainWeapon
				        .getATColorEfficiency());
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitMainWeaponAmmo)).setText(""
				        + mainWeapon.getAmmoAmount());

				// secondary weapon
				if (unit.getWeapons().size() > 1) {
					((ViewGroup) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeapon))
					        .setVisibility(View.VISIBLE);
					Weapon secondaryWeapon = unit.getWeapons().get(1);
					((TextView) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeaponName)).setText(secondaryWeapon
					        .getName());
					((TextView) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeaponName))
					        .setCompoundDrawablesWithIntrinsicBounds(secondaryWeapon.getImage(), 0, 0, 0);
					((TextView) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeaponAP))
					        .setBackgroundResource(secondaryWeapon.getAPColorEfficiency());
					((TextView) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeaponAT))
					        .setBackgroundResource(secondaryWeapon.getATColorEfficiency());
					((TextView) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeaponAmmo)).setText(""
					        + secondaryWeapon.getAmmoAmount());
				} else {
					((ViewGroup) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeapon)).setVisibility(View.GONE);
				}
				// frags
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitFrags)).setText(getString(R.string.frags_number,
				        unit.getFrags()));

				// current action
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitAction)).setText(unit.getCurrentAction().name());
				((TextView) mSelectedUnitLayout.findViewById(R.id.unitAction)).setVisibility(unit.isDead() ? View.GONE
				        : View.VISIBLE);

				mSelectedUnitLayout.setVisibility(View.VISIBLE);

				Order o = unit.getOrder();
				if (unit.getRank() == Rank.ally && o != null) {
					if (o instanceof FireOrder) {
						FireOrder f = (FireOrder) o;
						crosshair.setColor(Color.RED);
						crosshair.setPosition(f.getTarget().getSprite().getX() - crosshair.getWidth() / 2, f
						        .getTarget().getSprite().getY()
						        - crosshair.getHeight() / 2);
						crosshair.setVisible(true);
					} else if (o instanceof MoveOrder) {
						MoveOrder f = (MoveOrder) o;
						crosshair.setColor(Color.GREEN);
						crosshair.setPosition(f.getxDestination() - crosshair.getWidth() / 2, f.getyDestination()
						        - crosshair.getHeight() / 2);
						crosshair.setVisible(true);
					} else {
						crosshair.setVisible(false);
					}
				} else {
					crosshair.setVisible(false);
				}

			}
		});
	}

	private void updateUnitInfoVisibility(boolean isAlly) {
		int visibility = isAlly ? View.VISIBLE : View.GONE;
		((TextView) mSelectedUnitLayout.findViewById(R.id.unitExperience)).setVisibility(visibility);
		((TextView) mSelectedUnitLayout.findViewById(R.id.unitMainWeaponAmmo)).setVisibility(visibility);
		((TextView) mSelectedUnitLayout.findViewById(R.id.unitSecondaryWeaponAmmo)).setVisibility(visibility);
		((TextView) mSelectedUnitLayout.findViewById(R.id.unitFrags)).setVisibility(visibility);
	}

	private Font mFont;
	private Text fpsText;
	private GraphicElementFactory mGameElementFactory;
	private InputManager mInputManager;
	private Dialog mGameMenuDialog;
	public Crosshair crosshair, crossHairLine;
	public Protection protection;
	public TMXLayer tmxLayer;

	public static int gameCounter = 0;

	private static final int UPDATE_VISION_FREQUENCY = 10;
	private static final int CHECK_FREQUENCY_FREQUENCY = 10;
	private static final int AI_FREQUENCY = 10;

	private void updateGame() {
		gameCounter++;
		if (gameCounter > 999) {
			gameCounter = 0;
		}
		if (gameCounter % UPDATE_VISION_FREQUENCY == 0) {
			updateVisibility();
		}
		for (Player player : battle.getPlayers()) {
			for (Unit unit : player.getUnits()) {

				if (!unit.isDead()) {
					if (player.isAI() && gameCounter % AI_FREQUENCY == 0) {
						// update AI orders
						AI.updateUnitOrder(battle, unit);
					}
					if (unit.getOrder() != null) {
						// resolve unit action
						unit.resolveOrder(battle);
					} else {
						// no order : take initiative
						unit.takeInitiative();
					}
				}
			}
			// check victory conditions
			if (gameCounter % CHECK_FREQUENCY_FREQUENCY == 0 && player.checkIfPlayerWon(battle)) {
				endGame(player, false);
			}
		}
		updateSelectedElementLayout(mInputManager.selectedElement);
	}

	/**
	 * Update player vision
	 */
	private void updateVisibility() {
		for (Unit unit : battle.getPlayers().get(1).getUnits()) {
			if (unit.getRank() == Rank.enemy && !unit.isDead()) {
				unit.setVisible(false);
			}
		}
		for (Unit u : battle.getMe().getUnits()) {
			for (Unit e : battle.getEnemies(u)) {
				if (GameUtils.canSee(battle.getMap(), u, e)) {
					e.setVisible(true);
				}
			}
		}
	}

	private void updateMoves() {
		for (Player player : battle.getPlayers()) {
			for (Unit unit : player.getUnits()) {
				if (unit.getOrder() != null && unit.getOrder() instanceof MoveOrder) {
					// move unit
					unit.move();
					TMXTile newTile = tmxLayer.getTMXTileAt(unit.getSprite().getX(), unit.getSprite().getY());
					if (newTile.getTileX() != unit.getTilePosition().getTileX()
					        || newTile.getTileY() != unit.getTilePosition().getTileY()) {
						unit.setTilePosition(battle.getMap().getTiles()[newTile.getTileRow()][newTile.getTileColumn()]);
					}
				}
			}
		}
		updateSelectedElementLayout(mInputManager.selectedElement);
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
		mGameElementFactory = new GraphicElementFactory(this, getVertexBufferObjectManager(), getTextureManager());

		mGameElementFactory.initGraphics(battle);

		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256,
		        Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, Color.WHITE.hashCode());
		this.mFont.load();
		mLoadingScreen.dismiss();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
		GoogleAnalyticsHandler.sendTiming(getApplicationContext(), TimingCategory.resources, TimingName.load_game,
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

		// tile map
		try {
			final TMXLoader tmxLoader = new TMXLoader(this.getAssets(), this.mEngine.getTextureManager(),
			        TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getVertexBufferObjectManager(), null);
			this.mTMXTiledMap = tmxLoader.loadFromAsset("tmx/" + battle.getTileMapName());

		} catch (final TMXLoadException e) {
			Debug.e(e);
		}

		tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(tmxLayer);

		// init battle's map
		Tile[][] lstTiles = new Tile[tmxLayer.getTileRows()][tmxLayer.getTileColumns()];
		for (TMXTile[] tt : tmxLayer.getTMXTiles()) {
			for (TMXTile t : tt) {
				lstTiles[t.getTileRow()][t.getTileColumn()] = new Tile(t, mTMXTiledMap);
			}
		}
		battle.getMap().setTiles(lstTiles);
		battle.getMap().setTmxLayer(tmxLayer);

		/* Make the camera not exceed the bounds of the TMXEntity. */
		this.mCamera.setBounds(0, 0, tmxLayer.getHeight(), tmxLayer.getWidth());
		this.mCamera.setBoundsEnabled(true);

		final FPSCounter fpsCounter = new FPSCounter();
		this.mEngine.registerUpdateHandler(fpsCounter);
		fpsText = new Text(0, 0, mFont, "FPS:", 10, getVertexBufferObjectManager());
		mScene.registerUpdateHandler(new TimerHandler(0.1f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				fpsText.setText("FPS: " + Math.round(fpsCounter.getFPS()));
			}
		}));
		mScene.attachChild(fpsText);

		selectionCircle = new SelectionCircle(GraphicElementFactory.mGfxMap.get("selection.png"),
		        getVertexBufferObjectManager());

		crosshair = new Crosshair(GraphicElementFactory.mGfxMap.get("crosshair.png"), getVertexBufferObjectManager());
		crosshair.setVisible(false);
		mScene.attachChild(crosshair);
		Text distanceText = new Text(0, 0, mFont, "", 5, getVertexBufferObjectManager());
		mScene.attachChild(distanceText);
		crossHairLine = new Crosshair(GraphicElementFactory.mGfxMap.get("crosshair.png"),
		        getVertexBufferObjectManager(), distanceText);
		crossHairLine.setVisible(false);
		mScene.attachChild(crossHairLine);
		protection = new Protection(GraphicElementFactory.mGfxMap.get("protection.png"), getVertexBufferObjectManager());
		protection.setVisible(false);
		mScene.attachChild(protection);

		orderLine = new Line(0, 0, 0, 0, getVertexBufferObjectManager());
		orderLine.setColor(0.5f, 1f, 0.3f);
		orderLine.setLineWidth(50.0f);
		mScene.attachChild(orderLine);

		// Render loop
		mScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				updateMoves();
			}
		});

		pOnCreateSceneCallback.onCreateSceneFinished(mScene);

		// if (isLoadedGame) {
		// startGame();
		// } else {
		prepareDeploymentPhase();
		// }
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public void onBackPressed() {
		pauseGame();
	}

	private void openGameMenu() {
		mGameMenuDialog = new Dialog(this, R.style.FullScreenDialog);
		mGameMenuDialog.setContentView(R.layout.dialog_game_menu);
		mGameMenuDialog.setCancelable(true);
		Animation menuButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
		// surrender button
		mGameMenuDialog.findViewById(R.id.surrenderButton).setAnimation(menuButtonAnimation);
		mGameMenuDialog.findViewById(R.id.surrenderButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog confirmDialog = new CustomAlertDialog(GameActivity.this, R.style.Dialog,
				        getString(R.string.confirm_surrender_message), new DialogInterface.OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
						        if (which == R.id.okButton) {
							        endGame(battle.getEnemyPlayer(battle.getMe()), true);
							        GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
							                EventAction.button_press, "surrender_game");
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
				startActivity(new Intent(GameActivity.this, HomeActivity.class));
				finish();
				GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.ui_action,
				        EventAction.button_press, "exit_game");
			}
		});
		mGameMenuDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				resumeGame();
			}
		});
		mGameMenuDialog.show();
		menuButtonAnimation.start();
	}

	private boolean hasToSaveGame = true;

	@Override
	protected void onPause() {
		super.onPause();
		if (mGameMenuDialog != null) {
			mGameMenuDialog.dismiss();
		}
		if (mLoadingScreen != null) {
			mLoadingScreen.dismiss();
		}
		GraphicElementFactory.mGfxMap = new HashMap<String, TextureRegion>();
		if (hasToSaveGame) {
			SaveGameHelper.saveGame(mDbHelper, battle);
		}
	}

	private void pauseGame() {
		openGameMenu();
		mEngine.stop();
	}

	private void resumeGame() {
		mEngine.start();
	}

	private void prepareDeploymentPhase() {
		mDeploymentStartTime = System.currentTimeMillis();
		displayBigLabel(R.string.deployment_phase, R.color.bg_btn_green);

		for (Player player : battle.getPlayers()) {
			if (!player.getArmy().equals(ArmiesData.GERMANY) && battle.getMap().isAllyLeftSide()) {
				player.setXPositionDeployment(0);
			} else {
				player.setXPositionDeployment(battle.getMap().getWidth() - GameUtils.DEPLOYMENT_ZONE_SIZE);
			}
			for (Unit unit : player.getUnits()) {
				TMXTile t = tmxLayer.getTMXTile((int) (player.getXPositionDeployment() + Math.random() * 5),
				        (int) (Math.random() * battle.getMap().getHeight()));
				mGameElementFactory.addGameElement(mScene, unit, mInputManager, (player.getArmyIndex() == 0), t);
				unit.setTilePosition(battle.getMap().getTiles()[t.getTileRow()][t.getTileColumn()]);
				// units init rotation
				if (player.getXPositionDeployment() == 0) {
					unit.getSprite().setRotation(90);
				} else {
					unit.getSprite().setRotation(-90);
				}
			}
		}

		deploymentZone = new DeploymentZone(battle.getMe().getXPositionDeployment() * 32.0f, 0.0f, (battle.getMe()
		        .getXPositionDeployment() + GameUtils.DEPLOYMENT_ZONE_SIZE) * 32.0f,
		        battle.getMap().getHeight() * 32.0f, getVertexBufferObjectManager());
		mScene.attachChild(deploymentZone);
	}

	private void displayBigLabel(final int textResource, final int color) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bigLabel.setVisibility(View.VISIBLE);
				bigLabel.setTextColor(getResources().getColor(color));
				bigLabel.setText(textResource);
				bigLabel.startAnimation(bigLabelAnimation);
			}
		});
	}

	private void startGame() {
		if (mDeploymentStartTime > 0) {
			GoogleAnalyticsHandler.sendTiming(getApplicationContext(), TimingCategory.in_game,
			        TimingName.deployment_time, (System.currentTimeMillis() - mDeploymentStartTime) / 1000);
			mGameStartTime = System.currentTimeMillis();
		}

		displayBigLabel(R.string.go, R.color.bg_btn_green);

		deploymentZone.setVisible(false);

		battle.setPhase(Phase.combat);

		// update game logic loop
		TimerHandler spriteTimerHandler;
		spriteTimerHandler = new TimerHandler(1.0f / GameUtils.GAME_LOOP_FREQUENCY, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				updateGame();
			}
		});
		mEngine.registerUpdateHandler(spriteTimerHandler);
	}

	private void endGame(final Player winningPlayer, boolean instantly) {
		if (mGameStartTime > 0L) {
			GoogleAnalyticsHandler.sendTiming(getApplicationContext(), TimingCategory.in_game, TimingName.game_time,
			        (System.currentTimeMillis() - mGameStartTime) / 1000);
		}

		// stop engine
		mEngine.stop();

		if (instantly) {
			// show battle report without big label animation
			goToReport(winningPlayer == battle.getMe());

		} else {
			// show battle report when big label animation is over
			bigLabelAnimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					goToReport(winningPlayer == battle.getMe());
				}
			});

			// show victory / defeat big label
			if (winningPlayer == battle.getMe()) {
				// victory
				displayBigLabel(R.string.victory, R.color.green);
			} else {
				// defeat
				displayBigLabel(R.string.defeat, R.color.red);
			}

			GoogleAnalyticsHandler.sendEvent(getApplicationContext(), EventCategory.in_game, EventAction.end_game,
			        winningPlayer == battle.getMe() ? "victory" : "defeat");
		}
	}

	private void goToReport(boolean victory) {
		hasToSaveGame = false;
		long battleId = SaveGameHelper.saveGame(mDbHelper, battle);
		Intent i = new Intent(GameActivity.this, BattleReportActivity.class);
		Bundle extras = new Bundle();
		extras.putLong("game_id", battleId);
		extras.putBoolean("victory", victory);
		i.putExtras(extras);
		startActivity(i);
		finish();
	}

	@Override
	public void drawSprite(float x, float y, String spriteName, final int duration, int size) {
		final Sprite sprite = GraphicElementFactory.createSprite(0, 0, spriteName, getVertexBufferObjectManager());
		sprite.setScale(size);
		sprite.setPosition(x - sprite.getWidth() / 2, y - sprite.getHeight() / 2);
		mScene.attachChild(sprite);
		sprite.registerUpdateHandler(new IUpdateHandler() {

			private int timeLeft = duration;

			public void onUpdate(float pSecondsElapsed) {
				if (--timeLeft <= 0) {
					runOnUpdateThread(new Runnable() {
						@Override
						public void run() {
							mScene.detachChild(sprite);
						}
					});
				}
			}

			@Override
			public void reset() {
			}
		});
	}

}
