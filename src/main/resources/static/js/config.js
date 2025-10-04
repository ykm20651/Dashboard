// API 설정 파일
const API_CONFIG = {
  // 개발 환경
  development: {
    baseURL: 'http://localhost:8080'
  },
  // 운영 환경 (EC2)
  production: {
    baseURL: 'http://15.164.99.177:8080'  // 실제 EC2 IP로 변경
  }
};

// 현재 환경 감지 (간단한 방법)
const isProduction = window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1';
const currentConfig = isProduction ? API_CONFIG.production : API_CONFIG.development;

// API URL 헬퍼 함수
const API_BASE_URL = currentConfig.baseURL;

// API 호출 헬퍼 함수
const apiCall = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = localStorage.getItem('token');
  
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    }
  };
  
  const mergedOptions = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  };
  
  try {
    const response = await fetch(url, mergedOptions);
    
    if (!response.ok) {
      if (response.status === 401) {
        // 토큰 만료 또는 인증 실패
        localStorage.removeItem('token');
        localStorage.removeItem('email');
        localStorage.removeItem('role');
        window.location.href = 'login.html';
        throw new Error('인증이 필요합니다. 다시 로그인해주세요.');
      }
      throw new Error(`API 호출 실패: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API 호출 오류:', error);
    throw error;
  }
};

// 내보내기 (모듈 시스템 사용 시)
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { API_BASE_URL, apiCall };
}
