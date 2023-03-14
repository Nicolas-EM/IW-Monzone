


// cómo pintar 1 notificacion (devuelve html que se puede insertar en un div)
function renderNotif(notif) {
    console.log("rendering: ", notif);
    return `<div class="row my-2">
                <div class="card text-white bg-warning" role="button" tabindex="0">
                    <div class="card-header"><h5>${notif.msg}</h5></div>
                    <div class="card-body">
                        <td class="card-title">Text</td>
                    </div>
                </div>
            </div>`
    // return `<div>${msg.from} @${msg.sent}: ${msg.text}</div>`;
}

// pinta mensajes viejos al cargarse, via AJAX
go(config.rootUrl + "/user/receivedNotifs", "GET")
.then(notifs => {
    let notifsDiv = document.getElementById("notifs-tab-pane");
    let inviteDiv = document.getElementById("invite-tab-pane");
    notifs.forEach(notif => {
        if(notif.type == 'INVITATION'){
            inviteDiv.insertAdjacentHTML("beforeend", renderNotif(notif));
        }
        else {
            notifsDiv.insertAdjacentHTML("beforeend", renderNotif(notif));
        }
    })
    }
);

// y aquí pinta notificaciones según van llegando
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (notif) => {
        oldFn(notif); // llama al manejador anterior
        if(notif.type == 'INVITATION'){
            inviteDiv.insertAdjacentHTML("afterbegin", renderNotif(notif));
        }
        else {
            notifsDiv.insertAdjacentHTML("afterbegin", renderNotif(notif));
        }
    }
}
