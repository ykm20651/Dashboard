// ===================== DOM 로드 후 초기화 =====================
document.addEventListener("DOMContentLoaded", () => {
  // 🔹 Hero 배경 영상 재생 속도 조절
  const video = document.querySelector(".hero-video");
  if (video) {
    video.playbackRate = 0.85; // 재생속도 0.7배
  }

  // 🔹 fullPage.js 초기화
  new fullpage("#fullpage", {
    licenseKey: "gplv3-license",
    anchors: ["home", "analysis", "data", "report"],
    navigation: true,
    showActiveTooltip: true,
    paddingTop: "100px",
    fixedElements: ".topbar",
    scrollOverflow: true,
    responsiveWidth: 900,

    // 섹션 단위 스크롤
    autoScrolling: true,
    fitToSection: true,
    scrollBar: false,
    bigSectionsDestination: "top",
  });

  // 🔹 로그인 상태 UI 세팅
  initializeAuthUI();

  // 🔹 버튼 이벤트 리스너 세팅
  setupButtonEventListeners();
});

// ===================== 인증 상태에 따른 UI 초기화 =====================
function initializeAuthUI() {
  const isLoggedIn = localStorage.getItem("token") !== null;
  const userRole = localStorage.getItem("role");
  const userEmail = localStorage.getItem("email");

  // 헤더 상태 설정
  const guestAuth = document.getElementById("guestAuth");
  const userAuth = document.getElementById("userAuth");
  const userEmailSpan = document.getElementById("userEmail");
  const userRoleSpan = document.getElementById("userRole");

  if (isLoggedIn) {
    // 로그인 상태
    guestAuth.style.display = "none";
    userAuth.style.display = "flex";
    userEmailSpan.textContent = userEmail || "";
    userRoleSpan.textContent = userRole === "OWNER" ? "선주" : "선원";

    // 역할별 메인 버튼 설정
    setupRoleBasedButtons(userRole);
  } else {
    // 비로그인 상태
    guestAuth.style.display = "flex";
    userAuth.style.display = "none";

    // 게스트 버튼 표시
    document.getElementById("guestButtons").style.display = "block";
    document.getElementById("ownerButtons").style.display = "none";
    document.getElementById("crewButtons").style.display = "none";
  }
}

// ===================== 역할별 메인 버튼 설정 =====================
function setupRoleBasedButtons(userRole) {
  // 모든 버튼 그룹 숨기기
  document.getElementById("guestButtons").style.display = "none";
  document.getElementById("ownerButtons").style.display = "none";
  document.getElementById("crewButtons").style.display = "none";

  // 역할에 따른 버튼 그룹 표시
  if (userRole === "OWNER") {
    document.getElementById("ownerButtons").style.display = "block";
  } else if (userRole === "CREW") {
    document.getElementById("crewButtons").style.display = "block";
  }
}

// ===================== 버튼 이벤트 리스너 설정 =====================
function setupButtonEventListeners() {
  // 분석 시작 버튼 (게스트/선주 공통)
  const startAnalysisBtn = document.getElementById("start-analysis");
  if (startAnalysisBtn) {
    startAnalysisBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (typeof fullpage_api !== "undefined") {
        fullpage_api.moveTo("analysis");
      }
    });
  }

  // 분석 실행 버튼
  const runAnalysisBtn = document.getElementById("run-analysis");
  if (runAnalysisBtn) {
    runAnalysisBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (!isLoggedIn()) {
        alert("로그인이 필요합니다.");
        window.location.href = "login.html";
        return;
      }
      alert("사고 유형 분석 서비스는 향후 제공 예정입니다.");
      window.location.href = "incident_register.html";
    });
  }

  // 사고 관리 버튼
  const manageIncidentsBtn = document.getElementById("manage-incidents");
  if (manageIncidentsBtn) {
    manageIncidentsBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (!isLoggedIn()) {
        alert("로그인이 필요합니다.");
        window.location.href = "login.html";
        return;
      }
      window.location.href = "incidents.html";
    });
  }

  // 보고서 작성 버튼
  const makeReportBtn = document.getElementById("make-report");
  if (makeReportBtn) {
    makeReportBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (!isLoggedIn()) {
        alert("로그인이 필요합니다.");
        window.location.href = "login.html";
        return;
      }
      window.location.href = "report.html";
    });
  }

  // 사용자 메뉴 토글
  const userMenuBtn = document.getElementById("userMenuBtn");
  const userMenu = document.getElementById("userMenu");
  if (userMenuBtn && userMenu) {
    userMenuBtn.addEventListener("click", (e) => {
      e.stopPropagation(); // 바깥 클릭 이벤트와 충돌 방지
      userMenu.style.display = userMenu.style.display === "none" ? "block" : "none";
    });
  }

  // 로그아웃 버튼
  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (confirm("로그아웃하시겠습니까?")) {
        logout();
      }
    });
  }

  // 마이페이지 링크
  const myPageLink = document.getElementById("myPageLink");
  if (myPageLink) {
    myPageLink.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "mypage.html";
    });
  }

  // 내 사고 관리 링크
  const myIncidentsLink = document.getElementById("myIncidentsLink");
  if (myIncidentsLink) {
    myIncidentsLink.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "incidents.html";
    });
  }

  // 내 보고서 링크
  const myReportsLink = document.getElementById("myReportsLink");
  if (myReportsLink) {
    myReportsLink.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "report.html";
    });
  }

  // 메뉴 외부 클릭 시 메뉴 닫기
  document.addEventListener("click", (e) => {
    const userMenuWrapper = document.querySelector(".user-menu");
    if (userMenuWrapper && !e.target.closest(".user-menu")) {
      const menu = document.getElementById("userMenu");
      if (menu) menu.style.display = "none";
    }
  });
}

// ===================== 로그인 상태 확인 =====================
function isLoggedIn() {
  return localStorage.getItem("token") !== null;
}

// ===================== 로그아웃 처리 =====================
function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("tokenType");
  localStorage.removeItem("email");
  localStorage.removeItem("role");
  window.location.href = "login.html";
}
