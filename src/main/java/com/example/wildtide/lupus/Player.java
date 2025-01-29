package com.example.wildtide.lupus;

import javax.websocket.Session;

public class Player<Role> {
    //session got from the WebSocket connection
    //used for back-->site comunication
    private Session session;
    private String name;
    private Role role;
    private boolean isProtected;
    private boolean isGhost;

    public Player(String name, Role role) {
        this.name=name;
        this.role=role;
    }

    public void sendMessage(String message) {
        //TO DO
    }

    public void setSession(Session session) {
        this.session=session;
    }

    public String getPlayerName() {
        return name;
    }
    public void setPlayerName(String name) {
        this.name=name;
    }

    public Role getRole() {
        return role;
    }

    public boolean getIsProtected() {
        return isProtected;
    }
    public void setIsProtected(boolean isProtected) {
        this.isProtected=isProtected;
    }

    public boolean getIsGhost() {
        return isGhost;
    }
    public void setIsGhost(boolean isGhost) {
        this.isGhost = isGhost;
    }
}
