package com.example.wildtide.lupus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Game extends Thread{
    private String gameName;
    private boolean hasStarted=false;
    private ArrayList<Player<?>> rolesToBeAssigned=new ArrayList<Player<?>>(Arrays.asList(new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo()), new Player<Lupo>("", new Lupo()), new Player<Veggente>("", new Veggente()), new Player<Medium>("", new Medium()), new Player<Villico>("", new Villico()), new Player<Guardia>("", new Guardia()), new Player<Villico>("", new Villico()), new Player<Mitomane>("", new Mitomane()), new Player<Massone>("", new Massone()), new Player<Massone>("", new Massone()), new Player<Lupo>("", new Lupo()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo())));
    private ArrayList<String> namePlayersList=new ArrayList<String>();
    private ArrayList<Player<?>> playersList=new ArrayList<Player<?>>();

    public Game(String name) {
        this.gameName=name;
    }

    @Override
    public void run() {
        assignRoles();
        //while ()
        //condizione per cui lupi/villani vincono
        //ogni step "bloccante" in attesa del completamento, poi si passa al successivo

        //ad ogni step/cambio di "stato" della partita: invio ad ogni client del "blocco" di dati/info sulla partita che devono sapere/visualizzare
    }

    private void assignRoles() {
        for (String playerName:namePlayersList) {
            Collections.shuffle(namePlayersList);
            Collections.shuffle(rolesToBeAssigned);
            rolesToBeAssigned.getFirst().setPlayerName(playerName);
            playersList.add(rolesToBeAssigned.getFirst());
            namePlayersList.remove(0);
            rolesToBeAssigned.remove(0);
        }
    }

    /* private Player getFromName(String name) {
        for (Player player:playersList) {
            if (player.getPlayerName().equals(name)) {
                return player;
            }
        }
        return null;
    } */

    public boolean addPlayer(String username) {
        if (namePlayersList.size()>=24) return false;//limite di 24 giocatori
        if (!namePlayersList.contains(username)) {
            namePlayersList.add(username);
            return true;
        }
        return false;
    }

    public String getGameName() {
        return gameName;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public String getWhoStarted() {
        return namePlayersList.getFirst();
    }
}
