package me.atomoyo.atomhub.network;

public enum Channel {

    BAN("network:ban"),
    KICK("network:kick"),
    MUTE("network:mute"),
    UNMUTE("network:unmute"),
    TRANSFER("network:transfer"),
    SERVERINFO("network:serverinfo"),
    PLAYERLIST("network:playerlist"),
    BROADCAST("network:broadcast");

    private final String id;

    Channel(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
