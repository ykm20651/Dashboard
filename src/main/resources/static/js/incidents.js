document.addEventListener("DOMContentLoaded", () => {
  if (!requireAuth()) return;

  const incidentList = document.getElementById("incidentList");
  const msg = document.getElementById("msg");

  function getStatusText(status = "") {
    const key = status.toUpperCase();
    const map = {
      OPEN: "등록 완료",
      REPORT_GENERATED: "보고서 생성 완료",
      CLOSED: "종결",
    };
    return map[key] || status;
  }

  // ✅ 사고 목록 로드
  async function loadIncidents() {
    try {
      const res = await fetch("http://52.79.99.132/incidents", {
        method: "GET",
        headers: getAuthHeaders(),
      });

      if (!res.ok) throw new Error("사고 목록을 불러올 수 없습니다.");

      const incidents = await res.json();
      incidentList.innerHTML = "";

      if (incidents.length === 0) {
        incidentList.innerHTML = `<tr><td colspan="6">등록된 사고가 없습니다.</td></tr>`;
        return;
      }

      incidents.forEach((i) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${i.title}</td>
          <td>${i.incidentType}</td>
          <td>${i.location}</td>
          <td>${new Date(i.happenedAt).toLocaleString()}</td>
          <td>${getStatusText(i.status)}</td>
          <td>
            <button class="btn tiny" onclick="viewDetail('${i.id}')">상세</button>
            <button class="btn tiny" onclick="viewReport('${i.id}')">보고서</button>
            <button class="btn tiny danger" onclick="deleteIncident('${i.id}')">삭제</button>
          </td>`;
        incidentList.appendChild(tr);
      });
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  }

  window.viewDetail = (id) =>
    (window.location.href = `incident_detail.html?id=${id}`);

  window.viewReport = (id) =>
    (window.location.href = `report.html?incidentId=${id}`);

  // ✅ 사고 삭제
  window.deleteIncident = async (id) => {
    if (!confirm("정말 이 사고를 삭제하시겠습니까?")) return;

    try {
      const res = await fetch(`http://52.79.99.132/incidents/${id}`, {
        method: "DELETE",
        headers: getAuthHeaders(),
      });

      if (res.status === 401 || res.status === 403) {
        showToast("❌ 삭제 권한이 없습니다.", "error");
        return;
      }

      if (res.status === 404) {
        showToast("⚠️ 이미 삭제된 사고입니다.", "warning");
        return;
      }

      if (!res.ok) {
        throw new Error("사고 삭제 실패");
      }

      showToast("✅ 사고가 삭제되었습니다.", "success");

      setTimeout(loadIncidents, 1000);
    } catch (err) {
      showToast("❌ " + err.message, "error");
    }
  };

  loadIncidents();
});
