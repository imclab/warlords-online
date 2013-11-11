package com.giggs.apps.chaos.game.model.units;

import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.model.GameElement;
import com.giggs.apps.chaos.game.model.orders.Order;

public abstract class Unit extends GameElement {

	/**
     * 
     */
	private static final long serialVersionUID = -1514358997270651189L;

	protected ArmiesData army;
	private int image;
	protected int experience;
	protected int morale;

	private int health;
	private int frags = 0;
	private Order order;

	public Unit(int name, String spriteName) {
		super(name, spriteName);
	}

	public ArmiesData getArmy() {
		return army;
	}

	public void setArmy(ArmiesData army) {
		this.army = army;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public int getMorale() {
		return morale;
	}

	public void setMorale(int morale) {
		this.morale = morale;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getFrags() {
		return frags;
	}

	public void setFrags(int frags) {
		this.frags = frags;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}