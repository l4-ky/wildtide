const UrlPrefix='/lockey';
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById("logInBtn").addEventListener('click', ()=>{
        event.preventDefault();
        try {
            if(checkAndGetFormFields()) {
                fetch(UrlPrefix+'/logIn', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'Username' : document.getElementById("usernameField").value,
                        'Pin' : document.getElementById("pinField").value
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        sessionStorage.setItem('username', document.getElementById("usernameField").value);
                        sessionStorage.setItem('pin', document.getElementById("pinField").value);
                        window.location.href = '/lockey/home.html';
                    } else {
                        alert(data.message);
                    }
                })
                .catch(error => console.error(error));
            }
        } catch (error) {
            alert(error.message);
        }
    });

    document.getElementById('signInBtn').addEventListener('click', ()=>{
        try {
            if(checkAndGetFormFields()) {
                fetch(UrlPrefix+'/signIn', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'Username' : document.getElementById("usernameField").value,
                        'Pin' : document.getElementById("pinField").value
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        sessionStorage.setItem('username', document.getElementById("usernameField").value);
                        sessionStorage.setItem('pin', document.getElementById("pinField").value);
                        window.location.href = '/lockey/home.html';
                    } else {
                        alert(data.message);
                    }
                })
                .catch(error => console.error(error));
            }
        } catch (error) {
            alert(error.message);
        }
    });
});

function checkAndGetFormFields() {
    //add code to check if the form fields are empty/valid
    let username = document.getElementById("usernameField").value;
    if (!username.trim()) {
        throw new Error('Username cannot be empty or whitespace.');
    }
    if (username.includes(' ')) {
        throw new Error('Username cannot contain, start or end with spaces.');
    }
    let pin = document.getElementById("pinField").value;
    if (!/^\d+$/.test(pin)) {
        throw new Error('PIN must contain only digits and cannot be empty.');
    }
    return true;
}