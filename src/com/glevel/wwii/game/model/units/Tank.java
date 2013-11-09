package com.glevel.wwii.game.model.units;

import java.util.List;

import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.model.weapons.Weapon;

public class Tank extends Vehicle {

    /**
     * 
     */
    private static final long serialVersionUID = -4095890968516700014L;
    private static final int TANK_VIRTUAL_WIDTH = 2, TANK_VIRTUAL_HEIGHT = 4;

    public Tank(ArmiesData army, int name, int image, Experience experience, List<Weapon> weapons, int moveSpeed,
            int armor) {
        super(army, name, image, experience, weapons, moveSpeed, VehicleType.tank, armor, TANK_VIRTUAL_WIDTH,
                TANK_VIRTUAL_HEIGHT);
    }

    @Override
    public float getUnitSpeed() {
        // depends on health
        float healthFactor = 1 - getHealth().ordinal() * 0.25f;
        switch (getTilePosition().getGround()) {
        case concrete:
            return 1.0f * healthFactor;
        case grass:
            return 1.0f * healthFactor;
        case mud:
            return 0.6f * healthFactor;
        case water:
            return 0.1f * healthFactor;
        }
        return 0;
    }

    @Override
    public float getUnitTerrainProtection() {
        return 1.0f;
    }

    @Override
    protected int getUnitPrice() {
        // TODO Auto-generated method stub
        return 0;
    }
}
