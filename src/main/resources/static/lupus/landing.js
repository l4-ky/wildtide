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
        if (list.length==0) console.log("no existing games...");//DEBUG
        list.forEach(game=>{
            //se ho creto io una partita non deve mostrarla tra quelle disponibili
            console.log(game);
            /* if (game.name!=gameNameThatIHaveCreated) {
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
                section.appendhild(h3);
                section.appendChild(h4);
                section.appendChild(enterBtn);
                currentGamesDiv.appendChild(section);
            } */
        });
    }) ;
}, 3000);

let createdGameName;
createGameBtn=document.getElementById("createGameBtn");
createGameBtn.onclick= async ()=>{
    createdGameName=createGameBtn.previousElementSibling.value;
    let x=await testUsername(false, usernameInput.value);
    console.log(x);
    if (x) {
        fetch(URLPrefix+"newGame/", {
            method: "POST",
            headers: {
                "GameName":createdGameName,
                "Username":usernameInput.value
            }
        });
        let timeoutId=setTimeout(() => {
            alert("La partita da te creata ("+createdGameName+") è stata annullata per inattività.");
            window.location.reload();
        },60000);
        window.addEventListener('beforeunload', function(event) {
            this.clearTimeout(timeoutId);
        });
    } else {
        console.log("nope");
        //TO BE SHOWN
        //alert("Il nome giocatore o partita scelto non è accettabile.Riprova.\n(non può essere vuoto o contentere spazi, ammette solo lettere, cifre o _)");
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
        console.log(isNameOK);
        return isNameOK;
    });
}
