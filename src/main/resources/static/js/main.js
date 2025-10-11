document.addEventListener('DOMContentLoaded', () => {
  // ===== 풀페이지 초기화 =====
  new fullpage('#fullpage', {
    licenseKey: 'gplv3-license',
    anchors: ['home', 'analysis', 'data', 'report'],
    navigation: true,
    navigationTooltips: ['메인 화면', '유형 분석', '데이터 관리', '보고서 생성'],
    showActiveTooltip: true,
    paddingTop: '100px',
    fixedElements: '.topbar',
    scrollOverflow: true,
    responsiveWidth: 900,
    autoScrolling: true,
    fitToSection: true,
    scrollBar: false,
    bigSectionsDestination: 'top'
  });

  // ===== 로그인 체크 (임시) =====
  function requireLogin(callback) {
    if (callback) callback();
  }

  // ===== 버튼 동작 =====
  // 메인 화면 → 분석 섹션 이동
  document.getElementById('start-analysis').addEventListener('click', e => {
    e.preventDefault();
    fullpage_api.moveTo('analysis');
  });

  // 사고 유형 분석 버튼
  document.getElementById('run-analysis').addEventListener('click', e => {
    e.preventDefault();
    requireLogin(() => {
      Swal.fire({
        title: '현재 서비스 개발 중입니다.',
        text: '사고 등록 페이지로 이동합니다.',
        icon: 'info',
        background: '#1e1e1e',       // 다크톤 배경
        color: '#f1f1f1',             // 밝은 텍스트
        iconColor: '#ca6521ff',         // 브랜드 블루 포인트
        confirmButtonText: '확인',
        confirmButtonColor: '#2e2e2e',
        customClass: {
          popup: 'oasis-alert',
          title: 'oasis-alert-title',
          confirmButton: 'oasis-confirm-btn'
        },
        backdrop: 'rgba(0, 0, 0, 0.6)'
      }).then(() => {
        // ✅ 팝업 닫힌 후 사고등록 페이지로 이동
        window.location.href = 'incident_register.html';
      });
    });
  });

  // 사고 관리 버튼
  document.getElementById('manage-incidents').addEventListener('click', e => {
    e.preventDefault();
    requireLogin(() => window.location.href = 'incidents.html');
  });

  // 보고서 생성 버튼
  document.getElementById('make-report').addEventListener('click', e => {
    e.preventDefault();
    requireLogin(() => window.location.href = 'report.html');
  });
});
