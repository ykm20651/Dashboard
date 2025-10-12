document.addEventListener("DOMContentLoaded", () => {
  // 로그인 상태 확인
  if (!requireAuth()) return;
  
  const incidentList = document.getElementById("incidentList");
  const msg = document.getElementById("msg");

  // ✅ 상태 변환 함수 추가
  function getStatusText(status) {
    const map = {
      open: "처리 전",
      report_generated: "조사 중",
      closed: "종결"
    };
    return map[status] || status; // 혹시 서버에서 다른 값이 와도 그대로 표시
  }

  // 사고 목록 불러오기
  async function loadIncidents() {
    try {
      // 토큰 확인
      const token = localStorage.getItem("token");
      if (!token) {
        msg.innerText = "❌ 로그인이 필요합니다.";
        msg.style.color = "red";
        setTimeout(() => {
          window.location.href = "login.html";
        }, 2000);
        return;
      }

      console.log("사고 목록 로드 중...");

      const res = await fetch("http://52.79.99.132/incidents", {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });

      console.log("사고 목록 응답 상태:", res.status);

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
        console.error("사고 목록 로드 실패:", errorText);
        throw new Error("사고 목록을 불러올 수 없습니다: " + res.status);
      }

      const incidents = await res.json();
      console.log("로드된 사고 개수:", incidents.length);
      
      incidentList.innerHTML = "";

      if (incidents.length === 0) {
        incidentList.innerHTML = `<tr><td colspan="6">사고가 없습니다.</td></tr>`;
        return;
      }

      incidents.forEach(incident => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${incident.title}</td>
          <td>${incident.incidentType}</td>
          <td>${incident.location}</td>
          <td>${new Date(incident.happenedAt).toLocaleString()}</td>
          <td>${getStatusText(incident.status)}</td>
          <td>
            <button class="btn tiny" onclick="viewDetail('${incident.id}')">상세</button>
            <button class="btn tiny" onclick="viewReport('${incident.id}')">보고서</button>
            <button class="btn tiny danger" onclick="deleteIncident('${incident.id}')">삭제</button>
          </td>
        `;
        incidentList.appendChild(tr);
      });
    } catch (err) {
      console.error("사고 목록 로드 에러:", err);
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  }

  // 상세보기
  window.viewDetail = (id) => {
    window.location.href = `incident_detail.html?id=${id}`;
  };

  // 보고서
  window.viewReport = (id) => {
    window.location.href = `report.html?id=${id}`;
  };

  // 삭제
  window.deleteIncident = async (id) => {
    if (!confirm("정말 이 사고를 삭제하시겠습니까?")) return;

    try {
      // 토큰 확인
      const token = localStorage.getItem("token");
      if (!token) {
        msg.innerText = "❌ 로그인이 필요합니다.";
        msg.style.color = "red";
        setTimeout(() => {
          window.location.href = "login.html";
        }, 2000);
        return;
      }

      console.log("삭제 요청 - 사고 ID:", id);
      console.log("사용 중인 토큰:", token.substring(0, 20) + "...");

      const res = await fetch(`http://52.79.99.132/incidents/${id}`, {
        method: "DELETE",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });

      console.log("삭제 응답 상태:", res.status);

      if (res.status === 401) {
        msg.innerText = "❌ 로그인이 만료되었습니다. 다시 로그인해주세요.";
        msg.style.color = "red";
        localStorage.clear();
        setTimeout(() => {
          window.location.href = "login.html";
        }, 2000);
        return;
      }

      if (res.status === 403) {
        msg.innerText = "❌ 삭제 권한이 없습니다.";
        msg.style.color = "red";
        return;
      }

      if (res.status === 404) {
        msg.innerText = "❌ 해당 사고를 찾을 수 없습니다.";
        msg.style.color = "red";
        return;
      }

      if (!res.ok) {
        const errorText = await res.text();
        console.error("삭제 실패 응답:", errorText);
        throw new Error("사고 삭제 실패: " + res.status);
      }

      msg.innerText = "✅ 사고가 삭제되었습니다.";
      msg.style.color = "green";
      loadIncidents();
    } catch (err) {
      console.error("삭제 에러:", err);
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  };

  loadIncidents();
});
