document.addEventListener("DOMContentLoaded", () => {
  
  if (!requireAuth()) return;

  const params = new URLSearchParams(window.location.search);
  const incidentId = params.get("id");

  const titleInput = document.getElementById("editTitle");
  const descInput = document.getElementById("editDesc");
  const locationInput = document.getElementById("editLocation");
  const typeInput = document.getElementById("editType");
  const timeInput = document.getElementById("editTime");
  const statusSelect = document.getElementById("editStatus");

  const editBtn = document.getElementById("editBtn");
  const saveEditBtn = document.getElementById("saveEdit");
  const cancelEditBtn = document.getElementById("cancelEdit");
  const msg = document.getElementById("msg");

  const evidenceGrid = document.getElementById("evidenceGrid");
  const deleteSelectedBtn = document.getElementById("deleteSelected");
  const evidenceForm = document.getElementById("evidenceForm");

  let editMode = false;
  let deletedEvidenceIds = new Set();

  /* ✅ 상태 맵핑 테이블 (명세서 기준) */
  const statusTextMap = {
    OPEN: "등록 완료",
    REPORT_GENERATED: "보고서 생성 완료",
    CLOSED: "종결",
  };

  const reverseStatusMap = {
    "등록 완료": "OPEN",
    "보고서 생성 완료": "REPORT_GENERATED",
    "종결": "CLOSED",
  };

  /* ------------------------------
      ✅ 사고 상세 불러오기
  ------------------------------ */
  async function loadIncident() {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`http://52.79.99.132/incidents/${incidentId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("사고 불러오기 실패");

      const data = await res.json();

      titleInput.value = data.title || "";
      descInput.value = data.description || "";
      locationInput.value = data.location || "";
      typeInput.value = data.incidentType || "";
      timeInput.value = new Date(data.happenedAt).toLocaleString();

      // ✅ 상태 코드 대소문자 방어 + 매핑
      const statusKey = (data.status || "").toUpperCase();
      const statusText = statusTextMap[statusKey] || "등록 완료";

      // ✅ select 박스가 한글 value를 가질 경우
      statusSelect.value = statusText;

      // 만약 select의 value가 영어 코드(OPEN 등)라면 아래 줄로 바꾸세요 👇
      // statusSelect.value = statusKey;

    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  }

  /* ------------------------------
      ✅ 증거자료 불러오기
  ------------------------------ */
  async function loadEvidence(editMode = false) {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(
        `http://52.79.99.132/incidents/${incidentId}/evidence-files`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (!res.ok) throw new Error("증거자료 불러오기 실패");
      const files = await res.json();

      evidenceGrid.innerHTML = "";
      if (!files.length) {
        evidenceGrid.innerHTML = `<p class="empty-text">증거자료가 없습니다.</p>`;
        return;
      }

      files.forEach((file) => {
        if (deletedEvidenceIds.has(file.id)) return;

        const card = document.createElement("div");
        card.className = "evidence-card";
        card.innerHTML = `
          ${editMode ? `<input type="checkbox" class="select-check" value="${file.id}">` : ""}
          <div class="preview">${renderFilePreview(file)}</div>
          <p class="desc">${file.description || ""}</p>
          <p class="time">${new Date(file.createdAt).toLocaleString()}</p>
        `;
        evidenceGrid.appendChild(card);
      });
    } catch (err) {
      console.error("❌ 증거자료 로드 오류:", err);
    }
  }

  /* ------------------------------
      ✅ 파일 미리보기
  ------------------------------ */
  function renderFilePreview(file) {
    if (file.fileType.startsWith("image")) {
      return `<img src="${file.fileUrl}" alt="evidence" class="thumb" onclick="openImageViewer('${file.fileUrl}')">`;
    } else if (file.fileType.startsWith("video")) {
      return `<video src="${file.fileUrl}" class="thumb" controls></video>`;
    } else {
      return `<a href="${file.fileUrl}" target="_blank" class="file-link">📄 파일 보기</a>`;
    }
  }

  /* ------------------------------
      ✅ 이미지 팝업
  ------------------------------ */
  window.openImageViewer = function (imageUrl) {
    if (document.querySelector(".image-viewer")) return;
    const viewer = document.createElement("div");
    viewer.className = "image-viewer";
    viewer.innerHTML = `
      <span class="image-viewer-close" onclick="closeImageViewer()">×</span>
      <img src="${imageUrl}" alt="Preview Image">
    `;
    document.body.appendChild(viewer);
  };

  window.closeImageViewer = function () {
    const viewer = document.querySelector(".image-viewer");
    if (viewer) viewer.remove();
  };

  /* ------------------------------
      ✅ 수정 모드 토글
  ------------------------------ */
  function toggleForm(state) {
    editMode = state;
    [titleInput, descInput, locationInput, typeInput, statusSelect].forEach(
      (input) => (input.disabled = !state)
    );
    editBtn.style.display = state ? "none" : "inline-block";
    saveEditBtn.style.display = state ? "inline-block" : "none";
    cancelEditBtn.style.display = state ? "inline-block" : "none";
    deleteSelectedBtn.style.display = state ? "inline-block" : "none";
    loadEvidence(state);
  }

  editBtn.addEventListener("click", () => toggleForm(true));
  cancelEditBtn.addEventListener("click", () => {
    toggleForm(false);
    deletedEvidenceIds.clear();
    loadIncident();
  });

  /* ------------------------------
      ✅ 수정 저장 (PUT)
  ------------------------------ */
  saveEditBtn.addEventListener("click", async () => {
    try {
      const token = localStorage.getItem("token");

      // ✅ 선택된 증거 삭제
      for (let id of deletedEvidenceIds) {
        await fetch(`http://52.79.99.132/incidents/${incidentId}/evidence-files/${id}`, {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      // ✅ 상태를 영어 코드로 변환
      const selectedStatus = reverseStatusMap[statusSelect.value] || "OPEN";

      const body = {
        title: titleInput.value,
        description: descInput.value,
        location: locationInput.value,
        incidentType: typeInput.value,
        status: selectedStatus,
      };

      const res = await fetch(`http://52.79.99.132/incidents/${incidentId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(body),
      });

      if (!res.ok) throw new Error("수정 실패");

      msg.innerText = "✅ 수정 완료되었습니다.";
      msg.style.color = "green";
      deletedEvidenceIds.clear();

      toggleForm(false);
      await loadIncident();
      await loadEvidence(false);

      // ✅ 2초 후 목록 페이지로 이동
      setTimeout(() => {
        window.location.href = "incidents.html";
      }, 2000);
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });

  /* ------------------------------
      ✅ 증거자료 업로드
  ------------------------------ */
  evidenceForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const fileInput = document.getElementById("fileInput");
    const descInput = document.getElementById("fileDesc");

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("description", descInput.value);

    try {
      const res = await fetch(
        `http://52.79.99.132/incidents/${incidentId}/evidence-files`,
        {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
          body: formData,
        }
      );
      if (!res.ok) throw new Error("증거자료 업로드 실패");

      msg.innerText = "✅ 증거자료 업로드 성공";
      msg.style.color = "green";
      fileInput.value = "";
      descInput.value = "";
      loadEvidence(editMode);
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });

  /* ------------------------------
      ✅ 증거자료 삭제 선택
  ------------------------------ */
  deleteSelectedBtn.addEventListener("click", () => {
    const checked = document.querySelectorAll(".select-check:checked");
    if (checked.length === 0) {
      msg.innerText = "⚠️ 삭제할 증거자료를 선택하세요.";
      msg.style.color = "orange";
      return;
    }

    checked.forEach((box) => {
      deletedEvidenceIds.add(box.value);
      box.closest(".evidence-card").remove();
    });

    msg.innerText = "🗑️ 선택한 증거자료가 삭제 예정입니다. 저장 시 반영됩니다.";
    msg.style.color = "#ffb347";
  });

  /* ------------------------------
      ✅ 초기 로드
  ------------------------------ */
  loadIncident();
  loadEvidence(false);
});

/* ------------------------------
    ✅ 증거자료 전체보기 팝업 (PRO)
------------------------------ */
const evidenceModal = document.getElementById("evidenceModal");
const evidenceListContainer = document.getElementById("evidenceListContainer");
const closeEvidenceModal = document.getElementById("closeEvidenceModal");
const openEvidenceModal = document.getElementById("openEvidenceModal");

const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const prevPageBtn = document.getElementById("prevPage");
const nextPageBtn = document.getElementById("nextPage");
const pageIndicator = document.getElementById("pageIndicator");

let allEvidence = [];
let filteredEvidence = [];
let currentPage = 1;
const ITEMS_PER_PAGE = 12;

/* 팝업 열기 */
openEvidenceModal.addEventListener("click", async () => {
  evidenceModal.classList.remove("hidden");
  await loadEvidenceModal();
});

/* 팝업 닫기 */
closeEvidenceModal.addEventListener("click", () => {
  evidenceModal.classList.add("hidden");
});

/* 증거자료 불러오기 */
async function loadEvidenceModal() {
  try {
    const token = localStorage.getItem("token");
    const res = await fetch(`http://52.79.99.132/incidents/${incidentId}/evidence-files`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) throw new Error("증거자료를 불러올 수 없습니다.");
    allEvidence = await res.json();

    applyFilters();
  } catch (err) {
    console.error("모달 증거자료 로드 오류:", err);
  }
}

/* 검색 & 정렬 적용 */
function applyFilters() {
  const keyword = searchInput.value.trim().toLowerCase();
  const sortType = sortSelect.value;

  filteredEvidence = allEvidence.filter((file) => {
    const desc = (file.description || "").toLowerCase();
    const type = (file.fileType || "").toLowerCase();
    return desc.includes(keyword) || type.includes(keyword);
  });

  // 정렬
  if (sortType === "latest") {
    filteredEvidence.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else if (sortType === "oldest") {
    filteredEvidence.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
  } else if (sortType === "image") {
    filteredEvidence = filteredEvidence.filter((f) => f.fileType.startsWith("image"));
  } else if (sortType === "video") {
    filteredEvidence = filteredEvidence.filter((f) => f.fileType.startsWith("video"));
  }

  currentPage = 1;
  renderEvidenceList();
}

/* 페이지 렌더링 */
function renderEvidenceList() {
  evidenceListContainer.innerHTML = "";

  if (!filteredEvidence.length) {
    evidenceListContainer.innerHTML = `<p style="color:#bbb;text-align:center;">검색 결과가 없습니다.</p>`;
    pageIndicator.textContent = "0 / 0";
    return;
  }

  const start = (currentPage - 1) * ITEMS_PER_PAGE;
  const end = start + ITEMS_PER_PAGE;
  const pageItems = filteredEvidence.slice(start, end);

  pageItems.forEach((file) => {
    const div = document.createElement("div");
    div.className = "evidence-item";
    div.innerHTML = `
      ${
        file.fileType.startsWith("image")
          ? `<img src="${file.fileUrl}" alt="evidence" onclick="openImageViewer('${file.fileUrl}')">`
          : file.fileType.startsWith("video")
          ? `<video src="${file.fileUrl}" controls></video>`
          : `<a href="${file.fileUrl}" target="_blank" class="file-link">📄 파일 보기</a>`
      }
      <p class="desc">${file.description || ""}</p>
      <p class="time">${new Date(file.createdAt).toLocaleString()}</p>
    `;
    evidenceListContainer.appendChild(div);
  });

  const totalPages = Math.ceil(filteredEvidence.length / ITEMS_PER_PAGE);
  pageIndicator.textContent = `${currentPage} / ${totalPages}`;
  prevPageBtn.disabled = currentPage === 1;
  nextPageBtn.disabled = currentPage === totalPages;
}

/* 페이지 버튼 이벤트 */
prevPageBtn.addEventListener("click", () => {
  if (currentPage > 1) {
    currentPage--;
    renderEvidenceList();
  }
});
nextPageBtn.addEventListener("click", () => {
  const totalPages = Math.ceil(filteredEvidence.length / ITEMS_PER_PAGE);
  if (currentPage < totalPages) {
    currentPage++;
    renderEvidenceList();
  }
});

/* 실시간 검색 / 정렬 */
searchInput.addEventListener("input", applyFilters);
sortSelect.addEventListener("change", applyFilters);

