// envio de mensajes con AJAX
document.getElementById("cur2").addEventListener('change', function(){
    const selector = document.getElementsByClassName('curr');
    const values = document.getElementById("cur2").value;
    let currencyString = "";
    console.log("onChangeCurrency(${currency}) called");
    switch(selector){
        case 0: 
            currencyString= "€";
        case 1:
            currencyString= "$";
        case 2:
            currencyString= "£";
    }

    for (let i = 0; i < selector.length; i++) {
        selector[i].innerHTML = currencyString;
    }
});
