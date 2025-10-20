document.addEventListener('DOMContentLoaded', function () {
  var toastElList = [].slice.call(document.querySelectorAll('.toast'));
  toastElList.forEach(function (toastEl) {
    new bootstrap.Toast(toastEl);
  });

  document.querySelectorAll('.toast[data-autoshow="true"]').forEach(function (t) {
    bootstrap.Toast.getOrCreateInstance(t).show();
  });

  var firstInput = document.querySelector('.form-inner input');
  if (firstInput) {
    firstInput.focus();
    firstInput.classList.add('pulse-focus');
    setTimeout(function(){ firstInput.classList.remove('pulse-focus'); }, 900);
  }
});
