package com.giggs.apps.chaos.game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.giggs.apps.chaos.game.GameConverterHelper;
import com.giggs.apps.chaos.game.model.map.Map;
import com.giggs.apps.chaos.game.model.units.Unit;

public class Battle implements Serializable, Parcelable {

    /**
     * 
     */
    private static final long serialVersionUID = -5687413891626670339L;

    private long id = 0L;
    private Map map;
    private List<Player> players = new ArrayList<Player>();
    private int turnCount = 1;
    private boolean isWinter = false;

    public List<Unit> unitsToAdd = new ArrayList<Unit>();
    public List<Unit> unitsToRemove = new ArrayList<Unit>();

    public Battle() {
    }

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

    public Player getMe(int myArmyIndex) {
        return players.get(myArmyIndex);
    }

    public List<Unit> getUnitsToAdd() {
        return unitsToAdd;
    }

    public void setUnitsToAdd(List<Unit> unitsToAdd) {
        this.unitsToAdd = unitsToAdd;
    }

    public List<Unit> getUnitsToRemove() {
        return unitsToRemove;
    }

    public void setUnitsToRemove(List<Unit> unitsToRemove) {
        this.unitsToRemove = unitsToRemove;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(GameConverterHelper.toByte(this).toByteArray());
    }

    public static final Parcelable.Creator<Battle> CREATOR = new Parcelable.Creator<Battle>() {
        public Battle createFromParcel(Parcel in) {
            return new Battle(in);
        }

        public Battle[] newArray(int size) {
            return new Battle[size];
        }
    };

    private Battle(Parcel in) {
        Battle battle = GameConverterHelper.getBattleFromLoadGame(in.createByteArray());
        id = battle.getId();
        players = battle.getPlayers();
        map = battle.getMap();
        turnCount = battle.getTurnCount();
        isWinter = battle.isWinter();
        unitsToAdd = new ArrayList<Unit>();
        unitsToRemove = new ArrayList<Unit>();
    }

}
