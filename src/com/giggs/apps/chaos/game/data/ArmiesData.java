package com.giggs.apps.chaos.game.data;

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.game.model.units.chaos.Demon;
import com.giggs.apps.chaos.game.model.units.dwarf.Warrior;
import com.giggs.apps.chaos.game.model.units.human.Knight;
import com.giggs.apps.chaos.game.model.units.orc.Orc;
import com.giggs.apps.chaos.game.model.units.undead.Skeleton;

public enum ArmiesData {
	HUMAN(R.string.human_army, new Knight(0).getImage()), ORCS(R.string.orcs_army, new Orc(0).getImage()), UNDEAD(
	        R.string.undead_army, new Skeleton(0).getImage()), CHAOS(R.string.chaos_army, new Demon(0).getImage()), DWARF(
	        R.string.dwarf_army, new Warrior(0).getImage());

	private final int name;
	private final int image;

	ArmiesData(int name, int image) {
		this.name = name;
		this.image = image;
	}

	public int getName() {
		return name;
	}

	public int getImage() {
		return image;
	}

}
