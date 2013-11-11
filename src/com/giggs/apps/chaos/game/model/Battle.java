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

	public Map getMap() {
		return map;
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

}
