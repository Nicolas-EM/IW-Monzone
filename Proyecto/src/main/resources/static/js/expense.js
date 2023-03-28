window.addEventListener("load", (event) => {
  console.log("page is fully loaded");

  document.getElementById('imgBtn').addEventListener('click', function () {
    document.getElementById('imgFileInput').click();
  });

  document.getElementById('imgFileInput').addEventListener("change", function (e) {
    console.log("change detected");

    var reader = new FileReader();
    reader.onload = function (e) {
      // get loaded data and render thumbnail.
      document.getElementById("imgBtn").src = e.target.result;
    };
    // read the image file as a data URL.
    reader.readAsDataURL(this.files[0]);
  });

  const checkboxes = document.getElementsByClassName('participateCheckbox');
  for (let i = 0; i < checkboxes.length; i++) {
    checkboxes[i].addEventListener('change', function () {
      const amount = document.getElementById('amount').value;
      onChangeAmount(amount);
    })
  }

  // Submit Button
  document.getElementById("btn-save").onclick = (e) => {
    e.preventDefault();
    console.log('Saving expense');
    const b = document.getElementById("btn-save");
    const notificationUrl = b.getAttribute("data-notification-url");
    const name = document.getElementById("name").value;
    const amount = document.getElementById("amount").value;
    const paidById = document.getElementById("paidById").value;
  
    go(b.getAttribute('formaction'), 'POST', {
      name,
      desc: document.getElementById("desc").value,
      dateString: document.getElementById("dateString").value,
      amount,
      paidById,
      participateIds: Array.from(document.querySelectorAll('input[name="participateIds"]:checked')).map(cb => cb.value),
      typeId: document.getElementById("typeId").value
    })
      .then(d => {
        console.log("Expense: success", d);
        if(d.action === "redirect"){
          console.log("Redirecting to ", d.redirect);
          window.location.replace(d.redirect);
        }
      })
      .then(d => {
        // Send expense creation notification
        go(notificationUrl, 'POST', {
          expenseName: name,
          action: (b.getAttribute('formaction').includes("ne")) ? "created" : "updated"
        }).then(response => {
          console.log("Notification sent", response);
        }).catch(error => {
          console.log("Error sending notification", error);
        });
      })
      .catch(e => console.log("Error creating expense", e))
  }
});

/* Changes value pero user */
function onChangeAmount(amount) {
  console.log(`onChangeAmount(${amount}) called`);
  const checkboxes = document.getElementsByClassName('participateCheckbox');
  const numChecked = document.querySelectorAll('input:checked').length;
  const values = document.getElementsByClassName('amountPerMember');

  for (let i = 0; i < checkboxes.length; i++) {
    if (checkboxes[i].checked) {
      values[i].innerHTML = (Math.round(amount / numChecked * 100) / 100).toFixed(2);
    }
    else {
      values[i].innerHTML = '';
    }
  }  
}