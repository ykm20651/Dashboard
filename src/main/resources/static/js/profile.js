// 프로필 페이지 JavaScript

document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;
  
  loadUserInfo();
  setupEventListeners();
});

/** 사용자 정보 로드 */
async function loadUserInfo() {
  try {
    const userEmail = getUserEmail();
    const userRole = getUserRole();
    const userName = userEmail.split("@")[0];

    document.getElementById("avatarName").textContent = `${userName}님`;
    document.getElementById("userEmailSidebar").textContent = userEmail;
    document.getElementById("userRoleBadge").textContent =
      userRole === "OWNER" ? "선주" : "선원";
    document.getElementById("profileEmail").value = userEmail;
    document.getElementById("profileRole").value =
      userRole === "OWNER" ? "선주" : "선원";
    document.getElementById("profileName").value = userName;
  } catch (error) {
    showMessage("사용자 정보를 불러오지 못했습니다.", "error");
  }
}

/** 이벤트 리스너 설정 */
function setupEventListeners() {
  const updateBtn = document.getElementById('updateProfileBtn');
  const deleteBtn = document.getElementById('deleteAccountBtn');

  if (updateBtn) {
    updateBtn.addEventListener('click', updateProfile);
  }

  if (deleteBtn) {
    deleteBtn.addEventListener('click', deleteAccount);
  }
}

/** 프로필 업데이트 */
async function updateProfile() {
  const name = document.getElementById('profileName').value.trim();
  const newPassword = document.getElementById('newPassword').value;
  const confirmPassword = document.getElementById('confirmPassword').value;

  // 유효성 검사
  if (!name) {
    showMessage('이름을 입력해주세요.', 'error');
    return;
  }

  if (newPassword && newPassword !== confirmPassword) {
    showMessage('비밀번호가 일치하지 않습니다.', 'error');
    return;
  }

  try {
    showMessage('정보를 업데이트하는 중...', 'info');

    // 사용자 ID 가져오기 (실제로는 JWT 토큰에서 추출해야 함)
    const userId = getCurrentUserId(); // 이 함수를 구현해야 함
    
    const updateData = { name };
    if (newPassword) {
      updateData.password = newPassword;
    }

    const response = await fetch(`http://52.79.99.132/users/${userId}`, {
      method: 'PATCH',
      headers: getAuthHeaders(),
      body: JSON.stringify(updateData)
    });

    await handleApiError(response, "정보 업데이트에 실패했습니다.");

    showMessage('정보가 성공적으로 업데이트되었습니다.', 'success');
    
    // 비밀번호 필드 초기화
    document.getElementById('newPassword').value = '';
    document.getElementById('confirmPassword').value = '';
    
    // 사용자 정보 다시 로드
    setTimeout(() => {
      loadUserInfo();
    }, 1000);

  } catch (error) {
    showMessage('정보 업데이트 실패: ' + error.message, 'error');
  }
}

/** 계정 삭제 */
async function deleteAccount() {
  if (!confirm('정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    return;
  }

  try {
    showMessage('계정을 삭제하는 중...', 'info');

    const userId = getCurrentUserId();
    
    const response = await fetch(`http://52.79.99.132/users/${userId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });

    await handleApiError(response, "계정 삭제에 실패했습니다.");

    showMessage('계정이 성공적으로 삭제되었습니다.', 'success');
    
    // 로그아웃 처리
    setTimeout(() => {
      logout();
    }, 2000);

  } catch (error) {
    showMessage('계정 삭제 실패: ' + error.message, 'error');
  }
}

/** 현재 사용자 ID 가져오기 */
function getCurrentUserId() {
  // 실제로는 JWT 토큰에서 사용자 ID를 추출해야 함
  // 임시로 localStorage에서 가져오거나 다른 방법 사용
  const token = getToken();
  if (!token) {
    throw new Error('로그인이 필요합니다.');
  }
  
  // JWT 토큰에서 사용자 ID 추출 (실제 구현 필요)
  // 현재는 임시로 사용자 이메일을 기반으로 ID 생성
  const email = getUserEmail();
  return email ? email.split('@')[0] : 'unknown';
}

/** 메시지 표시 함수 */
function showMessage(message, type = 'info') {
  const messageArea = document.getElementById('messageArea');
  
  if (!messageArea) {
    alert(message);
    return;
  }

  const messageElement = document.createElement('div');
  messageElement.className = `message ${type}`;
  messageElement.textContent = message;
  
  messageArea.appendChild(messageElement);
  
  setTimeout(() => {
    messageElement.remove();
  }, 5000);
}
