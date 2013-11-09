package com.glevel.wwii.game.model.weapons;

import org.andengine.util.color.Color;

import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.andengine.custom.CustomColors;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.Player;
import com.glevel.wwii.game.model.units.Unit;
import com.glevel.wwii.game.model.units.Unit.Action;

public class DeflectionWeapon extends Weapon {

    /**
     * 
     */
    private static final long serialVersionUID = -3068800131021401992L;
    private static final float EXPLOSION_EPICENTER_FACTOR = 0.5f;
    private static final int CHANCE_TO_HIT_IN_EPICENTER = 100;
    private static final int CHANCE_TO_HIT_AROUND = 50;
    private static final int BASIC_MAXIMAL_DEFLECTION = 5;// in meters

    private final int explosionSize;// in meters

    public DeflectionWeapon(int name, int image, int apPower, int atPower, int range, int nbMagazines, int cadence,
            int magazineSize, int reloadSpeed, int shootSpeed, int explosionSize) {
        super(name, image, apPower, atPower, range, nbMagazines, cadence, magazineSize, reloadSpeed, shootSpeed);
        this.explosionSize = explosionSize;
    }

    @Override
    public void resolveFireShot(Battle battle, Unit shooter, Unit target) {
        float distance = GameUtils.getDistanceBetween(shooter, target);

        // deflection depends on distance and experience of the shooter
        int deflection = (int) (Math.random() * (getMaxDeflection(shooter, distance)) * GameUtils.PIXEL_BY_METER);
        double angle = Math.random() * 360;

        // calculate impact position
        float[] impactPosition = GameUtils.getCoordinatesAfterTranslation(target.getSprite().getX(), target.getSprite()
                .getY(), deflection, angle, Math.random() < 0.5);

        // draw explosion sprite
        battle.getOnNewSprite().drawSprite(impactPosition[0], impactPosition[1], "explosion.png",
                3 * GameUtils.GAME_LOOP_FREQUENCY, explosionSize);

        // get all the units in the explosion
        float distanceToImpact;// in meters
        for (Player p : battle.getPlayers()) {
            for (Unit unit : p.getUnits()) {
                distanceToImpact = GameUtils.getDistanceBetween(unit, impactPosition[0], impactPosition[1])
                        / GameUtils.PIXEL_BY_METER;
                if (distanceToImpact < explosionSize) {
                    // increase panic
                    unit.getShots();
                    if (distanceToImpact < explosionSize * EXPLOSION_EPICENTER_FACTOR) {
                        // great damage in the explosion's epicenter
                        resolveDamageDiceRoll(CHANCE_TO_HIT_IN_EPICENTER, unit);
                    } else {
                        // minor damage further
                        int tohit = CHANCE_TO_HIT_AROUND;

                        // add terrain protection
                        tohit *= unit.getUnitTerrainProtection();

                        if (unit.getCurrentAction() == Action.hiding || unit.getCurrentAction() == Action.reloading) {
                            // target is hiding : tohit depends on target's
                            // experience
                            tohit -= 5 * (unit.getExperience().ordinal() + 1);
                        }

                        resolveDamageDiceRoll(tohit, unit);
                    }
                }
            }
        }
    }

    private int getMaxDeflection(Unit shooter, float distance) {
        return BASIC_MAXIMAL_DEFLECTION + Weapon.distanceToRangeCategory(distance) - shooter.getExperience().ordinal();
    }

    @Override
    public Color getDistanceColor(Unit shooter, Unit target) {
        int defl = getMaxDeflection(shooter, GameUtils.getDistanceBetween(shooter, target)) - BASIC_MAXIMAL_DEFLECTION;
        if (defl <= -1) {
            return Color.GREEN;
        } else if (defl == 0) {
            return Color.YELLOW;
        } else if (defl == 1) {
            return CustomColors.ORANGE;
        } else {
            return Color.RED;
        }
    }

    public int getExplosionSize() {
        return explosionSize;
    }

}
