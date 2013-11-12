package com.giggs.apps.chaos.game.data;

public enum TerrainData {
    grass(12, "ti_empty_grass.png"), fort(1, "ti_fort.png"), castle(0, "ti_castle.png"), forest(5, "ti_forest.png"), mountain(
            4, "ti_mountains.png"), farm(1, "ti_windmills.png");

    private final int quantityFactor;
    private final String spriteName;

    TerrainData(int quantityFactor, String spriteName) {
        this.quantityFactor = quantityFactor;
        this.spriteName = spriteName;
    }

    public int getQuantityFactor() {
        return quantityFactor;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public boolean isUnitFactory() {
        if (this == castle || this == fort) {
            return true;
        }
        return false;
    }

    public boolean canBeControlled() {
        if (this == castle || this == fort || this == farm) {
            return true;
        }
        return false;
    }
}
