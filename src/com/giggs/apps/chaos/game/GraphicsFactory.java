package com.giggs.apps.chaos.game;

import java.util.HashMap;

import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.Player;

public class GraphicsFactory {

    private Context mContext;
    private VertexBufferObjectManager mVertexBufferObjectManager;
    private TextureManager mTextureManager;

    private BitmapTextureAtlas mTexture;
    public static HashMap<String, TextureRegion> mGfxMap = new HashMap<String, TextureRegion>();

    public GraphicsFactory(Context context, VertexBufferObjectManager vertexBufferObjectManager,
            TextureManager textureManager) {
        mContext = context;
        mVertexBufferObjectManager = vertexBufferObjectManager;
        mTextureManager = textureManager;
    }

    public void initGraphics(Battle battle) {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        // load map graphics
        for (TerrainData terrain : TerrainData.values()) {
            loadGfxFromAssets(256, 256, terrain.getSpriteName());
        }

        // load all units graphics
        for (Player player : battle.getPlayers()) {
        }

        // stuff to load
        loadGfxFromAssets(256, 256, "control_zone.png");
        loadGfxFromAssets(128, 128, "selection.png");
        loadGfxFromAssets(64, 64, "buy.png");
    }

    private void loadGfxFromAssets(int textureWidth, int textureHeight, String imageName) {
        if (mGfxMap.get(imageName) == null) {
            mTexture = new BitmapTextureAtlas(mTextureManager, textureWidth, textureHeight, TextureOptions.DEFAULT);
            TextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, mContext,
                    imageName, 0, 0);
            mTexture.load();
            mGfxMap.put(imageName, textureRegion);
        }
    }

    private void loadGfxFromResources(int textureWidth, int textureHeight, int imageResource, String imageName) {
        if (mGfxMap.get(imageName) == null) {
            mTexture = new BitmapTextureAtlas(mTextureManager, textureWidth, textureHeight, TextureOptions.DEFAULT);
            TextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromResource(mTexture, mContext,
                    imageResource, 0, 0);
            mTexture.load();
            mGfxMap.put(imageName, textureRegion);
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
