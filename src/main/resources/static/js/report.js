// 보고서 페이지 JavaScript

let currentReports = [];
let currentIncidents = [];

document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;
  
  // 데이터 로드
  loadData();
  
  // 이벤트 리스너 설정
  setupEventListeners();
});

/**
 * 데이터 로드
 */
async function loadData() {
  try {
    await Promise.all([
      loadIncidents(),
      loadReports()
    ]);
    
    updateStats();
    displayReports();
    populateIncidentSelect();
    
  } catch (error) {
    showMessage('데이터 로드 실패: ' + error.message, 'error');
  }
}

/**
 * 사고 목록 로드
 */
async function loadIncidents() {
  try {
    const response = await fetch("http://52.79.99.132/incidents", {
      method: "GET",
      headers: getAuthHeaders()
    });
    
    await handleApiError(response, "사고 목록을 불러올 수 없습니다.");
    
    currentIncidents = await response.json();
    return currentIncidents;
  } catch (error) {
    console.error("사고 목록 로드 실패:", error);
    // 더미 데이터 사용
    currentIncidents = generateDummyIncidents();
    showMessage('더미 데이터를 사용합니다.', 'info');
    return currentIncidents;
  }
}

/**
 * 보고서 목록 로드
 */
async function loadReports() {
  try {
    const response = await fetch("http://52.79.99.132/reports", {
      method: "GET",
      headers: getAuthHeaders()
    });
    
    await handleApiError(response, "보고서 목록을 불러올 수 없습니다.");
    
    currentReports = await response.json();
    return currentReports;
  } catch (error) {
    console.error("보고서 목록 로드 실패:", error);
    // 더미 데이터 사용
    currentReports = generateDummyReports();
    showMessage('더미 데이터를 사용합니다.', 'info');
    return currentReports;
  }
}

/**
 * 통계 업데이트
 */
function updateStats() {
  const total = currentReports.length;
  const completed = currentReports.filter(r => r.status === '완료').length;
  const pending = currentReports.filter(r => r.status === '진행중' || r.status === '대기').length;
  
  document.getElementById('totalReports').textContent = total;
  document.getElementById('completedReports').textContent = completed;
  document.getElementById('pendingReports').textContent = pending;
}

/**
 * 보고서 목록 표시
 */
function displayReports() {
  const container = document.getElementById('reportsList');
  
  if (currentReports.length === 0) {
    container.innerHTML = `
      <div class="no-data">
        <p>생성된 보고서가 없습니다.</p>
        <button class="btn-primary" onclick="showGenerateModal()">
          첫 보고서 생성하기
        </button>
      </div>
    `;
    return;
  }
  
  container.innerHTML = currentReports.map(report => `
    <div class="report-card">
      <div class="report-header">
        <h3 class="report-title">${report.title}</h3>
        <span class="report-status status-${report.status.toLowerCase()}">${report.status}</span>
      </div>
      
      <div class="report-meta">
        <span>📅 생성일: ${new Date(report.generatedAt).toLocaleDateString()}</span>
        <span>🚨 관련 사고: ${report.incidentTitle}</span>
        <span>📋 유형: ${report.type}</span>
      </div>
      
      ${report.description ? `<div class="report-description">${report.description}</div>` : ''}
      
      <div class="report-actions">
        <button class="btn-primary btn-small" onclick="viewReport('${report.id}')">
          <span class="btn-icon">👁️</span>
          보기
        </button>
        <button class="btn-secondary btn-small" onclick="downloadReport('${report.id}')">
          <span class="btn-icon">⬇️</span>
          다운로드
        </button>
        <button class="btn-danger btn-small" onclick="deleteReport('${report.id}')">
          <span class="btn-icon">🗑️</span>
          삭제
        </button>
      </div>
    </div>
  `).join('');
}

/**
 * 사고 선택 드롭다운 채우기
 */
function populateIncidentSelect() {
  const select = document.getElementById('selectedIncidents');
  select.innerHTML = currentIncidents.map(incident => `
    <option value="${incident.id}">${incident.title} (${incident.incidentType})</option>
  `).join('');
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
  // 새 보고서 생성 버튼
  document.getElementById('generateNewReport').addEventListener('click', showGenerateModal);
  
  // 모달 내 확인 버튼
  document.getElementById('confirmGenerate').addEventListener('click', generateReport);
  
  // 필터 및 검색
  document.getElementById('statusFilter').addEventListener('change', filterReports);
  document.getElementById('typeFilter').addEventListener('change', filterReports);
  document.getElementById('searchInput').addEventListener('input', filterReports);
  document.getElementById('refreshBtn').addEventListener('click', refreshData);
}

/**
 * 보고서 생성 모달 표시
 */
function showGenerateModal() {
  if (currentIncidents.length === 0) {
    showMessage('먼저 사고를 등록해주세요.', 'error');
    window.location.href = 'incident_register.html';
    return;
  }
  
  // 폼 초기화
  document.getElementById('reportTitle').value = '';
  document.getElementById('selectedIncidents').selectedIndex = -1;
  document.getElementById('reportType').selectedIndex = 0;
  document.getElementById('reportDescription').value = '';
  
  document.getElementById('generateModal').style.display = 'flex';
}

/**
 * 보고서 생성 모달 닫기
 */
function closeGenerateModal() {
  document.getElementById('generateModal').style.display = 'none';
}

/**
 * 보고서 생성
 */
async function generateReport() {
  const title = document.getElementById('reportTitle').value.trim();
  const selectedIncidents = Array.from(document.getElementById('selectedIncidents').selectedOptions)
    .map(option => option.value);
  const type = document.getElementById('reportType').value;
  const description = document.getElementById('reportDescription').value.trim();
  
  if (!title) {
    showMessage('보고서 제목을 입력해주세요.', 'error');
    return;
  }
  
  if (selectedIncidents.length === 0) {
    showMessage('관련 사고를 선택해주세요.', 'error');
    return;
  }
  
  try {
    showMessage('보고서를 생성하는 중...', 'info');
    
    // 실제 API 호출
    const response = await fetch("http://52.79.99.132/reports", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({
        title,
        incidentIds: selectedIncidents,
        type,
        description
      })
    });
    
    await handleApiError(response, "보고서 생성에 실패했습니다.");
    
    const newReport = await response.json();
    
    // 로컬 목록에 추가 (실제 API 응답이 없을 경우 더미 데이터 사용)
    const dummyReport = {
      id: Date.now().toString(),
      title,
      incidentTitle: currentIncidents.find(i => selectedIncidents.includes(i.id))?.title || '선택된 사고',
      type: getReportTypeText(type),
      status: '완료',
      generatedAt: new Date().toISOString(),
      description
    };
    
    currentReports.unshift(dummyReport);
    
    closeGenerateModal();
    updateStats();
    displayReports();
    showMessage('보고서가 성공적으로 생성되었습니다.', 'success');
    
  } catch (error) {
    showMessage('보고서 생성 실패: ' + error.message, 'error');
  }
}

/**
 * 보고서 필터링
 */
function filterReports() {
  const statusFilter = document.getElementById('statusFilter').value;
  const typeFilter = document.getElementById('typeFilter').value;
  const searchTerm = document.getElementById('searchInput').value.toLowerCase();
  
  let filteredReports = currentReports.filter(report => {
    const matchesStatus = !statusFilter || report.status === statusFilter;
    const matchesType = !typeFilter || report.type.includes(typeFilter);
    const matchesSearch = !searchTerm || report.title.toLowerCase().includes(searchTerm);
    
    return matchesStatus && matchesType && matchesSearch;
  });
  
  // 임시로 필터링된 결과 표시
  const container = document.getElementById('reportsList');
  if (filteredReports.length === 0) {
    container.innerHTML = '<div class="no-data"><p>조건에 맞는 보고서가 없습니다.</p></div>';
    return;
  }
  
  container.innerHTML = filteredReports.map(report => `
    <div class="report-card">
      <div class="report-header">
        <h3 class="report-title">${report.title}</h3>
        <span class="report-status status-${report.status.toLowerCase()}">${report.status}</span>
      </div>
      
      <div class="report-meta">
        <span>📅 생성일: ${new Date(report.generatedAt).toLocaleDateString()}</span>
        <span>🚨 관련 사고: ${report.incidentTitle}</span>
        <span>📋 유형: ${report.type}</span>
      </div>
      
      ${report.description ? `<div class="report-description">${report.description}</div>` : ''}
      
      <div class="report-actions">
        <button class="btn-primary btn-small" onclick="viewReport('${report.id}')">
          <span class="btn-icon">👁️</span>
          보기
        </button>
        <button class="btn-secondary btn-small" onclick="downloadReport('${report.id}')">
          <span class="btn-icon">⬇️</span>
          다운로드
        </button>
        <button class="btn-danger btn-small" onclick="deleteReport('${report.id}')">
          <span class="btn-icon">🗑️</span>
          삭제
        </button>
      </div>
    </div>
  `).join('');
}

/**
 * 데이터 새로고침
 */
async function refreshData() {
  showMessage('데이터를 새로고침하는 중...', 'info');
  await loadData();
  showMessage('데이터가 새로고침되었습니다.', 'success');
}

// 유틸리티 함수들

/**
 * 보고서 유형 텍스트 변환
 */
function getReportTypeText(type) {
  const typeMap = {
    'insurance': '보험 보고서',
    'official': '공식 보고서',
    'internal': '내부 보고서',
    'custom': '커스텀 보고서'
  };
  return typeMap[type] || type;
}

/**
 * 메시지 표시
 */
function showMessage(message, type = 'info') {
  const messageArea = document.getElementById('messageArea');
  const messageElement = document.createElement('div');
  messageElement.className = `message ${type}`;
  messageElement.textContent = message;
  
  messageArea.appendChild(messageElement);
  
  setTimeout(() => {
    messageElement.remove();
  }, 5000);
}

/**
 * 더미 사고 데이터 생성
 */
function generateDummyIncidents() {
  return [
    {
      id: '1',
      title: '선박 충돌 사고',
      incidentType: 'COLLISION',
      location: '부산항',
      happenedAt: new Date(Date.now() - 86400000).toISOString(),
      status: 'INVESTIGATING'
    },
    {
      id: '2',
      title: '화물 유출 사고',
      incidentType: 'LEAK',
      location: '인천항',
      happenedAt: new Date(Date.now() - 172800000).toISOString(),
      status: 'COMPLETED'
    },
    {
      id: '3',
      title: '선박 화재',
      incidentType: 'FIRE',
      location: '울산항',
      happenedAt: new Date(Date.now() - 259200000).toISOString(),
      status: 'REPORTED'
    }
  ];
}

/**
 * 더미 보고서 데이터 생성
 */
function generateDummyReports() {
  return [
    {
      id: '1',
      title: '선박 충돌 사고 보고서',
      incidentTitle: '선박 충돌 사고',
      type: '보험 보고서',
      status: '완료',
      generatedAt: new Date(Date.now() - 86400000).toISOString(),
      description: '선박 간 충돌로 인한 손상에 대한 상세 보고서입니다.'
    },
    {
      id: '2',
      title: '화물 유출 사고 보고서',
      incidentTitle: '화물 유출 사고',
      type: '공식 보고서',
      status: '완료',
      generatedAt: new Date(Date.now() - 172800000).toISOString(),
      description: '유류 유출로 인한 환경 오염에 대한 공식 보고서입니다.'
    },
    {
      id: '3',
      title: '선박 화재 내부 보고서',
      incidentTitle: '선박 화재',
      type: '내부 보고서',
      status: '진행중',
      generatedAt: new Date(Date.now() - 259200000).toISOString(),
      description: '엔진실 화재 발생에 대한 내부 조사 보고서입니다.'
    }
  ];
}

// 액션 함수들
function viewReport(id) {
  const report = currentReports.find(r => r.id === id);
  if (report) {
    showMessage(`보고서 "${report.title}" 보기 기능은 준비 중입니다.`, 'info');
  }
}

function downloadReport(id) {
  const report = currentReports.find(r => r.id === id);
  if (report) {
    // 더미 PDF 다운로드 시뮬레이션
    const dummyContent = `
보고서: ${report.title}
생성일: ${new Date(report.generatedAt).toLocaleDateString()}
관련 사고: ${report.incidentTitle}
유형: ${report.type}
상태: ${report.status}

${report.description || '추가 설명이 없습니다.'}
    `;
    
    const blob = new Blob([dummyContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${report.title.replace(/\s+/g, '_')}.txt`;
    a.click();
    URL.revokeObjectURL(url);
    
    showMessage('보고서가 다운로드되었습니다.', 'success');
  }
}

function deleteReport(id) {
  if (confirm('정말로 이 보고서를 삭제하시겠습니까?')) {
    currentReports = currentReports.filter(report => report.id !== id);
    updateStats();
    displayReports();
    showMessage('보고서가 삭제되었습니다.', 'success');
  }
}