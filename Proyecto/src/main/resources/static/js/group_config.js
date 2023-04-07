
const infoGroup = document.getElementById("info-group");
const groupId = infoGroup.dataset.groupid;
const budget = infoGroup.dataset.budget;
const isGroupAdmin = infoGroup.dataset.isgroupadmin;
const currencies = infoGroup.dataset.currencies;

// envio de invitaciones con AJAX
let b = document.getElementById("confirmInviteBtn");
b.onclick = (e) => {
    e.preventDefault();
    // th:formaction="@{/group/{action}(action=${group?.getId()} + '/inviteMember')}"
    go(b.parentNode.action, 'POST', {
            username: document.getElementById("inviteUsername").value
        })
        .then(d => console.log("happy", d))
        .catch(e => console.log("sad", e))
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

function renderGroupMembers(group){
    const membersTable = document.getElementById('membersTable');

    go(`${config.rootUrl}/group/${groupId}/getGroupMembers`, "GET")
        .then(members => {
            Array.from(members).forEach(member => {
                if(member.enabled)
                    membersTable.insertAdjacentHTML("beforeend", renderMember(member, group));
            })
        })
}

function renderMember(member, group){
    let memberHTML = `<div class="row p-2 member">`;
    console.log(`isGroupAdmin: ${isGroupAdmin}`);
    if(isGroupAdmin){
        memberHTML = `${memberHTML}
                    <div class="col btn-remove">
                    <form method="post" th:action="@{/group/${groupId}/delMember}">
                        <input type="hidden" name="removeId" th:value="${member.idUser}">
                        <button type="submit" class="btn">
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
                    <span class="dot" th:style="${member.balance} >= 0 ? 'background: green' : 'background: red'"></span>
                </div>
                <div class="balance col d-flex align-items-center">
                    ${member.balance} ${group.currencyString}
                </div>
            </div>`;

    console.log(`Rendering member:`);
    console.log(memberHTML);
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
      .catch(e => console.log("Error creating/updating group", e))
};

// Submit Button (DELETE GROUP)
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
      .catch(e => console.log("Error deleting group", e))
};

// Render INCOMING updates of group
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (obj) => {
        oldFn(obj); // llama al manejador anterior

        if (obj.type == "GROUP") {
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
                default:
            }
        }
    }
}

// function renderGroup(group) {
//     let currencyArray = JSON.parse(currencies);
//     let currencyOptions = '';
//     currencyArray.forEach((curr, index) => {
//       const selected = group && group.currency && group.currency === curr ? 'selected' : '';
//       currencyOptions += `<option value="${index}" ${selected}>${curr}</option>`;
//     });

//     return `<label for="name" class="form-label w-100">Name</label>
//             <input id="name" name="name" type="text" class="rounded-corners mb-4 w-100" required="required" value="${group ? group.name : ''}" ${isGroupAdmin == null ? '' : isGroupAdmin ? '' : 'disabled'}>
//             <label for="desc" class="form-label w-100">Description (optional)</label>
//             <textarea id="desc" name="desc" class="rounded-corners form-control" rows="5" ${isGroupAdmin == null ? '' : isGroupAdmin ? '' : 'disabled'}>${group ? group.desc : ''}</textarea>
//             <label for="currency" class="form-label mt-4 w-100">Currency</label>
//             <select class="form-select rounded-corners" required name="currId" ${isGroupAdmin == null ? '' : isGroupAdmin ? '' : 'disabled'}>
//                 <option ${group ? '' : 'selected disabled hidden'} value="">Select Currency</option>
//                 ${currencyOptions}
//             </select>
//             <!-- Budgets -->
//             <div class="row">
//                 <!-- Set personal budget -->
//                 <div class="col">
//                     <label for="budget" class="form-label mt-4 w-100">Your budget:</label>
//                     <input id="budget" name="budget" type="number" min="0" step="0.1" class="rounded-corners w-100 bg-white budget" required="required" value="${budget}" placeholder="${group ? '' : 'Enter Budget'}"></input>
//                 </div>
//                 <!-- Total Budget -->
//                 <div ${group ? '' : 'hidden'} class="col">
//                     <label class="form-label mt-4 w-100">Group budget:</label>
//                     <input disabled class="rounded-corners w-100 bg-white budget" value="${group ? group.totBudget : ''}"></input>
//                 </div>
//             </div>`;
// }

