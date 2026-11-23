package com.study.springboot.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.springboot.domain.EmailVerification;
import com.study.springboot.domain.Member;
import com.study.springboot.domain.VerificationPurpose;
import com.study.springboot.dto.EmailSendResponse;
import com.study.springboot.dto.EmailVerifyResponse;
import com.study.springboot.repository.EmailVerificationRepository;

@Service
@Transactional
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final long COOLDOWN_SECONDS = CODE_EXPIRY_MINUTES * 60L;

    private final EmailVerificationRepository verificationRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.mock-enabled:false}")
    private boolean mockEnabled;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public EmailVerificationService(
            EmailVerificationRepository verificationRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.verificationRepository = verificationRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    public boolean isValidEmailFormat(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public EmailSendResponse sendVerificationCode(String rawEmail, VerificationPurpose purpose) {
        String email = Member.normalizeEmail(rawEmail);
        if (!isValidEmailFormat(email)) {
            return new EmailSendResponse(false, "올바른 이메일 주소를 입력해 주세요.", null, 0L);
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<EmailVerification> latestOpt = verificationRepository
                .findTopByEmailAndPurposeAndConsumedFalseOrderByCreatedAtDesc(email, purpose);

        if (latestOpt.isPresent()) {
            EmailVerification latest = latestOpt.get();
            if (latest.isVerified() && !latest.isExpired(now.minusMinutes(10))) {
                return new EmailSendResponse(false, "이미 이메일 인증이 완료되었습니다.", null, 0L);
            }
            if (!latest.isExpired(now)) {
                long remainingSeconds = Math.max(1L, Duration.between(now, latest.getExpiresAt()).getSeconds());
                long mins = remainingSeconds / 60;
                long secs = remainingSeconds % 60;
                String remainText = (mins > 0) ? (mins + "분 " + secs + "초") : (secs + "초");
                return new EmailSendResponse(
                        false,
                        "이미 인증번호가 발송되었습니다. 5분 뒤에 다시 발송 가능합니다. (남은 시간: " + remainText + ")",
                        null,
                        remainingSeconds
                );
            }
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime expiresAt = now.plusMinutes(CODE_EXPIRY_MINUTES);
        String purposeLabel = (purpose == VerificationPurpose.SIGNUP) ? "회원가입" : "최초 암호 설정";

        if (!mockEnabled) {
            if (mailHost == null || mailHost.isBlank()
                    || mailUsername == null || mailUsername.isBlank()
                    || mailPassword == null || mailPassword.isBlank()) {
                log.warn("[MAIL CONFIG MISSING] spring.mail.host / username / password 설정이 비어 있습니다.");
                return new EmailSendResponse(
                        false,
                        "SMTP 메일 발송 설정이 비어 있습니다. application.properties에 발신 이메일과 앱 비밀번호를 입력해 주세요.",
                        null,
                        0L
                );
            }

            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                return new EmailSendResponse(false, "메일 발송기(JavaMailSender)를 초기화할 수 없습니다.", null, 0L);
            }

            String senderAddress = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom.trim() : mailUsername.trim();
            if (!senderAddress.contains("@") && mailHost.contains("naver.com")) {
                senderAddress = senderAddress + "@naver.com";
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(senderAddress);
                message.setTo(email);
                message.setSubject("[SpringBoot] " + purposeLabel + " 이메일 6자리 인증번호");
                message.setText("안녕하세요.\n\n"
                        + purposeLabel + "을 위한 6자리 인증번호는 [" + code + "] 입니다.\n"
                        + CODE_EXPIRY_MINUTES + "분 이내에 입력해 주세요.");
                mailSender.send(message);

                verificationRepository.save(new EmailVerification(email, code, purpose, expiresAt));
                log.info("[MAIL SENT] purpose={}, to={}", purpose, email);
                return new EmailSendResponse(
                        true,
                        "인증번호 6자리를 이메일로 발송했습니다. 5분 뒤에 다시 발송 가능합니다.",
                        null,
                        COOLDOWN_SECONDS
                );
            } catch (MailException e) {
                log.error("[MAIL SEND FAILED] to={}, error={}", email, e.getMessage(), e);
                return new EmailSendResponse(
                        false,
                        "메일 발송에 실패했습니다. SMTP 계정(이메일/앱 비밀번호)과 SMTP 허용 설정을 확인해 주세요.",
                        null,
                        0L
                );
            }
        }

        verificationRepository.save(new EmailVerification(email, code, purpose, expiresAt));
        log.info("====================================================");
        log.info("[개발 모드 이메일 인증] 용도: {}, 수신자: {}, 인증번호(6자리): {}", purposeLabel, email, code);
        log.info("====================================================");
        return new EmailSendResponse(
                true,
                "인증번호 6자리가 발급되었습니다. 5분 뒤에 다시 발송 가능합니다. (개발 모드)",
                code,
                COOLDOWN_SECONDS
        );
    }

    public EmailVerifyResponse verifyCode(String rawEmail, String inputCode, VerificationPurpose purpose) {
        String email = Member.normalizeEmail(rawEmail);
        if (inputCode == null || !inputCode.trim().matches("^\\d{6}$")) {
            return new EmailVerifyResponse(false, "인증번호는 숫자 6자리로 입력해 주세요.");
        }

        Optional<EmailVerification> latestOpt = verificationRepository
                .findTopByEmailAndPurposeAndConsumedFalseOrderByCreatedAtDesc(email, purpose);

        if (latestOpt.isEmpty()) {
            return new EmailVerifyResponse(false, "발송된 인증번호가 없습니다. 먼저 인증번호를 요청해 주세요.");
        }

        EmailVerification latest = latestOpt.get();
        if (latest.isExpired(LocalDateTime.now())) {
            return new EmailVerifyResponse(false, "인증번호 유효시간(5분)이 만료되었습니다. 다시 발송해 주세요.");
        }

        if (!latest.getCode().equals(inputCode.trim())) {
            return new EmailVerifyResponse(false, "인증번호가 일치하지 않습니다.");
        }

        latest.markVerified();
        verificationRepository.save(latest);
        return new EmailVerifyResponse(true, "이메일 인증이 완료되었습니다.");
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String rawEmail, VerificationPurpose purpose) {
        String email = Member.normalizeEmail(rawEmail);
        return verificationRepository
                .findTopByEmailAndPurposeAndConsumedFalseOrderByCreatedAtDesc(email, purpose)
                .filter(EmailVerification::isVerified)
                .filter(v -> !v.isExpired(LocalDateTime.now().minusMinutes(10)))
                .isPresent();
    }

    public void consumeVerification(String rawEmail, VerificationPurpose purpose) {
        String email = Member.normalizeEmail(rawEmail);
        verificationRepository
                .findTopByEmailAndPurposeAndConsumedFalseOrderByCreatedAtDesc(email, purpose)
                .ifPresent(v -> {
                    v.markConsumed();
                    verificationRepository.save(v);
                });
    }
}
