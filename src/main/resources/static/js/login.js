document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");
  const msg = document.getElementById("msg");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      // 서버에 로그인 요청
      const res = await fetch("http://15.164.99.177/users/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
      });

      if (!res.ok) throw new Error("로그인 실패: 이메일 또는 비밀번호 확인 필요");

      const data = await res.json();

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
