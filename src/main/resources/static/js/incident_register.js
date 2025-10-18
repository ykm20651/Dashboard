document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return; // 로그인 여부 확인

  const form = document.getElementById("incidentForm");
  const msg = document.getElementById("msg");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const title = document.getElementById("title").value;
    const description = document.getElementById("description").value;
    const incidentType = document.getElementById("incidentType").value;
    const location = document.getElementById("location").value;
    const happenedAt = document.getElementById("happenedAt").value;

    try {
      const res = await fetch("http://52.79.99.132/incidents", {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({
          title,
          description,
          incidentType,
          location,
          happenedAt
        }),
      });

      if (res.status === 401) {
        showToast("로그인 세션이 만료되었습니다. 다시 로그인해주세요.", "error");
        setTimeout(() => (window.location.href = "login.html"), 1500);
        return;
      }

      if (!res.ok) {
        const err = await res.text();
        throw new Error(err || "사고 등록에 실패했습니다.");
      }

      const data = await res.json();
      console.log("사고 등록 성공:", data);

      showToast("사고가 성공적으로 등록되었습니다!", "success");

      setTimeout(() => {
        window.location.href = "incidents.html";
      }, 1500);
    } catch (err) {
      showToast("❌ " + err.message, "error");
      msg.innerText = err.message;
      msg.style.color = "red";
    }
  });
});
