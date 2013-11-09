package com.glevel.wwii.game.data;

import com.glevel.wwii.R;

public enum ArmiesData {
    USA(R.string.usa_army, R.drawable.ic_army_usa, R.color.bg_btn_green), GERMANY(R.string.german_army,
            R.drawable.ic_army_germany, R.color.bg_home);

    private final int name;
    private final int flagImage;
    private final int color;

    ArmiesData(int name, int flagImage, int color) {
        this.name = name;
        this.flagImage = flagImage;
        this.color = color;
    }

    public int getName() {
        return name;
    }

    public int getFlagImage() {
        return flagImage;
    }

    public int getColor() {
        return color;
    }

    public ArmiesData getEnemy() {
        if (ordinal() == 0) {
            return ArmiesData.values()[1];
        } else {
            return ArmiesData.values()[0];
        }
    }

}
