package com.glevel.wwii.game.data;

import com.glevel.wwii.R;

public class CampaignsData {

	public enum Campaigns {
		usa(ArmiesData.USA, R.string.market_garden, 2, new OperationsData[] { OperationsData.ARNHEM_BRIDGE });

		private final int id;
		private final ArmiesData army;
		private final int name;
		private final int nbOperationsToSucceed;
		private final OperationsData[] operations;

		Campaigns(ArmiesData army, int name, int nbOperationsToSucceed, OperationsData[] operations) {
			this.id = ordinal();
			this.army = army;
			this.name = name;
			this.nbOperationsToSucceed = nbOperationsToSucceed;
			this.operations = operations;
		}

		public int getId() {
			return id;
		}

		public ArmiesData getArmy() {
			return army;
		}

		public int getName() {
			return name;
		}

		public int getNbOperationsToSucceed() {
			return nbOperationsToSucceed;
		}

		public OperationsData[] getOperations() {
			return operations;
		}
	}

}
