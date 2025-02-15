const URLPrefix="/lupus/";
const selfUsername=window.sessionStorage.getItem("username");
const gameName=window.sessionStorage.getItem("gameName");
gameNameP=document.getElementById("gameName");
gameNameP.innerHTML=gameName;
selfUsernameP=document.getElementById("selfUsername");
selfUsernameP.innerHTML=selfUsername;

let socket;
fetch(URLPrefix+"canOpenWebsocket", {
    method: "GET",
    headers: {"GameName":gameName}
})
.then(response => response.json())
.then((canOpenWebsocket) => {
    if (canOpenWebsocket) {
        //TESTARE APERTURA SOCKET
        socket=new WebSocket(URLPrefix+gameName+"/"+selfUsername);
        socket.addEventListener("open", (event) => {
            console.log("websocket opened.");
        });
        socket.addEventListener("message", (event) => {
            //TO DO: ricavare messaggio;
            console.log(event.data);
            //
            let mess;
            if (mess.code==200) {
                addMessageToChat(mess.sender, mess.message);
            } else if (mess.code=300) {
                let container=document.getElementById("playersContainer");
                mess.message.forEach((player) => {
                    if (player.name=selfUsername) {
                        /* if (player.role=="Villico") //TO DO: set immagini
                        else if (player.role=="Lupo")
                        else if (player.role="Veggente")
                        else if (player.role="Mitomane")
                        else if (player.role="Medium")
                        else if (player.role="Guardia")
                        else if (player.role="Massone") */
                    } else {
                        //TO DO: aggiungere le cards per i player
                        card=document.createElement("div");
                        card.classList.add("playerCard");
                        section=document.createElement("section");
                        h5=document.createElement("h5");
                        h5.innerHTML=player.name;//TO DO: get name
                        h5.id=player.name;//same
                        section.appendChild(h5);
                        img=document.createElement("img");
                        /* if (player.role=="Villico") //TO DO: set immagini
                        else if (player.role=="Lupo")
                        else if (player.role="Veggente")
                        else if (player.role="Mitomane")
                        else if (player.role="Medium")
                        else if (player.role="Guardia")
                        else if (player.role="Massone") */
                        card.appendChild(section);
                        card.appendChild(img);
                        container.appendChild(card);
                    }
                });
            } else if (mess.code=400) {
                //TO DO: fixare notifica inizio partita
                alert("Partita iniziata!\n"+mess.message);
            }
        });
    } else {
        alert("Partita inesistente o scartata per inattività.\nRitorno automatico alla Home.");
        window.history.back();
    }
});


function addMessageToChat(sender, message) {
    //TO DO
}