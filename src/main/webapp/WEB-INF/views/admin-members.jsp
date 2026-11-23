<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원 관리 (ADMIN) - SpringBoot</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0;
            padding: 32px 20px;
            font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
            background: #f3f5f9;
            color: #1f2937;
        }
        .container {
            max-width: 920px;
            margin: 0 auto;
            background: #ffffff;
            border-radius: 14px;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
            padding: 28px;
        }
        .top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
        }
        h1 { margin: 0; font-size: 22px; }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }
        th, td {
            padding: 11px 10px;
            border-bottom: 1px solid #e5e7eb;
            text-align: left;
        }
        th { background: #f9fafb; color: #4b5563; }
        .badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
        }
        .badge-admin { background: #fee2e2; color: #b91c1c; }
        .badge-user { background: #dbeafe; color: #1d4ed8; }
        .btn {
            display: inline-block;
            padding: 8px 14px;
            border-radius: 8px;
            text-decoration: none;
            background: #2563eb;
            color: #ffffff;
            font-size: 13px;
            font-weight: 600;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="top">
        <h1>관리자 페이지 - 전체 회원 목록</h1>
        <a href="<c:url value='/welcome' />" class="btn">Welcome으로 돌아가기</a>
    </div>

    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>이메일</th>
            <th>이름</th>
            <th>권한</th>
            <th>암호 설정</th>
            <th>네이버 연동</th>
            <th>참고</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="m" items="${members}">
            <tr>
                <td><c:out value="${m.id}" /></td>
                <td><c:out value="${m.email}" /></td>
                <td><c:out value="${m.name}" /></td>
                <td>
                    <c:choose>
                        <c:when test="${m.role == 'ADMIN'}">
                            <span class="badge badge-admin">ADMIN</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-user">USER</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${m.hasPassword()}">설정됨</c:when>
                        <c:otherwise>미설정</c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${m.naverLinked}">연동됨</c:when>
                        <c:otherwise>-</c:otherwise>
                    </c:choose>
                </td>
                <td><c:out value="${m.note}" /></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
