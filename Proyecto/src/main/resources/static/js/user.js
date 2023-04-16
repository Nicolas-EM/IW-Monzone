// Submit Button (SAVE USER)
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

    go(b.getAttribute('formaction'), 'POST', {}, formData)
        .then(d => {
            console.log("name: ", formData.get("name"));
            console.log("username: ", formData.get("username"));
            console.log("User: success", d);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => console.log("Error saving user", e))
};

// Submit Button (SAVE PASSWORD)
document.getElementById("btn-savePassword").onclick = (e) => {
    e.preventDefault();
    console.log('Saving user password');
    const b = document.getElementById("btn-savePassword");
    const oldPwd = document.getElementById("oldPwd").value;
    const newPwd = document.getElementById("newPwd").value;

    go(b.getAttribute('formaction'), 'POST',{
        oldPwd,
        newPwd
    })
        .then(d => {
            console.log("oPass: ", oldPwd);
            console.log("nPass: ", newPwd);
            console.log("User change password: success", d);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => console.log("Error saving user password", e))
};