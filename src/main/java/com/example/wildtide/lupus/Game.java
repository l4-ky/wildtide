package com.example.wildtide.lupus;

import java.util.ArrayList;
import java.util.Collections;

public class Game extends Thread{
    private String gameName;
    private boolean  hasStarted=false;
    private ArrayList<String> tempPlayersList=new ArrayList<String>();
    private ArrayList<Player<?>> playersList=new ArrayList<Player<?>>();

    public Game(String name) {
        this.gameName=name;
    }

    @Override
    public void run() {
        assignRoles();
    }

    private void assignRoles() {
        playersList.add(new Player<Moderatore>(playersList.getFirst().getPlayerName(), new Moderatore()));
        tempPlayersList.remove(0);
        for(String playerName:tempPlayersList) {
            Collections.shuffle(tempPlayersList);
            //add and assign role

            tempPlayersList.remove(0);
        }
    }

    private Player getFromName(String name) {
        for (Player player:playersList) {
            if (player.getPlayerName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    public void addPlayer(String username) {
        if (!tempPlayersList.contains(username)) tempPlayersList.add(username);
    }

    public String getGameName() {
        return gameName;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public String getWhoStarted() {
        return tempPlayersList.getFirst();
    }
}
