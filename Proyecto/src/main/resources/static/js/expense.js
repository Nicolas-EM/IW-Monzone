// $('input[type="file"]').change(function (e) {
//   var fileName = e.target.files[0].name;
//   $("#file").val(fileName);

//   var reader = new FileReader();
//   reader.onload = function (e) {
//     // get loaded data and render thumbnail.
//     document.getElementById("preview").src = e.target.result;
//   };
//   // read the image file as a data URL.
//   reader.readAsDataURL(this.files[0]);
// });



window.addEventListener("load", (event) => {
  console.log("page is fully loaded");

  document.getElementById('imgBtn').addEventListener('click', function(){
    document.getElementById('imgFileInput').click();
  });

  document.getElementById('imgFileInput').addEventListener("change", function (e) {
    console.log("change detected");

    var reader = new FileReader();
    reader.onload = function (e) {
      // get loaded data and render thumbnail.
      document.getElementById("imgBtn").src = e.target.result;
    };
    // read the image file as a data URL.
    reader.readAsDataURL(this.files[0]);
  });

  document.getElementById('btn-save').addEventListener('click', function(){
    e.preventDefault();
    go(b.parentNode.action, 'POST', {
      message: document.getElementById("message").value
  })
  .then(d => console.log("happy", d))
  .catch(e => console.log("sad", e))
  })

  const checkboxes = document.getElementsByClassName('participateCheckbox');
  for(let i = 0; i < checkboxes.length; i++){
    checkboxes[i].addEventListener('change', function(){
      const amount = document.getElementById('amount').value;
      onChangeAmount(amount);
    })
  }
});

function onChangeAmount(amount){
  console.log(`onChangeAmount(${amount}) called`);
  const checkboxes = document.getElementsByClassName('participateCheckbox');
  const numChecked = document.querySelectorAll('input:checked').length;
  const values = document.getElementsByClassName('amountPerMember');

  for(let i = 0; i < checkboxes.length; i++){
    if(checkboxes[i].checked){
      values[i].innerHTML = amount / numChecked;
    }
    else{
      values[i].innerHTML = '';
    }
  }
}