const groupsTable = document.getElementById("groupsTable");
const usersList = document.getElementById("usersList");

go(`${config.rootUrl}/admin/getAllGroups`, "GET")
    .then(groups => {
        Array.from(groups).forEach(group => {
            const elem = document.getElementById(`group-${group.id}`);
            if (elem != null)
                groupsTable.removeChild(elem);
            groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group.id, group.name, group.enabled));
        })
    })
    .catch(e => {
        console.log("Error retrieving group", e);
    });

function renderGroup(id, name, enabled) {
    const backgroundColor = enabled ? "white" : "var(--bs-gray-400)";
    const borderLeftColor = enabled ? "var(--bs-yellow)" : "var(--bs-gray-600)";

    return `<div class="card m-2" role="button" onclick="location.href='/admin/${id}'" tabindex="0" style="background-color: ${backgroundColor}; border-left-color: ${borderLeftColor};">
                <div class="card-header">
                    ID: ${id}
                </div>
                <div class="card-body py-2 px-3">
                     Name: ${name}
                </div>
             </div>`;
}

go(`${config.rootUrl}/admin/getAllUsers`, "GET")
    .then(users => {
        Array.from(users).forEach(user => {
            const elem = document.getElementById(`user-${user.id}`);
            if (elem != null)
                usersList.removeChild(elem);
                usersList.insertAdjacentHTML("afterbegin", renderUser(user.id, user.name, user.username, user.enabled));
        })
    })
    .catch(e => {
        console.log("Error retrieving user", e);
    });

function renderUser(id, name, username, enabled) {
    const backgroundColor = enabled ? "white" : "var(--bs-gray-400)";
    const borderLeftColor = enabled ? "var(--bs-yellow)" : "var(--bs-gray-600)";

    return `<div class="card m-2" tabindex="0" style="background-color: ${backgroundColor}; border-left-color: ${borderLeftColor};">
                <div class="row row-cols-3 g-0">
                    <!-- Profile pic -->
                    <div class="mx-3 col-4 col-md-2 d-flex align-items-center justify-content-center">
                        Aquí va la ProfilePic
                    </div>
                    <!-- User Info -->
                    <div class="col-8 col-md-7">
                        <div class="row card-text-row">
                            <div class="card-title">${username}</div>
                        </div>
                        <div class="row card-text-row">
                            <div class="card-subtitle">${name}</div>
                        </div>
                    </div>
                    <!-- User ID -->
                    <div class="col-md-2 d-flex flex-column align-items-end justify-content-end">
                        <div class="card-subtitle card-text">${id}</div>
                    </div>
                </div>
             </div>`;
}