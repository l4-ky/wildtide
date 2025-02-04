const URLPrefix="/lupus/";
usernameInput=document.getElementById("usernameInput");
currentGamesDiv=document.getElementById("currentGames");
let gameNameThatIHaveCreated;
setInterval(() => {
    fetch(URLPrefix+"openGames", {
        method: "GET"
    })
    .then(response => response.json())
    .then(list => {
        console.log(list);//TO TEST
        list.forEach(game=>{
            //se ho creto io una partita non deve mostrarla tra quelle disponibili
            if (game.name!=gameNameThatIHaveCreated) {
                section=document.createElement("section");
                h3=document.createElement("h3");
                h3.innerHTML=game.name;//TO TEST: ottenere gameName
                h4=document.createElement("h4");
                h4.innerHTML=game.whoCreatedIt;//TO TEST: ottenerlo
                enterBtn=document.createElement("input");
                enterBtn.type="button";
                enterBtn.value="Enter";
                enterBtn.onclick=(event)=>{
                    let gameToEnter=event.currentTarget.previousElementSibling.previousElementSibling.value;
                    let tempUsername=usernameInput.value;
                    fetch(URLPrefix+"testUsername", {
                        method: "POST",
                        headers: {"Username":tempUsername}
                    })
                    .then(response => response.json())
                    .then(isNameOK => {
                        if (isNameOK) {
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
                    });
                };
                section.appendhild(h3);
                section.appendChild(h4);
                section.appendChild(enterBtn);
                currentGamesDiv.appendChild(section);
            }
        });
    });
}, 3000);

let createdGameName;
createGameBtn=document.getElementById("createGameBtn");
createGameBtn.onclick=()=>{
    createdGameName=createGameBtn.previousElementSibling.value;
    if (testUsername(usernameInput.value) && testUsername(createGameBtn)) {
        fetch(URLPrefix+"newGame/"+createdGameName, {
            method: "POST",
            headers: {"Username":usernameInput.value}
        });
        gameNameThatIHaveCreated=usernameInput.value;
        let timeoutId=setTimeout(() => {
            alert("La partita da te creata ("+gameNameThatIHaveCreated+") è stata annullata per inattività.");
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
    fetch(URLPrefix+"startGame/"+createdGameName, {
        method: "POST",
        headers: {"Username":usernameInput.value}
    })
    .then(response => response.json())
    .then(couldStart => {
        if (couldStart) {
            window.sessionStorage.setItem("gameName",createdGameName);
            window.sessionStorage.setItem("username",tempUsername);
            window.location.href="game.html";
        } else {
            alert("Troppi pochi giocatori in coda per la partita, oppure il nome utente non corrisponde a quello del creatore della partita");
        }
    });
};

function testUsername(nameToBeTested) {
    fetch(URLPrefix+"testUsername", {
        method: "POST",
        headers: {"Username":nameToBeTested}
    })
    .then(response => response.json())
    .then(isNameOK => {
        if (isNameOK) return true;
        else return false;
    });
}