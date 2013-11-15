package com.giggs.apps.chaos.game;

import java.util.HashMap;

import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;
import com.giggs.apps.chaos.game.model.units.Unit;

public class GraphicsFactory {

    private Context mContext;
    private TextureManager mTextureManager;

    private BitmapTextureAtlas mTexture;
    public static HashMap<String, TextureRegion> mGfxMap = new HashMap<String, TextureRegion>();
    public static HashMap<String, TiledTextureRegion> mTiledGfxMap = new HashMap<String, TiledTextureRegion>();

    public GraphicsFactory(Context context, VertexBufferObjectManager vertexBufferObjectManager,
            TextureManager textureManager) {
        mContext = context;
        mTextureManager = textureManager;
    }

    public void initGraphics(Battle battle) {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        // load map graphics
        for (TerrainData terrain : TerrainData.values()) {
            loadGfxFromAssets(256, 256, terrain.getSpriteName(), 0, 0);
            // add winter tiles
            loadGfxFromAssets(256, 256, terrain.getSpriteName().replace(".png", "") + "_winter.png", 0, 0);
        }

        // load all units graphics
        for (Player player : battle.getPlayers()) {
            for (Unit unit : UnitsData.getUnits(player.getArmy(), player.getArmyIndex())) {
                loadTiledTextureGfxFromAssets(126, 168, unit.getSpriteName());
                loadGfxFromAssets(80, 80, unit.getSpriteName().replace(".png", "") + "_image.png", 0, 0);
            }
        }

        // load flags
        for (int n = 0; n < battle.getPlayers().size(); n++) {
            loadGfxFromAssets(72, 80, "blason_" + (n + 1) + ".png", 0, 0);
        }

        // stuff to load
        loadGfxFromAssets(256, 256, "control_zone.png", 0, 0);
        loadGfxFromAssets(128, 128, "selection.png", 0, 0);
        loadGfxFromAssets(64, 64, "buy.png", 0, 0);
        loadGfxFromAssets(50, 80, "move_order.png", 0, 0);
        loadGfxFromAssets(70, 70, "defend_order.png", 0, 0);
        loadGfxFromAssets(2, 2, "transparent.png", 0, 0);
    }

    private void loadGfxFromAssets(int textureWidth, int textureHeight, String imageName, int x, int y) {
        if (mGfxMap.get(imageName) == null) {
            mTexture = new BitmapTextureAtlas(mTextureManager, textureWidth, textureHeight, TextureOptions.DEFAULT);
            TextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, mContext,
                    imageName, x, y);
            mTexture.load();
            mGfxMap.put(imageName, textureRegion);
        }
    }

    // can't manage to make it work : invisible sprite...
    @SuppressWarnings("unused")
    private void loadGfxFromResources(int textureWidth, int textureHeight, int imageResource, String imageName) {
        if (mGfxMap.get(imageName) == null) {
            mTexture = new BitmapTextureAtlas(mTextureManager, textureWidth, textureHeight, TextureOptions.DEFAULT);
            TextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromResource(mTexture, mContext,
                    imageResource, 0, 0);
            mTexture.load();
            mGfxMap.put(imageName, textureRegion);
        }
    }

    private void loadTiledTextureGfxFromAssets(int textureWidth, int textureHeight, String spriteName) {
        if (mGfxMap.get(spriteName) == null) {
            mTexture = new BitmapTextureAtlas(mTextureManager, textureWidth, textureHeight, TextureOptions.DEFAULT);
            TiledTextureRegion tiledTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mTexture,
                    mContext.getAssets(), spriteName, 0, 0, 3, 4);
            mTexture.load();
            mTiledGfxMap.put(spriteName, tiledTexture);
        }
    }

    // public GameSprite addGameElement(Scene scene, GameElement gameElement,
    // InputManager inputManager,
    // boolean isMySquad, TMXTile t) {
    // // create sprite
    // final GameSprite sprite = new GameSprite(gameElement, inputManager,
    // t.getTileX(), t.getTileY(),
    // mGfxMap.get(gameElement.getSpriteName()), mVertexBufferObjectManager);
    // gameElement.setSprite(sprite);
    // gameElement.setRank(isMySquad ? Rank.ally : Rank.enemy);
    // scene.attachChild(sprite);
    // scene.registerTouchArea(sprite);
    // return sprite;
    // }
    //
    // public static Sprite createSprite(float x, float y, String spriteName,
    // VertexBufferObjectManager vertexBufferObjectManager) {
    // return new Sprite(x, y, GraphicElementFactory.mGfxMap.get(spriteName),
    // vertexBufferObjectManager);
    // }

}
