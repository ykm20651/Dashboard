// 인증 관련 공통 유틸리티 함수들

/**
 * JWT 토큰을 가져옵니다
 * @returns {string|null} JWT 토큰 또는 null
 */
function getToken() {
    return localStorage.getItem("token");
}

/**
 * 사용자 역할을 가져옵니다
 * @returns {string|null} 사용자 역할 (OWNER, CREW) 또는 null
 */
function getUserRole() {
    return localStorage.getItem("role");
}

/**
 * 사용자 이메일을 가져옵니다
 * @returns {string|null} 사용자 이메일 또는 null
 */
function getUserEmail() {
    return localStorage.getItem("email");
}

/**
 * 로그인 상태를 확인합니다
 * @returns {boolean} 로그인 여부
 */
function isLoggedIn() {
    return getToken() !== null;
}

/**
 * 인증이 필요한 API 요청을 위한 헤더를 반환합니다
 * @returns {Object} Authorization 헤더가 포함된 헤더 객체
 */
function getAuthHeaders() {
    const token = getToken();
    if (!token) {
        throw new Error("로그인이 필요합니다.");
    }
    
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
        "Cache-Control": "no-cache",
        "Pragma": "no-cache"
    };
}

/**
 * 로그아웃 처리
 */
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("tokenType");
    localStorage.removeItem("email");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}

/**
 * 로그인 상태에 따라 리다이렉트 처리
 * @param {string} redirectTo - 로그인 후 리다이렉트할 페이지 (기본값: index.html)
 */
function requireAuth(redirectTo = "login.html") {
    if (!isLoggedIn()) {
        window.location.href = redirectTo;
        return false;
    }
    return true;
}

/**
 * 역할별 접근 권한 확인
 * @param {string|Array} allowedRoles - 허용된 역할 (문자열 또는 배열)
 * @returns {boolean} 접근 권한 여부
 */
function hasRole(allowedRoles) {
    const userRole = getUserRole();
    if (!userRole) return false;
    
    if (Array.isArray(allowedRoles)) {
        return allowedRoles.includes(userRole);
    }
    return userRole === allowedRoles;
}

/**
 * 역할별 접근 권한 확인 후 리다이렉트
 * @param {string|Array} allowedRoles - 허용된 역할
 * @param {string} redirectTo - 권한 없을 때 리다이렉트할 페이지
 * @returns {boolean} 접근 권한 여부
 */
function requireRole(allowedRoles, redirectTo = "index.html") {
    if (!requireAuth()) return false;
    
    if (!hasRole(allowedRoles)) {
        alert("접근 권한이 없습니다.");
        window.location.href = redirectTo;
        return false;
    }
    return true;
}

/**
 * 사용자 정보를 서버에서 가져옵니다
 * @param {string} userId - 사용자 ID
 * @returns {Promise<Object>} 사용자 정보
 */
async function fetchUserInfo(userId) {
    const response = await fetch(`http://52.79.99.132/users/${userId}`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    
    if (!response.ok) {
        throw new Error("사용자 정보를 가져올 수 없습니다.");
    }
    
    return await response.json();
}

/**
 * API 요청 시 에러 처리
 * @param {Response} response - fetch 응답 객체
 * @param {string} defaultMessage - 기본 에러 메시지
 * @throws {Error} 에러 객체
 */
async function handleApiError(response, defaultMessage = "요청 처리 중 오류가 발생했습니다.") {
    if (!response.ok) {
        if (response.status === 401) {
            logout();
            throw new Error("로그인이 만료되었습니다. 다시 로그인해주세요.");
        }
        
        let errorMessage = defaultMessage;
        try {
            const errorData = await response.json();
            errorMessage = errorData.error || errorData.message || errorMessage;
        } catch (e) {
            // JSON 파싱 실패 시 기본 메시지 사용
        }
        
        throw new Error(errorMessage);
    }
}
