package com.glevel.wwii.game.model.units;

import java.util.List;

import com.glevel.wwii.R;
import com.glevel.wwii.activities.GameActivity;
import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.GameElement;
import com.glevel.wwii.game.model.orders.DefendOrder;
import com.glevel.wwii.game.model.orders.FireOrder;
import com.glevel.wwii.game.model.orders.MoveOrder;
import com.glevel.wwii.game.model.orders.Order;
import com.glevel.wwii.game.model.weapons.TurretWeapon;
import com.glevel.wwii.game.model.weapons.Weapon;

public abstract class Unit extends GameElement {

    /**
     * 
     */
    private static final long serialVersionUID = -1514358997270651189L;

    private static final float DEFEND_ORDER_AMBUSH_DISTANCE = 300;

    protected final ArmiesData army;
    private final int image;
    private final int moveSpeed;
    private List<Weapon> weapons;
    protected Experience experience;

    private InjuryState health;
    private int frags;
    private boolean isAvailable;
    private Order order;
    private Action currentAction;
    private int panic;
    private int aimCounter = 0;

    public Unit(ArmiesData army, int name, int image, Experience experience, List<Weapon> weapons, int moveSpeed) {
        super(name, "soldier.png");
        this.army = army;
        this.image = image;
        this.experience = experience;
        this.weapons = weapons;
        this.moveSpeed = moveSpeed;
        this.health = InjuryState.none;
        this.currentAction = Action.waiting;
        this.setPanic(0);
        this.frags = 0;
    }

    protected abstract float getUnitSpeed();

    public abstract float getUnitTerrainProtection();

    protected abstract int getUnitPrice();

    public static enum InjuryState {
        none(R.color.green), injured(R.color.yellow), badlyInjured(R.color.orange), dead(R.color.red);

        private final int color;

        private InjuryState(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

    }

    public static enum Experience {
        recruit(R.color.recruit), veteran(R.color.veteran), elite(R.color.elite);

        private final int color;

        private Experience(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

    }

    public static enum Action {
        waiting, walking, running, firing, hiding, reloading, aiming
    }

    public InjuryState getHealth() {
        return health;
    }

    public void setHealth(InjuryState health) {
        this.health = health;
    }

    public int getMoveSpeed() {
        return moveSpeed;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    public Experience getExperience() {
        return experience;
    }

    public void setExperience(Experience experience) {
        this.experience = experience;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Action getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Action currentAction) {
        this.currentAction = currentAction;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public int getFrags() {
        return frags;
    }

    public void setFrags(int frags) {
        this.frags = frags;
    }

    public int getImage() {
        return image;
    }

    public ArmiesData getArmy() {
        return army;
    }

    public int getRealSellPrice(boolean isSelling) {
        if (isSelling) {
            return (int) (getUnitPrice() * GameUtils.SELL_PRICE_FACTOR);
        } else {
            return getUnitPrice();
        }
    }

    /**
     * Create a new instance of Unit. Used when we buy a unit.
     * 
     * @param army
     * @return
     */
    public Unit copy() {
        Unit unit = null;
        if (this instanceof Soldier) {
            unit = new Soldier(army, name, image, experience, weapons, moveSpeed);
        } else if (this instanceof Tank) {
            Vehicle vehicle = (Vehicle) this;
            unit = new Tank(army, name, image, experience, weapons, moveSpeed, vehicle.getArmor());
        }
        return unit;
    }

    public void move() {
        this.currentAction = Action.walking;
        // update position
        MoveOrder moveOrder = (MoveOrder) order;
        updateUnitRotation(moveOrder.getxDestination(), moveOrder.getyDestination());
        float dx = moveOrder.getxDestination() - sprite.getX();
        float dy = moveOrder.getyDestination() - sprite.getY();
        double angle = Math.atan(dy / dx);
        float dd = moveSpeed * 10 * 0.1f * getUnitSpeed();
        boolean hasArrived = false;
        if (Math.sqrt(dx * dx + dy * dy) <= dd) {
            hasArrived = true;
            dd = (float) Math.sqrt(dx * dx + dy * dy);
        }

        float[] newPosition = GameUtils.getCoordinatesAfterTranslation(sprite.getX(), sprite.getY(), dd, angle, dx > 0);
        sprite.setPosition(newPosition[0], newPosition[1]);

        if (hasArrived) {
            setOrder(null);
        }
    }

    private void updateUnitRotation(float xDestination, float yDestination) {
        float dx = xDestination - sprite.getX();
        float dy = yDestination - sprite.getY();
        double angle = Math.atan(dy / dx);
        if (dx > 0) {
            sprite.setRotation((float) (angle * 180 / Math.PI + 90));
        } else {
            sprite.setRotation((float) (angle * 180 / Math.PI + 270));
        }
    }

    public void fire(Battle battle) {
        FireOrder f = (FireOrder) order;

        Unit target = f.getTarget();
        if (target.isDead()) {
            // if target is dead, stop to shoot
            order = new DefendOrder();
            return;
        }

        // get most suitable weapon
        Weapon weapon = getBestWeapon(battle, target);
        if (weapon != null) {
            if (weapon instanceof TurretWeapon) {
                // turrets take time to rotate
                TurretWeapon turret = (TurretWeapon) weapon;
                boolean isRotatingOver = turret.rotateTurret(sprite, target.getSprite().getX(), target.getSprite()
                        .getY());
                if (!isRotatingOver) {
                    return;
                }
            } else {
                updateUnitRotation(target.getSprite().getX(), target.getSprite().getY());
            }

            if (weapon.getReloadCounter() > 0) {
                if (aimCounter == 0) {
                    aimCounter = -10;
                    // aiming
                    this.currentAction = Action.aiming;
                } else if (aimCounter < 0) {
                    aimCounter++;
                    if (aimCounter == 0) {
                        aimCounter = weapon.getCadence();
                    }
                    // aiming
                    this.currentAction = Action.aiming;
                } else if (GameActivity.gameCounter % (11 - weapon.getShootSpeed()) == 0) {
                    // firing !!!

                    // add muzzle flash sprite
                    sprite.isFiring = true;

                    weapon.setAmmoAmount(weapon.getAmmoAmount() - 1);
                    weapon.setReloadCounter(weapon.getReloadCounter() - 1);
                    aimCounter--;
                    currentAction = Action.firing;

                    // if not a lot of ammo, more aiming !
                    if (weapon.getAmmoAmount() == weapon.getMagazineSize() * 2) {
                        weapon.setCadence(Math.max(1, weapon.getCadence() / 2));
                    }

                    // increase target panic
                    target.getShots();

                    if (!target.isDead()) {// prevent the multiple frags bug

                        weapon.resolveFireShot(battle, this, target);

                        if (target.isDead()) {
                            target.died();
                            killedSomeone();
                        }
                    }
                }
            } else if (weapon.getReloadCounter() == 0) {
                // need to reload
                this.currentAction = Action.reloading;
                weapon.setReloadCounter(-weapon.getReloadSpeed());
            } else {
                // reloading
                this.currentAction = Action.reloading;
                if (GameActivity.gameCounter % 12 == 0) {
                    weapon.setReloadCounter(weapon.getReloadCounter() + 1);
                    if (weapon.getReloadCounter() == 0) {
                        // reloading is over
                        weapon.setReloadCounter(weapon.getMagazineSize());
                    }
                }

            }
        } else {
            // no weapon available for this fire order
            this.order = new DefendOrder();
        }
    }

    public Weapon getBestWeapon(Battle battle, Unit target) {
        float distance = GameUtils.getDistanceBetween(this, target);
        boolean canSeeTarget = GameUtils.canSee(battle.getMap(), this, target);
        Weapon bestWeapon = null;
        for (Weapon weapon : weapons) {
            if (weapon.canUseWeapon(target, distance, canSeeTarget)) {
                if (bestWeapon == null || weapon.getEfficiencyAgainst(target) > bestWeapon.getEfficiencyAgainst(target)) {
                    bestWeapon = weapon;
                }
            }
        }

        return bestWeapon;
    }

    public void applyDamage(int damage) {
        health = InjuryState.values()[Math.min(InjuryState.dead.ordinal(), health.ordinal() + damage)];
    }

    public void hide() {
        this.currentAction = Action.hiding;

        if (GameActivity.gameCounter % 3 == 0) {
            if (this.panic > 0) {
                this.panic--;
            }
        }
    }

    public int getPanic() {
        return panic;
    }

    public void setPanic(int panic) {
        this.panic = panic;
    }

    public void resolveOrder(Battle battle) {
        if (this.panic > 0) {
            // test if the unit can react
            if (Math.random() * 10 + getExperience().ordinal() < this.panic) {
                // the unit is under fire
                hide();
                return;
            }
        }

        if (this.order instanceof MoveOrder) {
            // TODO update A*
        } else if (this.order instanceof DefendOrder) {
            // search for enemies
            for (Unit u : battle.getEnemies(this)) {
                if (!u.isDead() && GameUtils.getDistanceBetween(this, u) < DEFEND_ORDER_AMBUSH_DISTANCE
                        && GameUtils.canSee(battle.getMap(), this, u)) {
                    // fire on enemy if close
                    order = new FireOrder(u);
                    return;
                }
            }
            // stay ambush
            hide();
        } else if (this.order instanceof FireOrder) {
            fire(battle);
        }
    }

    public void takeInitiative() {
        order = new DefendOrder();
    }

    public int getAimCounter() {
        return aimCounter;
    }

    public void setAimCounter(int aimCounter) {
        this.aimCounter = aimCounter;
    }

    public boolean isDead() {
        return health == InjuryState.dead;
    }

    public void getShots() {
        if (panic < 10) {
            panic++;
        }
    }

    public void killedSomeone() {
        // add frags
        frags++;
    }

    public void died() {
        sprite.setCanBeDragged(false);
        order = null;
    }

    public boolean canMove() {
        return moveSpeed > 0;
    }

}
