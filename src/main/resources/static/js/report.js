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
    const response = await fetch("http://52.79.99.132/reports", {
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
   ✅ 통계 업데이트
======================================== */
function updateStats() {
  const total = currentReports.length;
  const completed = currentReports.filter((r) => r.status === "완료").length;
  const pending = currentReports.filter(
    (r) => r.status === "진행중" || r.status === "대기"
  ).length;

  document.getElementById("totalReports").textContent = total;
  document.getElementById("completedReports").textContent = completed;
  document.getElementById("pendingReports").textContent = pending;
}

/* ========================================
   ✅ 보고서 목록 표시
======================================== */
function displayReports() {
  const container = document.getElementById("reportsList");

  if (!currentReports || currentReports.length === 0) {
    container.innerHTML = `
      <div class="no-data">
        <p>생성된 보고서가 없습니다.</p>
        <button class="btn-primary" onclick="showGenerateModal()">
          첫 보고서 생성하기
        </button>
      </div>
    `;
    return;
  }

  container.innerHTML = currentReports
    .map(
      (report) => `
    <div class="report-card">
      <div class="report-header">
        <h3 class="report-title">${report.title || "제목 없음"}</h3>
        <span class="report-status status-${(report.status || "대기").toLowerCase()}">
          ${report.status || "대기"}
        </span>
      </div>

      <div class="report-meta">
        <span>📅 생성일: ${new Date(
          report.generatedAt
        ).toLocaleDateString()}</span>
        <span>🚨 관련 사고: ${report.incidentTitle || "사고 미지정"}</span>
        <span>📋 유형: ${report.type || "유형 미상"}</span>
      </div>

      ${
        report.description
          ? `<div class="report-description">${report.description}</div>`
          : ""
      }

      <div class="report-actions">
        <button class="btn-primary btn-small" onclick="viewReport('${
          report.id
        }')">
          <span class="btn-icon">👁️</span> 보기
        </button>
        <button class="btn-secondary btn-small" onclick="downloadReport('${
          report.id
        }')">
          <span class="btn-icon">⬇️</span> 다운로드
        </button>
        <button class="btn-danger btn-small" onclick="deleteReport('${
          report.id
        }')">
          <span class="btn-icon">🗑️</span> 삭제
        </button>
      </div>
    </div>
  `
    )
    .join("");
}

/* ========================================
   ✅ 사고 선택 드롭다운 채우기
======================================== */
function populateIncidentSelect() {
  const select = document.getElementById("selectedIncidents");

  if (!currentIncidents.length) {
    select.innerHTML =
      '<option disabled>등록된 사고가 없습니다.</option>';
    return;
  }

  select.innerHTML = currentIncidents
    .map(
      (incident) =>
        `<option value="${incident.id}">${incident.title} (${incident.incidentType})</option>`
    )
    .join("");
}

/* ========================================
   ✅ 이벤트 리스너 설정
======================================== */
function setupEventListeners() {
  document
    .getElementById("generateNewReport")
    .addEventListener("click", showGenerateModal);

  document
    .getElementById("confirmGenerate")
    .addEventListener("click", generateReport);

  document
    .getElementById("statusFilter")
    .addEventListener("change", filterReports);
  document
    .getElementById("typeFilter")
    .addEventListener("change", filterReports);
  document
    .getElementById("searchInput")
    .addEventListener("input", filterReports);
  document
    .getElementById("refreshBtn")
    .addEventListener("click", refreshData);
}

/* ========================================
   ✅ 보고서 생성 모달
======================================== */
function showGenerateModal() {
  if (currentIncidents.length === 0) {
    showMessage("먼저 사고를 등록해주세요.", "error");
    window.location.href = "incident_register.html";
    return;
  }

  document.getElementById("reportTitle").value = "";
  document.getElementById("selectedIncidents").selectedIndex = -1;
  document.getElementById("reportType").selectedIndex = 0;
  document.getElementById("reportDescription").value = "";

  document.getElementById("generateModal").style.display = "flex";
}

function closeGenerateModal() {
  document.getElementById("generateModal").style.display = "none";
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
  const description = document
    .getElementById("reportDescription")
    .value.trim();

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

    const response = await fetch("http://52.79.99.132/reports", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({
        title,
        incidentIds: selectedIncidents,
        type,
        description,
      }),
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

/* ========================================
   ✅ 보고서 필터링 / 새로고침
======================================== */
function filterReports() {
  const statusFilter = document.getElementById("statusFilter").value;
  const typeFilter = document.getElementById("typeFilter").value;
  const searchTerm = document
    .getElementById("searchInput")
    .value.toLowerCase();

  const filteredReports = currentReports.filter((report) => {
    const matchesStatus = !statusFilter || report.status === statusFilter;
    const matchesType = !typeFilter || report.type.includes(typeFilter);
    const matchesSearch =
      !searchTerm || report.title.toLowerCase().includes(searchTerm);
    return matchesStatus && matchesType && matchesSearch;
  });

  const container = document.getElementById("reportsList");
  if (!filteredReports.length) {
    container.innerHTML =
      '<div class="no-data"><p>조건에 맞는 보고서가 없습니다.</p></div>';
    return;
  }

  container.innerHTML = filteredReports
    .map(
      (report) => `
      <div class="report-card">
        <div class="report-header">
          <h3 class="report-title">${report.title}</h3>
          <span class="report-status status-${report.status.toLowerCase()}">
            ${report.status}
          </span>
        </div>
        <div class="report-meta">
          <span>📅 생성일: ${new Date(
            report.generatedAt
          ).toLocaleDateString()}</span>
          <span>🚨 관련 사고: ${report.incidentTitle}</span>
          <span>📋 유형: ${report.type}</span>
        </div>
        ${
          report.description
            ? `<div class="report-description">${report.description}</div>`
            : ""
        }
        <div class="report-actions">
          <button class="btn-primary btn-small" onclick="viewReport('${
            report.id
          }')">👁️ 보기</button>
          <button class="btn-secondary btn-small" onclick="downloadReport('${
            report.id
          }')">⬇️ 다운로드</button>
          <button class="btn-danger btn-small" onclick="deleteReport('${
            report.id
          }')">🗑️ 삭제</button>
        </div>
      </div>`
    )
    .join("");
}

async function refreshData() {
  showMessage("데이터를 새로고침 중...", "info");
  await loadData();
  showMessage("데이터가 새로고침되었습니다.", "success");
}

/* ========================================
   ✅ 유틸리티
======================================== */
function showMessage(message, type = "info") {
  const messageArea = document.getElementById("messageArea");
  const msg = document.createElement("div");
  msg.className = `message ${type}`;
  msg.textContent = message;
  messageArea.appendChild(msg);
  setTimeout(() => msg.remove(), 4000);
}

function viewReport(id) {
  const report = currentReports.find((r) => r.id === id);
  if (report)
    showMessage(`"${report.title}" 보기 기능은 준비 중입니다.`, "info");
}

function downloadReport(id) {
  const report = currentReports.find((r) => r.id === id);
  if (!report) return;

  const content = `
보고서 제목: ${report.title}
생성일: ${new Date(report.generatedAt).toLocaleString()}
관련 사고: ${report.incidentTitle}
유형: ${report.type}
상태: ${report.status}

${report.description || ""}
`;

  const blob = new Blob([content], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `${report.title.replace(/\s+/g, "_")}.txt`;
  a.click();
  URL.revokeObjectURL(url);
  showMessage("보고서가 다운로드되었습니다.", "success");
}

async function deleteReport(id) {
  if (!confirm("정말로 이 보고서를 삭제하시겠습니까?")) return;

  try {
    const res = await fetch(`http://52.79.99.132/reports/${id}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
    });

    if (!res.ok) throw new Error("삭제 실패");

    currentReports = currentReports.filter((r) => r.id !== id);
    updateStats();
    displayReports();
    showMessage("보고서가 삭제되었습니다.", "success");
  } catch (err) {
    showMessage("보고서 삭제 실패: " + err.message, "error");
  }
}
