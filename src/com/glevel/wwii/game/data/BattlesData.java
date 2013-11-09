package com.glevel.wwii.game.data;

import com.glevel.wwii.R;

public enum BattlesData {
	ARNHEM_BRIDGE(R.string.about, R.drawable.map_0, 10, "map2.tmx");

	private final int id;
	private final int name;
	private final int image;
	private final int requisition;
	private final String tileMapName;

	BattlesData(int name, int image, int requisition, String tileMapName) {
		this.id = this.ordinal();
		this.name = name;
		this.image = image;
		this.requisition = requisition;
		this.tileMapName = tileMapName;
	}

	public int getName() {
		return name;
	}

	public int getImage() {
		return image;
	}

	public int getRequisition() {
		return requisition;
	}

	public int getId() {
		return id;
	}

	public String getTileMapName() {
		return tileMapName;
	}

}
