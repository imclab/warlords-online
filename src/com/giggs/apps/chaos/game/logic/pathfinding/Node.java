package com.giggs.apps.chaos.game.logic.pathfinding;

import java.io.Serializable;

public class Node implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -7275419769211638534L;
    private final String id;
    private final int x, y;

    public Node(int x, int y) {
        this.id = x + "," + y;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean canMoveIn() {
        return true;
    }

}
