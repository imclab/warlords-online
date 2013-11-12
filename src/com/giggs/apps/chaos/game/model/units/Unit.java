package com.giggs.apps.chaos.game.model.units;

import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.model.GameElement;
import com.giggs.apps.chaos.game.model.orders.Order;

public abstract class Unit extends GameElement {

    /**
     * 
     */
    private static final long serialVersionUID = -1514358997270651189L;

    private final int armyIndex;
    protected final ArmiesData army;
    private final int image;
    private final int price;

    protected int experience = 0;
    protected int morale = 100;
    private int health;
    private int frags = 0;
    private Order order;

    public Unit(int name, int image, String spriteName, ArmiesData army, int armyIndex, int price) {
        super(name, spriteName);
        this.image = image;
        this.army = army;
        this.armyIndex = armyIndex;
        this.price = price;
    }

    public ArmiesData getArmy() {
        return army;
    }

    public int getImage() {
        return image;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getMorale() {
        return morale;
    }

    public void setMorale(int morale) {
        this.morale = morale;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getFrags() {
        return frags;
    }

    public void setFrags(int frags) {
        this.frags = frags;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getArmyIndex() {
        return armyIndex;
    }

    public int getPrice() {
        return price;
    }

    public int getVision() {
        if (tilePosition.getTerrain() == TerrainData.mountain) {
            return 2;
        } else {
            return 1;
        }
    }

}