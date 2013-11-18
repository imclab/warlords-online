package com.giggs.apps.chaos.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.model.map.Map;

public class Battle implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5687413891626670339L;

    private long id = 0L;
    private Map map;
    private List<Player> players = new ArrayList<Player>();
    private int turnCount = 1;
    private boolean isWinter = false;

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = turnCount;
    }

    public boolean isWinter() {
        return isWinter;
    }

    public void setWinter(boolean isWinter) {
        this.isWinter = isWinter;
    }

    public Player getMeSoloMode() {
        return players.get(0);
    }

}
