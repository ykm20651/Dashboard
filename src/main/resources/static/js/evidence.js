// 02-01~02-04 API: 증거자료 관리
document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const incidentId = urlParams.get('incidentId');
  
  if (!incidentId) {
    document.getElementById("msg").innerText = "❌ 사고 ID가 없습니다.";
    return;
  }

  loadEvidenceFiles(incidentId);
  setupEvidenceUpload(incidentId);
});

// 02-01 API: 증거자료 목록 조회
async function loadEvidenceFiles(incidentId) {
  try {
    const evidenceFiles = await apiCall(`/incidents/${incidentId}/evidence-files`);
    
    const evidenceGrid = document.getElementById("evidenceGrid");
    evidenceGrid.innerHTML = "";
    
    if (evidenceFiles.length === 0) {
      evidenceGrid.innerHTML = "<p class='empty-text'>증거자료가 없습니다.</p>";
      return;
    }
    
    evidenceFiles.forEach(file => {
      const div = document.createElement("div");
      div.className = "evidence-item";
      div.innerHTML = `
        <div class="evidence-preview">
          ${file.fileType.startsWith('image/') ? 
            `<img src="${file.fileUrl}" alt="증거자료" style="max-width: 200px; max-height: 150px;">` :
            `<div class="file-icon">📄 ${file.fileUrl}</div>`
          }
        </div>
        <div class="evidence-info">
          <p><strong>${file.fileUrl}</strong></p>
          <p>${file.description}</p>
          <p class="file-date">${new Date(file.createdAt).toLocaleString()}</p>
        </div>
        <div class="evidence-actions">
          <button class="btn tiny" onclick="downloadEvidence('${file.id}')">다운로드</button>
          <button class="btn tiny" onclick="editEvidence('${file.id}')">수정</button>
          <button class="btn tiny danger" onclick="deleteEvidence('${file.id}')">삭제</button>
        </div>
      `;
      evidenceGrid.appendChild(div);
    });
    
  } catch (err) {
    document.getElementById("msg").innerText = "❌ " + err.message;
    document.getElementById("msg").style.color = "red";
  }
}

// 02-02 API: 증거자료 업로드
function setupEvidenceUpload(incidentId) {
  const form = document.getElementById("evidenceForm");
  const fileInput = document.getElementById("fileInput");
  const descInput = document.getElementById("fileDesc");
  
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    
    if (!fileInput.files[0]) {
      alert("파일을 선택해주세요.");
      return;
    }
    
    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("description", descInput.value);
    
    try {
      await apiCall(`/incidents/${incidentId}/evidence-files`, {
        method: "POST",
        body: formData,
        headers: {} // Content-Type을 설정하지 않음 (FormData가 자동으로 설정)
      });
      
      alert("✅ 증거자료가 업로드되었습니다.");
      fileInput.value = "";
      descInput.value = "";
      loadEvidenceFiles(incidentId); // 목록 새로고침
      
    } catch (err) {
      alert("❌ " + err.message);
    }
  });
}

// 02-03 API: 증거자료 삭제
async function deleteEvidence(fileId) {
  if (!confirm("정말 이 증거자료를 삭제하시겠습니까?")) return;

  try {
    await apiCall(`/evidence-files/${fileId}`, {
      method: "DELETE"
    });
    
    alert("✅ 증거자료가 삭제되었습니다.");
    const incidentId = new URLSearchParams(window.location.search).get('incidentId');
    loadEvidenceFiles(incidentId);
    
  } catch (err) {
    alert("❌ " + err.message);
  }
}

// 02-04 API: 증거자료 수정
async function editEvidence(fileId) {
  const newDescription = prompt("새로운 설명을 입력하세요:");
  if (!newDescription) return;

  try {
    await apiCall(`/evidence-files/${fileId}`, {
      method: "PUT",
      body: JSON.stringify({ description: newDescription })
    });
    
    alert("✅ 증거자료가 수정되었습니다.");
    const incidentId = new URLSearchParams(window.location.search).get('incidentId');
    loadEvidenceFiles(incidentId);
    
  } catch (err) {
    alert("❌ " + err.message);
  }
}

// 증거자료 다운로드
async function downloadEvidence(fileId) {
  try {
    const response = await fetch(`${API_BASE_URL}/evidence-files/${fileId}/download`, {
      headers: { "Authorization": `Bearer ${localStorage.getItem("token")}` }
    });
    
    if (!response.ok) throw new Error("다운로드 실패");
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `evidence_${fileId}`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
  } catch (err) {
    alert("❌ " + err.message);
  }
}
