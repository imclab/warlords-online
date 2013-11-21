package com.giggs.apps.chaos.game.logic.pathfinding;

public class AStarNode<N extends Node> {

    private N node;

    // used to construct the path after the search is done
    private AStarNode<N> cameFrom;

    // Distance from source along optimal path
    private double g;

    // Heuristic estimate of distance from the current node to the target node
    private double h;

    public AStarNode(N node, double g, double h) {
        this.node = node;
        this.g = g;
        this.h = h;
    }

    public N getNode() {
        return node;
    }

    public void setNode(N node) {
        this.node = node;
    }

    public AStarNode<N> getCameFrom() {
        return cameFrom;
    }

    public void setCameFrom(AStarNode<N> cameFrom) {
        this.cameFrom = cameFrom;
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return g + h;
    }

    public String getId() {
        return node.getId();
    }

    public void setG(double g) {
        this.g = g;
    }

    public void setH(double h) {
        this.h = h;
    }

}
