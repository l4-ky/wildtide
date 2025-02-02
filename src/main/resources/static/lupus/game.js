const URLPrefix="/lupus/";
const username=window.sessionStorage.getItem("username");
const gameName=window.sessionStorage.getItem("gameName");
const socket=new WebSocket(URLPrefix+gameName+"/"+username);
socket.addEventListener("open", (event) => {
    console.log("websocket opened.")
});
socket.addEventListener("message", (event) => {
    console.log(event.data);
    //logica x messagi in input
});