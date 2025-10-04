document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("incidentForm");
  const msg = document.getElementById("msg");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(form);
    const incidentData = {
      title: formData.get("title"),
      description: formData.get("description"),
      incidentType: formData.get("incidentType"),
      location: formData.get("location"),
      happenedAt: formData.get("happenedAt"),
      severity: formData.get("severity")
    };

    try {
      // 01-02 API: 사고 등록
      const response = await apiCall("/incidents", {
        method: "POST",
        body: JSON.stringify(incidentData)
      });

      msg.innerText = "✅ 사고가 성공적으로 등록되었습니다.";
      msg.style.color = "green";
      
      // 폼 초기화
      form.reset();
      
      // 2초 후 사고 목록 페이지로 이동
      setTimeout(() => {
        window.location.href = "incidents.html";
      }, 2000);

    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });
});