package com.giggs.apps.chaos.game.data;

import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.VictoryCondition;
import com.glevel.wwii.R;

public enum OperationsData {
	ARNHEM_BRIDGE(R.string.market_garden_intro_1, 0, 10, new Battle[] { new Battle(BattlesData.ARNHEM_BRIDGE, 3, 100,
	        new VictoryCondition(100), new VictoryCondition(100)) });

	private final int introText;
	private final int mapImage;
	private final int objectivePoints;
	private final Battle[] battles;

	OperationsData(int introText, int mapImage, int objectivePoints, Battle[] battles) {
		this.introText = introText;
		this.mapImage = mapImage;
		this.objectivePoints = objectivePoints;
		this.battles = battles;
	}

	public int getIntroText() {
		return introText;
	}

	public int getMapImage() {
		return mapImage;
	}

	public int getObjectivePoints() {
		return objectivePoints;
	}

	public Battle[] getBattles() {
		return battles;
	}

}
