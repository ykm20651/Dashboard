document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const incidentId = urlParams.get('id');
  
  if (!incidentId) {
    document.getElementById("msg").innerText = "❌ 사고 ID가 없습니다.";
    return;
  }

  loadReports(incidentId);
  setupReportUpload(incidentId);
});

// 03-02 API: 보고서 조회
async function loadReports(incidentId) {
  try {
    const reports = await apiCall(`/incidents/${incidentId}/reports`);
    
    const reportList = document.getElementById("reportList");
    reportList.innerHTML = "";
    
    if (reports.length === 0) {
      reportList.innerHTML = "<tr><td colspan='3'>보고서가 없습니다.</td></tr>";
      return;
    }
    
    reports.forEach(report => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${report.pdfUrl}</td>
        <td>${new Date(report.generatedAt).toLocaleString()}</td>
        <td>
          <button class="btn tiny" onclick="downloadReport('${report.id}')">다운로드</button>
        </td>
      `;
      reportList.appendChild(tr);
    });
    
  } catch (err) {
    document.getElementById("msg").innerText = "❌ " + err.message;
    document.getElementById("msg").style.color = "red";
  }
}

function setupReportUpload(incidentId) {
  const form = document.getElementById("reportForm");
  const fileInput = document.getElementById("reportFile");
  
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    
    if (!fileInput.files[0]) {
      alert("파일을 선택해주세요.");
      return;
    }
    
    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    
    try {
      // 03-01 API: 보고서 생성
      await apiCall(`/incidents/${incidentId}/reports`, {
        method: "POST",
        body: formData,
        headers: {} // Content-Type을 설정하지 않음 (FormData가 자동으로 설정)
      });
      
      alert("✅ 보고서가 업로드되었습니다.");
      fileInput.value = "";
      loadReports(incidentId); // 목록 새로고침
      
    } catch (err) {
      alert("❌ " + err.message);
    }
  });
}

async function downloadReport(reportId) {
  try {
    // 보고서 다운로드 API 호출 (구현 필요)
    const response = await fetch(`${API_BASE_URL}/reports/${reportId}/download`, {
      headers: { "Authorization": `Bearer ${localStorage.getItem("token")}` }
    });
    
    if (!response.ok) throw new Error("다운로드 실패");
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `report_${reportId}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
  } catch (err) {
    alert("❌ " + err.message);
  }
}