package com.giggs.apps.chaos.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.model.orders.Order;

public class Player implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6829836915818818596L;

    private final String id;
    private final String name;
    private final ArmiesData army;
    private final int armyIndex;
    private final boolean isAI;

    private int gold = 0;
    private boolean isDefeated = false;
    private List<Order> lstTurnOrders = new ArrayList<Order>();

    public Player(String id, String name, ArmiesData army, int armyIndex, boolean isAI) {
        this.id = id;
        this.name = name;
        this.army = army;
        this.armyIndex = armyIndex;
        this.isAI = isAI;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public void setDefeated(boolean isDefeated) {
        this.isDefeated = isDefeated;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArmiesData getArmy() {
        return army;
    }

    public int getArmyIndex() {
        return armyIndex;
    }

    public boolean isAI() {
        return isAI;
    }

    public void initNewTurn() {
        lstTurnOrders = new ArrayList<Order>();
    }

    public void giveOrder(Order order) {
        lstTurnOrders.add(order);
    }

}
