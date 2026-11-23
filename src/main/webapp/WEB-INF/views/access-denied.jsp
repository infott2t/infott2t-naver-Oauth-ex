<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>접근 권한 없음 - SpringBoot</title>
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
            background: #f3f5f9;
        }
        .card {
            background: #ffffff;
            padding: 32px;
            border-radius: 12px;
            text-align: center;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
            max-width: 400px;
        }
        h1 { color: #b91c1c; margin-top: 0; }
        a {
            display: inline-block;
            margin-top: 14px;
            padding: 10px 16px;
            border-radius: 8px;
            background: #2563eb;
            color: #ffffff;
            text-decoration: none;
            font-weight: 600;
        }
    </style>
</head>
<body>
<div class="card">
    <h1>403 접근 권한 없음</h1>
    <p>관리자(ADMIN) 권한이 필요한 페이지입니다.</p>
    <a href="<c:url value='/welcome' />">Welcome으로 돌아가기</a>
</div>
</body>
</html>
