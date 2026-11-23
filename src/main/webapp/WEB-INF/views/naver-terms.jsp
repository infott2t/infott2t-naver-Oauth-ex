<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>약관 동의 - 네이버 간편 회원가입</title>
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
            padding: 24px 16px;
        }
        .card {
            width: 100%;
            max-width: 600px;
            background: #ffffff;
            border-radius: 14px;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
            padding: 34px 30px;
        }
        .naver-tag {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 999px;
            background: #dcfce7;
            color: #15803d;
            font-size: 12px;
            font-weight: 700;
            margin-bottom: 10px;
        }
        h1 { margin: 0 0 8px; font-size: 24px; }
        .subtitle {
            margin: 0 0 18px;
            font-size: 13px;
            color: #6b7280;
            line-height: 1.5;
        }
        .profile-box {
            display: flex;
            align-items: center;
            gap: 14px;
            padding: 14px;
            border-radius: 10px;
            background: #f9fafb;
            border: 1px solid #e5e7eb;
            margin-bottom: 18px;
            font-size: 13px;
            line-height: 1.7;
        }
        .avatar {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #03c75a;
            flex-shrink: 0;
        }
        .info-guide {
            margin-bottom: 18px;
            padding: 14px;
            border-radius: 10px;
            background: #f8fafc;
            border: 1px solid #cbd5e1;
        }
        .info-guide-title {
            font-size: 13px;
            font-weight: 700;
            color: #0f172a;
            margin: 0 0 8px;
        }
        .info-guide-desc {
            font-size: 12px;
            color: #475569;
            margin: 0 0 10px;
            line-height: 1.5;
        }
        .provide-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
            background: #ffffff;
            border-radius: 6px;
            overflow: hidden;
        }
        .provide-table th, .provide-table td {
            padding: 8px 10px;
            border: 1px solid #e2e8f0;
            text-align: left;
        }
        .provide-table th {
            background: #f1f5f9;
            color: #334155;
            font-weight: 700;
        }
        .pill-basic {
            display: inline-block;
            padding: 2px 6px;
            border-radius: 4px;
            background: #e2e8f0;
            color: #334155;
            font-weight: 700;
            font-size: 11px;
        }
        .pill-req {
            display: inline-block;
            padding: 2px 6px;
            border-radius: 4px;
            background: #fee2e2;
            color: #b91c1c;
            font-weight: 700;
            font-size: 11px;
        }
        .alert-error {
            padding: 12px 14px;
            border-radius: 8px;
            background: #fef2f2;
            border: 1px solid #fecaca;
            color: #b91c1c;
            font-size: 13px;
            margin-bottom: 16px;
        }
        .agree-all {
            padding: 12px 14px;
            border-radius: 8px;
            background: #eff6ff;
            border: 1px solid #bfdbfe;
            margin-bottom: 16px;
            font-weight: 700;
            font-size: 14px;
            color: #1e3a8a;
        }
        .term-section { margin-bottom: 16px; }
        .term-check {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 8px;
            cursor: pointer;
        }
        .req { color: #dc2626; font-weight: 700; }
        .term-content {
            height: 110px;
            overflow-y: auto;
            padding: 10px 12px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            background: #fafafa;
            font-size: 12px;
            color: #4b5563;
            line-height: 1.6;
        }
        .agree-hint {
            font-size: 12px;
            color: #dc2626;
            margin: 6px 0 10px;
            text-align: center;
        }
        .btn-row {
            display: flex;
            gap: 10px;
            margin-top: 10px;
        }
        .btn {
            flex: 1;
            padding: 12px;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
        }
        .btn-naver {
            background: #03c75a;
            color: #ffffff;
        }
        .btn-naver:hover:not(:disabled) { background: #02b350; }
        .btn-naver:disabled {
            background: #9ca3af;
            cursor: not-allowed;
        }
        .btn-cancel {
            background: #e5e7eb;
            color: #374151;
            max-width: 150px;
        }
        .btn-cancel:hover { background: #d1d5db; }
    </style>
</head>
<body>
<div class="card">
    <span class="naver-tag">NAVER 최초 간편 회원가입</span>
    <h1>약관 동의 및 제공 정보 안내</h1>
    <p class="subtitle">
        아직 가입되지 않은 계정입니다. 아래 네이버 제공 정보와 필수 약관에 모두 동의하시면<br>
        일반 사용자(USER)로 간편 회원가입이 완료되어 Welcome 페이지로 이동합니다.
    </p>

    <div class="profile-box">
        <c:if test="${not empty naverProfile.profileImage}">
            <img src="<c:out value='${naverProfile.profileImage}' />" alt="네이버 프로필 사진" class="avatar">
        </c:if>
        <div>
            <strong>연동될 네이버 계정 정보</strong><br>
            · 회원이름: <c:out value="${naverProfile.name}" /><br>
            · 연락처 이메일 주소: <c:out value="${naverProfile.email}" />
        </div>
    </div>

    <div class="info-guide">
        <p class="info-guide-title">네이버 간편로그인 시 제공되는 개인정보 안내</p>
        <p class="info-guide-desc">
            개인정보보호법에 따라 서비스 제공에 필요한 최소한의 정보만 네이버로부터 전달받아 사용합니다.
            별명, 성별, 생일 등 체크되지 않은 항목은 수집하지 않습니다.
        </p>
        <table class="provide-table">
            <thead>
            <tr>
                <th style="width: 32%;">제공 항목</th>
                <th style="width: 18%;">구분</th>
                <th>이용 목적</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><strong>이용자 식별자</strong></td>
                <td><span class="pill-basic">기본 제공</span></td>
                <td>네이버 간편로그인 계정 식별 및 중복 가입 방지</td>
            </tr>
            <tr>
                <td><strong>회원이름</strong></td>
                <td><span class="pill-req">필수</span></td>
                <td>서비스 내 회원 이름 표시 및 본인 확인</td>
            </tr>
            <tr>
                <td><strong>연락처 이메일 주소</strong></td>
                <td><span class="pill-req">필수</span></td>
                <td>일반 이메일 로그인과 계정 통합, 6자리 이메일 본인 인증 발송</td>
            </tr>
            <tr>
                <td><strong>프로필 사진</strong></td>
                <td><span class="pill-req">필수</span></td>
                <td>회원 환영(Welcome) 화면 프로필 이미지 표시</td>
            </tr>
            </tbody>
        </table>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert-error"><c:out value="${errorMessage}" /></div>
    </c:if>

    <form method="post" action="<c:url value='/oauth/naver/terms' />" id="termsForm">
        <div class="agree-all">
            <label style="display:flex; align-items:center; gap:8px; cursor:pointer;">
                <input type="checkbox" id="agreeAll">
                <span>전체 약관에 모두 동의합니다.</span>
            </label>
        </div>

        <div class="term-section">
            <label class="term-check">
                <input type="checkbox" name="agreeService" value="true" class="term-item req-item">
                <span><span class="req">[필수]</span> 서비스 이용약관 동의</span>
            </label>
            <div class="term-content">
                제1조 (목적)<br>
                본 약관은 SpringBoot 웹 서비스가 제공하는 회원제 서비스의 이용 조건 및 절차, 회원과 서비스 간의 권리·의무 및 책임사항을 규정함을 목적으로 합니다.<br><br>
                제2조 (회원가입 및 계정 통합)<br>
                회원은 이메일 회원가입 또는 네이버 간편로그인을 통해 가입할 수 있으며, 동일한 이메일 주소는 하나의 통합 회원 계정으로 관리됩니다. 네이버 간편로그인으로 최초 가입한 회원은 추후 이메일 6자리 인증을 거쳐 로그인용 암호를 추가로 설정할 수 있습니다.<br><br>
                제3조 (회원의 의무)<br>
                회원은 타인의 정보를 도용하거나 부정한 목적으로 서비스를 이용하여서는 안 됩니다.
            </div>
        </div>

        <div class="term-section">
            <label class="term-check">
                <input type="checkbox" name="agreePrivacy" value="true" class="term-item req-item">
                <span><span class="req">[필수]</span> 개인정보 수집 및 이용 동의</span>
            </label>
            <div class="term-content">
                1. 네이버로부터 제공받아 수집하는 개인정보 항목:<br>
                   - 기본 제공: 이용자 식별자<br>
                   - 필수 항목: 회원이름, 연락처 이메일 주소, 프로필 사진<br>
                2. 개인정보 수집 및 이용 목적: 회원 식별, 네이버 간편로그인 및 이메일 로그인 계정 통합, 6자리 이메일 본인 인증, 프로필 표시 및 권한(USER/ADMIN) 관리<br>
                3. 개인정보 보유 및 이용 기간: 회원 탈퇴 시 또는 관련 법령에 따른 보존 기간 만료 시까지 안전하게 보관 후 지체 없이 파기합니다.<br>
                4. 동의를 거부할 권리가 있으나, 필수 항목 동의 거부 시 회원가입 및 서비스 이용이 제한됩니다.
            </div>
        </div>

        <div id="agreeHint" class="agree-hint">
            필수 약관에 모두 동의하셔야 다음 단계로 진행할 수 있습니다.
        </div>

        <div class="btn-row">
            <button type="submit" class="btn btn-naver" id="submitBtn" disabled>약관 동의 및 회원가입 완료</button>
            <button type="button" class="btn btn-cancel" id="cancelBtn">취소</button>
        </div>
    </form>

    <form method="post" action="<c:url value='/oauth/naver/terms/cancel' />" id="cancelForm"></form>
</div>

<script>
    (function () {
        var agreeAll = document.getElementById('agreeAll');
        var items = document.querySelectorAll('.req-item');
        var submitBtn = document.getElementById('submitBtn');
        var agreeHint = document.getElementById('agreeHint');
        var termsForm = document.getElementById('termsForm');
        var cancelBtn = document.getElementById('cancelBtn');
        var cancelForm = document.getElementById('cancelForm');

        function syncState() {
            var allChecked = true;
            for (var i = 0; i < items.length; i++) {
                if (!items[i].checked) {
                    allChecked = false;
                    break;
                }
            }
            agreeAll.checked = allChecked;
            submitBtn.disabled = !allChecked;
            agreeHint.style.display = allChecked ? 'none' : 'block';
        }

        agreeAll.addEventListener('change', function () {
            for (var i = 0; i < items.length; i++) {
                items[i].checked = agreeAll.checked;
            }
            syncState();
        });

        for (var i = 0; i < items.length; i++) {
            items[i].addEventListener('change', syncState);
        }

        termsForm.addEventListener('submit', function (e) {
            var allChecked = true;
            for (var i = 0; i < items.length; i++) {
                if (!items[i].checked) {
                    allChecked = false;
                    break;
                }
            }
            if (!allChecked) {
                e.preventDefault();
                syncState();
            }
        });

        cancelBtn.addEventListener('click', function () {
            cancelForm.submit();
        });

        syncState();
    })();
</script>
</body>
</html>
