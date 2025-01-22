const UrlPrefix='/lockey';
var credentials;
document.addEventListener('DOMContentLoaded', ()=>{
    console.log(sessionStorage.getItem('username')+' - '+sessionStorage.getItem('pin'));
    let getUrl='/'+sessionStorage.getItem('username')+'/get';
    fetch(UrlPrefix+getUrl, {
        method: 'GET',
        headers: {
            'Pin' : sessionStorage.getItem('pin')
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.body.credentials==null) return;
        credentials=data.body.credentials;
        credentials.sort((a, b) => a.name.localeCompare(b.name));
        credentials.forEach((cred)=>{
            let obj=new Credentials(cred.name, cred.topFields, cred.bottomFields, cred.isPinned);
            displayCredentials(obj);
        });
    })
    .catch(error => console.error(error));
    //
    document.getElementById('back').onclick=()=>{
        sessionStorage.removeItem('username');
        sessionStorage.removeItem('pin');
        window.location.href = '/lockey/landing.html';
        sessionStorage.remove('editCred');
    }
    //
    document.getElementById('addBubbleButton').onclick=()=>{
        sessionStorage.setItem('editCred', '');
        window.location.href = '/lockey/edit.html';
    }
    //
    document.getElementById('accountBtn').onclick=()=>{
        let accountUsernameField=document.getElementById('accountUsernameField');
        let accountPinField=document.getElementById('accountPinField');
        accountUsernameField.value=sessionStorage.getItem('username');
        accountPinField.value=sessionStorage.getItem('pin');
        document.getElementById('accountSaveBtn').onclick=()=>{
            if (!accountUsernameField.value.includes(' ') && accountUsernameField.value.trim().length!==0 && accountPinField.value.trim().length!==0 && !(!/^\d+$/.test(accountPinField.value))) {
                fetch(UrlPrefix+'/putProfile', {
                    method: 'PUT',
                    headers: {
                        'OldUsername' : sessionStorage.getItem('username'),
                        'OldPin' : sessionStorage.getItem('pin'),
                        'NewUsername' : accountUsernameField.value,
                        'NewPin' : accountPinField.value
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data) {
                        sessionStorage.setItem('username', accountUsernameField.value);
                        sessionStorage.setItem('pin', accountPinField.value);
                        setTimeout(()=>{
                            window.location.reload();
                        }, 500);
                    } else {
                        alert("Couldn't update your profile info, please retry.\nUsername already taken or invalid.");
                    }
                })
                .catch(error => console.error(error));
            } else {
                alert('Username and pin cannot be empty or whitespace, and cannot contain, start or end with spaces.\nPIN must contain only digits.');
            }
        }
        if (document.getElementById('accountDiv').style.display=='block') {
            document.getElementById('accountDiv').style.display='none';
        } else {
            document.getElementById('accountDiv').style.display='block';
        }
    }
});

function displayCredentials(data) {
    const isPinned=data.isPinned;
    const mainDiv = document.createElement('div');
    mainDiv.className = 'bubble';
    const nameHeader = document.createElement('h3');
    nameHeader.textContent = data.name;
    nameHeader.onclick = (event)=>{
        sessionStorage.setItem('editCred', '');
        for (let i=0; i<credentials.length; i++) {
            let cred=credentials[i];
            if (cred.name==event.currentTarget.textContent) {
                sessionStorage.setItem('editCred', JSON.stringify(cred));
                console.log(sessionStorage.getItem('editCred'));
                break;
            }
        }
        window.location.href = '/lockey/edit.html';
    }
    mainDiv.appendChild(nameHeader);
    document.getElementById(isPinned?'pinnedBody':'normalBody').appendChild(mainDiv);
}
