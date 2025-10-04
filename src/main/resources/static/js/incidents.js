document.addEventListener("DOMContentLoaded", () => {
  const incidentList = document.getElementById("incidentList");
  const msg = document.getElementById("msg");

  // 사고 목록 불러오기
  async function loadIncidents() {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch("http://15.164.99.177/incidents", {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("사고 불러오기 실패");

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
          <td>${incident.status}</td>
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
      const res = await fetch(`http://15.164.99.177:8080/incidents/${id}`, {
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
