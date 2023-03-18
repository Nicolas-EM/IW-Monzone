// envio de invitiaciones con AJAX
let b = document.getElementById("confirmInviteBtn");
b.onclick = (e) => {
    e.preventDefault();
    // th:formaction="@{/group/{action}(action=${group?.getId()} + '/inviteMember')}"
    go(b.parentNode.action, 'POST', {
            username: document.getElementById("inviteUsername").value
        })
        .then(d => console.log("happy", d))
        .catch(e => console.log("sad", e))
}