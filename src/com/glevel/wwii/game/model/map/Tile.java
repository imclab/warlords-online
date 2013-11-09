package com.glevel.wwii.game.model.map;

import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;

import com.glevel.wwii.game.model.GameElement;

public class Tile extends TMXTile {

    private GameElement content = null;
    private GroundType ground = GroundType.grass;
    private TerrainType terrain = null;

    public static enum GroundType {
        grass, concrete, water, mud
    }

    public static enum TerrainType {
        house, field, wall, bush, tree
    }

    /**
     * Constructor from a .tmx tile map
     * 
     * @param tmxTile
     * @param tiledMap
     */
    public Tile(TMXTile tmxTile, TMXTiledMap tiledMap) {
        super(tmxTile.getGlobalTileID(), tmxTile.getTileColumn(), tmxTile.getTileRow(), tmxTile.getTileWidth(), tmxTile
                .getTileHeight(), tmxTile.getTextureRegion());

        // add tile properties retrieved from the .tmx
        TMXProperties<TMXTileProperty> lstProperties = tmxTile.getTMXTileProperties(tiledMap);
        if (lstProperties != null) {
            for (TMXTileProperty prop : lstProperties) {
                // setup ground type
                if (prop.getName().equals(GroundType.grass.name())) {
                    ground = GroundType.grass;
                } else if (prop.getName().equals(GroundType.concrete.name())) {
                    ground = GroundType.concrete;
                } else if (prop.getName().equals(GroundType.water.name())) {
                    ground = GroundType.water;
                } else if (prop.getName().equals(GroundType.mud.name())) {
                    ground = GroundType.mud;
                }

                // setup terrain type
                if (prop.getName().equals(TerrainType.bush.name())) {
                    terrain = TerrainType.bush;
                } else if (prop.getName().equals(TerrainType.house.name())) {
                    terrain = TerrainType.house;
                } else if (prop.getName().equals(TerrainType.field.name())) {
                    terrain = TerrainType.field;
                } else if (prop.getName().equals(TerrainType.wall.name())) {
                    terrain = TerrainType.wall;
                } else if (prop.getName().equals(TerrainType.tree.name())) {
                    terrain = TerrainType.tree;
                }
            }
        }
    }

    public GameElement getContent() {
        return content;
    }

    public void setContent(GameElement content) {
        this.content = content;
    }

    public TerrainType getTerrain() {
        return terrain;
    }

    public void setTerrain(TerrainType terrain) {
        this.terrain = terrain;
    }

    public GroundType getGround() {
        return ground;
    }

    public void setGround(GroundType ground) {
        this.ground = ground;
    }

}
