package com.example.wildtide.lupus;

import java.util.ArrayList;

import javax.websocket.Session;

public class Player<Role> {
    //session got from the WebSocket connection
    //used for back-->site comunication
    private Session session;
    private String name;
    private Role role;
    private boolean isProtected;
    private boolean isGhost;
    private boolean hasBeenKilledDuringNight=false;
    private int numeroNotteWhenKilled=0;
    private int numeroGiornoWhenKilled=0;

    public Player(String name, Role role) {
        this.name=name;
        this.role=role;
    }
    public Player(Player<?> oldPlayer, Role newRole) {
        this.session=oldPlayer.session;
        this.name=oldPlayer.name;
        this.role=newRole;//tutto uguale, tranne che non copio il ruolo ma utilizzo il parametro
        this.isProtected=oldPlayer.isProtected;
        this.isGhost=oldPlayer.isGhost;
        this.hasBeenKilledDuringNight=oldPlayer.hasBeenKilledDuringNight;
        this.numeroNotteWhenKilled=oldPlayer.numeroNotteWhenKilled;
    }

    @SuppressWarnings("rawtypes")//just because
    public void sendMessage(ArrayList toBeSent) {
        session.getAsyncRemote().sendObject(toBeSent);
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
    public void setRole(Role newRole) {
        this.role=newRole;
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

    public int getNumeroNotteWhenKilled() {
        return numeroNotteWhenKilled;
    }
    public void setNumeroNotteWhenKilled(int numeroNotteWhenKilled) {
        this.numeroNotteWhenKilled = numeroNotteWhenKilled;
    }

    public int getNumeroGiornoWhenKilled() {
        return numeroGiornoWhenKilled;
    }
    public void setNumeroGiornoWhenKilled(int numeroGiornoWhenKilled) {
        this.numeroGiornoWhenKilled = numeroGiornoWhenKilled;
    }

    public boolean getHasBeenKilledDuringNight() {
        return hasBeenKilledDuringNight;
    }
    public void setHasBeenKilledDuringNight(boolean hasBeenKilledDuringNight) {
        this.hasBeenKilledDuringNight = hasBeenKilledDuringNight;
    }

    @Override
    public String toString() {
        return "Player{" +
                "session=" + session +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", isProtected=" + isProtected +
                ", isGhost=" + isGhost +
                ", hasBeenKilledDuringNight=" + hasBeenKilledDuringNight +
                ", numeroNotteWhenKilled=" + numeroNotteWhenKilled +
                '}';
    }
}
