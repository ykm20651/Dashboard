document.addEventListener("DOMContentLoaded", () => {
  const analyzeForm = document.getElementById("analyzeForm");
  const evidenceForm = document.getElementById("evidenceForm");
  const msg = document.getElementById("msg");

  const token = localStorage.getItem("token");
  if (!token) {
    msg.innerText = "❌ 로그인 후 이용 가능합니다.";
    msg.style.color = "red";
    return;
  }

  // 1. 사고 분석 요청
  analyzeForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const incidentId = document.getElementById("incidentId").value;

    try {
      const res = await fetch(`http://15.164.99.177/incidents/${incidentId}/analyze`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("사고 분석 실패");

      const data = await res.json();
      msg.innerText = "✅ 분석 완료: " + JSON.stringify(data);
      msg.style.color = "green";
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });

  // 2. 증거자료 업로드
  evidenceForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const incidentId = document.getElementById("incidentId").value;
    const fileUrl = document.getElementById("fileUrl").value;
    const fileType = document.getElementById("fileType").value;
    const description = document.getElementById("description").value;

    if (!incidentId) {
      msg.innerText = "❌ 사고 ID를 먼저 입력하세요.";
      msg.style.color = "red";
      return;
    }

    try {
      const res = await fetch(`http://15.164.99.177/incidents/${incidentId}/evidence-files`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ fileUrl, fileType, description })
      });
      if (!res.ok) throw new Error("증거자료 업로드 실패");

      const data = await res.json();
      msg.innerText = "✅ 업로드 완료: " + JSON.stringify(data);
      msg.style.color = "green";
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });
});
