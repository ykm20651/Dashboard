/* =======================================
   OASIS | MyPage 통합 스크립트 (최종 확정)
   ======================================= */

let currentIncidents = [];
let currentReports = [];
let currentUser = null;

/* ---------------------------------------
   ✅ 초기 실행
--------------------------------------- */
document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ OASIS MyPage Loaded");

  if (!requireAuth()) return;

  loadUserInfo();
  loadDashboardData();
  bindQuickActionHandlers();

  showSection("dashboard");
});

/* ---------------------------------------
   ✅ 사용자 정보 로드
--------------------------------------- */
async function loadUserInfo() {
  try {
    const userEmail = getUserEmail ? getUserEmail() : "user@example.com";
    const userRole = getUserRole ? getUserRole() : "OWNER";
    const userName = userEmail.split("@")[0];

    document.getElementById("avatarName").textContent = `${userName}님`;
    document.getElementById("userEmailSidebar").textContent = userEmail;
    document.getElementById("userRoleBadge").textContent =
      userRole === "OWNER" ? "선주" : "선원";
    document.getElementById("profileEmail").value = userEmail;
    document.getElementById("profileRole").value =
      userRole === "OWNER" ? "선주" : "선원";
  } catch {
    showToast("사용자 정보를 불러오지 못했습니다.", "error");
  }
}

/* ---------------------------------------
   ✅ 대시보드 데이터 로드
--------------------------------------- */
async function loadDashboardData() {
  await loadIncidents();
  await loadReports();
  updateDashboardStats();
  displayRecentIncidents();
}

/* 사고 목록 불러오기 */
async function loadIncidents() {
  try {
    const res = await fetch("http://52.79.99.132/incidents", {
      headers: getAuthHeaders ? getAuthHeaders() : {},
    });
    if (!res.ok) throw new Error("사고 불러오기 실패");
    currentIncidents = await res.json();
  } catch {
    currentIncidents = generateDummyIncidents();
  }
}

/* 보고서 목록 불러오기 */
async function loadReports() {
  try {
    const res = await fetch("http://52.79.99.132/reports", {
      headers: getAuthHeaders ? getAuthHeaders() : {},
    });
    if (!res.ok) throw new Error("보고서 불러오기 실패");
    currentReports = await res.json();
  } catch {
    currentReports = generateDummyReports();
  }
}

/* 통계 업데이트 */
function updateDashboardStats() {
  document.getElementById("totalIncidents").textContent = currentIncidents.length;
  document.getElementById("completedIncidents").textContent =
    currentIncidents.filter((i) => i.status === "CLOSED").length; // ✅ 명세서: CLOSED
  document.getElementById("totalReports").textContent = currentReports.length;
}

/* 최근 사고 표시 */
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
          <span class="status-badge status-${(i.status || "OPEN").toLowerCase()}">
            ${getStatusText(i.status || "OPEN")}
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

/* ✅ 상태 한글 변환 (명세서 기준: OPEN / REPORT_GENERATED / CLOSED) */
function getStatusText(status = "") {
  // 1️⃣ 들어온 status를 전부 대문자로 변환
  const key = status.toUpperCase();

  // 2️⃣ 대문자 기준으로 매핑 테이블 생성
  const map = {
    OPEN: "등록 완료",
    REPORT_GENERATED: "보고서 생성 완료",
    CLOSED: "종결"
  };

  // 3️⃣ 매핑된 값 반환 (없으면 원본 그대로)
  return map[key] || status;
}

/* 더미 데이터 */
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
      status: "CLOSED",
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

/* ---------------------------------------
   ✅ 버튼 및 메뉴 동작
--------------------------------------- */
function bindQuickActionHandlers() {
  console.log("🔧 버튼 및 메뉴 이벤트 바인딩됨");

  /* 새 사고 등록 */
  document.getElementById("newIncidentBtn")?.addEventListener("click", () => {
    window.location.href = "incident_register.html";
  });

  /* 사고 분석 */
  document.getElementById("analysisBtn")?.addEventListener("click", () => {
    showToast("아직 서비스 개발 중입니다.", "info");
  });

  /* 전체 보기 */
  document.getElementById("viewAllIncidentsBtn")?.addEventListener("click", () => {
    window.location.href = "incidents.html";
  });

  /* 사이드바 - 내 정보 */
  document
    .querySelector('a[data-section="profile"]')
    ?.addEventListener("click", (e) => {
      e.preventDefault();
      showSection("profile");
      document.querySelectorAll(".sidebar-nav a").forEach((a) => a.classList.remove("active"));
      e.currentTarget.classList.add("active");
    });

  /* 사이드바 - 보고서 */
  document
    .querySelector('a[data-section="reports"]')
    ?.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "report.html";
    });

  /* 사이드바 - 대시보드 */
  document
    .querySelector('a[data-section="dashboard"]')
    ?.addEventListener("click", (e) => {
      e.preventDefault();
      showSection("dashboard");
      document.querySelectorAll(".sidebar-nav a").forEach((a) => a.classList.remove("active"));
      e.currentTarget.classList.add("active");
    });
}

/* ---------------------------------------
   ✅ 예쁜 토스트 메시지
--------------------------------------- */
function showToast(text, type = "info") {
  const existingToast = document.querySelector(".toast-message");
  if (existingToast) existingToast.remove();

  const toast = document.createElement("div");
  toast.className = `toast-message ${type}`;
  toast.textContent = text;
  document.body.appendChild(toast);

  setTimeout(() => toast.classList.add("show"), 100);
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 300);
  }, 2500);
}

/* ✅ 토스트 스타일 자동 삽입 */
const style = document.createElement("style");
style.innerHTML = `
.toast-message {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: #333;
  color: #fff;
  padding: 12px 18px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
  font-size: 15px;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.3s, transform 0.3s;
  z-index: 1000;
}
.toast-message.show { opacity: 1; transform: translateY(0); }
.toast-message.info { background: #4b9fff; }
.toast-message.success { background: #4caf50; }
.toast-message.error { background: #f44336; }
`;
document.head.appendChild(style);

/* ---------------------------------------
   ✅ 섹션 전환
--------------------------------------- */
function showSection(sectionId) {
  document.querySelectorAll(".content-section").forEach((sec) => {
    sec.classList.remove("active");
  });
  const target = document.getElementById(sectionId);
  if (target) target.classList.add("active");
}
