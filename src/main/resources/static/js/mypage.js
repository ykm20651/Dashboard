/* =======================================
   OASIS | MyPage 통합 스크립트 (DB 연동 버전)
   ======================================= */

let currentIncidents = [];
let currentReports = [];
let currentUser = null;

/* ---------------------------------------
   ✅ 초기 실행
--------------------------------------- */
document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ OASIS MyPage Loaded");

  if (!requireAuth()) return; // 로그인 여부 확인

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

/* ✅ 사고 목록 불러오기 (실제 DB) */
async function loadIncidents() {
  try {
    const res = await fetch("http://52.79.99.132/incidents", {
      headers: getAuthHeaders ? getAuthHeaders() : {},
    });
    if (!res.ok) throw new Error("사고 불러오기 실패");
    currentIncidents = await res.json();
  } catch (err) {
    showToast("사고 데이터를 불러올 수 없습니다.", "error");
    currentIncidents = [];
  }
}

/* ✅ 보고서 목록 불러오기 (실제 DB) */
async function loadReports() {
  try {
    const res = await fetch("http://52.79.99.132/reports", {
      headers: getAuthHeaders ? getAuthHeaders() : {},
    });
    if (!res.ok) throw new Error("보고서 불러오기 실패");
    currentReports = await res.json();
  } catch (err) {
    showToast("보고서 데이터를 불러올 수 없습니다.", "error");
    currentReports = [];
  }
}

/* ✅ 통계 업데이트 */
function updateDashboardStats() {
  document.getElementById("totalIncidents").textContent = currentIncidents.length;
  document.getElementById("completedIncidents").textContent =
    currentIncidents.filter((i) => i.status === "CLOSED").length;
  document.getElementById("totalReports").textContent = currentReports.length;
}

/* ✅ 최근 사고 표시 */
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
          <h4>${i.title || "제목 없음"}</h4>
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

/* ✅ 상태 한글 변환 (명세서 기준) */
function getStatusText(status = "") {
  const key = status.toUpperCase();
  const map = {
    OPEN: "등록 완료",
    REPORT_GENERATED: "보고서 생성 완료",
    CLOSED: "종결",
  };
  return map[key] || status;
}

/* ---------------------------------------
   ✅ 버튼 및 메뉴 동작
--------------------------------------- */
function bindQuickActionHandlers() {
  console.log("🔧 버튼 및 메뉴 이벤트 바인딩됨");

  document.getElementById("newIncidentBtn")?.addEventListener("click", () => {
    window.location.href = "incident_register.html";
  });

  document.getElementById("analysisBtn")?.addEventListener("click", () => {
    showToast("아직 서비스 개발 중입니다.", "info");
  });

  document.getElementById("viewAllIncidentsBtn")?.addEventListener("click", () => {
    window.open(
    "incidents.html",
    "IncidentsWindow",
    "width=1200,height=800,resizable=yes,scrollbars=yes"
  );
  });

  document
    .querySelector('a[data-section="profile"]')
    ?.addEventListener("click", (e) => {
      e.preventDefault();
      showSection("profile");
      document.querySelectorAll(".sidebar-nav a").forEach((a) => a.classList.remove("active"));
      e.currentTarget.classList.add("active");
    });

  document
    .querySelector('a[data-section="reports"]')
    ?.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "report.html";
    });

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

/* ---------------------------------------
   ✅ 내 정보(프로필) 기능 (비밀번호 선택적 변경)
   ✅ 서버 명세: { "name": "...", "password": "..." }
--------------------------------------- */
document.addEventListener("DOMContentLoaded", () => {
  const updateBtn = document.getElementById("updateProfileBtn");
  const deleteBtn = document.getElementById("deleteAccountBtn");

  /* ✅ 정보 업데이트 버튼 */
  updateBtn?.addEventListener("click", async () => {
    const name = document.getElementById("profileName").value.trim();
    const newPassword = document.getElementById("newPassword").value.trim();
    const confirmPassword = document.getElementById("confirmPassword").value.trim();

    // 🔸 비밀번호 입력 시 일치 여부 검사
    if (newPassword && newPassword !== confirmPassword) {
      showToast("비밀번호가 일치하지 않습니다", "error");
      return;
    }

    // 🔸 전송할 데이터 구성 (입력된 항목만)
    const bodyData = {};
    if (name) bodyData.name = name;
    if (newPassword) bodyData.password = newPassword;

    // 아무것도 입력 안 하면 중단
    if (Object.keys(bodyData).length === 0) {
      showToast("변경할 정보가 없습니다.", "info");
      return;
    }

    try {
      const userId = getUserId ? getUserId() : null;
      if (!userId) throw new Error("사용자 ID를 찾을 수 없습니다.");

      const res = await fetch(`http://52.79.99.132/users/${userId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          ...(getAuthHeaders ? getAuthHeaders() : {}),
        },
        body: JSON.stringify(bodyData), // ✅ {"name": "...", "password": "..."}
      });

      if (!res.ok) throw new Error("정보 업데이트 실패");

      // ✅ 성공 처리
      showToast("정보가 변경되었습니다", "info");

      // 입력칸 초기화
      document.getElementById("profileName").value = "";
      document.getElementById("newPassword").value = "";
      document.getElementById("confirmPassword").value = "";
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  });

  /* ✅ 계정 삭제 버튼 */
  deleteBtn?.addEventListener("click", () => {
    const overlay = document.createElement("div");
    overlay.className = "confirm-overlay";
    overlay.innerHTML = `
      <div class="confirm-box">
        <h3>⚠️ 정말로 삭제하시겠습니까?</h3>
        <p>이 작업은 되돌릴 수 없습니다.</p>
        <div class="confirm-actions">
          <button id="confirmYes" class="btn-danger">예</button>
          <button id="confirmNo" class="btn-secondary">아니오</button>
        </div>
      </div>
    `;
    document.body.appendChild(overlay);

    document.getElementById("confirmNo").addEventListener("click", () => overlay.remove());

    document.getElementById("confirmYes").addEventListener("click", async () => {
      try {
        const userId = getUserId ? getUserId() : null;
        if (!userId) throw new Error("사용자 ID를 찾을 수 없습니다.");

        const res = await fetch(`http://52.79.99.132/users/${userId}`, {
          method: "DELETE",
          headers: getAuthHeaders ? getAuthHeaders() : {},
        });

        if (!res.ok) throw new Error("계정 삭제 실패");

        overlay.remove();

        // ✅ 로그아웃 (토큰 제거)
        localStorage.removeItem("token");

        showToast("계정이 삭제되었습니다", "info");

        setTimeout(() => {
          window.location.href = "login.html";
        }, 1500);
      } catch (err) {
        showToast("❌ " + err.message, "error");
      }
    });
  });
});

/* ✅ 경고창 스타일 */
const confirmStyle = document.createElement("style");
confirmStyle.innerHTML = `
.confirm-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.confirm-box {
  background: #fff;
  padding: 24px 30px;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.25);
  text-align: center;
  max-width: 320px;
}
.confirm-box h3 {
  margin-bottom: 10px;
  font-size: 18px;
}
.confirm-actions {
  margin-top: 20px;
  display: flex;
  justify-content: space-around;
}
.btn-danger {
  background: #f44336;
  color: #fff;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-secondary {
  background: #ccc;
  color: #000;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-danger:hover { background: #d32f2f; }
.btn-secondary:hover { background: #b0b0b0; }
`;
document.head.appendChild(confirmStyle);
