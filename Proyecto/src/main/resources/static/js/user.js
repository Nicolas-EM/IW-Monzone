// Submit Button (SAVE)
document.getElementById("btn-save").onclick = (e) => {
    e.preventDefault();
    console.log('Saving user');
    const b = document.getElementById("btn-save");

    const formData = new FormData();
    if(document.getElementById("avatar").files[0] !== undefined){
        formData.append('imageFile', document.getElementById("avatar").files[0]);
    }
    formData.append('name', document.getElementById("name").value);
    formData.append('username', document.getElementById("username").value);
    formData.append('oldPwd', document.getElementById("oldPwd").value);
    formData.append('newPwd', document.getElementById("newPwd").value);

    go(b.getAttribute('formaction'), 'POST', formData, {})
        .then(d => {
            console.log("User: success", d);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => console.log("Error saving user", e))
};