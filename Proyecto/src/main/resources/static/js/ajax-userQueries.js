// Cálculo de los GASTOS del MES
// Tiene que enviarse al cambiar los dos campos (Date y Currency)
document.getElementById("currMonth").addEventListener('change', function () {
    const totalTextMonth = document.getElementById('total-exp');
    const dateString = document.getElementById("date").value;
    const currId = parseInt(document.getElementById("currMonth").value);
    
    // Cambiar el tipo de moneda según la seleccionada
    let currencyString = "";
    currencyString = getCurrencyString(currId, currencyString);

    go(`${config.rootUrl}/user/getMonthly/${dateString}/${currId}`, "GET", {
    })
        .then(data => {
            totalTextMonth.innerHTML = data + currencyString;
        })
});

// Cálculo de los GASTOS por CATEGORÍAS
// envio de mensajes con AJAX
document.getElementById("currType").addEventListener('change', function () {
    const selector = document.getElementsByClassName('curr');
    const amounts = document.getElementsByClassName('amount');
    const valueSelected = parseInt(document.getElementById("currType").value);

    // Cambiar el tipo de moneda según la seleccionada
    let currencyString = "";
    currencyString = getCurrencyString(valueSelected, currencyString);

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
      document.getElementById('f_avatar').click();
    });
  
    document.getElementById('f_avatar').addEventListener("change", function (e) {
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

function getCurrencyString(valueSelected, currencyString) {
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
            currencyString = "nada";
            break;
    }
    return currencyString;
}
