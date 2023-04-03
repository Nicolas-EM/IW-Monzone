


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

// pinta notifs viejos al cargarse, via AJAX
// pinta notifs de usuario
go(config.rootUrl + "/user/receivedUserNotifs", "GET")
.then(notifs => {
    console.log("USER notifications");
    console.log(notifs);
    let userNotifsDiv = document.getElementById("user-tab-pane");
    notifs.forEach(notif => {
        console.log(notif);
        userNotifsDiv.insertAdjacentHTML("beforeend", renderNotif(notif));
    })
    }
);
// pinta notifs de grupo
go(config.rootUrl + "/user/receivedGroupNotifs", "GET")
.then(notifs => {
    console.log("GROUP notifications");
    console.log(notifs);
    let groupNotifsDiv = document.getElementById("groupNotifs-tab-pane");
    notifs.forEach(notif => {
        console.log(notif);
        groupNotifsDiv.insertAdjacentHTML("beforeend", renderNotif(notif));
    })
    }
);

// y aquí pinta notificaciones según van llegando
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (notif) => {
        console.log("Received notification");
        oldFn(notif); // llama al manejador anterior
        if(notif.type == 'INVITATION'){
            inviteDiv.insertAdjacentHTML("afterbegin", renderNotif(notif));
        }
        else {
            notifsDiv.insertAdjacentHTML("afterbegin", renderNotif(notif));
        }
    }
}
