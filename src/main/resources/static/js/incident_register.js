document.addEventListener("DOMContentLoaded", () => {
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
      const token = localStorage.getItem("token");
      if (!token) throw new Error("로그인이 필요합니다.");

      const res = await fetch("http://52.79.99.132:8080/incidents", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ title, description, incidentType, location, happenedAt })
      });

      if (!res.ok) throw new Error("사고 등록 실패");

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
