<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>네이버 간편로그인 (테스트 모드) - SpringBoot</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
            background: #f0fdf4;
            color: #1f2937;
            padding: 20px;
        }
        .card {
            width: 100%;
            max-width: 430px;
            background: #ffffff;
            border: 2px solid #03c75a;
            border-radius: 14px;
            box-shadow: 0 10px 30px rgba(3, 199, 90, 0.12);
            padding: 32px 28px;
        }
        .logo {
            text-align: center;
            color: #03c75a;
            font-size: 26px;
            font-weight: 900;
            margin-bottom: 6px;
        }
        .subtitle {
            text-align: center;
            font-size: 13px;
            color: #4b5563;
            margin: 0 0 20px;
            line-height: 1.5;
        }
        .alert-error {
            padding: 10px 12px;
            border-radius: 8px;
            background: #fef2f2;
            border: 1px solid #fecaca;
            color: #b91c1c;
            font-size: 13px;
            margin-bottom: 14px;
        }
        .form-group { margin-bottom: 14px; }
        label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 6px;
        }
        input {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 14px;
        }
        .btn-naver {
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 8px;
            background: #03c75a;
            color: #ffffff;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            margin-top: 6px;
        }
        .btn-naver:hover { background: #02b350; }
        .footer {
            margin-top: 16px;
            text-align: center;
            font-size: 13px;
        }
        .footer a { color: #4b5563; text-decoration: none; }
    </style>
</head>
<body>
<div class="card">
    <div class="logo">NAVER</div>
    <p class="subtitle">
        <strong>개발/테스트용 네이버 간편로그인</strong><br>
        암호 입력 없이 네이버 계정 정보로 바로 가입 및 로그인합니다.<br>
        (<code>application.properties</code>에 네이버 Client ID/Secret 입력 시 실제 네이버 OAuth 화면으로 연결됩니다.)
    </p>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error"><c:out value="${errorMessage}" /></div>
    </c:if>

    <form method="post" action="<c:url value='/oauth/naver/mock' />">
        <div class="form-group">
            <label for="email">네이버 계정 이메일</label>
            <input type="email" id="email" name="email" required
                   value="<c:out value='${empty enteredEmail ? "naveruser@naver.com" : enteredEmail}' />"
                   placeholder="naveruser@naver.com">
        </div>
        <div class="form-group">
            <label for="name">네이버 사용자 이름</label>
            <input type="text" id="name" name="name" required
                   value="<c:out value='${empty enteredName ? "네이버사용자" : enteredName}' />"
                   placeholder="홍길동">
        </div>
        <button type="submit" class="btn-naver">네이버 간편로그인 계속하기</button>
    </form>

    <div class="footer">
        <a href="<c:url value='/login' />">← 로그인 화면으로 돌아가기</a>
    </div>
</div>
</body>
</html>
