package com.giggs.apps.chaos.game.multiplayer;

import java.io.Serializable;

import com.giggs.apps.chaos.game.GameConverterHelper;

public class Message implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5267336555255334959L;

    public enum MessageType {
        PARTICIPANT_ID, ARMY_INDEX, WHICH_ARMY, INIT_BATTLE, CHAT, TURN_ORDERS, START_GAME 
    }

    private final int senderIndex;
    private final MessageType type;
    private final byte[] content;

    public Message(int senderIndex, MessageType type, byte[] content) {
        this.senderIndex = senderIndex;
        this.type = type;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

    public int getSenderIndex() {
        return senderIndex;
    }

    public byte[] toByte() {
        return GameConverterHelper.toByte(this).toByteArray();
    }

}
