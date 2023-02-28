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
});
