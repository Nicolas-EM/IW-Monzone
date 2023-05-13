const groupsTable = document.getElementById("groupsTable");
const userId = groupsTable.dataset.userid;

function getGroups() {
    go(`${config.rootUrl}/user/getGroups`, "GET")
        .then(groups => {
            Array.from(groups).forEach(group => {
                const elem = document.getElementById(`group-${group.id}`);
                if (elem != null)
                    groupsTable.removeChild(elem);
                const member = group.members.find(member => member.userId === userId);
                if (member)
                    groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, member.balance));
            })
        })
        .catch(e => {
            console.log("Error retrieving group", e);
        })
}

// TODO: Arreglar
function getAllGroups() {
    go(`${config.rootUrl}/admin/getAllGroups`, "GET")
        .then(groups => {
            Array.from(groups).forEach(group => {
                const elem = document.getElementById(`group-${group.id}`);
                if (elem != null)
                    groupsTable.removeChild(elem);
                groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, "-"));
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
            if (elem != null)
                elem.parentElement.removeChild(elem);
            if (obj.action != "GROUP_DELETED") {
                const member = group.members.find(member => member.userId === userId);
                if (member)
                    groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, member.balance));
            }
        }

        else if (obj.type === "EXPENSE") {
            // get groupId from destination
            const expGroupId = parseInt(destination.split("/")[3]);
            const elem = document.getElementById(`group-${expGroupId}`);
            go(`${config.rootUrl}/group/${expGroupId}/getGroupConfig`, "GET")
                .then(group => {
                    elem.parentElement.removeChild(elem);
                    const member = group.members.find(member => member.userId === userId);
                    if (member)
                        groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, member.balance));
                })
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

let btnAdmin = document.getElementById("btn-admin");
if (btnAdmin != null){ // Si no existe al estar en una cuenta no ADMIN
    btnAdmin.onclick = (e) => {
        e.preventDefault();
        // Render all groups
        getAllGroups();
}     
};