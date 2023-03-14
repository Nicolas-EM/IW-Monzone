window.addEventListener("load", (event) => {  
    document.getElementById('inviteBtn').addEventListener('click', function(){
        let username = window.prompt("Username","");
        console.log(`Inviting ${username}`);
    });
  });