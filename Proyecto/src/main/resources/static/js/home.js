const groupsTable = document.getElementById("home_groups");

// Render INCOMING Groups
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        if (obj.type == "GROUP" && obj.action == "GROUP_INVITATION_ACCEPTED") {
            const group = obj.group;
            console.log(group.id, `group-${group.id}`, document.getElementById(`group-${group.id}`));
            if(document.getElementById(`group-${group.id}`) === null){
                groupsTable.insertAdjacentHTML("afterbegin", renderIncomingGroup(group));
                ws.subscribe(`/topic/group/${group.id}`);
            }
            
        }
    }
}

// Render incoming group
function renderIncomingGroup(group) {
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
                            <div class="balance col-9 w-auto ms-3">0.0 ${group.currencyString} </div>
                            <div class="col-3 col-num-members">
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