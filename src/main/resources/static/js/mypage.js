let currentIncidents = [];
let currentReports = [];
let currentUser = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return;

  loadUserInfo();
  loadDashboardData();
  setupEventListeners();

  bindQuickActionHandlers();
  document.addEventListener("click", delegatedClickHandler);

  showSection("dashboard");
});

/** 사용자 정보 로드 */
async function loadUserInfo() {
  try {
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
  } catch {
    showMessage("사용자 정보를 불러오지 못했습니다.", "error");
  }
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
    // 사고별로 보고서를 가져오는 방식으로 변경
    const reports = [];
    for (const incident of currentIncidents) {
      try {
        const res = await fetch(`http://52.79.99.132/incidents/${incident.id}/reports`, {
          headers: getAuthHeaders(),
        });
        if (res.ok) {
          const incidentReports = await res.json();
          reports.push(...incidentReports);
        }
      } catch (error) {
        console.warn(`사고 ${incident.id}의 보고서 로드 실패:`, error);
      }
    }
    currentReports = reports;
  } catch {
    currentReports = generateDummyReports();
  }
}

/** 통계 업데이트 */
function updateDashboardStats() {
  document.getElementById("totalIncidents").textContent = currentIncidents.length;
  document.getElementById("completedIncidents").textContent =
    currentIncidents.filter((i) => i.status === "closed").length;
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
          <span class="status-badge status-${(i.status || "open").toLowerCase()}">
            ${getStatusText(i.status || "open")}
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
}

/** 보고서 생성 */
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
    showSection("reports");
    displayReports();
    showMessage("보고서가 생성되었습니다.", "success");
  }, 600);
}

/** 상태 한글 변환 */
function getStatusText(status) {
  const map = {
    open: "처리 전",
    report_generated: "조사 중",
    closed: "종결",
  };
  return map[status] || "처리 전";
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
      status: "open",
    },
    {
      id: "2",
      title: "화재 사고",
      incidentType: "화재",
      location: "인천 북항",
      happenedAt: new Date().toISOString(),
      status: "closed",
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
