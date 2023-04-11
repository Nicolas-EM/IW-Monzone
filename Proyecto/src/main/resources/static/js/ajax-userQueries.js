// Cálculo de los GASTOS del MES
// Tiene que enviarse al cambiar los dos campos (Date y Currency)
document.getElementById("currMonth").addEventListener('change', function () {
    const totalTextMonth = document.getElementById('total-exp');
    go(`${config.rootUrl}/user/getMonthly`, "GET", {
        dateString: document.getElementById("date").value,
        currId: document.getElementById("currMonth").value
    })
        .then(total => {
            totalTextMonth.innerHTM = total;
        });
});

// Cálculo de los GASTOS por CATEGORÍAS
// envio de mensajes con AJAX
document.getElementById("currType").addEventListener('change', function () {
    const selector = document.getElementsByClassName('curr');
    const amounts = document.getElementsByClassName('amount');
    const valueSelected = parseInt(document.getElementById("currType").value);
    let currencyString = "";
    // Cambiar el tipo de moneda según la seleccionada
    switch (valueSelected) {
        case 0:
            currencyString = "€";
            break;
        case 1:
            currencyString = "$";
            break;
        case 2:
            currencyString = "£";
            break;
        default:
            currencyString = " ";
            break;
    }

    for (let i = 0; i < selector.length; i++) {
        selector[i].innerHTML = currencyString;
    }

    go(`${config.rootUrl}/user/getByType`, "GET", {
        currId: document.getElementById("currType").value
    })
        .then(totals => {
            for (let i = 0; i < amounts.length; i++) {
                amounts[i].innerHTML = totals[i];
            }
        });
});

// Cargar imagen de perfil
window.addEventListener("load", (event) => {
    console.log("page is fully loaded");
  
    document.getElementById('img-profile').addEventListener('click', function () {
      document.getElementById('avatar').click();
    });
  
    document.getElementById('avatar').addEventListener("change", function (e) {
      console.log("change detected");
  
      var reader = new FileReader();
      reader.onload = function (e) {
        // get loaded data and render thumbnail.
        document.getElementById("img-profile").src = e.target.result;
      };
      // read the image file as a data URL.
      reader.readAsDataURL(this.files[0]);
    });
});