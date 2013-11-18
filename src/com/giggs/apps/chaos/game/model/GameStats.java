package com.giggs.apps.chaos.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameStats implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3949000256983542839L;
    private final List<Float> population = new ArrayList<Float>();
    private final List<Integer> economy = new ArrayList<Integer>();
    private int nbUnitsCreated = 0;
    private int nbUnitsKilled = 0;
    private int nbBattlesWon = 0;
    private int gold = 0;

    public List<Float> getPopulation() {
        return population;
    }

    public List<Integer> getEconomy() {
        return economy;
    }

    public int getNbUnitsCreated() {
        return nbUnitsCreated;
    }

    public void incrementNbUnitsCreated(int modifier) {
        this.nbUnitsCreated += modifier;
    }

    public int getNbUnitsKilled() {
        return nbUnitsKilled;
    }

    public void incrementNbUnitsKilled(int modifier) {
        this.nbUnitsKilled += modifier;
    }

    public int getNbBattlesWon() {
        return nbBattlesWon;
    }

    public void incrementBattlesWon() {
        this.nbBattlesWon++;
    }

    public int getGold() {
        return gold;
    }

    public void incrementGold(int modifier) {
        this.gold += modifier;
    }

}
