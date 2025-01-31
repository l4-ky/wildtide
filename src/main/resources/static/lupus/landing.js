currentGamesDiv=document.getElementById("currentGames");
fetch("getGames", {
    method: "GET"
})
.then(/*TO DO: ottenere la lista dei Games aperti*/)
.then(list => {
    list.forEach(game=>{
        section=document.createElement("section");
        h3=document.createElement("h3");
        h3.innerHTML=game.name;//ottenere gameName
        h4=document.createElement("h4");
        h4.innerHTML=game.whoCreatedIt;//same, ottenerlo
        enterBtn=document.createElement("input");
        enterBtn.type="button";
        enterBtn.value="Enter";
        enterBtn.onclick=()=>{
            //prendere nome, etc
            //richiesta per entrare
            //cambio pagina
        };
        section.appendhild(h3);
        section.appendChild(h4);
        section.appendChild(enterBtn);
    });
});

usernameInput=document.getElementById("usernameInput");

createGameBtn=document.getElementById("createGameBtn");
createGameBtn.onclick=()=>{
    var createdGameName=createGameBtn.previousElementSibling.value;
    //controllo se username è giusto (usernameInput)
    //richiesta per creare Game
};
startGameBtn=document.getElementById("startGameBtn");
startGameBtn.onclick=()=>{
    //richiesta per startare Game, usando createdGameName
};
