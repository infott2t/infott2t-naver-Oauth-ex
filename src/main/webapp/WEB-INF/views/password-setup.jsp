<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>최초 암호 설정 - SpringBoot</title>
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
        h1 { margin: 0 0 8px; font-size: 23px; text-align: center; }
        .subtitle {
            margin: 0 0 22px;
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
        .alert-error { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; }
        .alert-info { background: #eff6ff; border: 1px solid #bfdbfe; color: #1d4ed8; }
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
    <h1>최초 암호 설정</h1>
    <p class="subtitle">
        이메일 6자리 인증을 완료한 뒤 암호를 2번 입력하시면<br>
        암호가 생성되며 이메일 로그인과 네이버 간편로그인을 모두 이용하실 수 있습니다.
    </p>

    <c:if test="${not empty infoMessage}">
        <div class="alert alert-info"><c:out value="${infoMessage}" /></div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error"><c:out value="${errorMessage}" /></div>
    </c:if>

    <form method="post" action="<c:url value='/password/setup' />">
        <div class="form-group">
            <label for="targetEmail">대상 이메일</label>
            <div class="inline-row">
                <input type="email" id="targetEmail" readonly value="<c:out value='${targetEmail}' />">
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
            <label for="password">새 암호 입력 (1차)</label>
            <input type="password" id="password" name="password" required minlength="4"
                   placeholder="새 암호를 입력하세요 (4자 이상)">
        </div>

        <div class="form-group">
            <label for="passwordConfirm">새 암호 확인 입력 (2차)</label>
            <input type="password" id="passwordConfirm" name="passwordConfirm" required minlength="4"
                   placeholder="새 암호를 한 번 더 입력하세요">
            <div id="pwMatchStatus" class="status-msg"></div>
        </div>

        <button type="submit" class="btn-primary">암호 생성 후 로그인</button>
    </form>

    <div class="footer-links">
        <a href="<c:url value='/login' />">로그인 화면으로 돌아가기</a>
    </div>
</div>

<script>
    (function () {
        var contextPath = '<c:out value="${pageContext.request.contextPath}" />';
        var targetEmail = document.getElementById('targetEmail').value;
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

            sendCodeBtn.disabled = true;
            sendCodeBtn.textContent = '발송 중...';
            sendStatus.className = 'status-msg';
            sendStatus.textContent = '인증번호를 발송하는 중입니다...';
            devCodeBox.style.display = 'none';

            var body = new URLSearchParams();
            body.append('email', targetEmail);
            body.append('purpose', 'PASSWORD_SETUP');

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
            var code = verifyCodeInput.value.trim();
            var body = new URLSearchParams();
            body.append('email', targetEmail);
            body.append('code', code);
            body.append('purpose', 'PASSWORD_SETUP');

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
