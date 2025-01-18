package com.example.wildtide;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;

@RestController
@RequestMapping("/lockey/")
public class Lockey_ControllerA {
    @PostMapping("signIn")
    public ResponseEntity<Lockey_BoolResponse> signin(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin) {
        try {
            if (Lockey_Manager.doFilesExistFor(username)) {
                System.out.println("[Files already exist for "+username+"]");
                return ResponseEntity.ok(new Lockey_BoolResponse(false, "Files already exist for "+username+". Please log in instead."));
            } else {
                Lockey_Manager.createFilesFor(username, pin);
                return ResponseEntity.ok(new Lockey_BoolResponse(true, "Files created for "+username));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new Lockey_BoolResponse(false, "An error occurred."));
        }
    }

    @PostMapping("logIn")
    public ResponseEntity<Lockey_BoolResponse> login(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin) {
        try {
            System.out.println("[Tentativo login: "+username+" - "+pin+"]");
            if (!Lockey_Manager.doFilesExistFor(username)) {
                System.out.println("[Files do not exist for "+username+"]");
                return ResponseEntity.ok(new Lockey_BoolResponse(false, "Files do not exist for "+username+". Please sign in first."));
            } else if (!Lockey_Manager.validAccess(username, pin)) {
                System.out.println("[Wrong credentials for "+username+"]");
                return ResponseEntity.ok(new Lockey_BoolResponse(false, "Wrong credentials for "+username+". Please try again."));
            } else {
                System.out.println("[Access granted for "+username+"]");
                return ResponseEntity.ok(new Lockey_BoolResponse(true, "Access granted for "+username));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new Lockey_BoolResponse(false, "An error occurred."));
        }
    }

    @PutMapping("putProfile")
    public boolean putProfile(@RequestHeader("OldUsername") String oldUsername, @RequestHeader("OldPin") String oldPin, @RequestHeader("NewUsername") String newUsername, @RequestHeader("NewPin") String newPin) throws ClassNotFoundException, IOException {
        System.out.println("[PUT Account]");
        System.out.println("[Old Credentials: " + oldUsername + " - " + oldPin + "]");
        System.out.println("[New Credentials: " + newUsername + " - " + newPin + "]");
        return Lockey_Manager.updateProfile(oldUsername, oldPin, newUsername, newPin);
    }

    @PutMapping("put")
    public void putInfo(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin, @RequestHeader("OldName") String oldName, @RequestBody String Xbody) throws ClassNotFoundException, IOException {
        Lockey_Credentials body=new Gson().fromJson(Xbody, Lockey_Credentials.class);
        System.out.println("[PUT: "+username+" - "+pin+" - "+oldName+" - "+body.getName()+"]");
        System.out.println(body);
        Lockey_Manager.store(username, pin, body);
        if (!oldName.equals("") && !oldName.equals(body.getName())) {
            Lockey_Manager.delete(username, pin, oldName);
        }
    }

    @GetMapping("{username}/get")
    public String/* ResponseEntity<CredResponse> */ getInfo(@PathVariable String username, @RequestHeader("Pin") String pin) throws ClassNotFoundException, IOException {
        ArrayList<Lockey_Credentials> content0 = null;
        try {
            System.out.println("[GET: "+username+" - "+pin+"]");
            content0 = Lockey_Manager.readContentOf(username, pin);
            System.out.println(content0);
        } catch (EOFException e) {
            ResponseEntity<Lockey_CredResponse> newRes= ResponseEntity.ok(new Lockey_CredResponse(false, null, e.getMessage()));
            return new Gson().toJson(newRes);
        }
        ResponseEntity<Lockey_CredResponse> newResp= ResponseEntity.ok(new Lockey_CredResponse(true, content0, "Credentials retrieved for "+username));
        return new Gson().toJson(newResp);
    }

    @DeleteMapping("delete")
    public void deleteInfo(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin, @RequestHeader("Name") String name) throws ClassNotFoundException, IOException {
        Lockey_Manager.delete(username, pin, name);
    }
}
