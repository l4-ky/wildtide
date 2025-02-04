const URLPrefix="/lupus/";
const username=window.sessionStorage.getItem("username");
const gameName=window.sessionStorage.getItem("gameName");
let socket;
fetch(URLPrefix+"canOpenWebsocket/"+gameName, {
    method: "GET"
})
.then(() => {
    /* teoricamente non ho bisogno di controllare alcun parametro perchè il metodo del Controller risponde solo quando ottiene il permesso di far aprire le Websocket */
    socket=new WebSocket(URLPrefix+gameName+"/"+username);
    socket.addEventListener("open", (event) => {
        console.log("websocket opened.");
    });
    socket.addEventListener("message", (event) => {
        console.log(event.data);
        //logica x messagi in input
        //TO DO: ricavare messaggio;
        let mess;
        if (mess.code==200) {
            addMessageToChat(mess.sender, mess.message);
        } else if (mess.code=300) {
            let container=document.getElementById("playersContainer");
            mess.message.forEach((player) => {
                if (player.name=username) {
                    let selfUsername=document.getElementById("selfUsername");
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
                    /* 
                    if (player.role=="Villico") //TO DO: set immagini
                    else if (player.role=="Lupo")
                    else if (player.role="Veggente")
                    else if (player.role="Mitomane")
                    else if (player.role="Medium")
                    else if (player.role="Guardia")
                    else if (player.role="Massone")
                    */
                    card.appendChild(section);
                    card.appendChild(img);
                    container.appendChild(card);
                }
            });
        }
    });
});

function addMessageToChat(sender, message) {
    //TO DO
}