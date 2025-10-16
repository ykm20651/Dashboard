// 보고서 페이지 JavaScript (DB 연동 버전)

let currentReports = [];
let currentIncidents = [];

document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;

  // 데이터 로드
  loadData();

  // 이벤트 리스너 설정
  setupEventListeners();
});

/* ========================================
   ✅ 데이터 로드
======================================== */
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

/* ========================================
   ✅ 사고 목록 로드
======================================== */
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

/* ========================================
   ✅ 보고서 목록 로드
======================================== */
async function loadReports() {
  try {
    const incidentId = getCurrentIncidentId();
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
