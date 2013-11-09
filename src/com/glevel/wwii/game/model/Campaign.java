package com.glevel.wwii.game.model;

import java.util.ArrayList;
import java.util.List;

import com.glevel.wwii.game.data.ArmiesData;
import com.glevel.wwii.game.data.CampaignsData.Campaigns;
import com.glevel.wwii.game.data.OperationsData;

public class Campaign {

    private final int campaignId;
    private final ArmiesData army;
    private final int name;
    private final int nbOperationsToSucceed;

    private long id;
    private List<Operation> operations;
    private Player player;

    public Campaign(Campaigns campaignsData) {
        this.campaignId = campaignsData.getId();
        this.name = campaignsData.getName();
        this.nbOperationsToSucceed = campaignsData.getNbOperationsToSucceed();
        this.army = campaignsData.getArmy();
        this.operations = new ArrayList<Operation>();
        for (OperationsData operationData : campaignsData.getOperations()) {
            operations.add(new Operation(operationData));
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public int getNbOperationsToSucceed() {
        return nbOperationsToSucceed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getName() {
        return name;
    }

    public ArmiesData getArmy() {
        return army;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public Operation getCurrentOperation() {
        for (int n = 0; n < getOperations().size(); n++) {
            if (!getOperations().get(n).isOver()) {
                return getOperations().get(n);
            }
        }
        return null;
    }

}
