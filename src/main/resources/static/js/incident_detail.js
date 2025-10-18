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

  let editMode = false;
  let deletedEvidenceIds = new Set();

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

      evidenceGrid.innerHTML = "";
      if (!files.length) {
        evidenceGrid.innerHTML = `<p class="empty-text">증거자료가 없습니다.</p>`;
        return;
      }

      for (const file of files) {
        if (deletedEvidenceIds.has(file.id)) continue;

        const card = document.createElement("div");
        card.className = "evidence-card";

        if (editMode) {
          const check = document.createElement("input");
          check.type = "checkbox";
          check.className = "select-check";
          check.value = file.id;
          card.appendChild(check);
        }

        const previewEl = await renderFilePreview(file);
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

  /* ✅ 파일 미리보기 (권한 무시 + 다운로드 지원) */
  async function renderFilePreview(file) {
    const token = localStorage.getItem("token");
    const container = document.createElement("div");
    container.classList.add("preview-container");
    container.innerHTML = `<p style="color:#bbb;">로딩 중...</p>`;

    try {
      const res = await fetch(file.fileUrl, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("썸네일 불러오기 실패");

      const blob = await res.blob();
      const objectURL = URL.createObjectURL(blob);

      if (file.fileType.startsWith("image")) {
        container.innerHTML = `
          <img src="${objectURL}" alt="evidence" class="thumb" onclick="openImageViewer('${objectURL}')">
          <a href="${objectURL}" download="${file.originalFileName || 'image.jpg'}" class="download-link">⬇️ 다운로드</a>
        `;
      } else if (file.fileType.startsWith("video")) {
        container.innerHTML = `
          <video src="${objectURL}" class="thumb" muted loop></video>
          <div class="video-actions">
            <button class="btn small" onclick="openVideoViewer('${objectURL}')">▶️ 보기</button>
            <a href="${objectURL}" download="${file.originalFileName || 'video.mp4'}" class="download-link">⬇️ 다운로드</a>
          </div>
        `;
      } else {
        container.innerHTML = `
          <a href="${objectURL}" target="_blank" class="file-link">📄 파일 보기</a>
          <a href="${objectURL}" download="${file.originalFileName || 'document'}" class="download-link">⬇️ 다운로드</a>
        `;
      }
    } catch (err) {
      console.error("미리보기 로드 오류:", err);
      container.innerHTML = `<p style="color:#f66;">불러오기 실패</p>`;
    }

    return container;
  }

  /* ✅ 이미지/비디오 팝업 */
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

  window.openVideoViewer = function (videoUrl) {
    if (document.querySelector(".video-viewer")) return;
    const viewer = document.createElement("div");
    viewer.className = "video-viewer";
    viewer.innerHTML = `
      <span class="video-viewer-close" onclick="closeVideoViewer()">×</span>
      <video src="${videoUrl}" controls autoplay></video>
    `;
    document.body.appendChild(viewer);
  };
  window.closeVideoViewer = function () {
    const viewer = document.querySelector(".video-viewer");
    if (viewer) viewer.remove();
  };

  /* ✅ 수정 모드 토글 */
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
    deletedEvidenceIds.clear();
    loadIncident();
  });

  /* ✅ 수정 저장 (PUT + 삭제 반영) */
  saveEditBtn.addEventListener("click", async () => {
    try {
      const token = localStorage.getItem("token");

      // 증거자료 삭제
      for (let id of deletedEvidenceIds) {
        await fetch(`http://52.79.99.132/evidence-files/${id}`, {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        });
      }

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

      showToast("수정 완료되었습니다.", "success");
      deletedEvidenceIds.clear();
      toggleForm(false);
      await loadIncident();
      await loadEvidence(false);

      setTimeout(() => (window.location.href = "incidents.html"), 2000);
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
      const res = await fetch(
        `http://52.79.99.132/incidents/${incidentId}/evidence-files`,
        {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
          body: formData,
        }
      );
      if (!res.ok) throw new Error("증거자료 업로드 실패");

      showToast("✅ 증거자료 업로드 성공", "success");
      fileInput.value = "";
      descInput.value = "";
      loadEvidence(editMode);
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  });

  /* ✅ 선택 삭제 */
  deleteSelectedBtn.addEventListener("click", () => {
    const checked = document.querySelectorAll(".select-check:checked");
    if (!checked.length) {
      showToast("삭제할 증거자료를 선택하세요.", "warning");
      return;
    }

    checked.forEach((box) => {
      deletedEvidenceIds.add(box.value);
      box.closest(".evidence-card").remove();
    });

    showToast("선택한 증거자료가 삭제 예정입니다. 저장 시 반영됩니다.", "warning");
  });

  loadIncident();
  loadEvidence(false);
});

/* ✅ 토스트 */
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
