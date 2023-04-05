// cómo pintar 1 notificacion (devuelve html que se puede insertar en un div)
function renderNotif(notif) {
    console.log("rendering: ", notif);
    return `<div class="row my-2">
                <form method="post" th:action="${notif.action}">
                    <div class="card text-white bg-warning" role="button">
                        <div class="card-body"><h5>${notif.message}</h5></div>
                    </div>
                </form>
            </div>`
}

// cómo pintar 1 notificacion (devuelve html que se puede insertar en un div)
function renderInvite(invite) {
    console.log("rendering: ", invite);
    return `<div class="row my-2">
                <div class="card text-white bg-warning" role="button">
                    <div class="card-body">
                        <div class="row">
                            <h5>${invite.message}</h5>
                        </div>
                        <div class="row">
                            <form class="col" method="post" action="${invite.actionEndpoint}">
                                <button onclick="acceptInvite(this)">Accept</button>
                            </form>
                            <form class="col" method="post">
                                <button type="submit">Delete</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>`
}

// pinta notifs viejos al cargarse, via AJAX
go(config.rootUrl + "/user/receivedNotifs", "GET")
.then(notifs => {
    let actionNotifsDiv = document.getElementById("actionNotifs-tab-pane");
    let notifsDiv = document.getElementById("notifs-tab-pane");

    notifs.forEach(notif => {
        if(notif.type == "GROUP_INVITATION"){
            actionNotifsDiv.insertAdjacentHTML("beforeend", renderInvite(notif));
        } else {
            notifsDiv.insertAdjacentHTML("beforeend", renderNotif(notif));
        }
    })
    }
);

// y aquí pinta notificaciones según van llegando
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (obj) => {
        oldFn(obj); // llama al manejador anterior

        if(obj.type == "NOTIFICATION"){
            console.log("Received notification");
            let p = document.querySelector("#nav-unread");
            if (p) {
                p.textContent = +p.textContent + 1;
            }
            
            let notif = obj.notification;
            let actionNotifsDiv = document.getElementById("actionNotifs-tab-pane");
            let notifsDiv = document.getElementById("notifs-tab-pane");

            if(notif.type == 'GROUP_INVITATION'){
                actionNotifsDiv.insertAdjacentHTML("afterbegin", renderInvite(notif));
            }
            else {
                notifsDiv.insertAdjacentHTML("afterbegin", renderNotif(notif));
            }
        }
    }
}

// Accept Invite Btn
function acceptInvite(btn) {
    go(btn.parentNode.action, 'POST', {})
    .then(d => console.log("happy", d))
    .catch(e => console.log("sad", e))
}