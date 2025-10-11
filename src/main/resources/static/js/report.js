document.addEventListener("DOMContentLoaded", () => {
  const params = new URLSearchParams(window.location.search);
  const incidentId = params.get("id");

  const reportList = document.getElementById("reportList");
  const generateBtn = document.getElementById("generateReport");
  const msg = document.getElementById("msg");

  // 보고서 목록 불러오기
  async function loadReports() {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`http://52.79.99.132:80/incidents/${incidentId}/reports`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("보고서 조회 실패");

      const reports = await res.json();
      reportList.innerHTML = "";

      if (reports.length === 0) {
        reportList.innerHTML = `<p class="empty-text">보고서가 없습니다.</p>`;
        return;
      }

      reports.forEach(r => {
        const card = document.createElement("div");
        card.className = "report-card";
        card.innerHTML = `
          <p>📄 보고서 생성일: ${new Date(r.generatedAt).toLocaleString()}</p>
          <a href="${r.pdfUrl}" target="_blank">다운로드</a>
        `;
        reportList.appendChild(card);
      });
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  }

  // 보고서 생성
  generateBtn.addEventListener("click", async () => {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`http://52.79.99.132:80/incidents/${incidentId}/reports`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("보고서 생성 실패");

      msg.innerText = "✅ 보고서 생성 완료";
      msg.style.color = "green";
      loadReports();
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });

  loadReports();
});
