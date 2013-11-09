package com.glevel.wwii.game.model.units;

import java.util.List;

import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.model.weapons.Weapon;

public abstract class Vehicle extends Unit {

    /**
     * 
     */
    private static final long serialVersionUID = -110116920924422447L;

    static enum VehicleType {
        light, tank
    }

    public Vehicle(ArmiesData army, int name, int image, Experience experience, List<Weapon> weapons, int moveSpeed,
            VehicleType type, int armor, int width, int height) {
        super(army, name, image, experience, weapons, moveSpeed);
        this.vehicleType = type;
        this.armor = armor;
        this.width = width;
        this.height = height;
    }

    private final VehicleType vehicleType;
    private final int width;
    private final int height;
    private int armor;

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
