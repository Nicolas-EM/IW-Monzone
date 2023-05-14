function getTimeAgo(dateString) {
    const date = new Date(dateString);

    const millisecondsPerMinute = 60000;
    const millisecondsPerHour = 3600000;
    const millisecondsPerDay = 86400000;
    const millisecondsPerWeek = 604800000;
    const millisecondsPerMonth = 2592000000;
    const millisecondsPerYear = 31536000000;

    const currentDate = new Date();

    // Calculate the difference in milliseconds between the current date and the given date
    const differenceInMilliseconds = currentDate - date;

    if (differenceInMilliseconds < 2 * millisecondsPerMinute) {
        return 'Just now';  // Less than 2 minutes ago
    } else if (differenceInMilliseconds < millisecondsPerHour) {
        const minutes = Math.floor(differenceInMilliseconds / millisecondsPerMinute);
        return minutes + ' minutes ago';
    } else if (differenceInMilliseconds < millisecondsPerDay) {
        const hours = Math.floor(differenceInMilliseconds / millisecondsPerHour);
        return hours === 1 ? '1 hour ago' : hours + ' hours ago';
    } else if (differenceInMilliseconds < millisecondsPerWeek) {
        const days = Math.floor(differenceInMilliseconds / millisecondsPerDay);
        return days === 1 ? '1 day ago' : days + ' days ago';
    } else if (differenceInMilliseconds < millisecondsPerMonth) {
        const weeks = Math.floor(differenceInMilliseconds / millisecondsPerWeek);
        return weeks === 1 ? '1 week ago' : weeks + ' weeks ago';
    } else if (differenceInMilliseconds < millisecondsPerYear) {
        const months = Math.floor(differenceInMilliseconds / millisecondsPerMonth);
        return months === 1 ? '1 month ago' : months + ' months ago';
    } else {
        const years = Math.floor(differenceInMilliseconds / millisecondsPerYear);
        return years === 1 ? '1 year ago' : years + ' years ago';
    }
}

function renderReadNotif(notif) {
    return `<div id="notif-${notif.id}" class="row my-2">
                <div class="card text-white" role="button">
                    <div class="card-body">
                        <div class="row">
                            <h5>${notif.message}</h5>
                        </div>
                        <div class="row">
                            <p>${notif.dateSent.substring(0, 10)} (${getTimeAgo(notif.dateSent)})</p>
                        </div>
                        <div class="row mt-2">
                            <div class="col invisible">
                            </div>
                            <form class="col" method="post" action="/user/${notif.id}/delete">
                                <button class="btn btn-delete btn-primary rounded-pill fw-bold" onclick="deleteNotif(event, this, ${notif.id})">Delete</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>`
}

function renderUnreadNotif(notif) {
    return `<div id="notif-${notif.id}" class="row my-2">
                <div class="card text-white" role="button">
                    <div class="card-body">
                        <div class="row">
                            <h5>${notif.message}</h5>
                        </div>
                        <div class="row">
                            <p>${notif.dateSent.substring(0, 10)} (${getTimeAgo(notif.dateSent)})</p>
                        </div>
                        <div class="row mt-2">
                            <form id="notifReadBtn-${notif.id}" class="col" method="post" action="/user/${notif.id}/read">
                                <button class="btn btn-func btn-primary rounded-pill fw-bold" onclick="markNotifRead(event, this, ${notif.id})" type="submit">Mark Read</button>
                            </form>
                            <form class="col" method="post" action="/user/${notif.id}/delete">
                                <button class="btn btn-delete btn-primary rounded-pill fw-bold" onclick="deleteNotif(event, this, ${notif.id})">Delete</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>`
}

function renderInvitation(notif) {
    return `<div id="notif-${notif.id}" class="row my-2">
                <div class="card text-white" role="button">
                    <div class="card-body">
                        <div class="row">
                            <h5>${notif.message}</h5>
                        </div>
                        <div class="row">
                            <p>${notif.dateSent.substring(0, 10)} (${getTimeAgo(notif.dateSent)})</p>
                        </div>
                        <div class="row mt-2">
                            <form id="notifReadBtn-${notif.id}" class="col" method="post" action="/group/${notif.idGroup}/acceptInvite">
                            <button class="btn btn-func btn-primary rounded-pill fw-bold" onclick="acceptInvite(event, this, ${notif.id})">Accept</button>
                            </form>
                            <form class="col" method="post" action="/user/${notif.id}/delete">
                                <button class="btn btn-delete btn-primary rounded-pill fw-bold" onclick="deleteNotif(event, this, ${notif.id})">Delete</button>
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
            if (notif.type == "GROUP_INVITATION") {
                actionNotifsDiv.insertAdjacentHTML("beforeend", renderInvitation(notif));
            } else {
                if (notif.dateRead === "") {
                    notifsDiv.insertAdjacentHTML("beforeend", renderUnreadNotif(notif));
                }
                else {
                    notifsDiv.insertAdjacentHTML("beforeend", renderReadNotif(notif));
                }
            }
        })
    }
    );

// y aquí pinta notificaciones según van llegando
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        if (obj.type == "NOTIFICATION") {
            console.log("Received notification");
            let p = document.querySelector("#nav-unread");
            if (p) {
                p.textContent = +p.textContent + 1;
            }

            let notif = obj.notification;
            let actionNotifsDiv = document.getElementById("actionNotifs-tab-pane");
            let notifsDiv = document.getElementById("notifs-tab-pane");

            if (notif.type == 'GROUP_INVITATION') {
                actionNotifsDiv.insertAdjacentHTML("afterbegin", renderInvitation(notif));
            }
            else {
                notifsDiv.insertAdjacentHTML("afterbegin", renderUnreadNotif(notif));
            }

            createToastNotification(notif.id, notif.message);
        }
    }
}

// Accept Invite Btn
function acceptInvite(event, btn, notifId) {
    event.preventDefault();
    go(btn.parentNode.action, 'POST', {})
        .then(d => {
            console.log("Invite accepted", d.status);
            ws.subscribe(`/topic/group/${d.id}`);
            deleteClientNotif(notifId);
            createToastNotification(notifId, "Invitation Accepted");
            document.getElementById('offcanvasNav').hide();
        })
        .catch(e => console.log("sad", e))
}

function markNotifRead(event, btn, notifId) {
    event.preventDefault();
    go(btn.parentNode.action, 'POST', {})
        .then(d => {
            document.getElementById(`notifReadBtn-${notifId}`).classList.add('invisible');

            let p = document.querySelector("#nav-unread");
            if (p) {
                p.textContent = +p.textContent - 1;
            }
        })
        .catch(e => console.log("Failed to mark notif as read", e))
}

// Delete notif client side
function deleteClientNotif(notifId) {
    const notifDiv = document.getElementById(`notif-${notifId}`);
    notifDiv.parentElement.removeChild(notifDiv);

    let p = document.querySelector("#nav-unread");
    if (p) {
        p.textContent = +p.textContent - 1;
    }
}

// Delete notif server side
function deleteNotif(event, btn, notifId) {
    event.preventDefault();
    go(btn.parentNode.action, 'POST', {})
        .then(d => {
            deleteClientNotif(notifId);
            createToastNotification(notifId, "Notification Deleted");
        })
        .catch(e => console.log("sad", e))
}

function createToastNotification(notifId, body, error = false) {
    if (error) {
        document.getElementById('toastNotifBar').insertAdjacentHTML("afterbegin", renderToastError(notifId, body));
    } else {
        document.getElementById('toastNotifBar').insertAdjacentHTML("afterbegin", renderToastNofif(notifId, body));
    }

    const toastNotif = bootstrap.Toast.getOrCreateInstance(document.getElementById(`toast-${notifId}`));
    toastNotif.show();
}

function renderToastNofif(notifId, body) {
    return `<div id="toast-${notifId}" class="toast bg-dark" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header bg-dark">
                    <img src="/img/icon.png" class="rounded me-2" alt="Monzone Icon" width="auto" height="50">
                    <strong class="me-auto text-light">Monzone</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body text-light">
                    ${body}
                </div>
            </div>`
}

function renderToastError(notifId, body) {
    return `<div id="toast-${notifId}" class="toast bg-danger" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header bg-danger">
                    <img src="/img/icon.png" class="rounded me-2" alt="Monzone Icon" width="auto" height="50">
                    <strong class="me-auto text-light">Monzone</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body text-light">
                    ${body}
                </div>
            </div>`
}