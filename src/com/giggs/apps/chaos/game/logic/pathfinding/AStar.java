package com.giggs.apps.chaos.game.logic.pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import com.giggs.apps.chaos.game.model.units.Unit;

public class AStar<N extends Node> {

    public List<N> search(N[][] nodes, N source, N target, boolean allowDiagonalMoves, Unit unit) {
        // setup graph
        Graph<N> graph = new Graph<N>(nodes);

        // prepare sets
        Map<String, AStarNode<N>> openSet = new HashMap<String, AStarNode<N>>();
        PriorityQueue<AStarNode<N>> pQueue = new PriorityQueue<AStarNode<N>>(20, new AStarNodeComparator<N>());
        Map<String, AStarNode<N>> closeSet = new HashMap<String, AStarNode<N>>();

        // add source
        AStarNode<N> start = new AStarNode<N>(source, 0, graph.calcManhattanDistance(source, target));
        openSet.put(source.getId(), start);
        pQueue.add(start);

        AStarNode<N> goal = null;
        while (openSet.size() > 0) {
            AStarNode<N> x = pQueue.poll();
            openSet.remove(x.getId());
            if (x.getId().equals(target.getId())) {
                goal = x;
                break;
            } else {
                closeSet.put(x.getId(), x);
                Set<N> neighbors = graph.getAdjacentNodes(x.getNode(), allowDiagonalMoves, unit);
                for (N neighbor : neighbors) {
                    AStarNode<N> visited = closeSet.get(neighbor.getId());
                    if (visited == null) {
                        double g = x.getG() + graph.calcManhattanDistance(x.getNode(), neighbor);
                        AStarNode<N> n = openSet.get(neighbor.getId());

                        if (n == null) {
                            // not in the open set
                            n = new AStarNode<N>(neighbor, g, graph.calcManhattanDistance(neighbor, target));
                            n.setCameFrom(x);
                            openSet.put(neighbor.getId(), n);
                            pQueue.add(n);
                        } else if (g < n.getG()) {
                            // Have a better route to the current node, change
                            // its parent
                            n.setCameFrom(x);
                            n.setG(g);
                            n.setH(graph.calcManhattanDistance(neighbor, target));
                        }
                    }
                }
            }
        }

        // after found the target, start to populate the path
        if (goal != null) {
            Stack<N> stack = new Stack<N>();
            List<N> list = new ArrayList<N>();
            stack.push(goal.getNode());
            AStarNode<N> parent = goal.getCameFrom();
            while (parent != null) {
                stack.push(parent.getNode());
                parent = parent.getCameFrom();
            }
            while (stack.size() > 0) {
                list.add(stack.pop());
            }
            return list;
        }

        return null;
    }

}
