function editar(){
    document.getElementById("username").removeAttribute("readonly", false) ;
    document.getElementById("username").value="Sari_";

    document.getElementById("paid").removeAttribute("readonly", false) ;
    document.getElementById("paid").value="yes_";

    document.getElementById("amount").removeAttribute("readonly", false) ;
    document.getElementById("amount").value="50_";
  }
  
  function bloquear(){
     document.getElementById("username").setAttribute("readonly", true) ;
     document.getElementById("paid").setAttribute("readonly", true) ;
     document.getElementById("amount").setAttribute("readonly", true) ;
  }