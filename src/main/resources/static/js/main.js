document.addEventListener('DOMContentLoaded', () => {
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

    // 👇 섹션 단위 스크롤 정확히 한 칸씩
    autoScrolling: true,
    fitToSection: true,
    scrollBar: false,
    bigSectionsDestination: 'top'
  });

  // 로그인 체크 (임시)
  function requireLogin(callback) {
    if (callback) callback();
  }

  // 버튼 동작
  document.getElementById('start-analysis').addEventListener('click', e => {
    e.preventDefault();
    fullpage_api.moveTo('analysis');
  });

  document.getElementById('run-analysis').addEventListener('click', e => {
    e.preventDefault();
    requireLogin(() => {
      alert('사고 유형 분석 서비스는 향후 제공 예정입니다.');
      window.location.href = 'incident_register.html';
    });
  });

  document.getElementById('manage-incidents').addEventListener('click', e => {
    e.preventDefault();
    requireLogin(() => window.location.href = 'incidents.html');
  });

  document.getElementById('make-report').addEventListener('click', e => {
    e.preventDefault();
    requireLogin(() => window.location.href = 'report.html');
  });
});
