package com.glevel.wwii.game.model.orders;

import com.glevel.wwii.game.model.units.Unit;

public class FireOrder extends Order {

    /**
     * 
     */
    private static final long serialVersionUID = -5452279368421461793L;
    private int xDestination;
    private int yDestination;
    private Unit target;

    public FireOrder(Unit target) {
        this.target = target;
    }

    public FireOrder(int xDestination, int yDestination) {
        this.xDestination = xDestination;
        this.yDestination = yDestination;
    }

    public int getxDestination() {
        return xDestination;
    }

    public void setxDestination(int xDestination) {
        this.xDestination = xDestination;
    }

    public int getyDestination() {
        return yDestination;
    }

    public void setyDestination(int yDestination) {
        this.yDestination = yDestination;
    }

    public Unit getTarget() {
        return target;
    }

    public void setTarget(Unit target) {
        this.target = target;
    }

}
