document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;
  
  const form = document.getElementById("incidentForm");
  const msg = document.getElementById("msg");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const title = document.getElementById("title").value;
    const description = document.getElementById("description").value;
    const incidentType = document.getElementById("incidentType").value;
    const location = document.getElementById("location").value;
    const happenedAtValue = document.getElementById("happenedAt").value;
    
    // 날짜를 ISO 8601 형식으로 변환
    const happenedAt = happenedAtValue ? new Date(happenedAtValue).toISOString() : new Date().toISOString();

    try {
      const res = await fetch("http://52.79.99.132/incidents", {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ title, description, incidentType, location, happenedAt })
      });

      await handleApiError(res, "사고 등록에 실패했습니다.");

      const data = await res.json();
      console.log("사고 등록 성공:", data);

      msg.innerText = "✅ 사고가 성공적으로 등록되었습니다!";
      msg.style.color = "green";

      setTimeout(() => {
        window.location.href = "incidents.html";
      }, 1500);

    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });
});
