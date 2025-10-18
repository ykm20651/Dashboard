document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return;

  const params = new URLSearchParams(window.location.search);
  const incidentId = params.get("id");
  window.currentIncidentId = incidentId;

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

  // 전체보기 모달 관련
  const modal = document.getElementById("evidenceModal");
  const modalGrid = document.getElementById("modalEvidenceGrid");
  const openModalBtn = document.getElementById("openEvidenceModal");
  const closeModalBtn = document.getElementById("closeEvidenceModal");
  const prevPageBtn = document.getElementById("prevPage");
  const nextPageBtn = document.getElementById("nextPage");
  const pageIndicator = document.getElementById("pageIndicator");

  let editMode = false;
  let allFiles = [];
  let currentPage = 1;

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

  /* ✅ 사고 상세 불러오기 */
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
      typeInput.value = (data.incidentType || "").toUpperCase();

      if (data.happenedAt) {
        const localDate = new Date(data.happenedAt);
        const localISO = new Date(localDate.getTime() - localDate.getTimezoneOffset() * 60000)
          .toISOString()
          .slice(0, 16);
        timeInput.value = localISO;
      } else timeInput.value = "";

      const statusKey = (data.status || "").toUpperCase();
      const statusText = statusTextMap[statusKey] || "등록 완료";
      statusSelect.value = statusText;
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  }

  /* ✅ 증거자료 불러오기 */
  async function loadEvidence(editMode = false) {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`http://52.79.99.132/incidents/${incidentId}/evidence-files`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("증거자료 불러오기 실패");

      const files = await res.json();
      allFiles = files;
      evidenceGrid.innerHTML = "";

      if (!files.length) {
        evidenceGrid.innerHTML = `<p class="empty-text">증거자료가 없습니다.</p>`;
        return;
      }

      for (const file of files) {
        const card = document.createElement("div");
        card.className = "evidence-card file-wrapper";

        // ✅ 선택 체크박스 (편집 모드일 때만)
        if (editMode) {
          const check = document.createElement("input");
          check.type = "checkbox";
          check.className = "select-check";
          check.value = file.id;
          card.appendChild(check);
        }

        // ✅ 다운로드 버튼만 유지
        const actions = document.createElement("div");
        actions.className = "file-actions";
        actions.innerHTML = `<a href="${file.fileUrl}" download class="icon-btn">⬇️</a>`;
        card.appendChild(actions);

        // ✅ 썸네일
        const previewEl = await renderFileThumbnail(file);
        card.appendChild(previewEl);

        const desc = document.createElement("p");
        desc.className = "desc";
        desc.textContent = file.description || "";
        card.appendChild(desc);

        const time = document.createElement("p");
        time.className = "time";
        time.textContent = new Date(file.createdAt).toLocaleString();
        card.appendChild(time);

        evidenceGrid.appendChild(card);
      }
    } catch (err) {
      console.error("증거자료 로드 오류:", err);
    }
  }

  /* ✅ 썸네일 렌더링 */
  async function renderFileThumbnail(file) {
    const container = document.createElement("div");
    container.classList.add("preview-container");

    if (file.fileType.startsWith("image")) {
      container.innerHTML = `<img src="${file.fileUrl}" alt="evidence" class="thumb">`;
    } else if (file.fileType.startsWith("video")) {
      container.innerHTML = `<video src="${file.fileUrl}" class="thumb" muted loop></video>`;
    } else {
      container.innerHTML = `<p>${file.originalFileName}</p>`;
    }
    return container;
  }

  /* ✅ 수정 모드 */
  function toggleForm(state) {
    editMode = state;
    [titleInput, descInput, locationInput, typeInput, statusSelect, timeInput].forEach(
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
    loadIncident();
  });

  /* ✅ 수정 저장 */
  saveEditBtn.addEventListener("click", async () => {
    try {
      const token = localStorage.getItem("token");
      const selectedStatus = reverseStatusMap[statusSelect.value] || "OPEN";
      const happenedAtISO = timeInput.value ? new Date(timeInput.value).toISOString() : null;

      const body = {
        title: titleInput.value,
        description: descInput.value,
        location: locationInput.value,
        incidentType: typeInput.value.toUpperCase(),
        happenedAt: happenedAtISO,
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

      showToast("✅ 수정 완료", "success");
      toggleForm(false);
      await loadIncident();
      await loadEvidence(false);
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  });

  /* ✅ 증거자료 업로드 */
  evidenceForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const fileInput = document.getElementById("fileInput");
    const descInput = document.getElementById("fileDesc");

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("description", descInput.value);

    try {
      const res = await fetch(`http://52.79.99.132/incidents/${incidentId}/evidence-files`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });
      if (!res.ok) throw new Error("증거자료 업로드 실패");

      showToast("✅ 업로드 성공", "success");
      fileInput.value = "";
      descInput.value = "";
      loadEvidence(editMode);
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  });

  /* ✅ 선택 삭제 (실시간 서버 반영) */
  deleteSelectedBtn.addEventListener("click", async () => {
    const checked = document.querySelectorAll(".select-check:checked");
    if (!checked.length) {
      showToast("삭제할 증거자료를 선택하세요.", "warning");
      return;
    }

    if (!confirm("선택한 증거자료를 삭제하시겠습니까?")) return;

    try {
      const token = localStorage.getItem("token");
      for (const box of checked) {
        await fetch(`http://52.79.99.132/evidence-files/${box.value}`, {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      showToast("✅ 선택한 증거자료가 삭제되었습니다.", "success");
      loadEvidence(editMode);
    } catch (err) {
      showToast("❌ 삭제 실패", "error");
    }
  });

  /* ✅ 전체보기 모달 */
  openModalBtn.addEventListener("click", async () => {
    document.body.classList.add("modal-open");
    modal.classList.remove("hidden");
    await loadEvidence(false);
    renderModalPage(1);
  });
  closeModalBtn.addEventListener("click", () => {
    modal.classList.add("hidden");
    document.body.classList.remove("modal-open");
  });

  function renderModalPage(page) {
    modalGrid.innerHTML = "";
    const perPage = 10;
    const total = Math.ceil(allFiles.length / perPage);
    const start = (page - 1) * perPage;
    const slice = allFiles.slice(start, start + perPage);

    slice.forEach((f) => {
      const div = document.createElement("div");
      div.className = "modal-item file-wrapper";
      div.innerHTML = `
        <div class="file-actions">
          <a href="${f.fileUrl}" download class="icon-btn">⬇️</a>
        </div>
        ${
          f.fileType.startsWith("image")
            ? `<img src="${f.fileUrl}" class="thumb">`
            : f.fileType.startsWith("video")
            ? `<video src="${f.fileUrl}" class="thumb" muted loop></video>`
            : `<p>${f.originalFileName}</p>`
        }
        <p class="desc">${f.description || ""}</p>
      `;
      modalGrid.appendChild(div);
    });

    pageIndicator.textContent = `${page} / ${total}`;
    currentPage = page;
    prevPageBtn.disabled = page === 1;
    nextPageBtn.disabled = page === total;
  }

  prevPageBtn.addEventListener("click", () => {
    if (currentPage > 1) renderModalPage(currentPage - 1);
  });
  nextPageBtn.addEventListener("click", () => {
    const total = Math.ceil(allFiles.length / 10);
    if (currentPage < total) renderModalPage(currentPage + 1);
  });

  loadIncident();
  loadEvidence(false);
});

/* ✅ 토스트 메시지 */
function showToast(message, type = "info") {
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);
  setTimeout(() => (toast.style.opacity = "1"), 50);
  setTimeout(() => {
    toast.style.opacity = "0";
    setTimeout(() => toast.remove(), 500);
  }, 2200);
}
