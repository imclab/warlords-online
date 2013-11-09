package com.glevel.wwii.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.model.units.Unit;

public class Player implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6829836915818818596L;

    private final String name;
    private final ArmiesData army;
    private final int armyIndex;
    private final boolean isAI;
    private VictoryCondition victoryCondition;

    private List<Unit> units = new ArrayList<Unit>();
    private int requisition;
    private int xPositionDeployment;

    /**
     * Campaign's player constructor
     */
    public Player(ArmiesData army) {
        this.name = "Me";
        this.army = army;
        this.armyIndex = 0;
        this.isAI = false;
        this.setVictoryCondition(null);
    }

    public Player(String name, ArmiesData army, int armyIndex, boolean isAI, VictoryCondition victoryCondition) {
        this.name = name;
        this.army = army;
        this.armyIndex = armyIndex;
        this.isAI = isAI;
        this.setVictoryCondition(victoryCondition);
    }

    public boolean checkIfPlayerWon(Battle battle) {
        return getVictoryCondition().checkVictory(this, battle);
    }

    public String getName() {
        return name;
    }

    public int getRequisition() {
        return requisition;
    }

    public void setRequisition(int requisition) {
        this.requisition = requisition;
    }

    public ArmiesData getArmy() {
        return army;
    }

    public List<Unit> gameElement() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public boolean isAI() {
        return isAI;
    }

    public VictoryCondition getVictoryCondition() {
        return victoryCondition;
    }

    public int getArmyIndex() {
        return armyIndex;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public int getXPositionDeployment() {
        return xPositionDeployment;
    }

    public void setXPositionDeployment(int xPositionDeployment) {
        this.xPositionDeployment = xPositionDeployment;
    }

    public void setVictoryCondition(VictoryCondition victoryCondition) {
        this.victoryCondition = victoryCondition;
    }

}
