package com.example.wildtide.lupus;

import java.util.Collection;
import java.util.HashMap;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/lupus/")
@ServerEndpoint("/lupus/{gameName}/{username}")
public class LupusController {
    private HashMap<String, Game> gamesHashMap=new HashMap<String, Game>();

    //PREP METHODS
    @PostMapping("testUsername")
    public boolean testUsername(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username, @RequestHeader("Exists") boolean exists) {
        if(username==null || username.isEmpty() || username.matches("^[^a-zA-Z0-9_]*$") || username.contains(" ")) {
            return false;
        } else {
            Game foundGame=gamesHashMap.get(gameName);
            if (foundGame!=null) {
                for (String name:foundGame.getNamePlayersList()) {
                    if (name.equals(username)) {
                        return false;
                    }
                }
            } else {
                //il Game creato non esiste
                if (exists==false) {
                    //ma il Game in questione è in fase di creazione, quindi risponde 'true' per accettare il nome richiesto
                    return true;
                } else {
                    //il Game cercato semplicemente non esiste, probabilmente per nome inserito sbagliato o proprio non esistente
                    return false;
                }
            }
        }
        System.out.println(true);
        return true;
    }
    
    @GetMapping("openGames")
    public Collection<Game> getExistingGames() {
        Collection<Game> temp=gamesHashMap.values();
        return temp;
    }

    //RIPRENDERE DA QUA
    @PostMapping("newGame")
    public void createNewGame(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
        System.out.println(gameName+"\n"+username);//DEBUG
        Game newOne=new Game(gameName);
        gamesHashMap.put(gameName, newOne);
        System.out.println(gamesHashMap.get(gameName));//DEBUG
        newOne.addPlayer(username);
        new Thread(() -> {
            try {
                Thread.sleep(70000);//un minuto e un secondo.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!newOne.isAlive()) {
                gamesHashMap.remove(gameName);
            }
        }).start();
    }
    
    @PostMapping("enterGame/{gameName}")
    public boolean enterGame(@PathParam("gameName") String gameName, @RequestHeader("Username") String username) {
        Game foundGame=gamesHashMap.get(gameName);
        if (foundGame==null || foundGame.hasStarted()) return false;
        return foundGame.addPlayer(username);
    }
    
    @PostMapping("startGame/{gameName}")
    public boolean startGame(@PathParam("gameName") String gameName, @RequestHeader("Username") String username) {
        Game askedToStart=gamesHashMap.get(gameName);
        if (askedToStart.getWhoCreated().equals(username) && askedToStart.getNamePlayersList().size()>=8) {
            askedToStart.start();
            return true;
        } else {
            return false;
        }
    }
    
    //TO DO: deve essere DeleteMapping
    @PutMapping("discardGame")
    public boolean discardGame(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
        Game toDiscard=gamesHashMap.get(gameName);
        if (toDiscard.getWhoCreated().equals(username) && !toDiscard.hasStarted()) {
            toDiscard.disposeOfGame();
            gamesHashMap.remove(gameName);
            return true;
        } else {
            return false;
        }
    }

    @GetMapping("canOpenWebsocket/{gameName}")
    public boolean canOpenWebsocket(@PathParam("gameName") String gameName) {
        Game foundGame=gamesHashMap.get(gameName);
        while (!foundGame.getCanOpenWebsockets()) {
            //do absolutely nothing, i'm waiting for approval to allow the client to open the Websocket
        }
        return true;
    }

    @OnOpen
    public boolean onOpen(Session session, @PathParam("gameName") String gameName, @PathParam("username") String username) {
        Game gameFound=gamesHashMap.get(gameName);
        if (gameFound!=null) {
            Player<?> playerFound=gameFound.getPlayersList().get(username);
            if (playerFound!=null) {
                playerFound.setSession(session);
                //aggiungo alla queue un placeholder per indicare che la websocket è aperta e connessa. (vedi inizio Game-run())
                //non serve indicare playerName o altre informazioni
                try {
                    gameFound.getQueue().put("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        } 
    }
    //END OF PREP METHODS
    
    //IN-GAME METHODS
    @PutMapping("choice/{gameName}")
    public void guardiaChoice(@PathParam("gameName") String gameName, @RequestBody String chosenPlayer) {
        try {
            gamesHashMap.get(gameName).getQueue().put(chosenPlayer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    //(i metodi 'choice' che non hanno alterazioni particolari si potrebbero unificare, hanno lo stesso corpo e svolgono la stessa funzione)
    /* @PutMapping("choice/{gameName}/guardia")
    public void guardiaChoice(@PathParam("gameName") String gameName, @RequestBody String chosenPlayer) {
        try {
            getFromName(gameName).getQueue().put(chosenPlayer);
            } catch (InterruptedException e) {
                e.printStackTrace();
                }
                }
                
                @PutMapping("choice/{gameName}/veggente")
                public void veggentiChoice(@PathParam("gameName") String gameName, @RequestBody String chosenPlayer) {
                    try {
                        getFromName(gameName).getQueue().put(chosenPlayer);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
        }
    }

    @PutMapping("choice/{gameName}/lupi")
    public void lupiChoice(@PathParam("gameName") String gameName, @RequestBody String chosenPlayer) {
        try {
            getFromName(gameName).getQueue().put(chosenPlayer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PutMapping("choice/{gameName}/mitomane")
    public void mitomaneChoice(@PathParam("gameName") String gameName, @RequestBody String chosenPlayer) {
        try {
            getFromName(gameName).getQueue().put(chosenPlayer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PutMapping("choice/{gameName}/indiziato")
    public void indiziatoChoice(@PathParam("gameName") String gameName, @RequestBody String chosenPlayer) {
        try {
            getFromName(gameName).getQueue().put(chosenPlayer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    } */

    @PostMapping("chat/{gameName}/{toRole}")
    public void redirectMessage(@PathParam("gameName") String gameName, @PathParam("toRole") String toRole, @RequestHeader("Username") String senderName, @RequestBody String messageReceived) {
        gamesHashMap.get(gameName).redirect(toRole, senderName, messageReceived);
	}
    //END OF IN-GAME METHODS
}
