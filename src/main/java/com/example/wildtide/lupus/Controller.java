package com.example.wildtide.lupus;

import java.util.ArrayList;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/lupus/")
@ServerEndpoint("/lupus/{gameName}/{username}")
public class Controller {
    private ArrayList<Game> gamesList=new ArrayList<Game>();

    private Game getFromName(String name) {
        for (Game Game:gamesList) {
            if (Game.getName().equals(name)) {
                return Game;
            }
        }
        return null;
    }

    //PREP METHODS
    @OnOpen
    public boolean onOpen(Session session, @PathParam("gameName") String gameName, @PathParam("username") String username) {
        Game x=getFromName(gameName);
        if (x!=null) {
            x.addPlayer(username);
            return true;
        } else {
            return false;
        } 
    }

    @PostMapping("testUsername")
    public boolean testUsername(@RequestHeader("GameName") String gameName, @RequestHeader("Username") String username) {
		if(username==null || username.isEmpty() || username.matches("^[^a-zA-Z0-9_]*$") || username.contains(" ")) {
			return false;
		} else {
			for (Game game:gamesList) {
                if (!game.getGameName().equals(gameName)) break;
				for (String name:game.getNamePlayersList()) {
                    if (name.equals(username)) return false;
                }
			}
		}
		return true;
    }

    @PostMapping("newGame")
    public void createNewGame(@RequestHeader("GameName") String gameName) {
        Game newOne=new Game(gameName);
        gamesList.add(newOne);
        newOne.addPlayer(gameName);
    }

    @PostMapping("enterGame/{gameName}")
    public boolean enterGame(@PathParam("gameName") String gameName, @RequestHeader("Username") String username) {
        Game foundGame=getFromName(gameName);
        if (foundGame==null || foundGame.hasStarted()) return false;
        foundGame.addPlayer(username);
        return true;
    }

    @PostMapping("startGame/{gameName}")
    public void startGame(@PathParam("gameName") String gameName, @RequestHeader("Username") String username) {
        Game askedToStart=getFromName(username);
        if (askedToStart.getWhoStarted().equals(username) && askedToStart.getNamePlayersList().size()>=8) {
            askedToStart.start();
        }
    }
    //END OF PREP METHODS

    //IN-GAME METHODS
    //(i metodi 'choice' che non hanno alterazioni particolari si potrebbero unificare, hanno lo stesso corpo e svolgono la stessa funzione)
    @PutMapping("choice/{gameName}/guardia")
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
    }

    @SuppressWarnings("rawtypes")//just because
    @PostMapping("chat/{gameName}/{toWho}")
    public void postMethodName(@PathParam("gameName") String gameName, @RequestBody ArrayList messageReceived) {
        getFromName(gameName).redirectToAll(messageReceived);
    }    
    //END OF IN-GAME METHODS
}
