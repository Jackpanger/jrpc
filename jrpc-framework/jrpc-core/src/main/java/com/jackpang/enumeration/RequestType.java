package com.jackpang.enumeration;

/**
 * description: RequestType
 * date: 11/5/23 11:16 AM
 * author: jinhao_pang
 * version: 1.0
 */
public enum RequestType {
    REQUEST((byte) 1, "Normal request"), HEARTBEAT((byte) 2, "Heartbeat request");
    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
