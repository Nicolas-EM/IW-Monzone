// Profile form
document.getElementById("profileForm").addEventListener('submit', (e) => {
    e.preventDefault();
    console.log('Saving user');
    const b = document.getElementById("btn-save");

    const formData = new FormData();
    if (document.getElementById("avatar").files[0] !== undefined) {
        formData.append('imageFile', document.getElementById("avatar").files[0]);
    }
    const name = formData.append('name', document.getElementById("name").value);
    const username = formData.append('username', document.getElementById("username").value);
    console.log(formData.get("username"))
    go(b.getAttribute('formaction'), 'POST', formData, {})
        .then(d => {
            console.log("User: success", d);
            createToastNotification(`user success-${name}-${username}`, `User changes changed successfully`);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => {
            console.log("Error saving user", e);
            alert(JSON.parse(e.text).message);
        })
});

// Password form
document.getElementById("passwordForm").addEventListener('submit', (e) => {
    e.preventDefault();
    console.log('Saving user password');
    const b = document.getElementById("btn-savePassword");
    const oldPwd = document.getElementById("oldPwd").value;
    const newPwd = document.getElementById("newPwd").value;

    go(b.getAttribute('formaction'), 'POST', {
        oldPwd,
        newPwd
    })
        .then(d => {
            console.log("oPass: ", oldPwd);
            console.log("nPass: ", newPwd);
            console.log("User change password: success", d);
            createToastNotification(`change password success-${oldPwd}-${newPwd}`, `Password changed successfully`);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => {
            console.log("Error saving user password", e);
            alert(JSON.parse(e.text).message);
        })
});

const groupsTable = document.getElementById("groupsTable");

function getGroups() {
    go(`${config.rootUrl}/user/getGroups`, "GET")
        .then(groups => {
            Array.from(groups).forEach(group => {
                const elem = document.getElementById(`group-${group.id}`);
                if (elem != null)
                    groupsTable.removeChild(elem);
                go(`${config.rootUrl}/group/${group.id}/getBalance`, "GET")
                    .then(balance => {
                        groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, balance));
                    })
            })
        })
        .catch(e => {
            console.log("Error retrieving group", e);
        })
}

// Render current groups
getGroups();

// Render group
function renderGroup(group, balance) {   

return `<div id="group-${group.id}" class="card text-white h-100 mx-auto">
<label class="rounded-corners align-items-center w-100">
  <div class="row">
    <div class="groupName col-3">
      ${group.name}
    </div>
    <div class="col-4">
      Your budget: x ${group.currencyString}
    </div>
    <div class="col-5">
      <span class="dot" style="${balance >= 0 ? 'background: green' : 'background: red'}"></span>
      ${balance} ${group.currencyString}
    </div>
  </div>
</label>
</div>`
}

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
                    if (elem === null) {
                        groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, 0.0));
                    }
                } break;
                case "GROUP_MEMBER_REMOVED": {
                    go(`${config.rootUrl}/group/${group.id}/isMember`, "GET")
                        .then(isMember => {
                            if (!isMember)
                                elem.parentElement.removeChild(elem);
                        })
                } break;
                case "GROUP_MODIFIED": {
                    elem.parentElement.removeChild(elem);
                    go(`${config.rootUrl}/group/${group.id}/getBalance`, "GET")
                        .then(balance => {
                            groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, balance));
                        })
                } break;
                case "GROUP_DELETED": {
                    elem.parentElement.removeChild(elem);
                } break;
            }
        }

        else if (obj.type === "EXPENSE") {
            // get groupId from destination
            const expGroupId = parseInt(destination.split("/")[3]);
            const elem = document.getElementById(`group-${expGroupId}`);
            go(`${config.rootUrl}/group/${expGroupId}/getGroupConfig`, "GET")
                .then(group => {
                    go(`${config.rootUrl}/group/${expGroupId}/getBalance`, "GET")
                        .then(balance => {
                            elem.parentElement.removeChild(elem);
                            groupsTable.insertAdjacentHTML("afterbegin", renderGroup(group, balance));
                        })
                })
        }
    }
}