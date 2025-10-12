let currentIncidents = [];
let currentReports = [];
let currentUser = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return;

  loadUserInfo();
  loadDashboardData();
  setupEventListeners();

  document.getElementById("newIncidentBtn")?.addEventListener("click", () => {
    window.location.href = "incident_register.html";
  });
  document.getElementById("analysisBtn")?.addEventListener("click", () => {
    showMessage("아직 준비 중입니다.", "info");
  });
  document
    .getElementById("generateReportQuickBtn")
    ?.addEventListener("click", generateReport);

  showSection("dashboard");
});

/** 사용자 정보 로드 */
async function loadUserInfo() {
  const userEmail = getUserEmail();
  const userRole = getUserRole();
  const userName = userEmail.split("@")[0];

  document.getElementById("avatarName").textContent = `${userName}님`;
  document.getElementById("userEmailSidebar").textContent = userEmail;
  document.getElementById("userRoleBadge").textContent =
    userRole === "OWNER" ? "선주" : "선원";
  document.getElementById("profileEmail").value = userEmail;
  document.getElementById("profileRole").value =
    userRole === "OWNER" ? "선주" : "선원";
}

/** 대시보드 데이터 로드 */
async function loadDashboardData() {
  await loadIncidents();
  await loadReports();
  updateDashboardStats();
  displayRecentIncidents();
}

/** 사고 목록 */
async function loadIncidents() {
  try {
    const res = await fetch("http://52.79.99.132/incidents", {
      headers: getAuthHeaders(),
    });
    if (!res.ok) throw new Error("사고 불러오기 실패");
    currentIncidents = await res.json();
  } catch {
    currentIncidents = generateDummyIncidents();
  }
}

/** 보고서 목록 */
async function loadReports() {
  try {
    const res = await fetch("http://52.79.99.132/reports", {
      headers: getAuthHeaders(),
    });
    if (!res.ok) throw new Error("보고서 불러오기 실패");
    currentReports = await res.json();
  } catch {
    currentReports = generateDummyReports();
  }
}

/** 통계 업데이트 */
function updateDashboardStats() {
  document.getElementById("totalIncidents").textContent = currentIncidents.length;
  document.getElementById("completedIncidents").textContent =
    currentIncidents.filter(
      (i) => i.status === "RESOLVED" || i.status === "CLOSED"
    ).length;
  document.getElementById("totalReports").textContent = currentReports.length;
}

/** 최근 사고 표시 */
function displayRecentIncidents() {
  const container = document.getElementById("recentIncidents");
  if (!container) return;

  if (!currentIncidents.length) {
    container.innerHTML = '<p class="no-data">등록된 사고가 없습니다.</p>';
    return;
  }

  const recent = currentIncidents.slice(0, 5);
  container.innerHTML = recent
    .map(
      (i) => `
      <div class="incident-summary">
        <div class="summary-header">
          <h4>${i.title}</h4>
          <span class="status-badge status-${i.status.toLowerCase()}">
            ${getStatusText(i.status)}
          </span>
        </div>
        <p class="summary-meta">
          ${i.incidentType || "유형 미상"} • ${new Date(i.happenedAt).toLocaleDateString()}
        </p>
        <p class="summary-meta">📍 ${i.location || "위치 정보 없음"}</p>
      </div>
    `
    )
    .join("");

  container.insertAdjacentHTML(
    "beforeend",
    `<button class="btn-view-all" id="viewAllIncidentsBtn">전체 보기</button>`
  );

  document
    .getElementById("viewAllIncidentsBtn")
    .addEventListener("click", () => (window.location.href = "incidents.html"));
}

/** 상태 한글 변환 */
function getStatusText(status) {
  const map = {
    OPEN: "처리 전",
    INVESTIGATING: "조사 중",
    RESOLVED: "조치 완료",
    CLOSED: "종결",
  };
  return map[status] || status;
}

/** 메시지 표시 */
function showMessage(msg, type = "info") {
  const area = document.getElementById("messageArea");
  const el = document.createElement("div");
  el.className = `message ${type}`;
  el.textContent = msg;
  area.appendChild(el);
  setTimeout(() => el.remove(), 4000);
}

/** 더미 데이터 */
function generateDummyIncidents() {
  return [
    {
      id: "1",
      title: "선박 충돌 사고",
      incidentType: "충돌",
      location: "부산항 신항",
      happenedAt: new Date().toISOString(),
      status: "OPEN",
    },
    {
      id: "2",
      title: "화재 사고",
      incidentType: "화재",
      location: "인천 북항",
      happenedAt: new Date().toISOString(),
      status: "RESOLVED",
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
      generatedAt: new Date().toISOString(),
    },
  ];
}
