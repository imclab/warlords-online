package com.glevel.wwii.game.model;

import java.io.Serializable;
import java.util.List;

import com.glevel.wwii.game.model.units.Unit;

public class VictoryCondition implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8609453163507177850L;

    public static enum VictoryType {
        annihilation, takenadhold, rush
    }

    private VictoryType type;
    private int percentageToKill;
    private int objectiveXPosition;
    private int objectiveYPosition;
    private int timeToHoldObjective;
    private int currentTimeHold = 0;

    /**
     * Constructor annihilation
     * 
     * @param percentageToKill
     */
    public VictoryCondition(int percentageToKill) {
        type = VictoryType.annihilation;
        this.percentageToKill = percentageToKill;
    }

    /**
     * Constructor take and hold
     * 
     * @param objectiveXPosition
     * @param objectiveYPosition
     * @param timeToHold
     *            in seconds
     */
    public VictoryCondition(int objectiveXPosition, int objectiveYPosition, int timeToHold) {
        type = VictoryType.takenadhold;
        this.objectiveXPosition = objectiveXPosition;
        this.objectiveYPosition = objectiveYPosition;
        this.timeToHoldObjective = timeToHold;
    }

    /**
     * Constructor rush
     * 
     * @param objectiveXPosition
     * @param objectiveYPosition
     * @param timeToHoldObjective
     */
    public VictoryCondition(int objectiveXPosition, int objectiveYPosition) {
        type = VictoryType.rush;
        this.objectiveXPosition = objectiveXPosition;
        this.objectiveYPosition = objectiveYPosition;
    }

    /**
     * Checks if the victory condition has been reached.
     * 
     * @param player
     * @param battle
     * @return boolean
     */
    public boolean checkVictory(Player player, Battle battle) {
        switch (type) {
        case annihilation:
            int nbKilled = 0;
            // count dead enemies
            List<Unit> enemies = battle.getEnemies(player.getArmy());
            for (Unit u : enemies) {
                if (u.isDead()) {
                    nbKilled++;
                }
            }
            // check if objective is reached
            if (100 * nbKilled / enemies.size() >= percentageToKill) {
                return true;
            }
            break;
        case takenadhold:
            if (isOneUnitOnObjective(player)) {
                // increase time counter
                currentTimeHold++;
                // check if objective is reached
                if (currentTimeHold >= timeToHoldObjective) {
                    return true;
                }
            } else {
                // reset time counter when no units on the objective
                currentTimeHold = 0;
            }
            break;
        case rush:
            return isOneUnitOnObjective(player);
        }

        return false;
    }

    /**
     * Checks the presence or not of any of player's unit on the objective.
     * 
     * @param player
     * @return boolean
     */
    private boolean isOneUnitOnObjective(Player player) {
        for (Unit u : player.getUnits()) {
            if (u.getTilePosition().getTileColumn() == objectiveXPosition
                    && u.getTilePosition().getTileRow() == objectiveYPosition) {
                return true;
            }
        }
        return false;
    }

}
