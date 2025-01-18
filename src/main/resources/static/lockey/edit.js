const UrlPrefix='/lockey';
var editCred;
var editCredStorage;
document.addEventListener('DOMContentLoaded', ()=>{
    let fieldsDiv=document.getElementById('fieldsDiv');
    editCredStorage=sessionStorage.getItem('editCred');
    let isPinnedBtn=document.getElementById('isPinnedBtn');
    isPinnedBtn.onclick=()=>{
        isPinnedBtn.textContent=isPinnedBtn.textContent==='Pinned'?'Not pinned':'Pinned';
    }
    if (editCredStorage!='') {
        let x=JSON.parse(editCredStorage);
        editCred=new Credentials(x.name, x.topFields, x.bottomFields, x.isPinned);
        document.getElementById('nameField').value=editCred.name;
        isPinnedBtn.textContent=editCred.isPinned?'Pinned':'Not pinned';
        for (let i = 0; i < editCred.topFields.length; i++) {
            let input1=document.createElement('input');
            input1.type='text';
            input1.className='light';
            input1.value=editCred.topFields[i];
            let input2=document.createElement('input');
            input2.type='text';
            input2.className='light';
            input2.value=editCred.bottomFields[i];
            let section1=document.createElement('section');
            section1.appendChild(input1);
            section1.appendChild(input2);
            let button=document.createElement('button');
            button.textContent='-';
            button.classList.add('smallBtn');
            button.classList.add('lightBtn');
            button.onclick=(event)=>{
                fieldsDiv.removeChild(event.currentTarget.parentNode);
            }
            let section2=document.createElement('section');
            section2.classList.add('bubblePiece');
            section2.appendChild(section1);
            section2.appendChild(button);
            fieldsDiv.appendChild(section2);
        }
    }
    let button=document.createElement('button');
    button.textContent='+';
    button.classList.add('smallBtn');
    button.classList.add('lightBtn');
    button.onclick=()=>{
        let input1=document.createElement('input');
        input1.type='text';
        input1.className='light';
        input1.value='';
        let input2=document.createElement('input');
        input2.type='text';
        input2.className='light';
        input2.value='';
        let section1=document.createElement('section');
        section1.appendChild(input1);
        section1.appendChild(input2);
        let button=document.createElement('button');
        button.textContent='-';
        button.classList.add('smallBtn');
        button.classList.add('lightBtn');
        button.onclick=(event)=>{
            fieldsDiv.removeChild(event.currentTarget.parentNode);
        }
        let section2=document.createElement('section');
        section2.classList.add('bubblePiece');
        section2.appendChild(section1);
        section2.appendChild(button);
        fieldsDiv.insertBefore(section2, fieldsDiv.lastElementChild);
    }
    fieldsDiv.appendChild(button);
});
document.addEventListener('DOMContentLoaded', ()=>{
    //save functionality
    document.getElementById('saveBtn').onclick=()=>{
        let oldName;
        if (editCredStorage=='') oldName='';
        else oldName=editCred.name;
        let name=document.getElementById('nameField').value;
        if (name==='' || name.trim().length===0) {
            alert('Name cannot be empty or whitespace.');
            return;
        }
        let fieldPairs=fieldsDiv.children;
        fieldPairs=Array.from(fieldPairs);
        fieldPairs.pop();//the last button is not a field pair
        let topFields=[];
        let bottomFields=[];
        for (let i = 0; i < fieldPairs.length; i++) {
            let k=fieldPairs[i].firstElementChild.children;
            let field1=k[0].value;
            let field2=k[1].value;
            console.log(field1+' - '+field2);
            if ((field1==='' || field1.trim().length===0) && (field2==='' || field2.trim().length===0)) {
                break;
            }
            topFields.push(field1);
            bottomFields.push(field2);
        }
        let pinned=document.getElementById('isPinnedBtn').textContent==='Pinned';
        let cred=new Credentials(name, topFields, bottomFields, pinned);
        //
        fetch(UrlPrefix+'/put', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Username' : sessionStorage.getItem('username'),
                'Pin' : sessionStorage.getItem('pin'),
                'OldName' : oldName
            },
            body: JSON.stringify(cred)
        });
        //
        backToHome();
    }
    //
    document.getElementById('discardBtn').onclick=()=>{
        backToHome();
    }
    //è possibile cancellare una 'Credentials' solo se questa è effettivamente presente, e quindi se 'editCred' (l'oggetto temporaneo nella pagina) è stato inizializzato
    document.getElementById('deleteBtn').onclick=()=>{
        if (editCredStorage=='') {
            alert('This credentials have just been created, cannot be deleted now.\nYou must save it first, then it can be deleted.');
        } else {
            fetch(UrlPrefix+'/delete', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Username' : sessionStorage.getItem('username'),
                    'Pin' : sessionStorage.getItem('pin'),
                    'Name' : editCred.name
                }
            });
            backToHome();
        }
    }
});

function backToHome() {
    sessionStorage.removeItem('editCred');
    window.location.href = '/lockey/home.html';
}