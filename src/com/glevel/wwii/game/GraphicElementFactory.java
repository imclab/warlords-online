package com.glevel.wwii.game;

import java.util.HashMap;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.GameElement;
import com.glevel.wwii.game.model.GameElement.Rank;
import com.glevel.wwii.game.model.GameSprite;
import com.glevel.wwii.game.model.Player;

public class GraphicElementFactory {

    private Context mContext;
    private VertexBufferObjectManager mVertexBufferObjectManager;
    private TextureManager mTextureManager;

    private BitmapTextureAtlas mTexture;
    public static HashMap<String, TextureRegion> mGfxMap = new HashMap<String, TextureRegion>();

    public GraphicElementFactory(Context context, VertexBufferObjectManager vertexBufferObjectManager,
            TextureManager textureManager) {
        mContext = context;
        mVertexBufferObjectManager = vertexBufferObjectManager;
        mTextureManager = textureManager;
    }

    public void initGraphics(Battle battle) {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        // load all game elements graphics
        for (Player player : battle.getPlayers()) {
            for (GameElement gameElement : player.getUnits()) {
                loadGfx(128, 128, gameElement.getSpriteName());
            }
        }

        // stuff to load
        loadGfx(128, 128, "selection.png");
        loadGfx(128, 128, "crosshair.png");
        loadGfx(64, 64, "muzzle_flash.png");
        loadGfx(128, 128, "protection.png");
        loadGfx(128, 128, "explosion.png");
    }

    private void loadGfx(int textureWidth, int textureHeight, String imageName) {
        if (mGfxMap.get(imageName) == null) {
            mTexture = new BitmapTextureAtlas(mTextureManager, textureWidth, textureHeight, TextureOptions.DEFAULT);
            TextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTexture, mContext,
                    imageName, 0, 0);
            mTexture.load();
            mGfxMap.put(imageName, textureRegion);
        }
    }

    public GameSprite addGameElement(Scene scene, GameElement gameElement, InputManager inputManager,
            boolean isMySquad, TMXTile t) {
        // create sprite
        final GameSprite sprite = new GameSprite(gameElement, inputManager, t.getTileX(), t.getTileY(),
                mGfxMap.get(gameElement.getSpriteName()), mVertexBufferObjectManager);
        gameElement.setSprite(sprite);
        gameElement.setRank(isMySquad ? Rank.ally : Rank.enemy);
        scene.attachChild(sprite);
        scene.registerTouchArea(sprite);
        return sprite;
    }

    public static Sprite createSprite(float x, float y, String spriteName,
            VertexBufferObjectManager vertexBufferObjectManager) {
        return new Sprite(x, y, GraphicElementFactory.mGfxMap.get(spriteName), vertexBufferObjectManager);
    }

}
