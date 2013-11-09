package com.glevel.wwii.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.glevel.wwii.game.data.OperationsData;

public class Operation implements Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = -792129337599797570L;
	private final int introductionText;
	private final int mapImage;
	private final int objectivePoints;
	private final List<Battle> lstBattles;

	private int currentPoints = 0;

	public Operation(OperationsData operationData) {
		this.introductionText = operationData.getIntroText();
		this.mapImage = operationData.getMapImage();
		this.objectivePoints = operationData.getObjectivePoints();
		this.lstBattles = new ArrayList<Battle>();
		for (Battle battle : operationData.getBattles()) {
			lstBattles.add(battle);
		}
	}

	public List<Battle> getLstBattles() {
		return lstBattles;
	}

	public int getCurrentPoints() {
		return currentPoints;
	}

	public void setCurrentPoints(int currentPoints) {
		this.currentPoints = currentPoints;
	}

	public int getObjectivePoints() {
		return objectivePoints;
	}

	public int getIntroductionText() {
		return introductionText;
	}

	public int getMapImage() {
		return mapImage;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isSuccess() {
		return isOver() && currentPoints >= objectivePoints;
	}

	public boolean isOver() {
		for (Battle b : lstBattles) {
			if (!b.isDone()) {
				return false;
			}
		}
		return true;
	}

}
