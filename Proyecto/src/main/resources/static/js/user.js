// Submit Button (SAVE)
document.getElementById("btn-save").onclick = (e) => {
    e.preventDefault();
    console.log('Saving user');
    const b = document.getElementById("btn-save");
    const name = document.getElementById("name").value;
    const username = document.getElementById("username").value;
    const oldPwd = document.getElementById("oldPwd").value;
    const newPwd = document.getElementById("newPwd").value;

    go(b.getAttribute('formaction'), 'POST', {
        name,
        username,
        oldPwd,
        newPwd
    })
        .then(d => {
            console.log("User: success", d);
            if (d.action === "redirect") {
                console.log("Redirecting to ", d.redirect);
                window.location.replace(d.redirect);
            }
        })
        .catch(e => console.log("Error saving user", e))
};