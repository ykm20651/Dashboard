document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("signupForm");
  const msg = document.getElementById("msg");
  const roleSelect = document.getElementById("role");
  const ownerExtra = document.getElementById("owner-extra");
  const crewExtra = document.getElementById("crew-extra");

  // 초기 상태: '선택'이 기본으로 선택
  roleSelect.value = "";

  // 역할 선택 시 추가 입력란 표시
  roleSelect.addEventListener("change", () => {
    if (roleSelect.value === "OWNER") {
      ownerExtra.style.display = "block";
      crewExtra.style.display = "none";
    } else if (roleSelect.value === "CREW") {
      crewExtra.style.display = "block";
      ownerExtra.style.display = "none";
    } else {
      ownerExtra.style.display = "none";
      crewExtra.style.display = "none";
    }
  });

  // 회원가입 처리
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const name = document.getElementById("name").value;
    const role = document.getElementById("role").value;

    if (!role) {
      msg.innerText = "⚠️ 역할을 선택해주세요.";
      msg.style.color = "orange";
      return;
    }

    try {
      // 1차 요청: 회원가입
      const res = await fetch("http://52.79.99.132/users", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Cache-Control": "no-cache",
          "Pragma": "no-cache"
        },
        body: JSON.stringify({ email, password, name, role })
      });

      if (!res.ok) throw new Error("회원가입 실패");

      const data = await res.json();
      const userId = data.id;

      // 2차 요청: 역할별 추가 정보
      if (role === "OWNER") {
        const companyName = document.getElementById("companyName").value;
        const shipRegId = document.getElementById("shipRegId").value;
        const contactNumber = document.getElementById("contactNumber").value;
        const businessNumber = document.getElementById("businessNumber").value;

        const ownerRes = await fetch(`http://52.79.99.132/users/${userId}/owner-info`, {
          method: "POST",
          headers: { 
            "Content-Type": "application/json",
            "Cache-Control": "no-cache",
            "Pragma": "no-cache"
          },
          body: JSON.stringify({ companyName, shipRegId, contactNumber, businessNumber })
        });
        if (!ownerRes.ok) throw new Error("선주 정보 등록 실패");
      } else if (role === "CREW") {
        const ownerBusinessNumber = document.getElementById("ownerBusinessNumber").value;
        const position = document.getElementById("position").value;

        const crewRes = await fetch(`http://52.79.99.132/users/${userId}/crew-info`, {
          method: "POST",
          headers: { 
            "Content-Type": "application/json",
            "Cache-Control": "no-cache",
            "Pragma": "no-cache"
          },
          body: JSON.stringify({ ownerBusinessNumber, position })
        });
        if (!crewRes.ok) throw new Error("선원 정보 등록 실패");
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
