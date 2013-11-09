package com.glevel.wwii.game.model.weapons;

import com.glevel.wwii.game.model.GameSprite;

public class TurretWeapon extends IndirectWeapon {

    /**
     * 
     */
    private static final long serialVersionUID = -2063295813207549974L;
    private int turretRotationSpeed;

    public TurretWeapon(int name, int image, int apPower, int atPower, int range, int nbMagazines, int cadence,
            int magazineSize, int reloadSpeed, int shootSpeed, int explosionSize, int turretRotationSpeed) {
        super(name, image, apPower, atPower, range, nbMagazines, cadence, magazineSize, reloadSpeed, shootSpeed,
                explosionSize);
        this.setTurretRotationSpeed(turretRotationSpeed);
    }

    public int getTurretRotationSpeed() {
        return turretRotationSpeed;
    }

    public void setTurretRotationSpeed(int turretRotationSpeed) {
        this.turretRotationSpeed = turretRotationSpeed;
    }

    public boolean rotateTurret(GameSprite sprite, float xDestination, float yDestination) {
        float dx = xDestination - sprite.getX();
        float dy = yDestination - sprite.getY();
        double finalAngle = Math.atan(dy / dx) * 180 / Math.PI;
        if (dx > 0) {
            finalAngle += 90;
        } else {
            finalAngle -= 90;
        }
        double rotationStep = Math.min(finalAngle - sprite.getRotation(), turretRotationSpeed);
        sprite.setRotation((float) (sprite.getRotation() + rotationStep));

        return rotationStep < turretRotationSpeed;
    }

}
