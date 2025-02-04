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
            mess.message.forEach((player) => {
                //TO DO: aggiungere le cards per i player
            });
        }
    });
});

function addMessageToChat(sender, message) {
    //TO DO
}