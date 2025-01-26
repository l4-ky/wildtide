package com.example.wildtide.lupus;

import java.util.ArrayList;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/lupus/")
@ServerEndpoint("/lupus/websocket/{username}")
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
    public boolean onOpen(Session session, @PathParam("username") String username) {
        //TUTTO DA RIVEDERE, ASSEGNA LA SESSION AL THREAD E NON AL GAME
        Game x=getFromName(username);
        if (x!=null) {
            x.addPlayer(username);
            return true;
        } else {
            return false;
        } 
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

    @PutMapping("startGame/{gameName}")
    public void startGame(@PathParam("gameName") String gameName, @RequestHeader("Username") String username) {
        Game askedToStart=getFromName(username);
        if (askedToStart.getWhoStarted().equals(username)) {
            askedToStart.run();
        }
    }
    //END OF PREP METHODS

    //IN-GAME METHODS

    //END OF IN-GAME METHODS
}
