const URLPrefix="/lupus/";
usernameInput=document.getElementById("usernameInput");
currentGamesDiv=document.getElementById("currentGames");
participantsNumP=document.getElementById("participantsNum");
let gameNameThatIHaveCreated;
let usernameWithWhichIHaveCreatedANewGame;
setInterval(() => {
    fetch(URLPrefix+"openGames", {
        method: "GET"
    })
    .then(response => response.json())
    .then(list => {
        if (list.length==0) {
            currentGamesDiv.innerHTML="no existing games";
            console.log("no existing games");
        } else {
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
                    enterBtn.addEventListener("click",async(event)=>{
                        let gameToEnter=event.target.previousElementSibling.previousElementSibling.innerHTML;
                        let tempUsername=usernameInput.value;
                        if (await testUsername(gameToEnter, tempUsername)) {
                            fetch(URLPrefix+"enterGame", {
                                method: "POST",
                                headers: {
                                    "GameName":gameToEnter,
                                    "Username":tempUsername
                                }
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
                    });
                    section.appendChild(h3);
                    section.appendChild(h4);
                    section.appendChild(enterBtn);
                    currentGamesDiv.appendChild(section);
                } else {
                    participantsNumP.innerHTML=game.namePlayersList.length+" players joined!";
                }
            });
        }
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
        participantsNumP.style.visibility="visible";  
        fetch(URLPrefix+"newGame", {
            method: "POST",
            headers: {
                "GameName":gameNameThatIHaveCreated,
                "Username":usernameWithWhichIHaveCreatedANewGame
            }
        });
        //
        let createdGameTimeout=setTimeout(() => {
            alert("La partita da te creata ("+gameNameThatIHaveCreated+") e' stata annullata per inattivita'.");
            window.location.reload();
        },350000);//poco meno di 7 minuti
        window.addEventListener('beforeunload', function(event) {
            this.clearTimeout(createdGameTimeout);
        });
    } else {
        alert("Il nome giocatore o partita scelto non è accettabile.Riprova.\n(non può essere vuoto o contentere spazi, ammette solo lettere, cifre o _)");
    }
};
startGameBtn=document.getElementById("startGameBtn");
startGameBtn.onclick=()=>{
    fetch(URLPrefix+"startGame", {
        method: "POST",
        headers: {
            "GameName":gameNameThatIHaveCreated,
            "Username":usernameWithWhichIHaveCreatedANewGame
        }
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
    participantsNumP.style.visibility="hidden";
    fetch(URLPrefix+"discardGame", {
        method: "DELETE",
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
    //'gameName' usato anche come 'exists' nel contesto di creazione della partita
    if (typeof gameName === 'boolean') {
        return fetch(URLPrefix+"testUsername", {
            method: "POST",
            headers: {
                "GameName":"",
                "Username":nameToBeTested,
                "Exists":gameName
            }
        })
        .then(response => response.json())
        .then(isNameOK => {
            return isNameOK;
        });
    } else {
        return fetch(URLPrefix+"testUsername", {
            method: "POST",
            headers: {
                "GameName":gameName,
                "Username":nameToBeTested
            }
        })
        .then(response => response.json())
        .then(isNameOK => {
            return isNameOK;
        });
    }
}
