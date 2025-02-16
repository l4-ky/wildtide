package com.example.wildtide.lupus;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/lupus/")
public class LupusController {
    private HashMap<String, Game> gamesHashMap=new HashMap<String, Game>();

    //PREP METHODS
    @PostMapping("testUsername")
    public boolean testUsername(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
        Game found=gamesHashMap.get(gameName);
        if (found==null) {
            return 
                !(gameName==null || gameName.isEmpty() || gameName.matches(".*[^a-zA-Z0-9_].*"))
                &&
                !(username==null || username.isEmpty() || username.matches(".*[^a-zA-Z0-9_].*"));
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
        //ASSOLUTAMENTE DA DECOMMENTARE IL CONTROLLO DEL NUMERO DI GIOCATORI, qui sotto
        if (askedToStart.getWhoCreated().equals(username) /* && askedToStart.getNamePlayersList().size()>=8 */) {
            askedToStart.start();
            return true;
        } else {
            return false;
        }
    }

    @DeleteMapping("discardGame")
    public boolean discardGame(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
        Game toDiscard=gamesHashMap.get(gameName);
        if (toDiscard!=null && toDiscard.getWhoCreated().equals(username) && !toDiscard.hasStarted()) {
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
        while (!foundGame.getCanStartEmitters()) {
            //do absolutely nothing, i'm waiting for approval to allow the client to open the Websocket
        }
        return true;
    }

    @GetMapping("/sse/{gameName}/{username}")
    //@GetMapping("/sse")
    public SseEmitter openNewEmitter(@PathVariable("gameName") String gameName, @PathVariable("username") String username) {
        SseEmitter newEmitter=new SseEmitter(Long.MAX_VALUE);
        System.out.println(gameName+" - "+username);
        try {
            newEmitter.send(SseEmitter.event().name("INIT"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //PERCHE' QUA NON TROVA IL GAME O IL PLAYER ???
        Game foundGame=gamesHashMap.get(gameName);
        System.out.println(foundGame);
        if (foundGame==null) return null;
        Player<?> foundPlayer=foundGame.getPlayersList().get(username);
        System.out.println(foundPlayer);
        if (foundPlayer==null) return null;
        foundPlayer.setEmitter(newEmitter);
        return newEmitter;
    }

    /* @GetMapping(value = "/sse/{gameName}/{username}", produces = "text/event-stream")
    public void streamEvents(HttpServletResponse response, @PathParam("gameName") String gameName, @PathParam("username") String username) throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        ///
        SseEmitter newEmitter=new SseEmitter(Long.MAX_VALUE);
        Game foundGame=gamesHashMap.get(gameName);
        System.out.println(foundGame);
        if (foundGame==null) {
            out.write(new Gson().toJson(null));
            out.flush();
            return;
        }
        Player<?> foundPlayer=foundGame.getPlayersList().get(username);
        System.out.println(foundPlayer);
        if (foundPlayer==null) {
            out.write(new Gson().toJson(null));
            out.flush();
            return;
        }
        foundPlayer.setEmitter(newEmitter);
        out.write(new Gson().toJson(newEmitter));
        out.flush();
        out.close();
    } */
    //END OF PREP METHODS
    
    //IN-GAME METHODS
    @PutMapping("choice/{gameName}")
    public void guardiaChoice(@RequestHeader("GameName") String gameName, @RequestBody String chosenPlayer) {
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
    public void redirectMessage(@RequestHeader("GameName") String gameName, @RequestHeader("toRole") String toRole, @RequestHeader("Username") String senderName, @RequestBody String messageReceived) {
        gamesHashMap.get(gameName).redirect(toRole, senderName, messageReceived);
	}
    //END OF IN-GAME METHODS
}
