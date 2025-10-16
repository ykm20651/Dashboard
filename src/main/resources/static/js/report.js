// 보고서 페이지 JavaScript (DB 연동 버전)

let currentReports = [];
let currentIncidents = [];

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return;
  loadData();
  setupEventListeners();
});

/* ========================================
   ✅ 데이터 로드
======================================== */

function showMessage(message, type = "info") {
  const messageArea = document.getElementById("messageArea");
  messageArea.textContent = message;
  messageArea.className = `message-area ${type}`;
  messageArea.style.display = "block";
  setTimeout(() => {
    messageArea.style.display = "none";
  }, 3000);
}



async function loadData() {
  try {
    await Promise.all([loadIncidents(), loadReports()]);
    updateStats();
    displayReports();
    populateIncidentSelect();
  } catch (error) {
    showMessage("데이터 로드 실패: " + error.message, "error");
  }
}

async function loadIncidents() {
  try {
    const response = await fetch("http://52.79.99.132/incidents", {
      method: "GET",
      headers: getAuthHeaders(),
    });
    await handleApiError(response, "사고 목록을 불러올 수 없습니다.");
    currentIncidents = await response.json();
    return currentIncidents;
  } catch (error) {
    console.error("❌ 사고 목록 로드 실패:", error);
    showMessage("사고 데이터를 불러오지 못했습니다.", "error");
    currentIncidents = [];
  }
}

async function loadReports() {
  try {
    const incidentId = getCurrentIncidentId();
    if (!incidentId) return;
    const response = await fetch(`http://52.79.99.132/incidents/${incidentId}/reports`, {
      method: "GET",
      headers: getAuthHeaders(),
    });
    await handleApiError(response, "보고서 목록을 불러올 수 없습니다.");
    currentReports = await response.json();
    return currentReports;
  } catch (error) {
    console.error("❌ 보고서 목록 로드 실패:", error);
    showMessage("보고서 데이터를 불러오지 못했습니다.", "error");
    currentReports = [];
  }
}

/* ========================================
   ✅ 보고서 생성
======================================== */
async function generateReport() {
  const title = document.getElementById("reportTitle").value.trim();
  const selectedIncidents = Array.from(
    document.getElementById("selectedIncidents").selectedOptions
  ).map((option) => option.value);
  const type = document.getElementById("reportType").value;
  const description = document.getElementById("reportDescription").value.trim();

  if (!title) {
    showMessage("보고서 제목을 입력해주세요.", "error");
    return;
  }

  if (selectedIncidents.length === 0) {
    showMessage("관련 사고를 선택해주세요.", "error");
    return;
  }

  try {
    showMessage("보고서를 생성하는 중...", "info");
    const incidentId = selectedIncidents[0];

    const response = await fetch(`http://52.79.99.132/incidents/${incidentId}/reports`, {
      method: "POST",
      headers: getAuthHeaders(),
    });

    await handleApiError(response, "보고서 생성 실패");
    await loadReports();

    closeGenerateModal();
    updateStats();
    displayReports();
    showMessage("보고서가 성공적으로 생성되었습니다.", "success");
  } catch (error) {
    showMessage("보고서 생성 실패: " + error.message, "error");
  }
}

function getCurrentIncidentId() {
  const params = new URLSearchParams(window.location.search);
  return params.get("incidentId") || "";
}

function populateIncidentSelect() {
  const select = document.getElementById("selectedIncidents");
  select.innerHTML = "";
  currentIncidents.forEach((incident) => {
    const option = document.createElement("option");
    option.value = incident.id;
    option.textContent = incident.title;
    select.appendChild(option);
  });
}

function setupEventListeners() {
  document.getElementById("confirmGenerate").addEventListener("click", generateReport);
  document.getElementById("generateNewReport").addEventListener("click", () => {
    document.getElementById("generateModal").style.display = "block";
  });
  document.getElementById("refreshBtn").addEventListener("click", loadData);
}

function closeGenerateModal() {
  document.getElementById("generateModal").style.display = "none";
}

function updateStats() {
  document.getElementById("totalReports").textContent = currentReports.length;
  const completed = currentReports.filter((r) => r.status === "완료").length;
  const pending = currentReports.length - completed;
  document.getElementById("completedReports").textContent = completed;
  document.getElementById("pendingReports").textContent = pending;
}

function displayReports() {
  const list = document.getElementById("reportsList");
  list.innerHTML = "";
  if (currentReports.length === 0) {
    list.innerHTML = "<p class='empty-text'>등록된 보고서가 없습니다.</p>";
    return;
  }
  currentReports.forEach((report) => {
    const div = document.createElement("div");
    div.className = "report-card";
    div.innerHTML = `
      <p><strong>보고서:</strong> <a href="${report.pdfUrl}" target="_blank">다운로드</a></p>
      <p><strong>생성일:</strong> ${new Date(report.generatedAt).toLocaleString()}</p>
    `;
    list.appendChild(div);
  });
}
