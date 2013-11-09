package com.glevel.wwii.game.model.weapons;

import java.io.Serializable;

import org.andengine.util.color.Color;

import com.glevel.wwii.R;
import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.andengine.custom.CustomColors;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.units.Soldier;
import com.glevel.wwii.game.model.units.Unit;
import com.glevel.wwii.game.model.units.Unit.Action;
import com.glevel.wwii.game.model.units.Unit.InjuryState;
import com.glevel.wwii.game.model.units.Vehicle;

public class Weapon implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3726243358409900250L;
    private final int name;
    private final int image;
    private final int apPower;
    private final int atPower;
    private final int range;
    private final int magazineSize;
    private final int reloadSpeed;
    private final int shootSpeed;

    private int cadence;
    private int ammoAmount;
    private int reloadCounter;// while > 0 there are ammo left, while < 0
                              // reloading

    // price
    private static final int WEAPON_BASE_PRICE = 2;
    private static final int LONG_RANGE_THRESHOLD = 1000;// in meters
    private static final float LONG_RANGE_PRICE_MODIFIER = 1.5f;

    // hit chances
    private static final int[][] HIT_CHANCES = { { 70, 40, 20, 5 }, { 80, 55, 35, 10 }, { 90, 70, 50, 20 } };

    public Weapon(int name, int image, int apPower, int atPower, int range, int nbMagazines, int cadence,
            int magazineSize, int reloadSpeed, int shootSpeed) {
        this.name = name;
        this.image = image;
        this.apPower = apPower;
        this.atPower = atPower;
        this.range = range;
        this.ammoAmount = nbMagazines * magazineSize;
        this.cadence = cadence;
        this.magazineSize = magazineSize;
        this.reloadCounter = magazineSize;
        this.reloadSpeed = reloadSpeed;
        this.shootSpeed = shootSpeed;
    }

    public int getPrice() {
        int price = WEAPON_BASE_PRICE;
        price += 0.1 * (apPower + atPower) * ammoAmount / magazineSize;
        // long range
        if (range > LONG_RANGE_THRESHOLD) {
            price *= LONG_RANGE_PRICE_MODIFIER;
        }
        return price;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public int getReloadCounter() {
        return reloadCounter;
    }

    public void setReloadCounter(int reloadCounter) {
        this.reloadCounter = reloadCounter;
    }

    public int getName() {
        return name;
    }

    public int getImage() {
        return image;
    }

    public int getAmmoAmount() {
        return ammoAmount;
    }

    public void setAmmoAmount(int ammoAmount) {
        this.ammoAmount = ammoAmount;
    }

    public int getCadence() {
        return cadence;
    }

    public void setCadence(int cadence) {
        this.cadence = cadence;
    }

    public int getApPower() {
        return apPower;
    }

    public int getAtPower() {
        return atPower;
    }

    public int getRange() {
        return range;
    }

    public int getReloadSpeed() {
        return reloadSpeed;
    }

    public int getAPColorEfficiency() {
        return efficiencyValueToColor(apPower);
    }

    public int getATColorEfficiency() {
        return efficiencyValueToColor(atPower);
    }

    public int getShootSpeed() {
        return shootSpeed;
    }

    private int efficiencyValueToColor(int efficiency) {
        switch (efficiency) {
        case 1:
            return R.drawable.bg_unit_efficiency_grey;
        case 2:
            return R.drawable.bg_unit_efficiency_red;
        case 3:
            return R.drawable.bg_unit_efficiency_orange;
        case 4:
            return R.drawable.bg_unit_efficiency_yellow;
        case 5:
            return R.drawable.bg_unit_efficiency_green;
        default:
            return R.drawable.bg_unit_efficiency_black;
        }
    }

    public void resolveFireShot(Battle battle, Unit shooter, Unit target) {
        // does it touch the target ? Calculate the chance to hit
        resolveDamageDiceRoll(getToHit(shooter, target), target);
    }

    private int getToHit(Unit shooter, Unit target) {
        float distance = GameUtils.getDistanceBetween(shooter, target);

        int tohit = HIT_CHANCES[shooter.getExperience().ordinal()][distanceToRangeCategory(distance)];

        // add terrain protection
        tohit *= target.getUnitTerrainProtection();

        if (target.getCurrentAction() == Action.hiding || target.getCurrentAction() == Action.reloading) {
            // target is hiding : tohit depends on target's
            // experience
            tohit -= 5 * (target.getExperience().ordinal() + 1);
        }

        // tohit depends on weapon range
        if (distance > range / 2) {
            tohit -= 10;
        }

        return tohit;
    }

    public Color getDistanceColor(Unit shooter, Unit target) {
        int tohit = getToHit(shooter, target);
        if (tohit < 25) {
            return Color.RED;
        } else if (tohit < 50) {
            return CustomColors.ORANGE;
        } else if (tohit < 75) {
            return Color.YELLOW;
        } else {
            return Color.GREEN;
        }
    }

    protected void resolveDamageDiceRoll(int tohit, Unit target) {
        int diceRoll = (int) (Math.random() * 100);
        if (diceRoll < tohit) {
            // hit !
            if (diceRoll < tohit / 4) {
                // critical !
                target.setHealth(InjuryState.dead);
            } else if (diceRoll < tohit / 2) {
                // heavy !
                target.applyDamage(2);
            } else {
                if (Math.random() < 0.5) {
                    // light injured
                    target.applyDamage(1);
                } else {
                    // nothing
                }
            }
        }
    }

    protected static int distanceToRangeCategory(float distance) {
        if (distance < 50 * GameUtils.PIXEL_BY_METER) {
            return 0;
        } else if (distance < 100 * GameUtils.PIXEL_BY_METER) {
            return 1;
        } else if (distance < 200 * GameUtils.PIXEL_BY_METER) {
            return 2;
        } else {
            return 3;
        }
    }

    public boolean canUseWeapon(Unit target, float distance, boolean canSeeTarget) {
        if (target instanceof Vehicle && atPower == 0 || target instanceof Soldier && apPower == 0) {
            // weapon is useless against target
            return false;
        } else if (ammoAmount <= 0) {
            // out of ammo
            return false;
        } else if (distance > range * GameUtils.PIXEL_BY_METER) {
            // out of range
            return false;
        } else if (!canSeeTarget && !(this instanceof IndirectWeapon)) {
            // needs to see target
            return false;
        }

        return true;
    }

    public int getEfficiencyAgainst(Unit target) {
        if (target instanceof Soldier) {
            return apPower;
        } else {
            return atPower;
        }
    }

}
