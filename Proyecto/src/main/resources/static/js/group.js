// Delete Button
document.getElementsByClassName("button-del-expense").array.forEach(btn => {
    b.onclick = (e) => {
        e.preventDefault();
        console.log('Deleting expense');
        
        go(b.parentNode.action, 'POST', {})
          .then(d => {
            console.log("Expense deleted: success", d);
            if(d.action === "redirect"){
              console.log("Redirecting to ", d.redirect);
              window.location.replace(d.redirect);
            }
          })
    };
});