// ✅ 보고서 관리 페이지 JS (유형별 보고서 목록 필터링 완전 버전)
let currentReports = [];
let currentIncidents = [];
let selectedIncidentId = null;

document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return; // 로그인 여부 확인
  loadData(false);
  setupEventListeners();
});

/* ==============================
   🔹 데이터 로드
============================== */
async function loadData(showToast = false) {
  try {
    await loadIncidents();
    await loadReports();
    updateStats();
    displayReports();
    if (showToast) showMessage("새로고침 완료", "success");
  } catch (error) {
    showMessage("데이터 로드 실패: " + error.message, "error");
  }
}

/* ==============================
   🔹 사고 / 보고서 로드
============================== */
async function loadIncidents() {
  try {
    const res = await fetch("http://52.79.99.132/incidents", {
      headers: getAuthHeaders(),
    });
    if (!res.ok) throw new Error("사고 목록을 불러올 수 없습니다.");
    currentIncidents = await res.json();
  } catch (err) {
    console.error("사고 목록 로드 실패:", err);
    currentIncidents = [];
  }
}

async function loadReports() {
  try {
    const allReports = [];
    for (const incident of currentIncidents) {
      const res = await fetch(`http://52.79.99.132/incidents/${incident.id}/reports`, {
        headers: getAuthHeaders(),
      });
      if (res.ok) {
        const reports = await res.json();
        reports.forEach((r) => {
          allReports.push({
            ...r,
            incidentId: incident.id,
            incidentTitle: incident.title,
            incidentType: incident.incidentType,
            downloadUrl: r.pdfUrl || (
           r.fileName
           ? `http://52.79.99.132/files/report/${encodeURIComponent(r.fileName)}`
            : null
            ),

          });
        });
      }
    }
    currentReports = allReports;
  } catch (err) {
    console.error("보고서 목록 로드 실패:", err);
    currentReports = [];
  }
}

/* ==============================
   🔹 보고서 생성
============================== */
async function generateReport() {
  if (!selectedIncidentId) {
    showMessage("사고를 선택하세요.", "warning");
    return;
  }

  try {
    showMessage("보고서 생성 중... (약 1분 소요)", "info");

    const res = await fetch(`http://52.79.99.132/incidents/${selectedIncidentId}/reports`, {
      method: "POST",
      headers: getAuthHeaders(),
    });

    if (!res.ok) throw new Error("보고서 생성 실패");

    await loadReports();
    displayReports();
    updateStats();

    showMessage("보고서가 성공적으로 생성되었습니다.", "success");
    closeGenerateModal();
  } catch (err) {
    console.error(err);
    showMessage("보고서 생성 실패: " + err.message, "error");
  }
}

/* ==============================
   🔹 보고서 목록 렌더링 (유형별 필터 적용)
============================== */
function displayReports() {
  const list = document.getElementById("reportsList");
  list.innerHTML = "";

  const selectedType = document.getElementById("typeFilter")?.value || "";
  const searchKeyword = document.getElementById("searchInput")?.value.trim().toLowerCase() || "";

  let filteredReports = currentReports;

  // 🔹 유형 필터 적용
  if (selectedType) {
    filteredReports = filteredReports.filter((r) => r.incidentType === selectedType);
  }

  // 🔹 제목 검색 필터 적용
  if (searchKeyword) {
    filteredReports = filteredReports.filter((r) =>
      (r.incidentTitle || "").toLowerCase().includes(searchKeyword)
    );
  }

  if (filteredReports.length === 0) {
    list.innerHTML = "<p class='no-data'>등록된 보고서가 없습니다.</p>";
    return;
  }

  filteredReports.forEach((r) => {
    const card = document.createElement("div");
    card.className = "report-card";
    const dateStr = formatDate(r.generatedAt || r.createdAt);

    card.innerHTML = `
      <div class="report-row">
        <div class="report-left">
          <h3>${escapeHtml(r.incidentTitle || "제목 없음")}</h3>
          <p class="report-type">유형: ${convertType(r.incidentType)}</p>
        </div>
        <div class="report-center">
          <p class="report-date">${dateStr}</p>
        </div>
        <div class="report-right">
          ${
            r.downloadUrl
              ? `<button class="btn-small" onclick="window.open('${r.downloadUrl}', '_blank')">다운로드</button>`
              : `<button class="btn-small" disabled>PDF 없음</button>`
          }
          <button class="btn-small danger" onclick="deleteReport('${r.incidentId}','${r.id}')">삭제</button>
        </div>
      </div>
    `;
    list.appendChild(card);
  });
}

/* ==============================
   🔹 보고서 삭제
============================== */
async function deleteReport(incidentId, reportId) {
  if (!confirm("정말 삭제하시겠습니까?")) return;
  try {
    const res = await fetch(
      `http://52.79.99.132/incidents/${incidentId}/reports/${reportId}`,
      {
        method: "DELETE",
        headers: getAuthHeaders(),
      }
    );

    if (!res.ok && res.status !== 204) throw new Error("삭제 실패");

    currentReports = currentReports.filter(
      (r) => !(r.incidentId === incidentId && r.id === reportId)
    );
    displayReports();
    updateStats();
    showMessage("보고서가 삭제되었습니다.", "success");
  } catch (err) {
    showMessage("삭제 실패: " + err.message, "error");
  }
}

/* ==============================
   🔹 사고 선택 테이블 (모달용)
============================== */
function populateIncidentTable() {
  const tbody = document.getElementById("incidentTableBody");
  tbody.innerHTML = "";

  if (currentIncidents.length === 0) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:#ccc;">등록된 사고가 없습니다</td></tr>`;
    return;
  }

  currentIncidents.forEach((i) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${escapeHtml(i.title || "제목 없음")}</td>
      <td>${convertType(i.incidentType)}</td>
      <td>${escapeHtml(i.location || "미기재")}</td>
      <td>${formatDate(i.happenedAt)}</td>
    `;
    tr.addEventListener("click", () => selectIncident(tr, i.id));
    tbody.appendChild(tr);
  });
}

function selectIncident(row, id) {
  document.querySelectorAll(".incident-table tbody tr").forEach((r) => r.classList.remove("selected"));
  row.classList.add("selected");
  selectedIncidentId = id;
}

/* ==============================
   🔹 이벤트
============================== */
function setupEventListeners() {
  const modal = document.getElementById("generateModal");

  // 🔹 검색창 입력 시 실시간 반영
  document.getElementById("searchInput").addEventListener("input", displayReports);

  // 새로고침
  document.getElementById("refreshBtn").addEventListener("click", () => loadData(true));

  // 새 보고서 생성 → 모달 열기
  document.getElementById("generateNewReport").addEventListener("click", async () => {
    await loadIncidents();
    populateIncidentTable();
    selectedIncidentId = null;

    modal.style.display = "flex";
    modal.classList.add("window-mode");
  });

  // ✅ 유형 필터 변경 시 보고서 목록 즉시 갱신
  document.getElementById("typeFilter").addEventListener("change", displayReports);

  // 보고서 생성 버튼
  document.getElementById("confirmGenerate").addEventListener("click", generateReport);

  // ESC로 닫기
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeGenerateModal();
  });

  // 배경 클릭 닫기
  modal.addEventListener("click", (e) => {
    if (e.target === modal) closeGenerateModal();
  });
}

/* ==============================
   🔹 공통 함수
============================== */
function updateStats() {
  document.getElementById("totalReports").textContent = currentReports.length;
  const completed = currentReports.filter(
    (r) =>
      (r.status || "").toUpperCase() === "완료" ||
      (r.status || "").toUpperCase() === "COMPLETED"
  ).length;
  document.getElementById("completedReports").textContent = completed;
  document.getElementById("pendingReports").textContent =
    currentReports.length - completed;
}

function formatDate(iso) {
  if (!iso) return "날짜 없음";
  const d = new Date(iso);
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(d.getHours()).padStart(2, "0");
  const mi = String(d.getMinutes()).padStart(2, "0");
  return `${d.getFullYear()}. ${mm}. ${dd} ${hh}:${mi}`;
}

function convertType(t) {
  return (
    {
      OIL_SPILL: "유류 유출",
      COLLISION: "충돌",
      FIRE: "화재",
      CREW_INJURY: "선원 인명피해",
    }[t] || "기타"
  );
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (m) => {
    return (
      {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      }[m] || m
    );
  });
}

/* ==============================
   🔹 모달 제어
============================== */
function closeGenerateModal() {
  const modal = document.getElementById("generateModal");
  modal.style.display = "none";
  modal.classList.remove("window-mode");
}

/* ==============================
   🔹 토스트 메시지
============================== */
function showMessage(msg, type = "info") {
  if (msg.includes("로드")) return;

  let container = document.getElementById("toastContainer");
  if (!container) {
    container = document.createElement("div");
    container.id = "toastContainer";
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.textContent = msg;
  container.appendChild(toast);

  requestAnimationFrame(() => toast.classList.add("show"));
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 300);
  }, 2500);
}
