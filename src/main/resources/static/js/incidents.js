document.addEventListener("DOMContentLoaded", () => {
  
  if (!requireAuth()) return; // 로그인 확인

  const incidentList = document.getElementById("incidentList");
  const pagination = document.getElementById("pagination");
  let allIncidents = [];
  const rowsPerPage = 5;

  function getStatusText(status = "") {
    const key = status.toUpperCase();
    const map = {
      OPEN: "등록 완료",
      REPORT_GENERATED: "보고서 생성 완료",
      CLOSED: "종결",
    };
    return map[key] || status;
  }

  // ✅ 사고 목록 로드
  async function loadIncidents() {
    try {
      const res = await fetch(`http://52.79.99.132/incidents`, {
        method: "GET",
        headers: getAuthHeaders(),
      });

      if (!res.ok) throw new Error("사고 목록을 불러올 수 없습니다.");

      allIncidents = await res.json();
      renderPage(1);
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  }

  // ✅ 페이지 렌더링
  function renderPage(page) {
    incidentList.innerHTML = "";

    const start = (page - 1) * rowsPerPage;
    const end = start + rowsPerPage;
    const paginated = allIncidents.slice(start, end);

    if (paginated.length === 0) {
      incidentList.innerHTML = `<tr><td colspan="6">등록된 사고가 없습니다.</td></tr>`;
      pagination.innerHTML = "";
      return;
    }

    paginated.forEach((i) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${i.title}</td>
        <td>${i.incidentType}</td>
        <td>${i.location}</td>
        <td>${new Date(i.happenedAt).toLocaleString()}</td>
        <td>${getStatusText(i.status)}</td>
        <td>
          <button class="btn tiny" onclick="viewDetail('${i.id}')">상세</button>
          <button class="btn tiny" onclick="viewReport('${i.id}')">보고서</button>
          <button class="btn tiny danger" onclick="deleteIncident('${i.id}')">삭제</button>
        </td>`;
      incidentList.appendChild(tr);
    });

    renderPagination(page);
  }

 // ✅ 페이지네이션 버튼 생성
function renderPagination(currentPage) {
  const totalPages = Math.ceil(allIncidents.length / rowsPerPage);
  pagination.innerHTML = "";

  if (totalPages <= 1) return;

  // ◀ 이전 버튼
  const prevBtn = document.createElement("button");
  prevBtn.textContent = "◀";
  prevBtn.classList.add("nav-btn");
  prevBtn.disabled = currentPage === 1;
  prevBtn.addEventListener("click", () => renderPage(currentPage - 1));
  pagination.appendChild(prevBtn);

  // 페이지 번호 버튼
  for (let i = 1; i <= totalPages; i++) {
    const btn = document.createElement("button");
    btn.textContent = i;
    if (i === currentPage) btn.classList.add("active");
    btn.addEventListener("click", () => renderPage(i));
    pagination.appendChild(btn);
  }

  // ▶ 다음 버튼
  const nextBtn = document.createElement("button");
  nextBtn.textContent = "▶";
  nextBtn.classList.add("nav-btn");
  nextBtn.disabled = currentPage === totalPages;
  nextBtn.addEventListener("click", () => renderPage(currentPage + 1));
  pagination.appendChild(nextBtn);
}

  window.viewDetail = (id) => (window.location.href = `incident_detail.html?id=${id}`);
  window.viewReport = (id) => (window.location.href = `report.html?incidentId=${id}`);

  window.deleteIncident = async (id) => {
    if (!confirm("정말 이 사고를 삭제하시겠습니까?")) return;

    try {
      const res = await fetch(`http://52.79.99.132/incidents/${id}`, {
        method: "DELETE",
        headers: getAuthHeaders(),
      });

      if (res.status === 401 || res.status === 403) {
        showToast("삭제 권한이 없습니다.", "error");
        return;
      }

      if (res.status === 404) {
        showToast("이미 삭제된 사고입니다.", "warning");
        return;
      }

      if (!res.ok) throw new Error("사고 삭제 실패");

      allIncidents = allIncidents.filter((i) => i.id !== id);
      renderPage(1);
      showToast("사고가 성공적으로 삭제되었어요.", "success");
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  };

  loadIncidents();
});
