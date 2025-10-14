/** ✅ 화면 하단 토스트 알림 표시 */
function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  if (!toast) return;

  toast.className = `toast show ${type}`;
  toast.textContent = message;

  setTimeout(() => {
    toast.className = "toast";
  }, 2500);
}
