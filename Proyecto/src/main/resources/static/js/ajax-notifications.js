


// cómo pintar 1 notificacion (devuelve html que se puede insertar en un div)
function renderNotif(notif) {
    console.log("rendering: ", notif);
    return `<div class="row my-2">
                <div class="card text-white bg-warning" role="button">
                    <div class="card-header"><h5>${notif.message}</h5></div>
                </div>
            </div>`
}

// pinta notifs viejos al cargarse, via AJAX
go(config.rootUrl + "/user/receivedNotifs", "GET")
.then(notifs => {
    let actionNotifsDiv = document.getElementById("action-tab-pane");
    let notifsDiv = document.getElementById("notifs-tab-pane");

    notifs.forEach(notif => {
        if(Object.hasOwn(notif, 'actionEndpoint')){
            actionNotifsDiv.insertAdjacentHTML("beforeend", renderNotif(notif));
        } else {
            notifsDiv.insertAdjacentHTML("beforeend", renderNotif(notif));
        }
    })
    }
);

// y aquí pinta notificaciones según van llegando
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (notif) => {
        console.log("Received notification");
        oldFn(notif); // llama al manejador anterior

        let actionNotifsDiv = document.getElementById("action-tab-pane");
        let notifsDiv = document.getElementById("notifs-tab-pane");

        if(notif.type == 'INVITATION'){
            actionNotifsDiv.insertAdjacentHTML("afterbegin", renderNotif(notif));
        }
        else {
            notifsDiv.insertAdjacentHTML("afterbegin", renderNotif(notif));
        }
    }
}
