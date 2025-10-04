document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");
  const msg = document.getElementById("msg");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      // 00-00 API: 로그인 요청
      const data = await apiCall("/users/login", {
        method: "POST",
        body: JSON.stringify({ email, password })
      });

      // 응답에서 토큰/역할/email 저장
      localStorage.setItem("token", data.token);
      localStorage.setItem("tokenType", data.tokenType);
      localStorage.setItem("email", data.email);
      localStorage.setItem("role", data.role);

      msg.innerText = "✅ 로그인 성공! 메인 페이지로 이동합니다.";
      msg.style.color = "green";

      setTimeout(() => {
        window.location.href = "index.html";
      }, 1500);

    } catch (err) {
      msg.innerText = "❌ " + err.message;
      msg.style.color = "red";
    }
  });
});
