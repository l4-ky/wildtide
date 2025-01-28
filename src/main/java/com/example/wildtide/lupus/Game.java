package com.example.wildtide.lupus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Game extends Thread{
    private String gameName;
    private boolean hasStarted=false;
    private boolean hasEnded=false;
    private ArrayList<String> namePlayersList=new ArrayList<String>();
    private ArrayList<Player<?>> playersList=new ArrayList<Player<?>>();

    public Game(String name) {
        this.gameName=name;
    }

    @Override
    public void run() {
        hasStarted=true;
        assignRoles();

        while (!hasEnded) {

        //-----NOTTE-----
        //"E' notte, chiudete tutti gli occhi"

        //Guardia del corpo
        //ogni notte protegge una persona a sua scelta.
        //dalla seconda notte in poi (prima della fase dei lupi mannari) il moderatore chiama la Guardia del corpo e questi gli indica una persona a scelta (no se stesso) che è protetta dai lupi mannari.
        //se quella persona è poi scelta anche dai lupi mannari come vittima, non muore e nella fase della notte nessuno è sbranato.

        //"Il veggente apre gli occhi e sceglie una persona"
        //Veggente sceglie e ottiene esito lupo/no (fase giocata anche se il veggente è morto(fantasma))
        //(possono essere due se il mitomane è diventato veggente; il veggente vede il mitomane-lupo mannaro come un lupo mannaro)
        //"Il veggente chiude gli occhi"
        //[7-10 secondi]

        //"I lupi mannari aprono gli occhi e scelgono una persona da sbranare"
        //i Lupi si riconoscono e scelgono chi sbaranare (votazione: scelta comune, oppure scelta per maggioranza a fine tempo)
        //"I lupi mannari chiudono gli occhi"
        //[15-20 secondi]

        //Medium
        //dalla seconda notte in poi, il moderatore chiama la sua fase e con un cenno del capo gli dice
        //“sì” se la persona linciata nel turno precedente era un lupo mannaro, “no” altrimenti.

        //Mitomane
        //alla fine della seconda notte indica al moderatore un altro giocatore ancora vivo.
        //se questo non è un lupo mannaro o il veggente, il mitomane resta un umano normale fino al termine della partita.
        //altrimenti assume immediatamente il ruolo rispettivamente di lupo mannaro o di veggente, a tutti gli effetti.

        //Massone
        //sono due umani che conoscono reciprocamente il ruolo dell’altro. solo durante la prima notte il moderatore chiama anche i Massoni i quali aprono gli occhi e si riconoscono.

        //-----GIORNO-----
        //"E' giorno, aprite tutti gli occhi"

        //viene mostrato a tutti chi è stato sbranato
        //(check se lupi hanno vinto)
        //da ora in poi non gioca più come personaggio ma come Fantasma, deve astenersi da commenti e non può più parlare per il resto della partita.

        //linciaggio
        //tre minuti di discussione per decidere chi eliminare
        //(partendo da indice 0) ognuno vota una persona. i due con più voti sono gli indiziati (in caso di parità, quello con l'indice più piccolo).
        //i due giocatori indiziati possono difendersi con un ultimo breve discorso (20 secondi a testa)
        //poi i giocatori non indiziati e ancora vivi (esclusi quindi gli indiziati e i fantasmi) votano di nuovo il giocatore tra gli indiziati che verrà linciato.
        //chi ha preso più voti è linciato e diventa un fantasma (se parità, indice più basso)

        //(check se umani hanno vinto)

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

    public ArrayList<String> getNamePlayersList() {
        return namePlayersList;
    }
}
