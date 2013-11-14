package com.giggs.apps.chaos.game.model.units;

import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.logic.GameLogic;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.model.GameElement;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
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
    private final int maxHealth;
    private final boolean isRangedAttack;
    private final WeaponType weaponType;
    private final ArmorType armorType;
    private final int damage;
    private final int armor;

    protected int experience = 0;
    protected int morale = 100;
    private int health;
    private int frags = 0;
    private Order order;

    public Unit(int name, int image, String spriteName, ArmiesData army, int armyIndex, int price, int health,
            boolean isRangedAttack, WeaponType weaponType, ArmorType armorType, int damage, int armor) {
        super(name, spriteName);
        this.image = image;
        this.army = army;
        this.armyIndex = armyIndex;
        this.price = price;
        this.maxHealth = health;
        this.health = health;
        this.isRangedAttack = isRangedAttack;
        this.weaponType = weaponType;
        this.armorType = armorType;
        this.damage = damage;
        this.armor = armor;
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
        if (order instanceof DefendOrder) {
            sprite.defend();
        } else if (order instanceof MoveOrder) {
            sprite.walk(GameLogic.getDirectionFromMoveOrder(((MoveOrder) order)));
        } else if (order == null) {
            sprite.stand();
        }
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

    public boolean canMove(Tile tile) {
        return true;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isRangedAttack() {
        return isRangedAttack;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public int getDamage() {
        return damage;
    }

    public int getArmor() {
        return armor;
    }

    public void setTilePosition(Tile tilePosition) {
        this.tilePosition = tilePosition;
        this.tilePosition.getContent().add(this);
    }

    public void updateTilePosition(Tile tilePosition) {
        if (this.tilePosition != null) {
            this.tilePosition.getContent().remove(this);
        }
        this.tilePosition = tilePosition;
        this.tilePosition.getContent().add(this);

        sprite.setPosition(GameUtils.TILE_SIZE * tilePosition.getX(), GameUtils.TILE_SIZE * tilePosition.getY());
    }

    public void updateMorale(int modifier) {
        morale += modifier;
        morale = Math.min(morale, 100);
        morale = Math.max(morale, 0);
        sprite.updateMorale(morale);
    }

    public void updateExperience(int modifier) {
        experience += modifier;
        experience = Math.min(experience, 100);
        experience = Math.max(experience, 0);
        sprite.updateExperience(experience);
    }

    public boolean updateHealth(int modifier) {
        health += modifier;
        health = Math.min(health, maxHealth);
        health = Math.max(health, 0);
        sprite.updateHealth(health);

        return health == 0;
    }

}
