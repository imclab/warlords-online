package com.giggs.apps.chaos.game.model.units;

import com.giggs.apps.chaos.game.GameUtils;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.TerrainData;
import com.giggs.apps.chaos.game.logic.GameLogic;
import com.giggs.apps.chaos.game.logic.GameLogic.ArmorType;
import com.giggs.apps.chaos.game.logic.GameLogic.WeaponType;
import com.giggs.apps.chaos.game.logic.MapLogic;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.model.GameElement;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.map.Tile;
import com.giggs.apps.chaos.game.model.orders.DefendOrder;
import com.giggs.apps.chaos.game.model.orders.MoveOrder;
import com.giggs.apps.chaos.game.model.orders.Order;

public abstract class Unit extends GameElement {

    /**
     * 
     */
    private static final long serialVersionUID = -1514358997270651189L;

    protected final ArmiesData army;
    protected final int armyIndex;
    private final int image;
    private final int price;
    private final int maxHealth;
    private final boolean isRangedAttack;
    private final WeaponType weaponType;
    private final ArmorType armorType;

    private int attack;
    private int armor;
    protected int experience = 0;
    protected int morale = 100;
    private int health;
    private int frags = 0;
    private Order order;

    public Unit(int name, int image, String spriteName, ArmiesData army, int armyIndex, int price, int health,
            boolean isRangedAttack, WeaponType weaponType, ArmorType armorType, int attack, int armor) {
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
        this.attack = attack;
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

    public void setOrder(Order order, boolean showIt) {
        this.order = order;
        if (sprite != null && showIt) {
            if (order instanceof DefendOrder) {
                sprite.defend();
            } else if (order instanceof MoveOrder) {
                sprite.walk(GameLogic.getDirectionFromMoveOrder(((MoveOrder) order)));
            } else if (order == null) {
                sprite.stand();
            }
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

    public int getAttack() {
        return attack;
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

        if (sprite != null) {
            sprite.setPosition(GameUtils.TILE_SIZE * tilePosition.getX(), GameUtils.TILE_SIZE * tilePosition.getY());
        }
    }

    public void updateMorale(int modifier) {
        morale += modifier;
        morale = Math.min(morale, 100);
        morale = Math.max(morale, 0);
        if (sprite != null) {
            sprite.updateMorale(morale);
        }
    }

    public void updateExperience(int modifier) {
        experience += modifier;
        experience = Math.min(experience, 100);
        experience = Math.max(experience, 0);
        if (sprite != null) {
            sprite.updateExperience(experience);
        }
    }

    public boolean updateHealth(int modifier) {
        health += modifier;
        health = Math.min(health, maxHealth);
        health = Math.max(health, 0);
        if (sprite != null) {
            sprite.updateHealth(health);
        }

        return isDead();
    }

    public void initTurn(Map map) {
        if (tilePosition.getTerrain() == TerrainData.castle || tilePosition.getTerrain() == TerrainData.fort) {
            updateHealth(150);
            updateMorale(25);
        } else if (tilePosition.getTerrain() == TerrainData.farm) {
            updateMorale(25);
        } else {
            updateMorale(10);
        }
    }

    public boolean attack(Unit target) {
        int damage = getDamage(target);
        target.applyDamage(damage);
        frags += damage;
        return target.getHealth() == 0;
    }

    public void applyDamage(int damage) {
        updateHealth(-damage);
        updateMorale(-damage / 10);
    }

    public boolean flee(Battle battle) {
        if (order != null && order instanceof MoveOrder) {
            MoveOrder moveOrder = (MoveOrder) order;
            if (canFleeHere(moveOrder.getOrigin())) {
                updateTilePosition(moveOrder.getOrigin());
                return true;
            }
        }
        adjacentTilesBoucle: for (Tile tile : MapLogic.getAdjacentTiles(battle.getMap(), tilePosition, 1, false)) {
            if (canFleeHere(tile)) {
                // check if enemies come from this position
                for (Unit u : tile.getContent()) {
                    if (u.getArmyIndex() != armyIndex && u.getOrder() != null && u.getOrder() instanceof MoveOrder) {
                        MoveOrder moveOrder = (MoveOrder) order;
                        if (moveOrder.getOrigin() == tile) {
                            continue adjacentTilesBoucle;
                        }
                    }
                }
                updateTilePosition(tile);
                return true;
            }
        }
        // cannot go anywhere...
        return false;
    }

    public boolean canFleeHere(Tile tile) {
        return canMove(tile)
                && (tile.getContent().size() == 0 || tile.getContent().get(0).getArmyIndex() == armyIndex
                        && tile.getContent().size() < GameUtils.MAX_UNITS_PER_TILE);
    }

    public int getDamage(Unit target) {
        float attackFactor = GameLogic.WEAPONS_EFFICIENCY[weaponType.ordinal()][target.getArmorType().ordinal()];
        int damage = (int) Math.max(0,
                attack * attackFactor * health / maxHealth * morale / 100
                        * (1 + 0.1 * Math.random() + (float) experience / 100) - target.getArmor());

        // terrain modifier
        if (target.getTilePosition().getTerrain() == TerrainData.castle
                || target.getTilePosition().getTerrain() == TerrainData.fort) {
            damage *= 0.5;

            // ranged attacks are very effective when defending strong positions
            if (isRangedAttack && (order == null || order instanceof DefendOrder)) {
                damage *= 1.3;
            }
        }

        // orcs are agressive !
        if (army == ArmiesData.ORCS && order != null && order instanceof MoveOrder) {
            damage *= 1.15;
        }

        // order modifier
        if (target.getOrder() != null && target.getOrder() instanceof DefendOrder) {
            if (target.getArmy() == ArmiesData.ORCS) {
                damage *= 0.85;
            } else if (target.getArmy() == ArmiesData.DWARF) {
                damage *= 0.6;
            } else {
                damage *= 0.7;
            }
        }

        if (target.getArmy() == ArmiesData.DWARF && target.getTilePosition().getTerrain() == TerrainData.mountain) {
            damage *= 0.8;
        }

        return damage;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

}
