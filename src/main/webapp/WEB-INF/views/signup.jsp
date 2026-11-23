<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - SpringBoot</title>
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
            max-width: 460px;
            background: #ffffff;
            border-radius: 14px;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
            padding: 34px 30px;
        }
        h1 { margin: 0 0 6px; font-size: 24px; text-align: center; }
        .subtitle {
            margin: 0 0 22px;
            font-size: 13px;
            color: #6b7280;
            text-align: center;
        }
        .alert {
            padding: 12px 14px;
            border-radius: 8px;
            font-size: 13px;
            margin-bottom: 16px;
            line-height: 1.5;
        }
        .alert-error { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; }
        .form-group { margin-bottom: 14px; }
        label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 6px;
            color: #374151;
        }
        .inline-row {
            display: flex;
            gap: 8px;
        }
        input[type="email"], input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 14px;
            outline: none;
        }
        input[readonly] {
            background: #f3f4f6;
            color: #4b5563;
        }
        input:focus {
            border-color: #2563eb;
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }
        .btn-sub {
            white-space: nowrap;
            padding: 0 14px;
            border: none;
            border-radius: 8px;
            background: #374151;
            color: #ffffff;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
        }
        .btn-sub:hover:not(:disabled) { background: #1f2937; }
        .btn-sub:disabled {
            background: #9ca3af;
            cursor: not-allowed;
        }
        .btn-verify { background: #059669; }
        .btn-verify:hover:not(:disabled) { background: #047857; }
        .status-msg {
            margin-top: 6px;
            font-size: 12px;
            line-height: 1.4;
        }
        .status-ok { color: #047857; font-weight: 600; }
        .status-err { color: #b91c1c; }
        .timer-box {
            margin-top: 8px;
            padding: 9px 12px;
            border-radius: 8px;
            background: #eff6ff;
            border: 1px solid #bfdbfe;
            color: #1e40af;
            font-size: 13px;
            font-weight: 600;
            display: none;
            align-items: center;
            justify-content: space-between;
        }
        .timer-value {
            font-variant-numeric: tabular-nums;
            font-size: 15px;
            font-weight: 800;
            color: #dc2626;
        }
        .dev-code {
            margin-top: 6px;
            padding: 8px 10px;
            background: #fefce8;
            border: 1px dashed #eab308;
            border-radius: 6px;
            font-size: 12px;
            color: #854d0e;
            display: none;
        }
        .btn-primary {
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 8px;
            background: #2563eb;
            color: #ffffff;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            margin-top: 10px;
        }
        .btn-primary:hover { background: #1d4ed8; }
        .footer-links {
            margin-top: 18px;
            text-align: center;
            font-size: 13px;
            color: #6b7280;
        }
        .footer-links a {
            color: #2563eb;
            font-weight: 700;
            text-decoration: none;
        }
    </style>
</head>
<body>
<div class="card">
    <h1>회원가입</h1>
    <p class="subtitle">네이버 메일이 아닌 모든 이메일로 가입할 수 있으며, 일반 사용자(USER)로 등록됩니다.</p>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error"><c:out value="${errorMessage}" /></div>
    </c:if>

    <form method="post" action="<c:url value='/signup' />" id="signupForm">
        <div class="form-group">
            <label for="email">이메일 (6자리 메일 인증 필수)</label>
            <div class="inline-row">
                <input type="email" id="email" name="email" required
                       value="<c:out value='${enteredEmail}' />"
                       placeholder="example@gmail.com">
                <button type="button" class="btn-sub" id="sendCodeBtn">인증번호 발송</button>
            </div>
            <div id="sendStatus" class="status-msg"></div>
            <div id="timerBox" class="timer-box">
                <span>5분 뒤에 재발송 가능합니다.</span>
                <span class="timer-value" id="timerText">05:00</span>
            </div>
            <div id="devCodeBox" class="dev-code"></div>
        </div>

        <div class="form-group">
            <label for="verifyCode">이메일 인증번호 (6자리 숫자)</label>
            <div class="inline-row">
                <input type="text" id="verifyCode" maxlength="6" inputmode="numeric"
                       placeholder="6자리 숫자 입력">
                <button type="button" class="btn-sub btn-verify" id="verifyCodeBtn">인증 확인</button>
            </div>
            <div id="verifyStatus" class="status-msg">
                <c:if test="${emailVerified}">
                    <span class="status-ok">이메일 인증이 완료되었습니다.</span>
                </c:if>
            </div>
        </div>

        <div class="form-group">
            <label for="name">이름</label>
            <input type="text" id="name" name="name" required
                   value="<c:out value='${enteredName}' />"
                   placeholder="이름을 입력하세요">
        </div>

        <div class="form-group">
            <label for="password">암호 (4자 이상)</label>
            <input type="password" id="password" name="password" required minlength="4"
                   placeholder="암호를 입력하세요">
        </div>

        <div class="form-group">
            <label for="passwordConfirm">암호 확인</label>
            <input type="password" id="passwordConfirm" name="passwordConfirm" required minlength="4"
                   placeholder="암호를 한 번 더 입력하세요">
            <div id="pwMatchStatus" class="status-msg"></div>
        </div>

        <div class="form-group">
            <label for="note">참고 (본인 확인용 메모)</label>
            <input type="text" id="note" name="note" maxlength="500"
                   value="<c:out value='${empty enteredNote ? "회원 가입했음." : enteredNote}' />"
                   placeholder="회원 가입했음.">
        </div>

        <div style="margin: 14px 0; padding: 12px; border-radius: 8px; background: #f8fafc; border: 1px solid #cbd5e1; font-size: 12px; color: #475569; line-height: 1.6;">
            <strong style="color: #0f172a; display: block; margin-bottom: 4px;">회원가입 수집·제공 정보 안내</strong>
            · <strong>이메일 가입 필수 항목</strong>: 연락처 이메일 주소(6자리 인증), 회원이름, 암호(암호화 저장)<br>
            · <strong>네이버 간편로그인 연동 시 제공 항목</strong>: 이용자 식별자(기본), 회원이름(필수), 연락처 이메일 주소(필수), 프로필 사진(필수)<br>
            · 수집된 정보는 회원 식별, 통합 로그인 및 본인 확인 목적으로만 사용됩니다.
        </div>

        <button type="submit" class="btn-primary">회원가입 완료</button>
    </form>

    <div class="footer-links">
        이미 계정이 있으신가요? <a href="<c:url value='/login' />">로그인으로 돌아가기</a>
    </div>
</div>

<script>
    (function () {
        var contextPath = '<c:out value="${pageContext.request.contextPath}" />';
        var emailInput = document.getElementById('email');
        var sendCodeBtn = document.getElementById('sendCodeBtn');
        var verifyCodeInput = document.getElementById('verifyCode');
        var verifyCodeBtn = document.getElementById('verifyCodeBtn');
        var sendStatus = document.getElementById('sendStatus');
        var verifyStatus = document.getElementById('verifyStatus');
        var timerBox = document.getElementById('timerBox');
        var timerText = document.getElementById('timerText');
        var devCodeBox = document.getElementById('devCodeBox');
        var pwInput = document.getElementById('password');
        var pwConfirmInput = document.getElementById('passwordConfirm');
        var pwMatchStatus = document.getElementById('pwMatchStatus');

        var countdownInterval = null;
        var isVerifiedDone = false;

        function formatTime(totalSeconds) {
            var mins = Math.floor(totalSeconds / 60);
            var secs = totalSeconds % 60;
            return (mins < 10 ? '0' : '') + mins + ':' + (secs < 10 ? '0' : '') + secs;
        }

        function stopTimer() {
            if (countdownInterval !== null) {
                clearInterval(countdownInterval);
                countdownInterval = null;
            }
        }

        function startCooldownTimer(seconds) {
            stopTimer();
            var remaining = parseInt(seconds, 10);
            if (isNaN(remaining) || remaining <= 0) {
                return;
            }

            sendCodeBtn.disabled = true;
            timerBox.style.display = 'flex';
            timerText.textContent = formatTime(remaining);
            sendCodeBtn.textContent = '재발송 ' + formatTime(remaining);

            countdownInterval = setInterval(function () {
                remaining -= 1;
                if (remaining <= 0) {
                    stopTimer();
                    timerBox.style.display = 'none';
                    if (!isVerifiedDone) {
                        sendCodeBtn.disabled = false;
                        sendCodeBtn.textContent = '인증번호 발송';
                        sendStatus.className = 'status-msg status-err';
                        sendStatus.textContent = '인증 유효시간(5분)이 만료되었습니다. 인증번호를 다시 발송할 수 있습니다.';
                    }
                    return;
                }
                var formatted = formatTime(remaining);
                timerText.textContent = formatted;
                sendCodeBtn.textContent = '재발송 ' + formatted;
            }, 1000);
        }

        sendCodeBtn.addEventListener('click', function () {
            if (sendCodeBtn.disabled || isVerifiedDone) {
                return;
            }
            var email = emailInput.value.trim();
            if (!email) {
                sendStatus.className = 'status-msg status-err';
                sendStatus.textContent = '이메일을 먼저 입력해 주세요.';
                return;
            }

            sendCodeBtn.disabled = true;
            sendCodeBtn.textContent = '발송 중...';
            sendStatus.className = 'status-msg';
            sendStatus.textContent = '인증번호를 발송하는 중입니다...';
            devCodeBox.style.display = 'none';

            var body = new URLSearchParams();
            body.append('email', email);
            body.append('purpose', 'SIGNUP');

            fetch(contextPath + '/api/email/send-code', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: body.toString()
            })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                sendStatus.className = data.success ? 'status-msg status-ok' : 'status-msg status-err';
                sendStatus.textContent = data.message;

                if (data.success && data.devCode) {
                    devCodeBox.style.display = 'block';
                    devCodeBox.textContent = '[개발 모드] 발급된 6자리 인증번호: ' + data.devCode;
                }

                if (data.remainingSeconds && data.remainingSeconds > 0) {
                    startCooldownTimer(data.remainingSeconds);
                } else {
                    sendCodeBtn.disabled = false;
                    sendCodeBtn.textContent = '인증번호 발송';
                }
            })
            .catch(function () {
                sendStatus.className = 'status-msg status-err';
                sendStatus.textContent = '인증번호 발송 요청 중 오류가 발생했습니다.';
                sendCodeBtn.disabled = false;
                sendCodeBtn.textContent = '인증번호 발송';
            });
        });

        verifyCodeBtn.addEventListener('click', function () {
            var email = emailInput.value.trim();
            var code = verifyCodeInput.value.trim();
            var body = new URLSearchParams();
            body.append('email', email);
            body.append('code', code);
            body.append('purpose', 'SIGNUP');

            fetch(contextPath + '/api/email/verify-code', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: body.toString()
            })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                verifyStatus.className = data.success ? 'status-msg status-ok' : 'status-msg status-err';
                verifyStatus.textContent = data.message;
                if (data.success) {
                    isVerifiedDone = true;
                    stopTimer();
                    timerBox.style.display = 'none';
                    emailInput.readOnly = true;
                    sendCodeBtn.disabled = true;
                    sendCodeBtn.textContent = '인증 완료';
                    verifyCodeInput.readOnly = true;
                    verifyCodeBtn.disabled = true;
                }
            })
            .catch(function () {
                verifyStatus.className = 'status-msg status-err';
                verifyStatus.textContent = '인증 확인 중 오류가 발생했습니다.';
            });
        });

        function checkPasswordMatch() {
            if (!pwConfirmInput.value) {
                pwMatchStatus.textContent = '';
                return;
            }
            if (pwInput.value === pwConfirmInput.value) {
                pwMatchStatus.className = 'status-msg status-ok';
                pwMatchStatus.textContent = '두 암호가 일치합니다.';
            } else {
                pwMatchStatus.className = 'status-msg status-err';
                pwMatchStatus.textContent = '두 암호가 일치하지 않습니다.';
            }
        }
        pwInput.addEventListener('input', checkPasswordMatch);
        pwConfirmInput.addEventListener('input', checkPasswordMatch);
    })();
</script>
</body>
</html>
