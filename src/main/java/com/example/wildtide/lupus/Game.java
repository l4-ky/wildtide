package com.example.wildtide.lupus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Game extends Thread{
    private String gameName;
    private ArrayList<String> namePlayersList=new ArrayList<String>();
    //
    private HashMap<String, Player<?>> playersList=new HashMap<String, Player<?>>();
    private BlockingQueue<String> queue=new ArrayBlockingQueue<>(1);
    private boolean canStartEmitters=false;
    private boolean hasStarted=false;
    private boolean hasEnded=false;
    private boolean haveLupiWon=false;
    private int numeroNotte=0;
    private int numeroGiorno=0;
    private ArrayList<Player<?>> ghosts=new ArrayList<Player<?>>();//lista cronologica
    private boolean mitomaneHasSecondChance=false;
    //
    private int guardiaPhaseDuration=7;
    private int veggentePhaseDuration=7;
    private int lupiPhaseDuration=20;
    private int mitomanePhaseDuration=10;
    private int linciaggioPhaseDuration=4*60;

    public Game(String name) {
        this.gameName=name;
    }

    //TO DO: polling o similare per controllare che il Game sia 'alive', cioè che non sia una partita fantastma avviata e mai "giocata"

    int openedEmitters=0;
    @Override
    public void run(){
        hasStarted=true;
        assignRoles();
        canStartEmitters=true;
        //TO DO: se il Role incapsulato nel Player non viene usato per metodi aggiuntivi, può essere trasformato in una semplice stringa contenente il ruolo. tutti i riferimenti al Player Role dovranno poi esserea adeguati. (prossibile alleggerimento nelle prestazioni, in quanto elabora stringe e non oggetti, anche con meno chiamate a metodi)

        //rendevous per aspettare che tutti i Player abbiano aperto e collegato la WebSocket
        while (openedEmitters<playersList.size()) {
            System.out.println("waiting.. ("+openedEmitters+")");
            try {
                queue.take();
                System.out.println("TOOK ONE");
                openedEmitters++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        //TESTARE INVIO DATI + CORRETTA RICEZIONE NEL SITO
        System.out.println("Here");
        
        //invio info iniziali ai siti
        for (Player<?> player:playersList.values()) {
            System.out.println("Here with "+player);
            player.sendMessage(new ArrayList<>(Arrays.asList(300, "Moderatore", playersList.values())));
        }
        
        //
        messageTo(playersList, "La partita è iniziata!", 400);
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
                Player<Guardia> guardia=(Player<Guardia>)getOfRole("Guardia").getFirst();
                if (guardia!=null) {
                    messageTo(playersList, "Guardia del corpo, apri gli occhi!\nChi vuoi proteggere questa notte?");
                    try {
                        String chosenPlayerName=queue.poll(guardiaPhaseDuration,TimeUnit.SECONDS);
                        //controllo ridondante, ma da mantenere per sicurezza. in teoria il sito non deve permettere alla Guardia di scegliere se stessa
                        if (chosenPlayerName!=null) {
                            if (!chosenPlayerName.equals(guardia.getPlayerName())) {
                                Player<?> chosenPlayer=playersList.get(chosenPlayerName);
                                chosenPlayer.setIsProtected(true);
                            } else {
                                messageTo(guardia, "[Non è possibile proteggere se stessi.]");
                            }
                        } else {
                            messageTo(guardia, "[Tempo scaduto, non proteggerai nessuno questa notte.]");
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
            ArrayList<Player<?>> veggenti=getOfRole("Veggente");//se serve, fare il casting a 'Player<Veggente>' nell'interazione. così si rendono accessibili i metodi del 'role'
            messageTo(playersList, "Veggenti, aprite gli occhi!\n Di chi si vuole scoprire il ruolo?");
            try {
                boolean areAllGhosts=true;
                for (Player<?> veggente:veggenti) {
                    if (!veggente.getIsGhost()) areAllGhosts=false;
                }
                if (!areAllGhosts) {
                    String chosenPlayerName=queue.poll(veggentePhaseDuration,TimeUnit.SECONDS);
                    //still, controllo ridondante ma da mantenere per sicurezza sicurezza
                    if (chosenPlayerName!=null) {
                        if (!isNameInList(veggenti, chosenPlayerName)) {
                            String chosenPlayerRole=playersList.get(chosenPlayerName).getRole().getClass().getSimpleName();
                            for (Player<?> veggente:veggenti) {
                                if (!veggente.getIsGhost()) {
                                    messageTo(veggente, "Il giocatore che è stato osservato è un "+chosenPlayerRole);
                                }
                            }
                        } else {
                            messageTo(veggenti, "[Non è possibile conoscere il ruolo di se stessi o di un altro veggente.]");
                        }
                    } else {
                        messageTo(veggenti, "[Tempo scaduto, non conoscerai il ruolo di nessuno questa notte.]");
                    }
                } else {
                    Thread.sleep(veggentePhaseDuration*1000);//simulo l'esecuzione della fase
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            messageTo(playersList, "Veggenti, chiudete gli occhi!");
            
            //Lupi mannari
            //i Lupi si riconoscono e scelgono chi sbaranare (votazione: scelta comune, oppure scelta per maggioranza a fine tempo, oppure nessuno)
            //[15-20 secondi]
            ArrayList<Player<?>> lupi=getOfRole("Lupo");
            messageTo(playersList, "Lupi mannari, aprite gli occhi!\n Chi volete sbranare stanotte?");
            try {
                String chosenPlayerName=queue.poll(lupiPhaseDuration,TimeUnit.SECONDS);
                //still, controllo ridondante ma da mantenere per sicurezza sicurezza
                if (chosenPlayerName!=null) {
                    if (!isNameInList(lupi, chosenPlayerName)) {
                        Player<?> chosenPlayer=playersList.get(chosenPlayerName);
                        killPlayer(chosenPlayer, true);
                    } else {
                        messageTo(lupi, "[Non è possibile sbranare se stessi o un altro lupo.]");
                    }
                } else {
                    messageTo(lupi, "[Tempo scaduto, nessuno verrà sbranato questa notte.]");
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
                Player<Medium> medium=(Player<Medium>)getOfRole("Medium").getFirst();
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
            if (numeroNotte==2 || mitomaneHasSecondChance) {
                mitomaneHasSecondChance=false;
                @SuppressWarnings("unchecked")//mi dava fastidio il warning. se trova la guardia è safe, se non la trova non esegue il pezzo di codice di competenza
                Player<Mitomane> mitomane=(Player<Mitomane>)getOfRole("Mitomane").getFirst();
                if (mitomane!=null) {
                    messageTo(playersList, "Mitomane, apri gli occhi!");
                    try {
                        String chosenPlayerName=queue.poll(mitomanePhaseDuration  ,TimeUnit.SECONDS);
                        if (chosenPlayerName!=null) {
                            Player<?> chosenPlayer=playersList.get(chosenPlayerName);
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
                        } else {
                            messageTo(mitomane, "[Tempo scaduto, non potrai assumere le sembiaze di nessuno. Potrai riprovare durante la prossima notte.]");
                            mitomaneHasSecondChance=true;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }                    
                    messageTo(playersList, "Mitomane, chiudi gli occhi!");
                }
            }

            //Massoni
            //sono due umani che conoscono reciprocamente il ruolo dell’altro. solo durante la prima notte il moderatore chiama anche i Massoni i quali aprono gli occhi e si riconoscono.
            if (numeroNotte==1) {
                ArrayList<Player<?>> massoni=getOfRole("Massone");
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
            }

            //-----GIORNO-----
            numeroGiorno++;
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
            if (getOfRole("Lupo").size()==getOfRole("Villico").size()+getOfRole("Guardia").size()+getOfRole("Massone").size()+getOfRole("Medium").size()+getOfRole("Veggente").size()) {
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
                String votedPlayerName=queue.poll(linciaggioPhaseDuration, TimeUnit.SECONDS);
                if (votedPlayerName!=null) {
                    Player<?> votedPlayer=playersList.get(queue.take());
                    if (!votedPlayer.getIsProtected()) {
                        killPlayer(votedPlayer, false);
                        messageTo(playersList, "Il giocatore '"+votedPlayer.getPlayerName()+"' è stato linciato!");
                        messageTo(votedPlayer, "A partire da ora giocherai come Fantasma: dovrai astenerti dai commenti e non potrai parlare per il resto della partita.");
                    }
                } else {
                    messageTo(playersList, "[Tempo scaduto, nessuno verrà linciato quest'oggi.]");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //se non ci sono più lupi vivi, gli umani hanno vinto
            ArrayList<Player<?>> lupiInGame=getOfRole("Lupo");
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
            player.setPlayerName(namePlayersList.removeLast());
            playersList.put(player.getPlayerName(), player);
            if (namePlayersList.isEmpty()) return;//DEBUG, ASSOLUTAMENTE DA TOGLIERE
        }
        if (namePlayersList.isEmpty()) return;
        //
        ArrayList<Player<?>> list2=new ArrayList<Player<?>>(Arrays.asList(new Player<Medium>("", new Medium()), new Player<Villico>("", new Villico()), new Player<Guardia>("", new Guardia()), new Player<Villico>("", new Villico()), new Player<Mitomane>("", new Mitomane()), new Player<Massone>("", new Massone()), new Player<Massone>("", new Massone())));
        Collections.shuffle(list2);
        for (Player<?> player:list2) {
            player.setPlayerName(namePlayersList.removeLast());
            playersList.put(player.getPlayerName(), player);
            if (namePlayersList.isEmpty()) return;
        }
        //
        ArrayList<Player<?>> list3=new ArrayList<Player<?>>(Arrays.asList(new Player<Lupo>("", new Lupo()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Villico>("", new Villico()), new Player<Lupo>("", new Lupo())));
        Collections.shuffle(list3);
        for (Player<?> player:list3) {
            player.setPlayerName(namePlayersList.removeLast());
            playersList.put(player.getPlayerName(), player);
            if (namePlayersList.isEmpty()) return;
        }
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
        if (namePlayersList.contains(username)) return false;
        namePlayersList.add(username);
        return true;
    }

    /* private void updater(ArrayList<Player<?>> list, String message, int paramCode) {
        //TO DO
    } */

    //
    /* 
     * 200: normale messaggio testuale
     * 300: inizializzazione sito
     * 
     */
    private void messageTo(Player<?> x, String m) {
        messageTo(new ArrayList<Player<?>>(Arrays.asList(x)), m, 200);
    }
    private void messageTo(HashMap<String, Player<?>> temp, String message) {
        messageTo(new ArrayList<Player<?>>(temp.values()), message, 200);
    }
    private void messageTo(HashMap<String, Player<?>> temp, String message, int paramCode) {
        messageTo(new ArrayList<Player<?>>(temp.values()), message, paramCode);
    }

    private void messageTo(ArrayList<Player<?>> list, String message) {
        messageTo(list, message, 200);
    }
    private void messageTo(ArrayList<Player<?>> list, String message, int paramCode) {
        @SuppressWarnings({ "unchecked", "rawtypes" })//just because. sono consapevole della non tipizzazione della lista
        ArrayList listToBeSent=new ArrayList(Arrays.asList(paramCode, "Moderatore", message));
        for (Player<?> player:list) {
            player.sendMessage(listToBeSent);
        }
    }
    //
    
    @SuppressWarnings({ "rawtypes", "unchecked" })//just because
    public void redirect(String toRole, String senderName, String message) {
        new Thread(() -> {
            ArrayList toBeSent=new ArrayList(Arrays.asList(200, senderName, message));
            ArrayList<Player<?>> playersFound;
            if (toRole.equals("all")) {
                playersFound=(ArrayList<Player<?>>)playersList.values();
            } else {
                playersFound=getOfRole(toRole);
            }
            for (Player<?> player:playersFound) {
                player.sendMessage(toBeSent);
            }
        }).start();
    }

    @SuppressWarnings("unused")
    public ArrayList<Player<?>> getOfRole(String type) {
        ArrayList<Player<?>> list=new ArrayList<>();
        playersList.forEach((key, value) -> {
            list.add(value);
        });
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
        playersList.remove(mitomane.getPlayerName());
        playersList.put(lupo.getPlayerName(), lupo);
    }

    private void changeRoleToVeggente(Player<Mitomane> mitomane) {
        Player<Veggente> veggente=new Player<Veggente>(mitomane, new Veggente());
        playersList.remove(mitomane.getPlayerName()); 
        playersList.put(veggente.getPlayerName(), veggente);
    }

    private void changeRoleToVillico(Player<Mitomane> mitomane) {
        Player<Villico> villico=new Player<Villico>(mitomane, new Villico());
        playersList.remove(mitomane.getPlayerName());
        playersList.put(villico.getPlayerName(), villico);
    }

    private void killPlayer(Player<?> player, boolean hasBeenKilledDuringNight) {
        player.setIsGhost(true);//viene sbranato
        player.setHasBeenKilledDuringNight(hasBeenKilledDuringNight);
        if (hasBeenKilledDuringNight) {
            player.setNumeroNotteWhenKilled(numeroNotte);
        } else {
            player.setNumeroGiornoWhenKilled(numeroGiorno);
        }
        ghosts.add(player);//aggiunto alla lista cronologica dei giocatori morti
    }

    public void disposeOfGame() {
        //TO DO: notificare tutti i Player già partecipanti che questo Game è stato scartato. dovranno tornare alla pagina precedente
    }

    public String getGameName() {
        return gameName;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public String getWhoCreated() {
        return namePlayersList.getFirst();
    }

    public ArrayList<String> getNamePlayersList() {
        return namePlayersList;
    }
    public HashMap<String, Player<?>> getPlayersList() {
        return playersList;
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }
    
    public boolean getCanStartEmitters() {
        return canStartEmitters;
    }

    public boolean getHaveLupiWon() {
        return haveLupiWon;
    }
}
