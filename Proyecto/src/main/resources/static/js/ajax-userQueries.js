// envio de mensajes con AJAX
let b = document.getElementById("cur2");

b.onchange = (e) => {
    e.preventDefault();
    // go(config.rootUrl + "/user/" , 'GET', {
    //         curValue2: document.getElementById("curValue2").value
    //     })
    //     .then(d => console.log("happy", d))
    //     .catch(e => console.log("sad", e))
    // document.getElementsByClassName('curr').forEach(e => e.value = "sad");
}

// // cómo pintar 1 notificacion (devuelve html que se puede insertar en un div)
// function renderNotif(notif) {
//     console.log("rendering: ", notif);
//     return `<div class="row my-2">
//                 <div class="card text-white bg-warning" role="button" tabindex="0">
//                     <div class="card-header"><h5>${notif.msg}</h5></div>
//                     <div class="card-body">
//                         <td class="card-title">Text</td>
//                     </div>
//                 </div>
//             </div>`
//     // return `<div>${msg.from} @${msg.sent}: ${msg.text}</div>`;
// }

// // pinta mensajes viejos al cargarse, via AJAX
go(config.rootUrl + "/user/config", "GET")
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
)
.catch(console.log('Error :('));