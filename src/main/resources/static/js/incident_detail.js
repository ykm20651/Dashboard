document.addEventListener("DOMContentLoaded", () => {
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

  // 사고 상세 불러오기
  async function loadIncident() {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`http://15.164.99.177/incidents/${incidentId}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("사고 불러오기 실패");

      const data = await res.json();
      titleInput.value = data.title;
      descInput.value = data.description;
      locationInput.value = data.location;
      typeInput.value = data.incidentType;
      timeInput.value = data.happenedAt;
      statusSelect.value = data.status;
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  }

  // 증거자료 불러오기
  async function loadEvidence(editMode = false) {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`http://15.164.99.177/incidents/${incidentId}/evidence-files`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      const files = await res.json();

      evidenceGrid.innerHTML = "";
      if (files.length === 0) {
        evidenceGrid.innerHTML = `<p class="empty-text">증거자료가 없습니다.</p>`;
        return;
      }

      files.forEach(file => {
        const card = document.createElement("div");
        card.className = "evidence-card";
        card.innerHTML = `
          ${editMode ? `<input type="checkbox" class="select-check" value="${file.id}">` : ""}
          <div class="preview">${renderFilePreview(file)}</div>
          <p class="desc">${file.description}</p>
          <p class="time">${file.createdAt}</p>
        `;
        evidenceGrid.appendChild(card);
      });
    } catch (err) {
      console.error(err);
    }
  }

  function renderFilePreview(file) {
    if (file.fileType.startsWith("image")) {
      return `<img src="${file.fileUrl}" alt="evidence" class="thumb">`;
    } else if (file.fileType.startsWith("video")) {
      return `<video src="${file.fileUrl}" class="thumb" controls></video>`;
    } else {
      return `<a href="${file.fileUrl}" target="_blank" class="file-link">📄 파일 보기</a>`;
    }
  }

  function toggleForm(state) {
    editMode = state;
    [titleInput, descInput, locationInput, typeInput, timeInput, statusSelect]
      .forEach(input => input.disabled = !state);

    editBtn.style.display = state ? "none" : "inline-block";
    saveEditBtn.style.display = state ? "inline-block" : "none";
    cancelEditBtn.style.display = state ? "inline-block" : "none";

    // 증거자료 삭제 버튼 표시
    deleteSelectedBtn.style.display = state ? "inline-block" : "none";

    // 증거자료 다시 로드
    loadEvidence(state);
  }

  editBtn.addEventListener("click", () => toggleForm(true));
  cancelEditBtn.addEventListener("click", () => {
    toggleForm(false);
    loadIncident();
  });

  saveEditBtn.addEventListener("click", async () => {
    try {
      const token = localStorage.getItem("token");
      const body = {
        title: titleInput.value,
        description: descInput.value,
        location: locationInput.value,
        incidentType: typeInput.value,
        happenedAt: timeInput.value,
        status: statusSelect.value
      };

      const res = await fetch(`http://15.164.99.177/incidents/${incidentId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error("수정 실패");

      msg.innerText = "✅ 수정 완료";
      msg.style.color = "green";
      toggleForm(false);
      loadIncident();
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });

  evidenceForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");

    const fileInput = document.getElementById("fileInput");
    const descInput = document.getElementById("fileDesc");

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("description", descInput.value);

    try {
      const res = await fetch(`http://15.164.99.177/incidents/${incidentId}/evidence-files`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` },
        body: formData
      });
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

  deleteSelectedBtn.addEventListener("click", async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      msg.innerText = "⚠️ 권한이 없습니다. 로그인 후 이용하세요.";
      msg.style.color = "orange";
      return;
    }

    const checked = document.querySelectorAll(".select-check:checked");
    if (checked.length === 0) {
      msg.innerText = "⚠️ 삭제할 증거자료를 선택하세요.";
      msg.style.color = "orange";
      return;
    }

    for (let box of checked) {
      const res = await fetch(`http://15.164.99.177/incidents/${incidentId}/evidence-files/${box.value}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
      });

      if (res.status === 401 || res.status === 403) {
        msg.innerText = "❌ 삭제 권한이 없습니다.";
        msg.style.color = "red";
        return;
      }
    }

    msg.innerText = "✅ 선택한 증거자료가 삭제되었습니다.";
    msg.style.color = "green";
    loadEvidence(true);
  });

  loadIncident();
  loadEvidence(false);
});
