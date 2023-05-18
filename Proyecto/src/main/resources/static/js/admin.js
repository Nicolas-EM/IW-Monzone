const groupsTable = document.getElementById("groupsTable");

go(`${config.rootUrl}/admin/getAllGroups`, "GET")
    .then(groups => {
        Array.from(groups).forEach(group => {
            const elem = document.getElementById(`group-${group.id}`);
            if (elem != null)
                groupsTable.removeChild(elem);
            groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group.id, group.name));
        })
    })
    .catch(e => {
        console.log("Error retrieving group", e);
    });

function renderGroup(id, name) {
    return `<div class="card m-2" role="button" onclick="location.href='/error'" tabindex="0">
                <div class="card-header">
                    ID: ${id}
                </div>
                <div class="card-body py-2 px-3">
                    Name: ${name}
                </div>
            </div>`;
}