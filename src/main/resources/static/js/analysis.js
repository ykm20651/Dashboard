// 사고 유형 분석 페이지 JavaScript

let selectedImages = [];
let analysisHistory = [];

document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;
  
  // 이벤트 리스너 설정
  setupEventListeners();
  
  // 분석 히스토리 로드
  loadAnalysisHistory();
});

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
  const imageInput = document.getElementById('imageInput');
  const uploadArea = document.getElementById('uploadArea');
  const analyzeBtn = document.getElementById('analyzeBtn');
  const cameraBtn = document.getElementById('cameraBtn');
  
  // 파일 입력 이벤트
  imageInput.addEventListener('change', handleFileSelect);
  
  // 드래그 앤 드롭 이벤트
  uploadArea.addEventListener('dragover', handleDragOver);
  uploadArea.addEventListener('dragleave', handleDragLeave);
  uploadArea.addEventListener('drop', handleDrop);
  
  // 분석 버튼
  analyzeBtn.addEventListener('click', startAnalysis);
  
  // 카메라 버튼 (시뮬레이션)
  cameraBtn.addEventListener('click', simulateCameraCapture);
}

/**
 * 파일 선택 처리
 */
function handleFileSelect(event) {
  const files = Array.from(event.target.files);
  addImages(files);
}

/**
 * 드래그 오버 처리
 */
function handleDragOver(event) {
  event.preventDefault();
  event.currentTarget.classList.add('dragover');
}

/**
 * 드래그 리브 처리
 */
function handleDragLeave(event) {
  event.currentTarget.classList.remove('dragover');
}

/**
 * 드롭 처리
 */
function handleDrop(event) {
  event.preventDefault();
  event.currentTarget.classList.remove('dragover');
  
  const files = Array.from(event.dataTransfer.files);
  const imageFiles = files.filter(file => file.type.startsWith('image/'));
  
  if (imageFiles.length === 0) {
    showMessage('이미지 파일만 업로드할 수 있습니다.', 'error');
    return;
  }

  addImages(imageFiles);
}

/**
 * 이미지 추가
 */
function addImages(files) {
  files.forEach(file => {
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const imageData = {
          id: Date.now() + Math.random(),
          file: file,
          dataUrl: e.target.result,
          name: file.name
        };
        
        selectedImages.push(imageData);
        updateImagePreview();
      };
      reader.readAsDataURL(file);
    }
  });
  
  if (selectedImages.length > 0) {
    document.getElementById('imagePreview').style.display = 'block';
    showMessage(`${files.length}개의 이미지가 추가되었습니다.`, 'success');
  }
}

/**
 * 이미지 미리보기 업데이트
 */
function updateImagePreview() {
  const container = document.getElementById('previewContainer');
  
  container.innerHTML = selectedImages.map(image => `
    <div class="preview-item">
      <img src="${image.dataUrl}" alt="${image.name}">
      <button class="remove-btn" onclick="removeImage('${image.id}')">×</button>
    </div>
  `).join('');
}

/**
 * 이미지 제거
 */
function removeImage(imageId) {
  selectedImages = selectedImages.filter(img => img.id !== imageId);
  
  if (selectedImages.length === 0) {
    document.getElementById('imagePreview').style.display = 'none';
  } else {
    updateImagePreview();
  }
  
  showMessage('이미지가 제거되었습니다.', 'info');
}

/**
 * 모든 이미지 지우기
 */
function clearImages() {
  selectedImages = [];
  document.getElementById('imagePreview').style.display = 'none';
  document.getElementById('imageInput').value = '';
  showMessage('모든 이미지가 제거되었습니다.', 'info');
}

/**
 * 카메라 촬영 시뮬레이션
 */
function simulateCameraCapture() {
  // 실제 카메라 API는 HTTPS 환경에서만 작동
  if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
    showMessage('카메라 기능은 실제 환경에서 사용 가능합니다.', 'info');
  } else {
    // 더미 이미지로 시뮬레이션
    const dummyImage = {
      id: Date.now(),
      file: new File(['dummy'], 'camera_capture.jpg', { type: 'image/jpeg' }),
      dataUrl: 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=',
      name: '카메라 촬영_' + new Date().toLocaleTimeString() + '.jpg'
    };
    
    selectedImages.push(dummyImage);
    updateImagePreview();
    document.getElementById('imagePreview').style.display = 'block';
    showMessage('카메라로 촬영한 이미지가 추가되었습니다.', 'success');
  }
}

/**
 * 분석 시작
 */
async function startAnalysis() {
  if (selectedImages.length === 0) {
    showMessage('분석할 이미지를 선택해주세요.', 'error');
      return;
    }
  
  // 분석 진행 모달 표시
  showAnalysisModal();
  
  try {
    // 실제 API 호출 시뮬레이션
    const analysisResult = await simulateAnalysis();
    
    // 분석 완료 후 결과 표시
    displayAnalysisResult(analysisResult);
    
    // 히스토리에 추가
    addToHistory(analysisResult);
    
    hideAnalysisModal();
    showMessage('분석이 완료되었습니다.', 'success');
    
  } catch (error) {
    hideAnalysisModal();
    showMessage('분석 중 오류가 발생했습니다: ' + error.message, 'error');
  }
}

/**
 * 분석 시뮬레이션
 */
async function simulateAnalysis() {
  // 진행률 시뮬레이션
  let progress = 0;
  const progressInterval = setInterval(() => {
    progress += Math.random() * 20;
    if (progress > 95) progress = 95;
    
    updateProgress(progress);
  }, 200);
  
  // 실제 분석 시간 시뮬레이션 (3-5초)
  const analysisTime = 3000 + Math.random() * 2000;
  await new Promise(resolve => setTimeout(resolve, analysisTime));
  
  clearInterval(progressInterval);
  updateProgress(100);
  
  await new Promise(resolve => setTimeout(resolve, 500));
  
  // 더미 분석 결과 생성
  return generateAnalysisResult();
}

/**
 * 진행률 업데이트
 */
function updateProgress(percentage) {
  const progressFill = document.getElementById('progressFill');
  const progressPercentage = document.getElementById('progressPercentage');
  
  progressFill.style.width = percentage + '%';
  progressPercentage.textContent = Math.round(percentage) + '%';
}

/**
 * 분석 모달 표시
 */
function showAnalysisModal() {
  document.getElementById('analysisModal').style.display = 'flex';
  updateProgress(0);
}

/**
 * 분석 모달 숨기기
 */
function hideAnalysisModal() {
  document.getElementById('analysisModal').style.display = 'none';
}

/**
 * 분석 결과 표시
 */
function displayAnalysisResult(result) {
  // 기본 정보 업데이트
  document.getElementById('incidentType').textContent = result.type;
  document.getElementById('severityLevel').textContent = result.severity;
  document.getElementById('confidenceLevel').textContent = result.confidence + '%';
  
  // 상세 정보 업데이트
  document.getElementById('detectedLocation').textContent = result.location;
  document.getElementById('damageLevel').textContent = result.damageLevel;
  document.getElementById('estimatedCause').textContent = result.cause;
  document.getElementById('environmentalImpact').textContent = result.environmentalImpact;
  
  // 권장 조치사항 업데이트
  const recommendationsContainer = document.getElementById('recommendations');
  recommendationsContainer.innerHTML = result.recommendations.map(rec => `
    <div class="recommendation-item">
      <strong>${rec.priority}:</strong> ${rec.action}
    </div>
  `).join('');
  
  // 결과 섹션 표시
  document.getElementById('analysisResult').style.display = 'block';
  
  // 결과 섹션으로 스크롤
  document.getElementById('analysisResult').scrollIntoView({ 
    behavior: 'smooth',
    block: 'start'
  });
}

/**
 * 분석 히스토리에 추가
 */
function addToHistory(result) {
  const historyItem = {
    id: Date.now(),
    timestamp: new Date().toISOString(),
    type: result.type,
    severity: result.severity,
    confidence: result.confidence,
    imageCount: selectedImages.length
  };
  
  analysisHistory.unshift(historyItem);
  updateHistoryDisplay();
}

/**
 * 히스토리 표시 업데이트
 */
function updateHistoryDisplay() {
  const container = document.getElementById('historyList');
  
  if (analysisHistory.length === 0) {
    container.innerHTML = '<p class="no-data">분석 히스토리가 없습니다.</p>';
    return;
  }
  
  container.innerHTML = analysisHistory.slice(0, 10).map(item => `
    <div class="history-item">
      <div class="history-info">
        <h4>${item.type} 사고 분석</h4>
        <p>심각도: ${item.severity} | 신뢰도: ${item.confidence}% | 이미지: ${item.imageCount}개</p>
        <p>분석 시간: ${new Date(item.timestamp).toLocaleString()}</p>
      </div>
      <div class="history-actions">
        <button class="btn-secondary btn-small" onclick="viewHistoryItem('${item.id}')">보기</button>
        <button class="btn-danger btn-small" onclick="deleteHistoryItem('${item.id}')">삭제</button>
      </div>
    </div>
  `).join('');
}

/**
 * 분석 히스토리 로드
 */
function loadAnalysisHistory() {
  // 로컬 스토리지에서 히스토리 로드
  const savedHistory = localStorage.getItem('analysisHistory');
  if (savedHistory) {
    analysisHistory = JSON.parse(savedHistory);
    updateHistoryDisplay();
  }
}

/**
 * 히스토리 아이템 보기
 */
function viewHistoryItem(id) {
  const item = analysisHistory.find(h => h.id.toString() === id);
  if (item) {
    showMessage(`분석 결과: ${item.type} (심각도: ${item.severity}, 신뢰도: ${item.confidence}%)`, 'info');
  }
}

/**
 * 히스토리 아이템 삭제
 */
function deleteHistoryItem(id) {
  if (confirm('이 분석 기록을 삭제하시겠습니까?')) {
    analysisHistory = analysisHistory.filter(h => h.id.toString() !== id);
    updateHistoryDisplay();
    
    // 로컬 스토리지에 저장
    localStorage.setItem('analysisHistory', JSON.stringify(analysisHistory));
    showMessage('분석 기록이 삭제되었습니다.', 'success');
  }
}

// 액션 함수들

/**
 * 분석 결과로 사고 등록
 */
function createIncidentFromAnalysis() {
  const analysisResult = getCurrentAnalysisResult();
  if (analysisResult) {
    // 분석 결과를 URL 파라미터로 전달하여 사고 등록 페이지로 이동
    const params = new URLSearchParams({
      fromAnalysis: 'true',
      type: analysisResult.type,
      severity: analysisResult.severity,
      cause: analysisResult.cause,
      location: analysisResult.location
    });
    
    window.location.href = `incident_register.html?${params.toString()}`;
  }
}

/**
 * 분석 결과 저장
 */
function saveAnalysisResult() {
  const analysisResult = getCurrentAnalysisResult();
  if (analysisResult) {
    // 분석 결과를 로컬 스토리지에 저장
    const savedResults = JSON.parse(localStorage.getItem('savedAnalysisResults') || '[]');
    savedResults.unshift({
      ...analysisResult,
      id: Date.now(),
      savedAt: new Date().toISOString(),
      images: selectedImages.map(img => img.name)
    });
    
    localStorage.setItem('savedAnalysisResults', JSON.stringify(savedResults));
    showMessage('분석 결과가 저장되었습니다.', 'success');
  }
}

/**
 * 보고서 다운로드
 */
function downloadReport() {
  const analysisResult = getCurrentAnalysisResult();
  if (analysisResult) {
    const reportContent = generateReportContent(analysisResult);
    
    const blob = new Blob([reportContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `사고분석보고서_${new Date().toISOString().split('T')[0]}.txt`;
    a.click();
    URL.revokeObjectURL(url);
    
    showMessage('분석 보고서가 다운로드되었습니다.', 'success');
  }
}

// 유틸리티 함수들

/**
 * 현재 분석 결과 가져오기
 */
function getCurrentAnalysisResult() {
  // 마지막 분석 결과 반환 (실제로는 상태 관리 필요)
  return analysisHistory.length > 0 ? generateAnalysisResult() : null;
}

/**
 * 더미 분석 결과 생성
 */
function generateAnalysisResult() {
  const types = ['충돌', '화재', '유출', '좌초', '기타'];
  const severities = ['경미', '보통', '심각', '매우 심각'];
  const locations = ['선박 전면부', '선박 후면부', '엔진실', '갑판', '화물창', '기관실'];
  const damageLevels = ['최소', '경미', '중간', '심각', '매우 심각'];
  const causes = ['시계 불량', '기계 고장', '인적 오류', '날씨 악화', '설비 결함', '운항 실수'];
  const environmentalImpacts = ['없음', '최소', '보통', '심각'];
  
  const recommendations = [
    { priority: '긴급', action: '즉시 현장 조사 및 보험사 신고' },
    { priority: '높음', action: '환경 오염 방지를 위한 긴급 조치 필요' },
    { priority: '중간', action: '선박 안전 점검 및 운항 중단 권고' },
    { priority: '낮음', action: '관련 당국에 즉시 신고 필요' }
  ];
  
  return {
    type: types[Math.floor(Math.random() * types.length)],
    severity: severities[Math.floor(Math.random() * severities.length)],
    confidence: Math.floor(Math.random() * 30) + 70, // 70-99%
    location: locations[Math.floor(Math.random() * locations.length)],
    damageLevel: damageLevels[Math.floor(Math.random() * damageLevels.length)],
    cause: causes[Math.floor(Math.random() * causes.length)],
    environmentalImpact: environmentalImpacts[Math.floor(Math.random() * environmentalImpacts.length)],
    recommendations: recommendations.slice(0, Math.floor(Math.random() * 3) + 2)
  };
}

/**
 * 보고서 내용 생성
 */
function generateReportContent(result) {
  return `
사고 유형 분석 보고서
========================

분석 일시: ${new Date().toLocaleString()}
분석된 이미지: ${selectedImages.length}개

분석 결과
----------
• 사고 유형: ${result.type}
• 심각도: ${result.severity}
• 신뢰도: ${result.confidence}%
• 감지된 위치: ${result.location}
• 손상 정도: ${result.damageLevel}
• 추정 원인: ${result.cause}
• 환경 영향: ${result.environmentalImpact}

권장 조치사항
-------------
${result.recommendations.map(rec => `• [${rec.priority}] ${rec.action}`).join('\n')}

주의사항
--------
이 보고서는 AI 분석 결과이며, 전문가의 추가 검토가 필요할 수 있습니다.
실제 사고 처리 시에는 관련 법규 및 절차를 준수해야 합니다.

---
OASIS AI 분석 시스템
  `;
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