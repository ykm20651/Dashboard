document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const incidentId = urlParams.get('id');
  
  if (!incidentId) {
    document.getElementById("msg").innerText = "❌ 사고 ID가 없습니다.";
    return;
  }

  loadIncidentDetail(incidentId);
});

async function loadIncidentDetail(incidentId) {
  try {
    // 01-03 API: 사고 상세 조회
    const incident = await apiCall(`/incidents/${incidentId}`);
    
    // 사고 상세 정보 렌더링 (IncidentDetailResponse DTO 구조에 맞춤)
    document.getElementById("incidentTitle").textContent = incident.title;
    document.getElementById("incidentType").textContent = incident.incidentType;
    document.getElementById("location").textContent = incident.location;
    document.getElementById("happenedAt").textContent = new Date(incident.happenedAt).toLocaleString();
    document.getElementById("status").textContent = incident.status;
    document.getElementById("description").textContent = incident.description;
    
    // 증거자료 목록 렌더링
    if (incident.evidenceFiles && incident.evidenceFiles.length > 0) {
      const evidenceGrid = document.getElementById("evidenceGrid");
      evidenceGrid.innerHTML = "";
      incident.evidenceFiles.forEach(file => {
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
            <p>${file.description}</p>
          </div>
        `;
        evidenceGrid.appendChild(div);
      });
    }
    
    // 수정/삭제 버튼 이벤트 리스너
    document.getElementById("editBtn").addEventListener("click", () => {
      editIncident(incidentId);
    });
    
    document.getElementById("deleteBtn").addEventListener("click", () => {
      deleteIncident(incidentId);
    });
    
  } catch (err) {
    document.getElementById("msg").innerText = "❌ " + err.message;
    document.getElementById("msg").style.color = "red";
  }
}

async function editIncident(incidentId) {
  // 수정 폼으로 이동 (기존 데이터와 함께)
  window.location.href = `incident_register.html?edit=${incidentId}`;
}

async function deleteIncident(incidentId) {
  if (!confirm("정말 이 사고를 삭제하시겠습니까?")) return;

  try {
    await apiCall(`/incidents/${incidentId}`, {
      method: "DELETE"
    });

    alert("✅ 사고가 삭제되었습니다.");
    window.location.href = "incidents.html";
    
  } catch (err) {
    alert("❌ " + err.message);
  }
}