package com.glevel.wwii.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.data.BattlesData;
import com.glevel.wwii.game.interfaces.OnNewSpriteToDraw;
import com.glevel.wwii.game.model.map.Map;
import com.glevel.wwii.game.model.units.Unit;

public class Battle implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5687413891626670339L;
    private final int battleId;
    private final int name;
    private final int image;
    private final int requisition;
    private final String tileMapName;
    private transient final Map map;
    private final VictoryCondition alliesVictoryCondition, axisVictoryCondition;

    private long id = 0L;
    private int campaignId = 0;
    private int importance;
    private transient List<Player> players = new ArrayList<Player>();
    private transient Phase phase = Phase.deployment;
    private transient OnNewSpriteToDraw onNewSprite;

    // for campaign mode
    private boolean isDone = false;

    public static enum Phase {
        deployment, combat
    }

    /**
     * Single Battle Mode Constructor
     * 
     * @param data
     */
    public Battle(BattlesData data) {
        this.battleId = data.getId();
        this.name = data.getName();
        this.image = data.getImage();
        this.requisition = data.getRequisition();
        this.map = new Map();
        this.tileMapName = data.getTileMapName();
        this.alliesVictoryCondition = new VictoryCondition(90);
        this.axisVictoryCondition = new VictoryCondition(90);
    }

    /**
     * Campaign Mode Constructor
     * 
     * @param data
     */
    public Battle(BattlesData data, int importance, int requisition, VictoryCondition alliesVictoryCondition,
            VictoryCondition axisVictoryCondition) {
        this.battleId = data.getId();
        this.name = data.getName();
        this.image = data.getImage();
        this.importance = importance;
        this.requisition = requisition;
        this.map = new Map();
        this.tileMapName = data.getTileMapName();
        this.alliesVictoryCondition = alliesVictoryCondition;
        this.axisVictoryCondition = axisVictoryCondition;
    }

    public int getName() {
        return name;
    }

    public int getImage() {
        return image;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public int getRequisition() {
        return requisition;
    }

    public Map getMap() {
        return map;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Unit> getEnemies(Unit unit) {
        for (Player p : players) {
            if (p.getArmy() != unit.getArmy()) {
                return p.getUnits();
            }
        }
        return null;
    }

    public List<Unit> getEnemies(ArmiesData army) {
        for (Player p : players) {
            if (p.getArmy() != army) {
                return p.getUnits();
            }
        }
        return null;
    }

    public Player getEnemyPlayer(Player player) {
        for (Player p : players) {
            if (p != player) {
                return p;
            }
        }
        return null;
    }

    public Player getMe() {
        return getPlayers().get(0);
    }

    public OnNewSpriteToDraw getOnNewSprite() {
        return onNewSprite;
    }

    public void setOnNewSprite(OnNewSpriteToDraw onNewSprite) {
        this.onNewSprite = onNewSprite;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getBattleId() {
        return battleId;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public boolean isSingleBattle() {
        return campaignId == 0;
    }

    public String getTileMapName() {
        return tileMapName;
    }

    public VictoryCondition getPlayerVictoryCondition(ArmiesData army) {
        if (army == ArmiesData.USA) {
            return alliesVictoryCondition;
        } else {
            return axisVictoryCondition;
        }
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

}
