const groupsTable = document.getElementById("groupsTable");
const usersList = document.getElementById("usersList");
const groupsRendered = [];  // IDs of groups rendered
const usersRendered = [];   // IDs of users rendered

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

    groupsRendered.push(id);

    return `<div id="group-${id}" class="card m-2" role="button" onclick="location.href='/admin/${id}'" tabindex="0" style="background-color: ${backgroundColor}; border-left-color: ${borderLeftColor};">
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

    usersRendered.push(id);

    return `<div id="user-${id}" class="card m-2" tabindex="0">
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

document.getElementById('searchGroupInput').addEventListener('input', function () {
    this.value = this.value.replace(/[^\w\s]/gi, '');
})

document.getElementById('searchGroupBtn').addEventListener('click', (e) => {
    const searchQuery = document.getElementById('searchGroupInput').value;

    if(searchQuery === ""){
        groupsRendered.forEach(groupId => {
            document.getElementById(`group-${groupId}`).classList.remove('d-none');
        })
        document.getElementById('noGroupsFoundMsg').classList.add('d-none');
        return;
    }

    go(`${config.rootUrl}/admin/searchGroup/${searchQuery}`, "GET")
    .then(groupsFound => {
        console.log(groupsFound);

        groupsRendered.forEach(groupId => {
            if(!groupsFound.includes(groupId)){
                document.getElementById(`group-${groupId}`).classList.add('d-none');
            }
        })

        if(!groupsFound.length){
            console.log("No groups found");
            document.getElementById('noGroupsFoundMsg').classList.remove('d-none');
        }
    })
    .catch(e => {
        console.log("Error searching for group", e);
    });
})

document.getElementById('searchUserInput').addEventListener('input', function () {
    this.value = this.value.replace(/[^\w\s]/gi, '');
})

document.getElementById('searchUserBtn').addEventListener('click', (e) => {
    const searchQuery = document.getElementById('searchUserInput').value;

    if(searchQuery === ""){
        usersRendered.forEach(userId => {
            document.getElementById(`user-${userId}`).classList.remove('d-none');
        })
        document.getElementById('noUsersFoundMsg').classList.add('d-none');
        return;
    }

    go(`${config.rootUrl}/admin/searchUser/${searchQuery}`, "GET")
    .then(usersFound => {
        console.log(usersFound);

        usersRendered.forEach(userId => {
            if(!usersFound.includes(userId)){
                document.getElementById(`user-${userId}`).classList.add('d-none');
            }
        })

        if(!usersFound.length){
            console.log("No users found");
            document.getElementById('noUsersFoundMsg').classList.remove('d-none');
        }
    })
    .catch(e => {
        console.log("Error searching for user", e);
    });
})