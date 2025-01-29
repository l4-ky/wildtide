package com.example.wildtide.lupus;

import javax.websocket.Session;

public class Player<Role> {
    //session got from the WebSocket connection
    //used for back-->site comunication
    private Session session;
    private String name;
    private Role role;
    private boolean isProtected;

    public Player(String name, Role role) {
        this.name=name;
        this.role=role;
    }

    public void sendMessage(String message) {
        //
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

    public void setIsProtected(boolean x) {
        this.isProtected=x;
    }
    public boolean getIsProtected() {
        return isProtected;
    }
}
