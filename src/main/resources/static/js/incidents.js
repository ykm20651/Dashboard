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
      const res = await fetch("http://52.79.99.132/incidents", {
        method: "GET",
        headers: getAuthHeaders()
      });
      await handleApiError(res, "사고 목록을 불러올 수 없습니다.");

      const incidents = await res.json();
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
      const token = localStorage.getItem("token");
      const res = await fetch(`http://52.79.99.132/evidence-files/${id}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
      });

      if (res.status === 401 || res.status === 403) {
        msg.innerText = "❌ 삭제 권한이 없습니다.";
        msg.style.color = "red";
        return;
      }

      if (!res.ok) throw new Error("사고 삭제 실패");

      msg.innerText = "✅ 사고가 삭제되었습니다.";
      msg.style.color = "green";
      loadIncidents();
    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  };

  loadIncidents();
});
