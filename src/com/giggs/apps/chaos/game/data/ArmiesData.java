package com.giggs.apps.chaos.game.data;

import com.giggs.apps.chaos.R;

public enum ArmiesData {
    HUMAN(R.string.human_army, R.drawable.un_infantry), ORCS(R.string.orcs_army, R.drawable.un_orc), UNDEAD(
            R.string.undead_army, R.drawable.un_skeleton), CHAOS(R.string.chaos_army, R.drawable.un_chaos_wizards), DWARF(
            R.string.dwarf_army, R.drawable.un_bowmen);

    private final int name;
    private final int flagImage;

    ArmiesData(int name, int flagImage) {
        this.name = name;
        this.flagImage = flagImage;
    }

    public int getName() {
        return name;
    }

    public int getFlagImage() {
        return flagImage;
    }

}
