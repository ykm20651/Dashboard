// 04-01~04-02 API: 맞춤형 대응 가이드 관리
document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const incidentId = urlParams.get('incidentId');
  
  if (!incidentId) {
    document.getElementById("msg").innerText = "❌ 사고 ID가 없습니다.";
    return;
  }

  loadResponseGuides(incidentId);
  setupGuideCreation(incidentId);
});

// 04-02 API: 맞춤형 대응 가이드 조회
async function loadResponseGuides(incidentId) {
  try {
    const guides = await apiCall(`/incidents/${incidentId}/response-guide`);
    
    const guideList = document.getElementById("guideList");
    guideList.innerHTML = "";
    
    if (guides.length === 0) {
      guideList.innerHTML = "<p class='empty-text'>대응 가이드가 없습니다.</p>";
      return;
    }
    
    guides.forEach(guide => {
      const div = document.createElement("div");
      div.className = "guide-item";
      div.innerHTML = `
        <div class="guide-header">
          <h3>${guide.title}</h3>
        </div>
        <div class="guide-content">
          <p><strong>사고 유형:</strong> ${guide.incidentType}</p>
          <p><strong>생성일:</strong> ${new Date(guide.createdAt).toLocaleString()}</p>
          <div class="guide-description">
            <h4>설명:</h4>
            <p>${guide.description}</p>
          </div>
          <div class="guide-checklist">
            <h4>체크리스트:</h4>
            <ul>
              ${guide.checklist.map(item => `<li>${item}</li>`).join('')}
            </ul>
          </div>
          <div class="guide-legal">
            <h4>관련 법조항:</h4>
            <p>${guide.legalClause}</p>
          </div>
          <div class="guide-actions">
            <button class="btn primary" onclick="viewGuideDetail('${guide.id}')">상세보기</button>
            <button class="btn" onclick="downloadGuide('${guide.id}')">PDF 다운로드</button>
          </div>
        </div>
      `;
      guideList.appendChild(div);
    });
    
  } catch (err) {
    document.getElementById("msg").innerText = "❌ " + err.message;
    document.getElementById("msg").style.color = "red";
  }
}

// 04-01 API: 맞춤형 대응 가이드 전략 생성
function setupGuideCreation(incidentId) {
  const createBtn = document.getElementById("createGuideBtn");
  
  if (createBtn) {
    createBtn.addEventListener("click", async () => {
      if (!confirm("이 사고에 대한 맞춤형 대응 가이드를 생성하시겠습니까?")) return;
      
      try {
        const response = await apiCall(`/incidents/${incidentId}/response-guide`, {
          method: "POST"
        });
        
        alert("✅ 대응 가이드가 생성되었습니다.");
        loadResponseGuides(incidentId); // 목록 새로고침
        
      } catch (err) {
        alert("❌ " + err.message);
      }
    });
  }
}

// 가이드 상세보기
function viewGuideDetail(guideId) {
  // 가이드 상세 페이지로 이동 (구현 필요)
  window.location.href = `guide_detail.html?id=${guideId}`;
}

// 가이드 PDF 다운로드
async function downloadGuide(guideId) {
  try {
    const response = await fetch(`${API_BASE_URL}/response-guides/${guideId}/download`, {
      headers: { "Authorization": `Bearer ${localStorage.getItem("token")}` }
    });
    
    if (!response.ok) throw new Error("다운로드 실패");
    
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `response_guide_${guideId}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
  } catch (err) {
    alert("❌ " + err.message);
  }
}
