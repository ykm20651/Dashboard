document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;
  
  const form = document.getElementById("incidentForm");
  const msg = document.getElementById("msg");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const incidentType = document.getElementById("incidentType").value;
    const location = document.getElementById("location").value.trim();
    const happenedAtValue = document.getElementById("happenedAt").value;
    
    // 입력 검증
    if (!title) {
      msg.innerText = "❌ 사고 제목을 입력해주세요.";
      msg.style.color = "red";
      return;
    }
    if (!description) {
      msg.innerText = "❌ 사고 설명을 입력해주세요.";
      msg.style.color = "red";
      return;
    }
    if (!incidentType) {
      msg.innerText = "❌ 사고 유형을 선택해주세요.";
      msg.style.color = "red";
      return;
    }
    if (!location) {
      msg.innerText = "❌ 사고 장소를 입력해주세요.";
      msg.style.color = "red";
      return;
    }
    if (!happenedAtValue) {
      msg.innerText = "❌ 발생 시각을 입력해주세요.";
      msg.style.color = "red";
      return;
    }

    // 날짜를 LocalDateTime 형식으로 변환 (ISO 8601 without timezone)
    const happenedAt = new Date(happenedAtValue).toISOString().replace('Z', '');
    
    // 미래 날짜 검증
    if (new Date(happenedAtValue) > new Date()) {
      msg.innerText = "❌ 사고 발생 시각은 현재 시각보다 이전이어야 합니다.";
      msg.style.color = "red";
      return;
    }

    try {
      console.log("사고 등록 요청 데이터:", { title, description, incidentType: incidentType.toUpperCase(), location, happenedAt });
      
      const res = await fetch("http://52.79.99.132/incidents", {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ title, description, incidentType: incidentType.toUpperCase(), location, happenedAt })
      });

      console.log("사고 등록 응답 상태:", res.status);

      if (res.status === 401) {
        msg.innerText = "❌ 로그인이 만료되었습니다. 다시 로그인해주세요.";
        msg.style.color = "red";
        localStorage.clear();
        setTimeout(() => {
          window.location.href = "login.html";
        }, 2000);
        return;
      }

      if (!res.ok) {
        const errorText = await res.text();
        console.error("사고 등록 실패:", errorText);
        try {
          const errorData = JSON.parse(errorText);
          throw new Error(errorData.message || errorData.error || "사고 등록에 실패했습니다.");
        } catch (e) {
          throw new Error("사고 등록에 실패했습니다: " + res.status);
        }
      }

      const data = await res.json();
      console.log("사고 등록 성공:", data);

      msg.innerText = "✅ 사고가 성공적으로 등록되었습니다!";
      msg.style.color = "green";

      setTimeout(() => {
        window.location.href = "incidents.html";
      }, 1500);

    } catch (err) {
      console.error("사고 등록 에러:", err);
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });
});
