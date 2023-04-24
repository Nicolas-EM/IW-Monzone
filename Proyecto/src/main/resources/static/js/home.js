const groupsTable = document.getElementById("groupsTable");

function getBalance(groupId) {
    go(`${config.rootUrl}/group/${groupId}/getBalance`, "GET")
        .then(balance => {
            return balance;
        })
}

function getGroups() {
    go(`${config.rootUrl}/user/getGroups`, "GET")
        .then(groups => {
            Array.from(groups).forEach(group => {
                const elem = document.getElementById(`group-${group.id}`);
                if (elem != null)
                        groupsTable.removeChild(elem);
                const balance = getBalance(group.id);
                groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, balance));
            })
        })
        .catch(e => {
            console.log("Error retrieving group", e);
        })
}

// Render current groups
getGroups();

// Render INCOMING changes
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        if (obj.type == "GROUP") {
            const group = obj.group;
            const elem = document.getElementById(`group-${group.id}`);
            switch (obj.action) {
                case "GROUP_INVITATION_ACCEPTED": {
                    console.log(group.id, `group-${group.id}`, document.getElementById(`group-${group.id}`));
                    if (elem === null) {
                        groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, 0.0));
                        ws.subscribe(`/topic/group/${group.id}`);
                    }
                    else {
                        const balance = getBalance(group.id);
                        elem.parentElement.removeChild(elem);
                        groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, balance));
                    }
                } break;
                case "GROUP_MEMBER_REMOVED": {
                    go(`${config.rootUrl}/group/${groupId}/isMember`, "GET")
                        .then(isMember => {
                            elem.parentElement.removeChild(elem);
                            if (isMember) {
                                const balance = getBalance(group.id);
                                groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, balance));
                            }
                        })                    
                } break;
                case "GROUP_MODIFIED": {
                    elem.parentElement.removeChild(elem);
                    const balance = getBalance(group.id);
                    groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, balance));
                } break;
                case "GROUP_DELETED": {
                    elem.parentElement.removeChild(elem);
                } break;
            }
        }
    }
}

// Render group
function renderGroup(group, balance) {
    return `<div id="group-${group.id}" class="col">
                <div class="card text-white" role="button" onclick="location.href='/group/${group.id}'" tabindex="0">
                    <div class="card-header">
                        <h5>${group.name}</h5>
                    </div>
                    <div class="card-body">
                        <div class="row height">
                            <div>${group.desc}</div>
                        </div>
                        <div class="row">
                            <div class="balance col ms-3">${balance} ${group.currencyString} </div>
                            <div class="col me-3 col-num-members">
                                <div class="icon">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" class="bi bi-person-fill" viewBox="0 0 16 16">
                                        <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3Zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                                    </svg>
                                </div>
                                <div class="num-members">${group.numMembers}</div>
                        </div>
                        </div>
                    </div>
                </div>
            </div>`
}