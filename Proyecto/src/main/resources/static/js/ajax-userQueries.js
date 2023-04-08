// envio de mensajes con AJAX
document.getElementById("cur2").addEventListener('change', function () {
    const selector = document.getElementsByClassName('curr');
    const valueSelected = parseInt(document.getElementById("cur2").value);
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
});