package com.example.wildtide.lupus;

import java.util.Collection;
import java.util.HashMap;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.web.bind.annotation.DeleteMapping;
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
    public boolean testUsername(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
        Game found=gamesHashMap.get(gameName);
        if (found==null) {
            return !(
                (gameName==null || gameName.isEmpty() || gameName.matches(".*[^a-zA-Z0-9_].*") || gameName.contains(" "))
                &&
                (username==null || username.isEmpty() || username.matches(".*[^a-zA-Z0-9_].*") || username.contains(" "))
                );
        } else {
            for (String playerName:found.getNamePlayersList()) {
                if (playerName.equals(username)) return false;
            }
            return !(username==null || username.isEmpty() || username.matches(".*[^a-zA-Z0-9_].*") || username.contains(" "));
        }
    }
    
    @GetMapping("openGames")
    public Collection<Game> getExistingGames() {
        Collection<Game> temp=gamesHashMap.values();
        return temp;
    }

    @PostMapping("newGame")
    public void createNewGame(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
        Game newOne=new Game(gameName);
        gamesHashMap.put(gameName, newOne);
        newOne.addPlayer(username);
        new Thread(() -> {
            try {
                Thread.sleep(420000);//7 minuti
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!newOne.isAlive()) {
                gamesHashMap.remove(gameName);
            }
        }).start();
    }
    
    @PostMapping("enterGame")
    public boolean enterGame(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
        Game foundGame=gamesHashMap.get(gameName);
        if (foundGame==null || foundGame.hasStarted()) return false;
        return foundGame.addPlayer(username);
    }
    
    @PostMapping("startGame")
    public boolean startGame(@RequestHeader("gameName") String gameName, @RequestHeader("Username") String username) {
        Game askedToStart=gamesHashMap.get(gameName);
        if (askedToStart.getWhoCreated().equals(username) && askedToStart.getNamePlayersList().size()>=8) {
            askedToStart.start();
            return true;
        } else {
            return false;
        }
    }

    @DeleteMapping("discardGame")
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

    @GetMapping("canOpenWebsocket")
    public boolean canOpenWebsocket(@RequestHeader("GameName") String gameName) {
        Game foundGame=gamesHashMap.get(gameName);
        //non dovrebbe poter essere 'null' perchè il sito arriva a chiamare questo metodo quando ha già aperto la nuova pagina clickando sul bottone generato, quindi è QUASI sicuro che in Game esista (però potrebbe essere scaduto nell'istante in cui stava entrando)
        if (foundGame==null) return false;
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
