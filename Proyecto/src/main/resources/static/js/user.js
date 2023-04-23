// Profile form
document.getElementById("profileForm").addEventListener('submit', (e) => {
    e.preventDefault();
    console.log('Saving user');
    const b = document.getElementById("btn-save");

    const formData = new FormData();
    if (document.getElementById("avatar").files[0] !== undefined) {
        formData.append('imageFile', document.getElementById("avatar").files[0]);
    }
    const name = formData.append('name', document.getElementById("name").value);
    const username = formData.append('username', document.getElementById("username").value);
    console.log(formData.get("username"))
    go(b.getAttribute('formaction'), 'POST', formData, {})
        .then(d => {
            console.log("User: success", d);
            createToastNotification(`user success-${name}-${username}`, `User changes changed successfully`);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => {
            console.log("Error saving user", e);
            alert(JSON.parse(e.text).message);
        })
});

// Password form
document.getElementById("passwordForm").addEventListener('submit', (e) => {
    e.preventDefault();
    console.log('Saving user password');
    const b = document.getElementById("btn-savePassword");
    const oldPwd = document.getElementById("oldPwd").value;
    const newPwd = document.getElementById("newPwd").value;

    go(b.getAttribute('formaction'), 'POST', {
        oldPwd,
        newPwd
    })
        .then(d => {
            console.log("oPass: ", oldPwd);
            console.log("nPass: ", newPwd);
            console.log("User change password: success", d);
            createToastNotification(`change password success-${oldPwd}-${newPwd}`, `Password changed successfully`);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => {
            console.log("Error saving user password", e);
            alert(JSON.parse(e.text).message);
        })
});