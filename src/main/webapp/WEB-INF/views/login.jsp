<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - SpringBoot</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
            background: #f3f5f9;
            color: #1f2937;
            padding: 20px;
        }
        .card {
            width: 100%;
            max-width: 430px;
            background: #ffffff;
            border-radius: 14px;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
            padding: 34px 30px;
        }
        h1 {
            margin: 0 0 8px;
            font-size: 24px;
            text-align: center;
        }
        .subtitle {
            margin: 0 0 24px;
            font-size: 13px;
            color: #6b7280;
            text-align: center;
            line-height: 1.5;
        }
        .alert {
            padding: 12px 14px;
            border-radius: 8px;
            font-size: 13px;
            margin-bottom: 16px;
            line-height: 1.5;
        }
        .alert-error {
            background: #fef2f2;
            border: 1px solid #fecaca;
            color: #b91c1c;
        }
        .alert-success {
            background: #ecfdf5;
            border: 1px solid #a7f3d0;
            color: #047857;
        }
        .alert-info {
            background: #eff6ff;
            border: 1px solid #bfdbfe;
            color: #1d4ed8;
        }
        .form-group {
            margin-bottom: 14px;
        }
        label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 6px;
            color: #374151;
        }
        input[type="email"], input[type="password"] {
            width: 100%;
            padding: 11px 12px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 14px;
            outline: none;
        }
        input:focus {
            border-color: #2563eb;
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }
        .hint {
            font-size: 12px;
            color: #6b7280;
            margin-top: 5px;
        }
        .btn {
            display: block;
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            text-align: center;
            text-decoration: none;
        }
        .btn-primary {
            background: #2563eb;
            color: #ffffff;
            margin-top: 8px;
        }
        .btn-primary:hover { background: #1d4ed8; }
        .divider {
            display: flex;
            align-items: center;
            text-align: center;
            color: #9ca3af;
            font-size: 12px;
            margin: 20px 0;
        }
        .divider::before, .divider::after {
            content: '';
            flex: 1;
            border-bottom: 1px solid #e5e7eb;
        }
        .divider:not(:empty)::before { margin-right: 10px; }
        .divider:not(:empty)::after { margin-left: 10px; }
        .btn-naver {
            background: #03c75a;
            color: #ffffff;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }
        .btn-naver:hover { background: #02b350; }
        .naver-badge {
            font-weight: 900;
            font-size: 16px;
        }
        .footer-links {
            margin-top: 22px;
            text-align: center;
            font-size: 13px;
            color: #6b7280;
        }
        .footer-links a {
            color: #2563eb;
            font-weight: 700;
            text-decoration: none;
        }
        .footer-links a:hover { text-decoration: underline; }
        .dev-box {
            margin-top: 18px;
            padding: 10px 12px;
            border-radius: 8px;
            background: #f9fafb;
            border: 1px dashed #d1d5db;
            font-size: 12px;
            color: #4b5563;
            line-height: 1.5;
        }
    </style>
</head>
<body>
<div class="card">
    <h1>로그인</h1>
    <p class="subtitle">이메일과 암호 또는 네이버 간편로그인으로 시작하세요.</p>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">
            <c:out value="${errorMessage}" />
            <c:if test="${showSignupHint}">
                <div style="margin-top: 8px;">
                    <a href="<c:url value='/signup'><c:param name='email' value='${enteredEmail}'/></c:url>"
                       style="color: #b91c1c; font-weight: 700; text-decoration: underline;">
                        → 이 이메일로 회원가입하기
                    </a>
                </div>
            </c:if>
        </div>
    </c:if>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <c:if test="${not empty infoMessage}">
        <div class="alert alert-info">
            <c:out value="${infoMessage}" />
        </div>
    </c:if>

    <form method="post" action="<c:url value='/login' />">
        <div class="form-group">
            <label for="email">이메일</label>
            <input type="email" id="email" name="email" required
                   value="<c:out value='${enteredEmail}' />"
                   placeholder="example@email.com">
        </div>
        <div class="form-group">
            <label for="password">암호</label>
            <input type="password" id="password" name="password"
                   placeholder="암호를 입력하세요">
            <div class="hint">
                네이버 간편로그인으로 가입해 암호가 없는 회원은 이메일 입력 후 로그인 버튼을 누르면 최초 암호 설정 화면으로 이동합니다.
            </div>
        </div>
        <button type="submit" class="btn btn-primary">이메일로 로그인</button>
    </form>

    <div class="divider">또는</div>

    <a href="<c:url value='/oauth/naver/login' />" class="btn btn-naver">
        <span class="naver-badge">N</span>
        <span>네이버 간편로그인</span>
    </a>

    <div class="footer-links">
        처음 방문하셨나요? <a href="<c:url value='/signup' />">회원가입</a>
    </div>

    <div class="dev-box">
        <strong>테스트 안내</strong><br>
        · 기본 관리자 계정(ADMIN): <code>admin@study.com</code> / <code>admin1234</code><br>
        <c:choose>
            <c:when test="${naverRealConfigured}">
                · 네이버 OAuth: 실제 네이버 개발자센터 연동 활성화됨
            </c:when>
            <c:otherwise>
                · 네이버 OAuth: 테스트(Mock) 모드 동작 중 (<code>application.properties</code>에 키 입력 시 실연동 전환)
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
