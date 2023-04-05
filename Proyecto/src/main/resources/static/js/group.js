const expensesTable = document.getElementById("expensesTable");
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
    ws.receive = (obj) => {
        oldFn(obj); // llama al manejador anterior

        if(obj.type == "EXPENSE"){
            switch(obj.action){
                case "EXPENSE_CREATED":
                    expensesTable.insertAdjacentHTML("afterbegin", renderExpense(obj.expense));
                    break;
                case "EXPENSE_UPDATED":
                    // TODO
                    break;
                case "EXPENSE_DELETED":
                    // TODO
                    break;
                default:
            }
        }
    }
}

// Render single expense
function renderExpense(expense){
    return `<div class="col">
                <div class="card border-light text-white bg-warning mb-3 mx-auto" role="button" onclick="|location.href='/group/${groupId}/${expense.id}'|">
                    <div class="row row-cols-2 row-cols-md-4 g-0">
                        <!-- Icon -->
                        <div class="col-5 col-md-2 text-center">
                            <img src="/img/type/${expense.typeID}.png" alt="Category Icon" width="auto" height="50">
                        </div>
                        <!-- Text Info -->
                        <div class="col-7 col-md-5">
                            <div class="row card-text-row">
                                <div class="card-title">${expense.name}</div>
                            </div>
                            <div class="row card-text-row">
                                <div class="card-subtitle">${expense.desc}</div>
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
