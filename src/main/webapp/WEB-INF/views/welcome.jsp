<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome - SpringBoot</title>
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
            max-width: 560px;
            background: #ffffff;
            border-radius: 14px;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
            padding: 36px 32px;
        }
        .header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 18px;
        }
        h1 { margin: 0; font-size: 26px; }
        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
        }
        .badge-user { background: #dbeafe; color: #1d4ed8; }
        .badge-admin { background: #fee2e2; color: #b91c1c; }
        .badge-ok { background: #dcfce7; color: #15803d; }
        .badge-warn { background: #fef3c7; color: #b45309; }
        .alert-success {
            padding: 12px 14px;
            border-radius: 8px;
            background: #ecfdf5;
            border: 1px solid #a7f3d0;
            color: #047857;
            font-size: 13px;
            margin-bottom: 18px;
            line-height: 1.5;
        }
        .info-table {
            width: 100%;
            border-collapse: collapse;
            margin: 16px 0 20px;
            font-size: 14px;
        }
        .info-table th, .info-table td {
            padding: 11px 12px;
            border-bottom: 1px solid #e5e7eb;
            text-align: left;
            vertical-align: middle;
        }
        .info-table th {
            width: 34%;
            color: #6b7280;
            font-weight: 600;
        }
        .note-highlight {
            display: inline-block;
            padding: 6px 10px;
            border-radius: 6px;
            background: #eff6ff;
            border: 1px solid #bfdbfe;
            color: #1e40af;
            font-weight: 600;
        }
        .note-form {
            display: flex;
            gap: 8px;
            margin-top: 8px;
        }
        .note-form input[type="text"] {
            flex: 1;
            padding: 8px 10px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            font-size: 13px;
        }
        .note-form button {
            padding: 8px 12px;
            border: none;
            border-radius: 6px;
            background: #374151;
            color: #ffffff;
            font-size: 12px;
            font-weight: 600;
            cursor: pointer;
            white-space: nowrap;
        }
        .note-form button:hover { background: #1f2937; }
        .actions {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
        }
        .btn {
            display: inline-block;
            padding: 10px 16px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            border: none;
            cursor: pointer;
        }
        .btn-primary { background: #2563eb; color: #ffffff; }
        .btn-admin { background: #7c3aed; color: #ffffff; }
        .btn-danger { background: #ef4444; color: #ffffff; }
        .links {
            margin-top: 22px;
            padding-top: 16px;
            border-top: 1px solid #e5e7eb;
            font-size: 13px;
            color: #6b7280;
            display: flex;
            gap: 14px;
        }
        .links a { color: #2563eb; text-decoration: none; }
        .links a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<div class="card">
    <div class="header">
        <h1>Welcome!</h1>
        <c:choose>
            <c:when test="${loginUser.admin}">
                <span class="badge badge-admin">ROLE: ADMIN</span>
            </c:when>
            <c:otherwise>
                <span class="badge badge-user">ROLE: USER</span>
            </c:otherwise>
        </c:choose>
    </div>

    <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 14px;">
        <c:if test="${not empty loginUser.profileImage}">
            <img src="<c:out value='${loginUser.profileImage}' />" alt="프로필 사진"
                 style="width: 52px; height: 52px; border-radius: 50%; object-fit: cover; border: 2px solid #2563eb;">
        </c:if>
        <p style="margin: 0; color: #4b5563;">
            <strong><c:out value="${loginUser.name}" /></strong>님, 환영합니다!
        </p>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert-success"><c:out value="${successMessage}" /></div>
    </c:if>

    <table class="info-table">
        <tr>
            <th>회원 번호 (ID)</th>
            <td>#<c:out value="${loginUser.id}" /></td>
        </tr>
        <tr>
            <th>이메일</th>
            <td><c:out value="${loginUser.email}" /></td>
        </tr>
        <tr>
            <th>이름</th>
            <td><c:out value="${loginUser.name}" /></td>
        </tr>
        <tr>
            <th>권한 (Role)</th>
            <td><c:out value="${loginUser.role}" /></td>
        </tr>
        <tr>
            <th>현재 로그인 방식</th>
            <td>
                <c:choose>
                    <c:when test="${loginUser.loginMethod == 'NAVER'}">네이버 간편로그인</c:when>
                    <c:otherwise>이메일 + 암호 로그인</c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>이메일 암호 상태</th>
            <td>
                <c:choose>
                    <c:when test="${loginUser.hasPassword}">
                        <span class="badge badge-ok">암호 설정됨</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-warn">미설정 (간편로그인 전용)</span>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>네이버 간편로그인 연동</th>
            <td>
                <c:choose>
                    <c:when test="${loginUser.naverLinked}">
                        <span class="badge badge-ok">연동됨</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-warn">미연동</span>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>참고 (본인 확인용)</th>
            <td>
                <div class="note-highlight">
                    <c:out value="${empty loginUser.note ? '회원 가입했음.' : loginUser.note}" />
                </div>
                <form method="post" action="<c:url value='/welcome/note' />" class="note-form">
                    <input type="text" name="note" maxlength="500"
                           value="<c:out value='${loginUser.note}' />"
                           placeholder="본인 확인용 참고 메모 입력">
                    <button type="submit">참고 저장</button>
                </form>
            </td>
        </tr>
    </table>

    <div class="actions">
        <c:if test="${not loginUser.hasPassword}">
            <a href="<c:url value='/password/setup' />" class="btn btn-primary">이메일 로그인용 암호 설정하기</a>
        </c:if>
        <c:if test="${loginUser.admin}">
            <a href="<c:url value='/admin/members' />" class="btn btn-admin">관리자: 회원 목록 보기</a>
        </c:if>
        <form method="post" action="<c:url value='/logout' />" style="display:inline;">
            <button type="submit" class="btn btn-danger">로그아웃</button>
        </form>
    </div>

    <div class="links">
        <span>기존 페이지 바로가기:</span>
        <a href="<c:url value='/test1' />">/test1</a>
        <a href="<c:url value='/test2' />">/test2</a>
        <a href="<c:url value='/' />">/</a>
    </div>
</div>
</body>
</html>
