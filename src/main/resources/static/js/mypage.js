// 마이페이지 전용 JavaScript

let currentIncidents = [];
let currentReports = [];
let currentUser = null;

document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;

  // 사용자 정보 로드
  loadUserInfo();

  // 대시보드 데이터 로드
  loadDashboardData();

  // 이벤트 리스너 설정
  setupEventListeners();

  // 빠른 작업 버튼
  const newIncidentBtn = document.getElementById("newIncidentBtn");
  const analysisBtn = document.getElementById("analysisBtn");
  const reportQuickBtn = document.getElementById("generateReportQuickBtn");

  if (newIncidentBtn)
    newIncidentBtn.addEventListener("click", () => {
      window.location.href = "incident_register.html";
    });

  if (analysisBtn)
    analysisBtn.addEventListener("click", () => {
      showMessage("아직 준비 중입니다.", "info");
    });

  if (reportQuickBtn)
    reportQuickBtn.addEventListener("click", generateReport);

  // 기본 섹션
  showSection("dashboard");
});

/**
 * 사용자 정보 로드
 */
async function loadUserInfo() {
  try {
    const userEmail = getUserEmail();
    const userRole = getUserRole();

    // 사이드바 프로필
    document.getElementById("userName").textContent = userEmail.split("@")[0];
    document.getElementById("userEmailSidebar").textContent = userEmail;
    document.getElementById("userRoleBadge").textContent =
      userRole === "OWNER" ? "선주" : "선원";

    // 프로필 입력칸
    document.getElementById("profileEmail").value = userEmail;
    document.getElementById("profileRole").value =
      userRole === "OWNER" ? "선주" : "선원";

    showMessage("사용자 정보를 로드했습니다.", "success");
  } catch (error) {
    showMessage("사용자 정보 로드 실패: " + error.message, "error");
  }
}

/**
 * 대시보드 데이터 로드
 */
async function loadDashboardData() {
  try {
    await loadIncidents();
    await loadReports();
    updateDashboardStats();
    displayRecentIncidents();
  } catch (error) {
    showMessage("대시보드 데이터 로드 실패: " + error.message, "error");
  }
}

/**
 * 사고 목록 로드
 */
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
    console.error("사고 목록 로드 실패:", error);
    currentIncidents = generateDummyIncidents();
    showMessage("더미 데이터를 사용합니다.", "info");
    return currentIncidents;
  }
}

/**
 * 보고서 목록 로드
 */
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
    console.error("보고서 목록 로드 실패:", error);
    currentReports = generateDummyReports();
    showMessage("더미 데이터를 사용합니다.", "info");
    return currentReports;
  }
}

/**
 * 대시보드 통계 업데이트
 */
function updateDashboardStats() {
  const totalIncidents = currentIncidents.length;
  const completedIncidents = currentIncidents.filter(
    (i) => i.status === "COMPLETED"
  ).length;
  const totalReports = currentReports.length;

  document.getElementById("totalIncidents").textContent = totalIncidents;
  document.getElementById("completedIncidents").textContent =
    completedIncidents;
  document.getElementById("totalReports").textContent = totalReports;
}

/**
 * 최근 사고 표시
 */
function displayRecentIncidents() {
  const recentIncidents = currentIncidents.slice(0, 5);
  const container = document.getElementById("recentIncidents");

  if (!container) return;

  if (recentIncidents.length === 0) {
    container.innerHTML = '<p class="no-data">등록된 사고가 없습니다.</p>';
    return;
  }

  container.innerHTML = recentIncidents
    .map(
      (incident) => `
      <div class="incident-summary">
        <div class="summary-header">
          <h4>${incident.title}</h4>
          <span class="status-badge status-${incident.status.toLowerCase()}">${getStatusText(
        incident.status
      )}</span>
        </div>
        <p class="summary-meta">${incident.incidentType} • ${new Date(
        incident.happenedAt
      ).toLocaleDateString()}</p>
      </div>
    `
    )
    .join("");
}

/**
 * 섹션 표시
 */
function showSection(sectionId) {
  document
    .querySelectorAll(".content-section")
    .forEach((section) => section.classList.remove("active"));
  document
    .querySelectorAll(".nav-item")
    .forEach((item) => item.classList.remove("active"));

  const target = document.getElementById(sectionId);
  const nav = document.querySelector(`[data-section="${sectionId}"]`);

  if (target) target.classList.add("active");
  if (nav) nav.classList.add("active");

  if (sectionId === "reports") displayReports();
}

/**
 * 보고서 목록 표시
 */
function displayReports() {
  const container = document.getElementById("reportsList");
  if (!container) return;

  if (currentReports.length === 0) {
    container.innerHTML = `
      <div class="no-data">
        <p>생성된 보고서가 없습니다.</p>
        <button class="btn-primary" onclick="generateReport()">
          첫 보고서 생성하기
        </button>
      </div>
    `;
    return;
  }

  container.innerHTML = currentReports
    .map(
      (report) => `
    <div class="report-item">
      <div class="report-header">
        <h3 class="report-title">${report.title}</h3>
        <span class="report-date">${new Date(
          report.generatedAt
        ).toLocaleDateString()}</span>
      </div>
      <div class="report-meta">
        <span>사고: ${report.incidentTitle}</span>
        <span>상태: ${report.status}</span>
      </div>
      <div class="report-actions">
        <button class="btn-primary btn-small" onclick="viewReport('${report.id}')">보기</button>
        <button class="btn-secondary btn-small" onclick="downloadReport('${report.id}')">다운로드</button>
      </div>
    </div>
  `
    )
    .join("");
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
  document.querySelectorAll(".nav-item").forEach((item) => {
    item.addEventListener("click", (e) => {
      e.preventDefault();
      const sectionId = e.currentTarget.getAttribute("data-section");
      showSection(sectionId);
    });
  });

  const updateBtn = document.getElementById("updateProfileBtn");
  const deleteBtn = document.getElementById("deleteAccountBtn");
  const reportBtn = document.getElementById("generateReportBtn");

  if (updateBtn) updateBtn.addEventListener("click", updateProfile);
  if (deleteBtn) deleteBtn.addEventListener("click", deleteAccount);
  if (reportBtn) reportBtn.addEventListener("click", generateReport);
}

/**
 * 프로필 업데이트
 */
function updateProfile() {
  const newPassword = document.getElementById("newPassword").value;
  const confirmPassword = document.getElementById("confirmPassword").value;

  if (newPassword && newPassword !== confirmPassword) {
    showMessage("비밀번호가 일치하지 않습니다.", "error");
    return;
  }

  showMessage("프로필 업데이트 기능은 준비 중입니다.", "info");
}

/**
 * 계정 삭제
 */
function deleteAccount() {
  if (confirm("정말로 계정을 삭제하시겠습니까?")) {
    showMessage("계정 삭제 기능은 준비 중입니다.", "info");
  }
}

/**
 * 보고서 생성
 */
function generateReport() {
  if (currentIncidents.length === 0) {
    showMessage("생성할 사고가 없습니다. 먼저 사고를 등록해주세요.", "error");
    return;
  }

  showMessage("보고서 생성 중...", "info");

  setTimeout(() => {
    const newReport = {
      id: Date.now().toString(),
      title: `사고 보고서 - ${new Date().toLocaleDateString()}`,
      incidentTitle: currentIncidents[0].title,
      status: "완료",
      generatedAt: new Date().toISOString(),
    };

    currentReports.unshift(newReport);
    updateDashboardStats();
    displayReports();
    showMessage("보고서가 생성되었습니다.", "success");
  }, 1500);
}

/**
 * 상태 텍스트 변환
 */
function getStatusText(status) {
  const statusMap = {
    REPORTED: "신고됨",
    INVESTIGATING: "조사중",
    COMPLETED: "완료",
  };
  return statusMap[status] || status;
}

/**
 * 메시지 표시
 */
function showMessage(message, type = "info") {
  const messageArea = document.getElementById("messageArea");
  const msg = document.createElement("div");
  msg.className = `message ${type}`;
  msg.textContent = message;
  messageArea.appendChild(msg);
  setTimeout(() => msg.remove(), 4000);
}

/**
 * 더미 데이터
 */
function generateDummyIncidents() {
  return [
    {
      id: "1",
      title: "선박 충돌 사고",
      incidentType: "충돌",
      location: "부산항",
      happenedAt: new Date(Date.now() - 86400000).toISOString(),
      status: "INVESTIGATING",
    },
    {
      id: "2",
      title: "화물 유출 사고",
      incidentType: "유출",
      location: "인천항",
      happenedAt: new Date(Date.now() - 172800000).toISOString(),
      status: "COMPLETED",
    },
  ];
}

function generateDummyReports() {
  return [
    {
      id: "1",
      title: "선박 충돌 사고 보고서",
      incidentTitle: "선박 충돌 사고",
      status: "완료",
      generatedAt: new Date(Date.now() - 86400000).toISOString(),
    },
  ];
}

/**
 * 기타 동작
 */
function viewReport(id) {
  window.location.href = `report.html?id=${id}`;
}

function downloadReport(id) {
  showMessage("보고서 다운로드 기능은 준비 중입니다.", "info");
}
