package com.giggs.apps.chaos.game.multiplayer;

import java.io.Serializable;

import com.giggs.apps.chaos.game.GameConverterHelper;

public class ChatMessage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2733787909529937416L;
    private final String senderName;
    private final long timestamp;
    private final String content;
    private final boolean isOut;
    private boolean isRead;

    public ChatMessage(String senderName, String content, boolean isOut) {
        this.senderName = senderName;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isOut = isOut;
        this.isRead = isOut;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public boolean isOut() {
        return isOut;
    }

    public byte[] toByte() {
        return GameConverterHelper.toByte(this).toByteArray();
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

}
