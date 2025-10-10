// 마이페이지 전용 JavaScript

let currentIncidents = [];
let currentReports = [];
let currentUser = null;

document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;
  
  // 사용자 정보 로드
  loadUserInfo();
  
  // 대시보드 데이터 로드
  loadDashboardData();
  
  // 이벤트 리스너 설정
  setupEventListeners();
  
  // 기본적으로 대시보드 표시
  showSection('dashboard');
});

/**
 * 사용자 정보 로드
 */
async function loadUserInfo() {
  try {
    const userEmail = getUserEmail();
    const userRole = getUserRole();
    
    // 헤더에 사용자 정보 표시
    document.getElementById('userName').textContent = userEmail.split('@')[0];
    document.getElementById('userEmailSidebar').textContent = userEmail;
    document.getElementById('userRoleBadge').textContent = userRole === 'OWNER' ? '선주' : '선원';
    
    // 프로필 폼에 정보 설정
    document.getElementById('profileEmail').value = userEmail;
    document.getElementById('profileRole').value = userRole === 'OWNER' ? '선주' : '선원';
    
    // 사용자 상세 정보 가져오기 (실제 API 호출)
    // const userInfo = await fetchUserInfo(userId);
    // document.getElementById('profileName').value = userInfo.name || '';
    
    showMessage('사용자 정보를 로드했습니다.', 'success');
  } catch (error) {
    showMessage('사용자 정보 로드 실패: ' + error.message, 'error');
  }
}

/**
 * 대시보드 데이터 로드
 */
async function loadDashboardData() {
  try {
    // 사고 목록 로드
    await loadIncidents();
    
    // 보고서 목록 로드
    await loadReports();
    
    // 통계 업데이트
    updateDashboardStats();
    
    // 최근 사고 표시
    displayRecentIncidents();
    
  } catch (error) {
    showMessage('대시보드 데이터 로드 실패: ' + error.message, 'error');
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
    // 실제 API 엔드포인트가 있다면 사용
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
 * 대시보드 통계 업데이트
 */
function updateDashboardStats() {
  const totalIncidents = currentIncidents.length;
  const pendingIncidents = currentIncidents.filter(i => i.status === 'REPORTED' || i.status === 'INVESTIGATING').length;
  const completedIncidents = currentIncidents.filter(i => i.status === 'COMPLETED').length;
  const totalReports = currentReports.length;
  
  document.getElementById('totalIncidents').textContent = totalIncidents;
  document.getElementById('pendingIncidents').textContent = pendingIncidents;
  document.getElementById('completedIncidents').textContent = completedIncidents;
  document.getElementById('totalReports').textContent = totalReports;
}

/**
 * 최근 사고 표시
 */
function displayRecentIncidents() {
  const recentIncidents = currentIncidents.slice(0, 5);
  const container = document.getElementById('recentIncidents');
  
  if (recentIncidents.length === 0) {
    container.innerHTML = '<p class="no-data">등록된 사고가 없습니다.</p>';
    return;
  }
  
  container.innerHTML = recentIncidents.map(incident => `
    <div class="incident-summary">
      <div class="summary-header">
        <h4>${incident.title}</h4>
        <span class="status-badge status-${incident.status.toLowerCase()}">${getStatusText(incident.status)}</span>
      </div>
      <p class="summary-meta">${incident.incidentType} • ${new Date(incident.happenedAt).toLocaleDateString()}</p>
    </div>
  `).join('');
}

/**
 * 섹션 표시
 */
function showSection(sectionId) {
  // 모든 섹션 숨기기
  document.querySelectorAll('.content-section').forEach(section => {
    section.classList.remove('active');
  });
  
  // 모든 네비게이션 아이템 비활성화
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.remove('active');
  });
  
  // 선택된 섹션 표시
  document.getElementById(sectionId).classList.add('active');
  
  // 선택된 네비게이션 아이템 활성화
  document.querySelector(`[data-section="${sectionId}"]`).classList.add('active');
  
  // 섹션별 데이터 로드
  switch(sectionId) {
    case 'incidents':
      displayIncidents();
      break;
    case 'reports':
      displayReports();
      break;
    case 'analysis':
      setupAnalysisSection();
      break;
  }
}

/**
 * 사고 목록 표시
 */
function displayIncidents() {
  const container = document.getElementById('incidentsList');
  
  if (currentIncidents.length === 0) {
    container.innerHTML = `
      <div class="no-data">
        <p>등록된 사고가 없습니다.</p>
        <button class="btn-primary" onclick="window.location.href='incident_register.html'">
          첫 사고 등록하기
        </button>
      </div>
    `;
    return;
  }
  
  container.innerHTML = currentIncidents.map(incident => `
    <div class="incident-item">
      <div class="incident-header">
        <h3 class="incident-title">${incident.title}</h3>
        <span class="status-badge status-${incident.status.toLowerCase()}">${getStatusText(incident.status)}</span>
      </div>
      <div class="incident-meta">
        <span>유형: ${incident.incidentType}</span>
        <span>위치: ${incident.location}</span>
        <span>발생일: ${new Date(incident.happenedAt).toLocaleDateString()}</span>
      </div>
      <div class="incident-actions">
        <button class="btn-primary btn-small" onclick="viewIncident('${incident.id}')">상세보기</button>
        <button class="btn-secondary btn-small" onclick="editIncident('${incident.id}')">수정</button>
        <button class="btn-danger btn-small" onclick="deleteIncident('${incident.id}')">삭제</button>
      </div>
    </div>
  `).join('');
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
        <button class="btn-primary" onclick="generateReport()">
          첫 보고서 생성하기
        </button>
      </div>
    `;
    return;
  }
  
  container.innerHTML = currentReports.map(report => `
    <div class="report-item">
      <div class="report-header">
        <h3 class="report-title">${report.title}</h3>
        <span class="report-date">${new Date(report.generatedAt).toLocaleDateString()}</span>
      </div>
      <div class="report-meta">
        <span>사고: ${report.incidentTitle}</span>
        <span>상태: ${report.status}</span>
      </div>
      <div class="report-actions">
        <button class="btn-primary btn-small" onclick="viewReport('${report.id}')">보기</button>
        <button class="btn-secondary btn-small" onclick="downloadReport('${report.id}')">다운로드</button>
      </div>
    </div>
  `).join('');
}

/**
 * 분석 섹션 설정
 */
function setupAnalysisSection() {
  const imageInput = document.getElementById('imageInput');
  const uploadArea = document.getElementById('uploadArea');
  
  // 드래그 앤 드롭 지원
  uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.style.borderColor = '#007bff';
    uploadArea.style.backgroundColor = 'rgba(0, 123, 255, 0.1)';
  });
  
  uploadArea.addEventListener('dragleave', (e) => {
    e.preventDefault();
    uploadArea.style.borderColor = 'rgba(0, 123, 255, 0.5)';
    uploadArea.style.backgroundColor = 'rgba(0, 123, 255, 0.05)';
  });
  
  uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    const files = e.dataTransfer.files;
    if (files.length > 0) {
      handleImageUpload(files[0]);
    }
  });
  
  // 파일 선택 이벤트
  imageInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
      handleImageUpload(e.target.files[0]);
    }
  });
}

/**
 * 이미지 업로드 처리
 */
async function handleImageUpload(file) {
  if (!file.type.startsWith('image/')) {
    showMessage('이미지 파일만 업로드 가능합니다.', 'error');
    return;
  }
  
  showMessage('이미지를 분석 중입니다...', 'info');
  
  // 실제 API 호출 시뮬레이션
  setTimeout(() => {
    const result = generateAnalysisResult();
    displayAnalysisResult(result);
    showMessage('분석이 완료되었습니다.', 'success');
  }, 2000);
}

/**
 * 분석 결과 표시
 */
function displayAnalysisResult(result) {
  const resultContainer = document.getElementById('analysisResult');
  const resultContent = resultContainer.querySelector('.result-content');
  
  resultContent.innerHTML = `
    <div class="analysis-summary">
      <h4>분석 결과</h4>
      <div class="result-item">
        <strong>사고 유형:</strong> ${result.type}
      </div>
      <div class="result-item">
        <strong>심각도:</strong> ${result.severity}
      </div>
      <div class="result-item">
        <strong>신뢰도:</strong> ${result.confidence}%
      </div>
      <div class="result-item">
        <strong>권장 조치:</strong> ${result.recommendation}
      </div>
    </div>
    <div class="analysis-actions">
      <button class="btn-primary" onclick="createIncidentFromAnalysis()">사고 등록하기</button>
      <button class="btn-secondary" onclick="saveAnalysisResult()">결과 저장</button>
    </div>
  `;
  
  resultContainer.style.display = 'block';
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
  // 사이드바 네비게이션
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const sectionId = e.currentTarget.getAttribute('data-section');
      showSection(sectionId);
    });
  });
  
  // 프로필 업데이트
  document.getElementById('updateProfileBtn').addEventListener('click', updateProfile);
  
  // 계정 삭제
  document.getElementById('deleteAccountBtn').addEventListener('click', deleteAccount);
  
  // 보고서 생성
  document.getElementById('generateReportBtn').addEventListener('click', generateReport);
  
  // 필터 및 검색
  document.getElementById('statusFilter').addEventListener('change', filterIncidents);
  document.getElementById('typeFilter').addEventListener('change', filterIncidents);
  document.getElementById('searchInput').addEventListener('input', filterIncidents);
  document.getElementById('refreshBtn').addEventListener('click', refreshData);
  
  // 설정
  document.getElementById('exportDataBtn').addEventListener('click', exportData);
  document.getElementById('clearDataBtn').addEventListener('click', clearData);
}

/**
 * 프로필 업데이트
 */
async function updateProfile() {
  const newPassword = document.getElementById('newPassword').value;
  const confirmPassword = document.getElementById('confirmPassword').value;
  
  if (newPassword && newPassword !== confirmPassword) {
    showMessage('비밀번호가 일치하지 않습니다.', 'error');
    return;
  }
  
  try {
    // 실제 API 호출
    showMessage('프로필 업데이트 기능은 준비 중입니다.', 'info');
  } catch (error) {
    showMessage('프로필 업데이트 실패: ' + error.message, 'error');
  }
}

/**
 * 계정 삭제
 */
function deleteAccount() {
  if (confirm('정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    if (confirm('마지막 확인: 계정을 삭제하시겠습니까?')) {
      showMessage('계정 삭제 기능은 준비 중입니다.', 'info');
    }
  }
}

/**
 * 보고서 생성
 */
async function generateReport() {
  if (currentIncidents.length === 0) {
    showMessage('생성할 사고가 없습니다. 먼저 사고를 등록해주세요.', 'error');
    return;
  }
  
  try {
    showMessage('보고서 생성 중...', 'info');
    
    // 실제 API 호출 시뮬레이션
    setTimeout(() => {
      const newReport = {
        id: Date.now().toString(),
        title: `사고 보고서 - ${new Date().toLocaleDateString()}`,
        incidentTitle: currentIncidents[0].title,
        status: '완료',
        generatedAt: new Date().toISOString()
      };
      
      currentReports.unshift(newReport);
      updateDashboardStats();
      displayReports();
      showMessage('보고서가 생성되었습니다.', 'success');
    }, 2000);
    
  } catch (error) {
    showMessage('보고서 생성 실패: ' + error.message, 'error');
  }
}

/**
 * 사고 필터링
 */
function filterIncidents() {
  const statusFilter = document.getElementById('statusFilter').value;
  const typeFilter = document.getElementById('typeFilter').value;
  const searchTerm = document.getElementById('searchInput').value.toLowerCase();
  
  let filteredIncidents = currentIncidents.filter(incident => {
    const matchesStatus = !statusFilter || incident.status === statusFilter;
    const matchesType = !typeFilter || incident.incidentType === typeFilter;
    const matchesSearch = !searchTerm || incident.title.toLowerCase().includes(searchTerm);
    
    return matchesStatus && matchesType && matchesSearch;
  });
  
  // 필터링된 결과로 임시 표시
  const container = document.getElementById('incidentsList');
  if (filteredIncidents.length === 0) {
    container.innerHTML = '<p class="no-data">조건에 맞는 사고가 없습니다.</p>';
    return;
  }
  
  container.innerHTML = filteredIncidents.map(incident => `
    <div class="incident-item">
      <div class="incident-header">
        <h3 class="incident-title">${incident.title}</h3>
        <span class="status-badge status-${incident.status.toLowerCase()}">${getStatusText(incident.status)}</span>
      </div>
      <div class="incident-meta">
        <span>유형: ${incident.incidentType}</span>
        <span>위치: ${incident.location}</span>
        <span>발생일: ${new Date(incident.happenedAt).toLocaleDateString()}</span>
      </div>
      <div class="incident-actions">
        <button class="btn-primary btn-small" onclick="viewIncident('${incident.id}')">상세보기</button>
        <button class="btn-secondary btn-small" onclick="editIncident('${incident.id}')">수정</button>
        <button class="btn-danger btn-small" onclick="deleteIncident('${incident.id}')">삭제</button>
      </div>
    </div>
  `).join('');
}

/**
 * 데이터 새로고침
 */
async function refreshData() {
  showMessage('데이터를 새로고침하는 중...', 'info');
  await loadDashboardData();
  showMessage('데이터가 새로고침되었습니다.', 'success');
}

/**
 * 데이터 내보내기
 */
function exportData() {
  const data = {
    incidents: currentIncidents,
    reports: currentReports,
    exportDate: new Date().toISOString()
  };
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `oasis-data-${new Date().toISOString().split('T')[0]}.json`;
  a.click();
  URL.revokeObjectURL(url);
  
  showMessage('데이터가 내보내졌습니다.', 'success');
}

/**
 * 데이터 삭제
 */
function clearData() {
  if (confirm('모든 데이터를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    currentIncidents = [];
    currentReports = [];
    updateDashboardStats();
    displayIncidents();
    displayReports();
    showMessage('모든 데이터가 삭제되었습니다.', 'info');
  }
}

// 유틸리티 함수들

/**
 * 상태 텍스트 변환
 */
function getStatusText(status) {
  const statusMap = {
    'REPORTED': '신고됨',
    'INVESTIGATING': '조사중',
    'COMPLETED': '완료'
  };
  return statusMap[status] || status;
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
      status: 'INVESTIGATING',
      description: '선박 간 충돌로 인한 손상'
    },
    {
      id: '2',
      title: '화물 유출 사고',
      incidentType: 'LEAK',
      location: '인천항',
      happenedAt: new Date(Date.now() - 172800000).toISOString(),
      status: 'COMPLETED',
      description: '유류 유출로 인한 환경 오염'
    },
    {
      id: '3',
      title: '선박 화재',
      incidentType: 'FIRE',
      location: '울산항',
      happenedAt: new Date(Date.now() - 259200000).toISOString(),
      status: 'REPORTED',
      description: '엔진실 화재 발생'
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
      status: '완료',
      generatedAt: new Date(Date.now() - 86400000).toISOString()
    },
    {
      id: '2',
      title: '화물 유출 사고 보고서',
      incidentTitle: '화물 유출 사고',
      status: '완료',
      generatedAt: new Date(Date.now() - 172800000).toISOString()
    }
  ];
}

/**
 * 더미 분석 결과 생성
 */
function generateAnalysisResult() {
  const types = ['충돌', '화재', '유출', '좌초', '기타'];
  const severities = ['경미', '보통', '심각', '매우 심각'];
  const recommendations = [
    '즉시 현장 조사 및 보험사 신고',
    '환경 오염 방지를 위한 긴급 조치 필요',
    '선박 안전 점검 및 운항 중단 권고',
    '관련 당국에 즉시 신고 필요'
  ];
  
  return {
    type: types[Math.floor(Math.random() * types.length)],
    severity: severities[Math.floor(Math.random() * severities.length)],
    confidence: Math.floor(Math.random() * 30) + 70, // 70-99%
    recommendation: recommendations[Math.floor(Math.random() * recommendations.length)]
  };
}

// 액션 함수들
function viewIncident(id) {
  window.location.href = `incident_detail.html?id=${id}`;
}

function editIncident(id) {
  window.location.href = `incident_register.html?edit=${id}`;
}

function deleteIncident(id) {
  if (confirm('정말로 이 사고를 삭제하시겠습니까?')) {
    currentIncidents = currentIncidents.filter(incident => incident.id !== id);
    updateDashboardStats();
    displayIncidents();
    showMessage('사고가 삭제되었습니다.', 'success');
  }
}

function viewReport(id) {
  window.location.href = `report.html?id=${id}`;
}

function downloadReport(id) {
  showMessage('보고서 다운로드 기능은 준비 중입니다.', 'info');
}

function createIncidentFromAnalysis() {
  window.location.href = 'incident_register.html?fromAnalysis=true';
}

function saveAnalysisResult() {
  showMessage('분석 결과 저장 기능은 준비 중입니다.', 'info');
}
