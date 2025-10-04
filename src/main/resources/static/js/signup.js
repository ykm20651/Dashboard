document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("signupForm");
  const msg = document.getElementById("msg");
  const roleSelect = document.getElementById("role");
  const ownerExtra = document.getElementById("owner-extra");
  const crewExtra = document.getElementById("crew-extra");

  // 역할 선택 시 추가 입력란 표시
  roleSelect.addEventListener("change", () => {
    if (roleSelect.value === "OWNER") {
      ownerExtra.style.display = "block";
      crewExtra.style.display = "none";
    } else if (roleSelect.value === "CREW_MEMBER") {
      crewExtra.style.display = "block";
      ownerExtra.style.display = "none";
    } else {
      ownerExtra.style.display = "none";
      crewExtra.style.display = "none";
    }
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const name = document.getElementById("name").value;
    const role = document.getElementById("role").value;

    try {
      // 00-01 API: 공통 회원가입
      const data = await apiCall("/users", {
        method: "POST",
        body: JSON.stringify({ email, password, name, role })
      });
      const userId = data.id;

      // 00-02 API: 선주 추가 정보 입력
      if (role === "OWNER") {
        const companyName = document.getElementById("companyName").value;
        const shipRegId = document.getElementById("shipRegId").value;
        const contactNumber = document.getElementById("contactNumber").value;
        const businessNumber = document.getElementById("businessNumber").value;

        await apiCall(`/users/${userId}/owner-info`, {
          method: "POST",
          body: JSON.stringify({ companyName, shipRegId, contactNumber, businessNumber })
        });
      } 
      // 00-03 API: 선원 추가 정보 입력
      else if (role === "CREW_MEMBER") {
        const assignedOwnerId = document.getElementById("assignedOwnerId").value;
        const position = document.getElementById("position").value;

        await apiCall(`/users/${userId}/crew-info`, {
          method: "POST",
          body: JSON.stringify({ assignedOwnerId, position })
        });
      }

      msg.innerText = "✅ 회원가입 성공! 로그인 페이지로 이동합니다.";
      msg.style.color = "green";

      setTimeout(() => {
        window.location.href = "login.html";
      }, 2000);

    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });
});
