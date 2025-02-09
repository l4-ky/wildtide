const URLPrefix="/lupus/";
usernameInput=document.getElementById("usernameInput");
currentGamesDiv=document.getElementById("currentGames");
let gameNameThatIHaveCreated;
let usernameWithWhichIHaveCreatedANewGame;
setInterval(() => {
    fetch(URLPrefix+"openGames", {
        method: "GET"
    })
    .then(response => response.json())
    .then(list => {
        if (list.length==0) console.log("no existing games...");//DEBUG
        currentGamesDiv.replaceChildren();
        list.forEach(game=>{
            //se ho creato io una partita non deve mostrarla tra quelle disponibili
            console.log(game);
            //svuoto il container dei Games
            //visualizzo i vari Game
            if (game.gameName!=gameNameThatIHaveCreated) {
                section=document.createElement("section");
                h3=document.createElement("h3");
                h3.innerHTML=game.gameName;
                h4=document.createElement("h4");
                h4.innerHTML=game.whoCreated;
                enterBtn=document.createElement("input");
                enterBtn.type="button";
                enterBtn.value="Enter";
                enterBtn.onclick=(event)=>{
                    let gameToEnter=event.currentTarget.previousElementSibling.previousElementSibling.value;
                    let tempUsername=usernameInput.value;
                    if (testUsername(gameToEnter, tempUsername)) {
                        fetch(URLPrefix+"enterGame/"+gameToEnter, {
                            method: "POST",
                            headers: {"Username":tempUsername}
                        })
                        .then(response => response.json())
                        .then(couldEnter => {
                            if (couldEnter) {
                                window.sessionStorage.setItem("gameName",gameToEnter);
                                window.sessionStorage.setItem("username",tempUsername);
                                window.location.href="game.html";
                            }
                        });
                    } else {
                        alert("Il nome giocatore scelto non è accettabile.Riprova.\n(non può essere vuoto o contentere spazi, ammette solo lettere, cifre o _)");
                    }
                };
                section.appendChild(h3);
                section.appendChild(h4);
                section.appendChild(enterBtn);
                currentGamesDiv.appendChild(section);
            }
        });
    }) ;
}, 3000);

createGameBtn=document.getElementById("createGameBtn");
gameNameInput=document.getElementById("gameNameInput");
deleteGameBtn=document.getElementById("deleteGameBtn");
createGameBtn.onclick= async ()=>{
    gameNameThatIHaveCreated=gameNameInput.value;
    usernameWithWhichIHaveCreatedANewGame=usernameInput.value;
    if (await testUsername(false, usernameWithWhichIHaveCreatedANewGame)) {
        createGameBtn.disabled=true;
        startGameBtn.disabled=false;
        usernameInput.disabled=true;
        gameNameInput.disabled=true;
        deleteGameBtn.disabled=false;
        fetch(URLPrefix+"newGame", {
            method: "POST",
            headers: {
                "GameName":gameNameThatIHaveCreated,
                "Username":usernameWithWhichIHaveCreatedANewGame
            }
        });
        let timeoutId=setTimeout(() => {
            alert("La partita da te creata ("+gameNameThatIHaveCreated+") e' stata annullata per inattivita'.");
            window.location.reload();
        },60000);
        window.addEventListener('beforeunload', function(event) {
            this.clearTimeout(timeoutId);
        });
    } else {
        alert("Il nome giocatore o partita scelto non è accettabile.Riprova.\n(non può essere vuoto o contentere spazi, ammette solo lettere, cifre o _)");
    }
};
startGameBtn=document.getElementById("startGameBtn");
startGameBtn.onclick=()=>{
    fetch(URLPrefix+"startGame/"+gameNameThatIHaveCreated, {
        method: "POST",
        headers: {"Username":usernameWithWhichIHaveCreatedANewGame}
    })
    .then(response => response.json())
    .then(couldStart => {
        if (couldStart) {
            window.sessionStorage.setItem("gameName",gameNameThatIHaveCreated);
            window.sessionStorage.setItem("username",tempUsername);
            window.location.href="game.html";
        } else {
            alert("Troppi pochi giocatori in coda per la partita, oppure il nome utente non corrisponde a quello del creatore della partita");
        }
    });
};
deleteGameBtn.onclick=()=>{
    createGameBtn.disabled=false;
    startGameBtn.disabled=true;
    deleteGameBtn.disabled=true;
    gameNameInput.disabled=true;
    fetch(URLPrefix+"discardGame", {
        method: "PUT",
        headers: {
            "GameName":gameNameThatIHaveCreated,
            "Username":usernameWithWhichIHaveCreatedANewGame
        }
    })
    .then(response => response.json())
    .then(hasDiscardedOfGame => {
        if (hasDiscardedOfGame) {
            window.location.reload();
        } else {
            alert("Couldn't delete this game.");
        }
    });
};

async function testUsername(gameName, nameToBeTested) {
    return fetch(URLPrefix+"testUsername", {
        method: "POST",
        headers: {
            "GameName":gameName,
            "Username":nameToBeTested,
            "Exists":gameName
        }
    })
    .then(response => response.json())
    .then(isNameOK => {
        return isNameOK;
    });
}
