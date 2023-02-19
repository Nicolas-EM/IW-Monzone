function editar(){
    document.getElementById("campaign").removeAttribute("readonly", false) ;
    document.getElementById("campaign").value="editable";
  }
  
  function bloquear(){
     document.getElementById("campaign").setAttribute("readonly", true) ;
  }