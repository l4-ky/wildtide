package com.example.wildtide.lupus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Game extends Thread{
    private String gameName;
    private ArrayList<String> namePlayersList=new ArrayList<String>();
    //
    private ArrayList<Player<?>> playersList=new ArrayList<Player<?>>();
    private BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
    private boolean hasStarted=false;
    private boolean hasEnded=false;
    private boolean haveLupiWon=false;
    private int numeroNotte=0;
    private ArrayList<Player<?>> ghosts=new ArrayList<Player<?>>();//lista cronologica

    public Game(String name) {
        this.gameName=name;
    }

    @Override
    public void run(){
        hasStarted=true;
        assignRoles();
        while (!hasEnded) {
            numeroNotte++;

            //-----NOTTE-----
            messageTo(playersList, "E' notte, chiudete tutti gli occhi!");
            
            //Guardia del corpo
            //ogni notte protegge una persona a sua scelta.
            //dalla seconda notte in poi (prima della fase dei lupi mannari) il moderatore chiama la Guardia del corpo e questi gli indica una persona a scelta (no se stesso) che è protetta dai lupi mannari.
            //se quella persona è poi scelta anche dai lupi mannari come vittima, non muore e nella fase della notte nessuno è sbranato.
            if (numeroNotte>1) {
                @SuppressWarnings("unchecked")//mi dava fastidio il warning. se trova la guardia è safe, se non la trova non esegue il pezzo di codice di competenza
                Player<Guardia> guardia=(Player<Guardia>)getOfType(new Player<Guardia>("", new Guardia())).getFirst();
                if (guardia!=null) {
                    messageTo(playersList, "Guardia del corpo, apri gli occhi!\nChi vuoi proteggere questa notte?");
                    try {
                        String chosenPlayerName=queue.take();
                        //controllo ridondante, ma da mantenere per sicurezza. in teoria il sito non deve permettere alla Guardia di scegliere se stessa
                        if (!chosenPlayerName.equals(guardia.getPlayerName())) {
                            Player<?> chosenPlayer=getFromName(chosenPlayerName);
                            chosenPlayer.setIsProtected(true);
                        } else {
                            messageTo(guardia, "[Non è possibile proteggere se stessi.]");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    messageTo(playersList, "Guardia del corpo, chiudi gli occhi!");
                }
            }
            
            //Veggente
            //il Veggente sceglie e ottiene esito lupo/no (fase giocata anche se il veggente è morto(fantasma))
            //(possono essere due se il mitomane è diventato veggente; il veggente vede il mitomane-lupo mannaro come un lupo mannaro)
            //[7-10 secondi]
            ArrayList<Player<?>> veggenti=getOfType(new Player<Veggente>("", new Veggente()));//se serve, fare il casting a 'Player<Veggente>' nell'interazione. così si rendono accessibili i metodi del 'role'
            messageTo(playersList, "Veggenti, aprite gli occhi!\n Di chi si vuole scoprire il ruolo?");
            try {
                boolean areAllGhosts=true;
                for (Player<?> veggente:veggenti) {
                    if (!veggente.getIsGhost()) areAllGhosts=false;
                }
                if (!areAllGhosts) {
                    String chosenPlayerName=queue.take();
                    //still, controllo ridondante ma da mantenere per sicurezza sicurezza
                    if (!isNameInList(veggenti, chosenPlayerName)) {
                        String chosenPlayerRole=getFromName(chosenPlayerName).getRole().getClass().toString();
                        for (Player<?> veggente:veggenti) {
                            if (!veggente.getIsGhost()) {
                                messageTo(veggente, "Il giocatore che è stato osservato è un "+chosenPlayerRole);
                            }
                        }
                    } else {
                        messageTo(veggenti, "[Non è possibile conoscere il ruolo di se stessi o di un altro veggente.]");
                    }
                } else {
                    Thread.sleep(5000);//simulo l'esecuzione della fase
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageTo(playersList, "Veggenti, chiudete gli occhi!");
            
            //Lupi mannari
            //i Lupi si riconoscono e scelgono chi sbaranare (votazione: scelta comune, oppure scelta per maggioranza a fine tempo, oppure nessuno)
            //[15-20 secondi]
            ArrayList<Player<?>> lupi=getOfType(new Player<Lupo>("", new Lupo()));
            messageTo(playersList, "Lupi mannari, aprite gli occhi!\n Chi volete sbranare stanotte?");
            try {
                String chosenPlayerName=queue.take();
                //still, controllo ridondante ma da mantenere per sicurezza sicurezza
                if (!isNameInList(lupi, chosenPlayerName)) {
                    Player<?> chosenPlayer=getFromName(chosenPlayerName);
                    killPlayer(chosenPlayer, true);
                } else {
                    messageTo(lupi, "[Non è possibile sbranare se stessi o un altro lupo.]");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageTo(playersList, "Lupi mannari, chiudete gli occhi!");

            //Medium
            //dalla seconda notte in poi, il moderatore chiama la sua fase e con un cenno del capo gli dice
            //“sì” se la persona linciata nel turno precedente era un lupo mannaro, “no” altrimenti.
            if (numeroNotte>1) {
                @SuppressWarnings("unchecked")//mi dava fastidio il warning. se trova la guardia è safe, se non la trova non esegue il pezzo di codice di competenza
                Player<Medium> medium=(Player<Medium>)getOfType(new Player<Medium>("", new Medium())).getFirst();
                if (medium!=null) {
                    messageTo(playersList, "Medium, apri gli occhi!");
                    Player<?> killedLastNight=whoWaskilledLastNight(numeroNotte);
                    if (killedLastNight.getRole().getClass().getSimpleName().equals("Lupo")) {
                        messageTo(lupi, "Il giocatore ucciso la notte scorsa ("+killedLastNight.getPlayerName()+") era un lupo.");
                    } else {
                        messageTo(lupi, "Il giocatore ucciso la notte scorsa ("+killedLastNight.getPlayerName()+") non era un lupo.");
                    }
                    messageTo(playersList, "Medium, chiudi gli occhi!");
                }
            }

            //Mitomane
            //alla fine della seconda notte indica al moderatore un altro giocatore ancora vivo.
            //se questo non è un lupo mannaro o il veggente, il mitomane resta un umano normale fino al termine della partita.
            //altrimenti assume immediatamente il ruolo rispettivamente di lupo mannaro o di veggente, a tutti gli effetti.
            if (numeroNotte==2) {
                @SuppressWarnings("unchecked")//mi dava fastidio il warning. se trova la guardia è safe, se non la trova non esegue il pezzo di codice di competenza
                Player<Mitomane> mitomane=(Player<Mitomane>)getOfType(new Player<Mitomane>("", new Mitomane())).getFirst();
                if (mitomane!=null) {
                    messageTo(playersList, "Mitomane, apri gli occhi!");
                    try {
                        String chosenPlayerName=queue.take();
                        Player<?> chosenPlayer=getFromName(chosenPlayerName);
                        if (chosenPlayer.getRole().getClass().getSimpleName().equals("Lupo")) {
                            changeRoleToLupo(mitomane);
                            messageTo(mitomane, "La persona scelta era un Lupo! Otterrai questo nuovo ruolo e lo manterrai per il resto della partita.");
                        } else if (chosenPlayer.getRole().getClass().getSimpleName().equals("Veggente")) {
                            changeRoleToVeggente(mitomane);
                            messageTo(mitomane, "La persona scelta era un Veggente! Otterrai questo nuovo ruolo e lo manterrai per il resto della partita.");
                        } else {
                            changeRoleToVillico(mitomane);
                            messageTo(mitomane, "La persona scelta non era un Lupo, e nemmeno un Veggente!\nManterrai il ruolo di Villico per il resto della partita.");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }                    
                    messageTo(playersList, "Mitomane, chiudi gli occhi!");
                }
            }

            //Massoni
            //sono due umani che conoscono reciprocamente il ruolo dell’altro. solo durante la prima notte il moderatore chiama anche i Massoni i quali aprono gli occhi e si riconoscono.
            ArrayList<Player<?>> massoni=getOfType(new Player<Massone>("", new Massone()));
            if (!massoni.isEmpty()) {
                messageTo(playersList, "Massoni, aprite gli occhi!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (massoni.size()==1) messageTo(massoni, massoni.get(0).getPlayerName()+", siete il massone!");
                else messageTo(massoni, massoni.get(0).getPlayerName()+", "+massoni.get(1).getPlayerName()+", siete i massoni!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                messageTo(playersList, "Massoni, chiudete gli occhi!");
            }

            //-----GIORNO-----
            messageTo(playersList, "E' giorno, tutti possono aprire gli occhi!");

            //viene mostrato a tutti chi è stato sbranato
            //da ora in poi non gioca più come personaggio ma come Fantasma, deve astenersi da commenti e non può più parlare per il resto della partita.
            Player<?> killedLastNight=whoWaskilledLastNight(numeroNotte);
            messageTo(playersList, "Nella notte appena trascorsa '"+killedLastNight.getPlayerName()+"' è stato sbranato dai lupi.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageTo(killedLastNight, "A partire da ora giocherai come Fantasma: dovrai astenerti dai commenti e non potrai parlare per il resto della partita.");
            //se il numero di lupi è uguale alla somma dei numeri degli altri ruoli, allora hanno vinto i lupi
            if (getOfType(new Player<Lupo>("", new Lupo())).size()==getOfType(new Player<Villico>("", new Villico())).size()+getOfType(new Player<Guardia>("", new Guardia())).size()+getOfType(new Player<Massone>("", new Massone())).size()+getOfType(new Player<Medium>("", new Medium())).size()+getOfType(new Player<Veggente>("", new Veggente())).size()) {
                hasEnded=true;
                haveLupiWon=true;
            }

            //linciaggio
            //tre minuti di discussione per decidere chi eliminare
            //(partendo da indice 0) ognuno vota una persona. i due con più voti sono gli indiziati (in caso di parità, quello con l'indice più piccolo).
            //i due giocatori indiziati possono difendersi con un ultimo breve discorso (20 secondi a testa)
            //poi i giocatori non indiziati e ancora vivi (esclusi quindi gli indiziati e i fantasmi) votano di nuovo il giocatore tra gli indiziati che verrà linciato.
            //chi ha preso più voti è linciato e diventa un fantasma (se parità, indice più basso)
            try {
                Player<?> votedPlayer=getFromName(queue.take());
                if (!votedPlayer.getIsProtected()) {
                    killPlayer(votedPlayer, false);
                    messageTo(playersList, "Il giocatore '"+votedPlayer.getPlayerName()+"' è stato linciato!");
                    messageTo(votedPlayer, "A partire da ora giocherai come Fantasma: dovrai astenerti dai commenti e non potrai parlare per il resto della partita.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //se non ci sono più lupi vivi, gli umani hanno vinto
            ArrayList<Player<?>> lupiInGame=getOfType(new Player<Lupo>("", new Lupo()));
            boolean areThereLupiAlive=false;
            for (Player<?> lupo:lupiInGame) {
                if (lupo.getIsGhost()) areThereLupiAlive=true;
            }
            if (areThereLupiAlive) {
                hasEnded=true;
                haveLupiWon=false;
            }

            //a questo punto il giorno è terminato: si ricomincia con una nuova notte e così via, finché una fazione non vince.
        }

        //condizione per cui lupi/villani vincono
        //win umani, se linciano tutti i lupi
        //win lupi, se in un qualunque momento sono in numero pari agli umani ancora vivi

        //ogni step "bloccante" in attesa del completamento, poi si passa al successivo

        //ad ogni step/cambio di "stato" della partita: invio ad ogni client del "blocco" di dati/info sulla partita che devono sapere/visualizzare
    }

    private void assignRoles() {
        Collections.shuffle(namePlayersList);
        //
        ArrayList<Player<?>> list1=new ArrayList<Player<?>>(Arrays.asList(new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo()), new Player<Lupo>("", new Lupo()), new Player<Veggente>("", new Veggente())));
        Collections.shuffle(list1);
        //minimo 8 giocatori per iniziare la partita, quindi non serve un if per controllare se eseguire il primo ciclo
        for (Player<?> player:list1) {
            list1.getFirst().setPlayerName(player.getPlayerName());
            playersList.add(list1.getFirst());
            list1.remove(0);
            namePlayersList.remove(0);
        }
        if (namePlayersList.isEmpty()) return;
        //
        ArrayList<Player<?>> list2=new ArrayList<Player<?>>(Arrays.asList(new Player<Medium>("", new Medium()), new Player<Villico>("", new Villico()), new Player<Guardia>("", new Guardia()), new Player<Villico>("", new Villico()), new Player<Mitomane>("", new Mitomane()), new Player<Massone>("", new Massone()), new Player<Massone>("", new Massone())));
        Collections.shuffle(list2);
        for (Player<?> player:list2) {
            list2.getFirst().setPlayerName(player.getPlayerName());
            playersList.add(list2.getFirst());
            list2.remove(0);
            namePlayersList.remove(0);
            if (namePlayersList.isEmpty()) return;
        }
        //
        ArrayList<Player<?>> list3=new ArrayList<Player<?>>(Arrays.asList(new Player<Lupo>("", new Lupo()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo())));
        Collections.shuffle(list3);
        for (Player<?> player:list3) {
            list3.getFirst().setPlayerName(player.getPlayerName());
            playersList.add(list3.getFirst());
            list3.remove(0);
            namePlayersList.remove(0);
            if (namePlayersList.isEmpty()) return;
        }
    }

    private Player<?> getFromName(String name) {
        for (Player<?> player:playersList) {
            if (player.getPlayerName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    private boolean isNameInList(ArrayList<Player<?>> list, String name) {
        for (Player<?> player:list) {
            if (player.getPlayerName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean addPlayer(String username) {
        if (namePlayersList.size()>=24) return false;//limite di 24 giocatori
        if (!namePlayersList.contains(username)) {
            namePlayersList.add(username);
            return true;
        }
        return false;
    }

    private void updater(ArrayList<Player<?>> list, String message, int paramCode) {
        //TO DO
    }

    private void messageTo(Player<?> x, String m) {
        messageTo(x, m, 200);
    }
    private void messageTo(Player<?> x, String m, int paramCode) {
        messageTo(new ArrayList<Player<?>>(Arrays.asList(x)), m, paramCode);
    }

    private void messageTo(ArrayList<Player<?>> list, String message) {
        messageTo(list, message, 200);
    }
    private void messageTo(ArrayList<Player<?>> list, String message, int paramCode) {
        message="[Moderatore]: "+message;//eventualmente modificare il prefisso del messaggio (che qui è "[Moderatore]") per essere elaborato dal sito per essere visualizzato come desiderato (ex. per mettere in grassetto/colorato il nome del mittente)
        @SuppressWarnings({ "unchecked", "rawtypes" })//just because. sono consapevole della non tipizazzione della lista
        ArrayList listToBeSent=new ArrayList(Arrays.asList(paramCode, message));
        for (Player<?> player:list) {
            player.sendMessage(listToBeSent);
        }
    }

    @SuppressWarnings("rawtypes")//just because
    public void redirectToAll(ArrayList toBeSent) {
        //stacca thread che esegue il for qui sotto
        for (Player<?> player:playersList) {
            player.sendMessage(toBeSent);
        }
    }

    private ArrayList<Player<?>> getOfType(Player<?> type) {
        ArrayList<Player<?>> list=new ArrayList<>();
        for (Player<?> player:playersList) {
            if (player.getRole().getClass().equals(type.getRole().getClass())) {
                list.add(player);
            }
        }
        return list;
    }

    private Player<?> whoWaskilledLastNight(int numNotteAttuale) {
        for (int i=ghosts.size()-1; i>=0; i--) {
            if (ghosts.get(i).getHasBeenKilledDuringNight()) return ghosts.get(i);
        }
        return null;
    }

    private void changeRoleToLupo(Player<Mitomane> mitomane) {
        Player<Lupo> lupo=new Player<Lupo>(mitomane, new Lupo());
        int pos=playersList.indexOf(mitomane);
        playersList.remove(pos);
        playersList.add(pos, lupo);
    }

    private void changeRoleToVeggente(Player<Mitomane> mitomane) {
        Player<Veggente> veggente=new Player<Veggente>(mitomane, new Veggente());
        int pos=playersList.indexOf(mitomane);
        playersList.remove(pos);
        playersList.add(pos, veggente);
    }

    private void changeRoleToVillico(Player<Mitomane> mitomane) {
        Player<Villico> villico=new Player<Villico>(mitomane, new Villico());
        int pos=playersList.indexOf(mitomane);
        playersList.remove(pos);
        playersList.add(pos, villico);
    }

    private void killPlayer(Player<?> player, boolean hasBeenKilledDuringNight) {
        player.setIsGhost(true);//viene sbranato
        player.setHasBeenKilledDuringNight(hasBeenKilledDuringNight);
        player.setNumeroNotteWhenKilled(numeroNotte);
        ghosts.add(player);//aggiunto alla lista cronologica dei giocatori morti
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

    public ArrayList<String> getNamePlayersList() {
        return namePlayersList;
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }

    public boolean getHaveLupiWon() {
        return haveLupiWon;
    }
}
