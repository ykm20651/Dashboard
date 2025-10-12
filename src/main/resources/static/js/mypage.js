let currentIncidents = [];
let currentReports = [];
let currentUser = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return;

  loadUserInfo();
  loadDashboardData();
  setupEventListeners();
  setupNavigationHandlers();

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

/** 네비게이션 핸들러 설정 */
function setupNavigationHandlers() {
  // 왼쪽 사이드바 네비게이션 - 정확한 선택자 사용
  const profileLink = document.querySelector('a[data-section="profile"]');
  const reportsLink = document.querySelector('a[data-section="reports"]');

  // 내 정보 클릭 시 별도 페이지로 이동
  if (profileLink) {
    profileLink.addEventListener('click', (e) => {
      e.preventDefault();
      window.location.href = 'profile.html'; // 별도 페이지로 이동
    });
  }

  // 보고서 클릭 시 보고서 페이지로 이동
  if (reportsLink) {
    reportsLink.addEventListener('click', (e) => {
      e.preventDefault();
      window.location.href = 'report.html'; // 별도 페이지로 이동
    });
  }

  // 오른쪽 빠른 작업 바 - 정확한 ID 사용
  const newIncidentBtn = document.getElementById('newIncidentBtn');
  const analyzeBtn = document.getElementById('analysisBtn');
  const generateReportBtn = document.getElementById('generateReportQuickBtn');

  // 새 사고 등록 클릭 시 incident_register.html로 이동
  if (newIncidentBtn) {
    newIncidentBtn.addEventListener('click', () => {
      window.location.href = 'incident_register.html';
    });
  }

  // 사고 분석 클릭 시 준비중 메시지 표시
  if (analyzeBtn) {
    analyzeBtn.addEventListener('click', () => {
      showMessage('아직 준비중, 개발단계입니다', 'info');
    });
  }

  // 보고서 생성 클릭 시 보고서 페이지로 이동
  if (generateReportBtn) {
    generateReportBtn.addEventListener('click', () => {
      window.location.href = 'report.html'; // 별도 페이지로 이동
    });
  }

  // 대시보드 하단 전체 보기 버튼
  const viewAllBtn = document.getElementById('viewAllIncidentsBtn');

  if (viewAllBtn) {
    viewAllBtn.addEventListener('click', () => {
      window.location.href = 'incidents.html';
    });
  }
}

/** 메시지 표시 함수 */
function showMessage(message, type = 'info') {
  // 토스트 메시지 스타일로 표시
  const toast = document.createElement('div');
  toast.className = `toast-message toast-${type}`;
  toast.innerHTML = `
    <div class="toast-content">
      <span class="toast-icon">${getToastIcon(type)}</span>
      <span class="toast-text">${message}</span>
    </div>
  `;
  
  // 스타일 추가
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 10000;
    padding: 12px 16px;
    border-radius: 8px;
    color: white;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    transform: translateX(100%);
    transition: transform 0.3s ease;
    max-width: 300px;
    word-wrap: break-word;
  `;
  
  // 타입별 색상 설정
  const colors = {
    info: '#2196F3',
    success: '#4CAF50',
    warning: '#FF9800',
    error: '#F44336'
  };
  toast.style.backgroundColor = colors[type] || colors.info;
  
  document.body.appendChild(toast);
  
  // 애니메이션으로 나타나기
  setTimeout(() => {
    toast.style.transform = 'translateX(0)';
  }, 100);
  
  // 3초 후 사라지기
  setTimeout(() => {
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 300);
  }, 3000);
}

/** 토스트 아이콘 반환 */
function getToastIcon(type) {
  const icons = {
    info: 'ℹ️',
    success: '✅',
    warning: '⚠️',
    error: '❌'
  };
  return icons[type] || icons.info;
}
