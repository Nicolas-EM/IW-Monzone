
const infoGroup = document.getElementById("info-group");
const numMembers = document.getElementById("numMembers");
const groupId = infoGroup.dataset.groupid;
const budget = infoGroup.dataset.budget;
const isGroupAdmin = infoGroup.dataset.isgroupadmin;
const currencies = infoGroup.dataset.currencies;

// envio de invitaciones con AJAX
let b = document.getElementById("confirmInviteBtn");
if (b != null) {  // NO para /group/new
    b.onclick = (e) => {
        e.preventDefault();
        const username = document.getElementById("inviteUsername").value;
        go(b.parentNode.action, 'POST', {
            username
        })
            .then(d => {
                console.log("happy", d);
                createToastNotification(`invite-${groupId}-${username}`, `Invite sent to ${username}`);
            })
            .catch(e => {
                console.log("sad", e);
                alert(JSON.parse(e.text).message);
            })
    }
}

let modal = document.getElementById('inviteModal');
if (modal != null) { // NO para /group/new
    modal.addEventListener('hidden.bs.modal', event => {
        document.getElementById("inviteUsername").value = "";
    })
}
  

// Render current info group
if (groupId) {
    go(`${config.rootUrl}/group/${groupId}/getGroupConfig`, "GET")
        .then(group => {
            renderGroupData(group);
            renderGroupMembers(group);
        }
        );
}

function renderGroupData(group) {
    const groupName = document.getElementById('name');
    groupName.innerHTML = group.name;

    const groupDesc = document.getElementById('desc');
    groupDesc.innerHTML = group.desc;

    console.log(`Getting curr ${group.currency}`)
    const groupCurr = document.getElementById(`${group.currency}`);
    groupCurr.setAttribute('selected', true);

    const groupBudget = document.getElementById('totalBudget');
    groupBudget.setAttribute('value', group.totBudget);

}

function renderGroupMembers(group) {

    numMembers.innerHTML = group.numMembers;

    const membersTable = document.getElementById('membersTable');

    go(`${config.rootUrl}/group/${groupId}/getMembers`, "GET")
        .then(members => {
            membersTable.innerHTML = '';
            Array.from(members).forEach(member => {
                if (member.enabled)
                    membersTable.insertAdjacentHTML("beforeend", renderMember(member, group));
            })

            // Delete Member button function
            Array.from(document.getElementsByClassName('delMemberBtn')).forEach(btn => {
                btn.onclick = (e) => {
                    e.preventDefault();
                    go(btn.parentNode.action, 'POST', {
                        removeId: btn.parentNode.querySelector('input[name="removeId"]').value
                    })
                        .then(d => {
                            console.log("Removed member");
                            if (d.action == "redirect") {
                                console.log("Redirecting to ", d.redirect);
                                window.location.replace(d.redirect);
                            } else {
                                renderGroupMembers(group);
                            }
                        })
                        .catch(e => {
                            console.log("Failed to remove member", e);
                            alert(JSON.parse(e.text).message);
                        })
                }
            })
        })
}

function renderMember(member, group) {
    const truncatedAmount = Number(member.balance).toFixed(2);
    let memberHTML = `<div class="row p-2 member">`;
    if (isGroupAdmin === 'true') {
        memberHTML = `${memberHTML}
                    <div class="col btn-remove">
                    <form method="post" action="/group/${groupId}/delMember">
                        <input type="hidden" name="removeId" value="${member.idUser}">
                        <input type="hidden" name="_csrf" value="${config.csrf.value}">
                        <button type="submit" class="btn delMemberBtn">
                            <svg id="trash" xmlns="http://www.w3.org/2000/svg" width="25" fill="white" class="bi bi-trash3" viewbox="0 0 16 16">
                                <path d="M6.5 1h3a.5.5 0 0 1 .5.5v1H6v-1a.5.5 0 0 1 .5-.5ZM11 2.5v-1A1.5 1.5 0 0 0 9.5 0h-3A1.5 1.5 0 0 0 5 1.5v1H2.506a.58.58 0 0 0-.01 0H1.5a.5.5 0 0 0 0 1h.538l.853 10.66A2 2 0 0 0 4.885 16h6.23a2 2 0 0 0 1.994-1.84l.853-10.66h.538a.5.5 0 0 0 0-1h-.995a.59.59 0 0 0-.01 0H11Zm1.958 1-.846 10.58a1 1 0 0 1-.997.92h-6.23a1 1 0 0 1-.997-.92L3.042 3.5h9.916Zm-7.487 1a.5.5 0 0 1 .528.47l.5 8.5a.5.5 0 0 1-.998.06L5 5.03a.5.5 0 0 1 .47-.53Zm5.058 0a.5.5 0 0 1 .47.53l-.5 8.5a.5.5 0 1 1-.998-.06l.5-8.5a.5.5 0 0 1 .528-.47ZM8 4.5a.5.5 0 0 1 .5.5v8.5a.5.5 0 0 1-1 0V5a.5.5 0 0 1 .5-.5Z" />
                            </svg>
                        </button>
                    </form>
                </div>`;
    }
    memberHTML = `${memberHTML}
                <div id="name-col" class="col d-flex align-items-center border-end border-light-subtle">
                    ${member.username}
                </div>
                <div class="col d-flex align-items-center ps-4">
                    Budget: ${member.budget}
                </div>
                <div id="indicator">
                    <span class="dot" style="${truncatedAmount} >= 0 ? 'background: green' : 'background: red'"></span>
                </div>
                <div class="balance col d-flex align-items-center">
                    ${truncatedAmount} ${group.currencyString}
                </div>
            </div>`;
    return memberHTML;
}

// Submit Button (SAVE)
document.getElementById("btn-save").onclick = (e) => {
    e.preventDefault();
    console.log('Saving group');
    const b = document.getElementById("btn-save");
    const name = document.getElementById("name").value;
    const desc = document.getElementById("desc").value;
    const currId = document.querySelector('[name="currId"]').value;
    const budget = document.getElementById("budget").value;

    go(b.getAttribute('formaction'), 'POST', {
        name,
        desc,
        budget,
        currId
    })
        .then(d => {
            console.log("Group: success", d);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => {
            console.log("Error creating/updating group", e);
            alert(JSON.parse(e.text).message);
        })
};

// Submit Button (DELETE GROUP)
// NOT for /group/new
if (document.getElementById("btn-delete") != null) {
    document.getElementById("btn-delete").onclick = (e) => {
        e.preventDefault();
        console.log('Deleting group');
        const b = document.getElementById("btn-delete");

        go(b.getAttribute('formaction'), 'POST')
            .then(d => {
                console.log("Group: success", d);
                if (d.action === "redirect") {
                    console.log("Redirecting to ", d.redirect);
                    window.location.replace(d.redirect);
                }
            })
            .catch(e => {
                console.log("Error deleting group", e);
                alert(JSON.parse(e.text).message);
            })
    };
}

// Submit Button (LEAVE GROUP)
if (document.getElementById("btn-delete") != null) {
    document.getElementById("btn-leave").onclick = (e) => {
        e.preventDefault();
        console.log('Leaving group');
        const b = document.getElementById("btn-leave");
        const removeId = b.dataset.userid;

        go(b.getAttribute('formaction'), 'POST', {
            removeId
        })
            .then(d => {
                console.log("Group: success", d);
                if (d.action === "redirect") {
                    console.log("Redirecting to ", d.redirect);
                    window.location.replace(d.redirect);
                }
                else {
                    console.log("Error leaving group");
                }
            })
            .catch(e => {
                console.log("Error leaving group", e);
                alert(JSON.parse(e.text).message);
            })
    };
}

// Render INCOMING updates of group
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        // If receiving a group and on that group
        if (obj.type == "GROUP" && obj.group.id == groupId) {
            console.log("Updating group view");
            switch (obj.action) {
                case "GROUP_CREATED":
                    break;
                case "GROUP_MODIFIED":
                    renderGroupData(obj.group);
                    renderGroupMembers(obj.group);
                    break;
                case "GROUP_DELETED":
                    console.log("Redirecting to ", "/user/");
                    window.location.replace("/user/");
                    break;
                case "GROUP_INVITATION_ACCEPTED":
                    renderGroupMembers(obj.group);
                    break;
                case "GROUP_MEMBER_REMOVED":
                    renderGroupMembers(obj.group);
                    break;
                default:
                    break;
            }
        }

        // if receiving an expense
        else if (obj.type == "EXPENSE") {
            // get groupId from destination
            const expGroupId = parseInt(destination.split("/")[3]);
            // if on that group
            if (expGroupId == groupId) {
                go(`${config.rootUrl}/group/${expGroupId}/getGroupConfig`, "GET")
                    .then(group => {
                        console.log("Updating group view");
                        renderGroupMembers(group);
                    })
            }
        }
    }
}

document.getElementById('budget').addEventListener('input', function() {
    const value = this.value.replace(/[^\d.]/g, ''); // remove any non-digit or non-decimal point characters
    const decimalIndex = value.indexOf('.');
    if (decimalIndex !== -1) {
      const decimalPlaces = value.length - decimalIndex - 1;
      if (decimalPlaces > 2) {
        this.value = value.substring(0, decimalIndex + 3); // truncate to 2 decimal places
        return;
      }
    }
    this.value = value;
  });