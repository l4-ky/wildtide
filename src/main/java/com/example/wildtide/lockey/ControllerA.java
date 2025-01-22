package com.example.wildtide.lockey;
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
public class ControllerA {
    @PostMapping("signIn")
    public ResponseEntity<BoolResponse> signin(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin) {
        try {
            if (Manager.doFilesExistFor(username)) {
                System.out.println("[Files already exist for "+username+"]");
                return ResponseEntity.ok(new BoolResponse(false, "Files already exist for "+username+". Please log in instead."));
            } else {
                Manager.createFilesFor(username, pin);
                return ResponseEntity.ok(new BoolResponse(true, "Files created for "+username));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new BoolResponse(false, "An error occurred."));
        }
    }

    @PostMapping("logIn")
    public ResponseEntity<BoolResponse> login(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin) {
        try {
            System.out.println("[Tentativo login: "+username+" - "+pin+"]");
            if (!Manager.doFilesExistFor(username)) {
                System.out.println("[Files do not exist for "+username+"]");
                return ResponseEntity.ok(new BoolResponse(false, "Files do not exist for "+username+". Please sign in first."));
            } else if (!Manager.validAccess(username, pin)) {
                System.out.println("[Wrong credentials for "+username+"]");
                return ResponseEntity.ok(new BoolResponse(false, "Wrong credentials for "+username+". Please try again."));
            } else {
                System.out.println("[Access granted for "+username+"]");
                return ResponseEntity.ok(new BoolResponse(true, "Access granted for "+username));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new BoolResponse(false, "An error occurred."));
        }
    }

    @PutMapping("putProfile")
    public boolean putProfile(@RequestHeader("OldUsername") String oldUsername, @RequestHeader("OldPin") String oldPin, @RequestHeader("NewUsername") String newUsername, @RequestHeader("NewPin") String newPin) throws ClassNotFoundException, IOException {
        System.out.println("[PUT Account]");
        System.out.println("[Old Credentials: " + oldUsername + " - " + oldPin + "]");
        System.out.println("[New Credentials: " + newUsername + " - " + newPin + "]");
        return Manager.updateProfile(oldUsername, oldPin, newUsername, newPin);
    }

    @PutMapping("put")
    public void putInfo(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin, @RequestHeader("OldName") String oldName, @RequestBody String Xbody) throws ClassNotFoundException, IOException {
        Credentials body=new Gson().fromJson(Xbody, Credentials.class);
        System.out.println("[PUT: "+username+" - "+pin+" - "+oldName+" - "+body.getName()+"]");
        System.out.println(body);
        Manager.store(username, pin, body);
        if (!oldName.equals("") && !oldName.equals(body.getName())) {
            Manager.delete(username, pin, oldName);
        }
    }

    @GetMapping("{username}/get")
    public String/* ResponseEntity<CredResponse> */ getInfo(@PathVariable String username, @RequestHeader("Pin") String pin) throws ClassNotFoundException, IOException {
        ArrayList<Credentials> content0 = null;
        try {
            System.out.println("[GET: "+username+" - "+pin+"]");
            content0 = Manager.readContentOf(username, pin);
            System.out.println(content0);
        } catch (EOFException e) {
            ResponseEntity<CredResponse> newRes= ResponseEntity.ok(new CredResponse(false, null, e.getMessage()));
            return new Gson().toJson(newRes);
        }
        ResponseEntity<CredResponse> newResp= ResponseEntity.ok(new CredResponse(true, content0, "Credentials retrieved for "+username));
        return new Gson().toJson(newResp);
    }

    @DeleteMapping("delete")
    public void deleteInfo(@RequestHeader("Username") String username, @RequestHeader("Pin") String pin, @RequestHeader("Name") String name) throws ClassNotFoundException, IOException {
        Manager.delete(username, pin, name);
    }
}
