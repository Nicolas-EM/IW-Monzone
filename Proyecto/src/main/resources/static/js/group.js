const expensesTable = document.getElementById("expensesTable");
const debtsTable = document.getElementById("debtsTable");
const balancesTable = document.getElementById("balancesTable");
const groupId = expensesTable.dataset.groupid;
const currencyString = expensesTable.dataset.currency;

// Render EXISTING Expenses
go(`${config.rootUrl}/group/${groupId}/getExpenses`, "GET")
    .then(expenses => {
        expenses.forEach(expense => {
            expensesTable.insertAdjacentHTML("afterbegin", renderExpense(expense));
        })
    }
    );

// Render INCOMING Expenses
if (ws.receive) {
    const oldFn = ws.receive; // guarda referencia a manejador anterior
    ws.receive = (destination, obj) => {
        oldFn(destination, obj); // llama al manejador anterior

        // If expense destined to current group
        if (obj.type == "EXPENSE" && destination.includes(groupId)) {
            const expense = obj.expense;
            const expenseHTML = document.getElementById(`expense-${expense.expenseId}`)
            switch (obj.action) {
                case "EXPENSE_CREATED":
                    expensesTable.insertAdjacentHTML("afterbegin", renderExpense(expense));
                    renderAllBalances()
                    break;
                case "EXPENSE_MODIFIED":
                    expenseHTML.outerHTML = renderExpense(expense);
                    renderAllBalances()
                    break;
                case "EXPENSE_DELETED":
                    expenseHTML.parentElement.removeChild(expenseHTML);
                    renderAllBalances()
                    break;
                default:
            }

            renderAllDebts();
        }
    }
}

// Render single expense
function renderExpense(expense) {
    return `<div id="expense-${expense.expenseId}" class="col">
                <div class="card border-light text-white mb-3 mx-auto" role="button" onclick="location.href='/group/${groupId}/${expense.expenseId}'">
                    <div class="row row-cols-2 row-cols-md-4 g-0">
                        <!-- Icon -->
                        <div class="col-5 col-md-2 d-flex align-items-center justify-content-center">
                            <img src="/img/type/${expense.typeID}.png" alt="Category Icon" class="icon">
                        </div>
                        <!-- Text Info -->
                        <div class="col-7 col-md-5">
                            <div class="row card-text-row">
                                <div class="card-title">${expense.name}</div>
                            </div>
                            <div class="row card-text-row">
                                <div class="card-subtitle" style="min-height: 1.5em;">${expense.desc}</div>
                            </div>
                        </div>
                        <!-- Date -->
                        <div class="col-md-2 d-flex align-items-center justify-content-center">
                            <div class="card-text">${expense.date}</div>
                        </div>
                        <!-- Amount -->
                        <div class="col-md-3 d-flex align-items-center justify-content-center expense-amount">
                            <div class="card-text">${expense.amount} ${currencyString}</div>
                        </div>
                    </div>
                </div>
            </div>`
}

// Llamada inicial
renderAllDebts()
renderAllBalances()

function renderAllDebts() {
    debtsTable.innerHTML = '';  // clear debts table

    // Get and render debts
    go(`${config.rootUrl}/group/${groupId}/getDebts`, "GET")
        .then(debts => {
            debts.forEach(debt => {
                debtsTable.insertAdjacentHTML("afterbegin", renderDebt(debt));
            })

            if (debts.length == 0) {
                debtsTable.insertAdjacentHTML("afterbegin", renderNoDebts());
            } else {
                document.querySelectorAll(".btn-settle").forEach((btn) => {
                    btn.onclick = (e) => handleSettleExpenseClick(btn, e);
                });
            }
        }
        );
}

// Render single debt
function renderDebt(debt) {
    return `<div class="row">
                <div class="col">
                    <form method="post" action="/group/${groupId}/settle">
                        <input type="hidden" name="_csrf" value="${config.csrf.value}">
                        <input type="hidden" name="debtorId" value="${debt.debtorId}">
                        <input type="hidden" name="debtOwnerId" value="${debt.debtOwnerId}">
                        <input type="hidden" name="amount" value="${debt.amount}">
                        <button type="button" class="btn-settle">Mark as paid</button>
                    </form>
                </div>
                <div class="col">
                    <label>${debt.debtorName} owes ${debt.debtOwnerName} ${debt.amount} ${currencyString}</label>
                </div>
            </div>`
}

// Render no debts message
function renderNoDebts() {
    return `<div class="row">
                <div class="col">
                    There are no debts to settle :D
                </div>
            </div>`
}

// Render member balances
function renderAllBalances() {
    balancesTable.innerHTML = '';  // clear debts table

    go(`${config.rootUrl}/group/${groupId}/getMembers`, "GET")
        .then(members => {
            members.forEach(member => {
                balancesTable.insertAdjacentHTML("beforeend", renderMemberBalance(member));
            })
        }
        );
}

function renderMemberBalance(member) {
    if (member.balance < 0) {
        return `<div class="row">
                    <div class="col text-center">
                        <h5 class="back_balance_neg">${member.balance}${currencyString}</h5>
                    </div>   
                    <div class="col text-center">
                        <h5>${member.username}</h5>
                    </div>                             
                </div>`;
    }
    else if (member.balance > 0) {
        return `<div class="row">
                    <div class="col text-center">
                        <h5>${member.username}</h5>
                    </div>
                    <div class="col text-center">
                        <h5 class="back_balance_pos">${member.balance}${currencyString}</h5>
                    </div>                                
                </div>`;
    }
    else {
        return `<div class="row">
                    <div class="col text-center">
                        <h5>${member.username}</h5>
                    </div>
                    <div class="col text-center">
                        <h5>${member.balance}${currencyString}</h5>
                    </div>                                
                </div>`;
    }
}

// Mark debt as settled
function handleSettleExpenseClick(btn, e) {
    e.preventDefault();
    console.log("Settling expense");

    const debtorId = btn.parentNode.querySelector('input[name="debtorId"]').value;
    const debtOwnerId = btn.parentNode.querySelector('input[name="debtOwnerId"]').value;
    const amount = btn.parentNode.querySelector('input[name="amount"]').value;

    go(btn.parentNode.action, "POST", {
        debtorId,
        debtOwnerId,
        amount,
    })
        .then((d) => {
            console.log("Settle: success", d);
        })
        .catch(e => {
            console.log("Error settling debt", e);
            alert(JSON.parse(e.text).message);
        })
}
