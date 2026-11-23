package com.study.springboot.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.study.springboot.domain.Member;
import com.study.springboot.domain.VerificationPurpose;
import com.study.springboot.dto.EmailSendResponse;
import com.study.springboot.dto.EmailVerifyResponse;
import com.study.springboot.dto.LoginResult;
import com.study.springboot.dto.SessionUser;
import com.study.springboot.service.EmailVerificationService;
import com.study.springboot.service.MemberService;
import com.study.springboot.service.NaverOAuthService;

@Controller
public class AuthController {

    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;
    private final NaverOAuthService naverOAuthService;

    public AuthController(
            MemberService memberService,
            EmailVerificationService emailVerificationService,
            NaverOAuthService naverOAuthService) {
        this.memberService = memberService;
        this.emailVerificationService = emailVerificationService;
        this.naverOAuthService = naverOAuthService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(name = "logout", required = false) String logout,
            HttpSession session,
            Model model) {
        if (session.getAttribute(SessionUser.SESSION_KEY) != null) {
            return "redirect:/welcome";
        }
        if ("true".equals(logout)) {
            model.addAttribute("infoMessage", "안전하게 로그아웃되었습니다.");
        }
        model.addAttribute("naverRealConfigured", naverOAuthService.isRealOAuthConfigured());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "password", defaultValue = "") String password,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String normalizedEmail = Member.normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            model.addAttribute("errorMessage", "이메일을 입력해 주세요.");
            model.addAttribute("naverRealConfigured", naverOAuthService.isRealOAuthConfigured());
            return "login";
        }

        LoginResult result = memberService.loginWithEmail(normalizedEmail, password);

        switch (result.getStatus()) {
            case SUCCESS -> {
                session.removeAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY);
                session.setAttribute(SessionUser.SESSION_KEY, SessionUser.from(result.getMember(), "EMAIL"));
                return "redirect:/welcome";
            }
            case PASSWORD_NOT_SET -> {
                session.setAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY, result.getMember().getEmail());
                redirectAttributes.addFlashAttribute(
                        "infoMessage",
                        "간편로그인으로 가입된 계정이며 아직 암호가 설정되어 있지 않습니다. 이메일 6자리 인증 후 암호를 2번 입력하여 생성해 주세요."
                );
                return "redirect:/password/setup";
            }
            case USER_NOT_FOUND -> {
                model.addAttribute("errorMessage", "등록되지 않은 이메일입니다. 처음 사용자라면 아래 회원가입 링크를 통해 가입해 주세요.");
                model.addAttribute("showSignupHint", true);
                model.addAttribute("enteredEmail", email);
                model.addAttribute("naverRealConfigured", naverOAuthService.isRealOAuthConfigured());
                return "login";
            }
            case INVALID_PASSWORD -> {
                model.addAttribute("errorMessage", "이메일 또는 암호가 올바르지 않습니다.");
                model.addAttribute("enteredEmail", email);
                model.addAttribute("naverRealConfigured", naverOAuthService.isRealOAuthConfigured());
                return "login";
            }
        }
        return "login";
    }

    @GetMapping("/password/setup")
    public String passwordSetupPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String targetEmail = resolvePasswordSetupEmail(session);
        if (targetEmail == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "암호를 설정할 계정 정보가 없습니다. 로그인 화면에서 이메일을 입력해 주세요.");
            return "redirect:/login";
        }

        Optional<Member> memberOpt = memberService.findByEmail(targetEmail);
        if (memberOpt.isEmpty()) {
            session.removeAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY);
            return "redirect:/login";
        }

        if (memberOpt.get().hasPassword()) {
            session.removeAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY);
            return session.getAttribute(SessionUser.SESSION_KEY) != null ? "redirect:/welcome" : "redirect:/login";
        }

        model.addAttribute("targetEmail", targetEmail);
        model.addAttribute("emailVerified",
                emailVerificationService.isVerified(targetEmail, VerificationPurpose.PASSWORD_SETUP));
        return "password-setup";
    }

    @PostMapping("/password/setup")
    public String passwordSetupSubmit(
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "passwordConfirm", defaultValue = "") String passwordConfirm,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String targetEmail = resolvePasswordSetupEmail(session);
        if (targetEmail == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었습니다. 로그인 화면에서 다시 시도해 주세요.");
            return "redirect:/login";
        }

        try {
            Member updated = memberService.setupInitialPassword(targetEmail, password, passwordConfirm);
            session.removeAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY);
            session.setAttribute(SessionUser.SESSION_KEY, SessionUser.from(updated, "EMAIL"));
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "암호가 성공적으로 생성되어 로그인되었습니다. 이제 이메일+암호와 네이버 간편로그인 모두 이용하실 수 있습니다."
            );
            return "redirect:/welcome";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("targetEmail", targetEmail);
            model.addAttribute("emailVerified",
                    emailVerificationService.isVerified(targetEmail, VerificationPurpose.PASSWORD_SETUP));
            return "password-setup";
        }
    }

    @GetMapping("/signup")
    public String signupPage(
            @RequestParam(name = "email", required = false) String email,
            HttpSession session,
            Model model) {
        if (session.getAttribute(SessionUser.SESSION_KEY) != null) {
            return "redirect:/welcome";
        }
        if (email != null && !email.isBlank()) {
            model.addAttribute("enteredEmail", email);
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "passwordConfirm", defaultValue = "") String passwordConfirm,
            @RequestParam(name = "note", defaultValue = "회원 가입했음.") String note,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            memberService.registerUser(email, name, password, passwordConfirm, note);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "회원가입(일반 사용자 USER)이 완료되었습니다. 이메일과 암호로 로그인해 주세요."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("enteredEmail", email);
            model.addAttribute("enteredName", name);
            model.addAttribute("enteredNote", note);
            model.addAttribute("emailVerified",
                    emailVerificationService.isVerified(email, VerificationPurpose.SIGNUP));
            return "signup";
        }
    }

    @PostMapping("/api/email/send-code")
    @ResponseBody
    public EmailSendResponse sendVerificationCode(
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "purpose", defaultValue = "SIGNUP") String purposeStr,
            HttpSession session) {

        VerificationPurpose purpose = parsePurpose(purposeStr);
        String normalizedEmail = Member.normalizeEmail(email);

        if (!emailVerificationService.isValidEmailFormat(normalizedEmail)) {
            return new EmailSendResponse(false, "올바른 이메일 주소를 입력해 주세요.", null);
        }

        if (purpose == VerificationPurpose.SIGNUP) {
            Optional<Member> existing = memberService.findByEmail(normalizedEmail);
            if (existing.isPresent()) {
                if (!existing.get().hasPassword()) {
                    return new EmailSendResponse(
                            false,
                            "이미 네이버 간편로그인으로 가입된 이메일입니다. 로그인 페이지에서 이메일을 입력해 암호를 설정하거나 네이버로 로그인해 주세요.",
                            null
                    );
                }
                return new EmailSendResponse(false, "이미 가입된 이메일입니다. 로그인 페이지에서 로그인해 주세요.", null);
            }
        } else if (purpose == VerificationPurpose.PASSWORD_SETUP) {
            String sessionTargetEmail = resolvePasswordSetupEmail(session);
            if (sessionTargetEmail == null || !sessionTargetEmail.equals(normalizedEmail)) {
                return new EmailSendResponse(false, "암호 설정 대상 이메일이 세션과 일치하지 않습니다.", null);
            }
            Optional<Member> existing = memberService.findByEmail(normalizedEmail);
            if (existing.isEmpty() || existing.get().hasPassword()) {
                return new EmailSendResponse(false, "최초 암호 설정 대상 계정이 아닙니다.", null);
            }
        }

        return emailVerificationService.sendVerificationCode(normalizedEmail, purpose);
    }

    @PostMapping("/api/email/verify-code")
    @ResponseBody
    public EmailVerifyResponse verifyCode(
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "code", defaultValue = "") String code,
            @RequestParam(name = "purpose", defaultValue = "SIGNUP") String purposeStr,
            HttpSession session) {

        VerificationPurpose purpose = parsePurpose(purposeStr);
        String normalizedEmail = Member.normalizeEmail(email);

        if (purpose == VerificationPurpose.PASSWORD_SETUP) {
            String sessionTargetEmail = resolvePasswordSetupEmail(session);
            if (sessionTargetEmail == null || !sessionTargetEmail.equals(normalizedEmail)) {
                return new EmailVerifyResponse(false, "암호 설정 대상 이메일이 세션과 일치하지 않습니다.");
            }
        }

        return emailVerificationService.verifyCode(normalizedEmail, code, purpose);
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    private String resolvePasswordSetupEmail(HttpSession session) {
        String setupEmail = (String) session.getAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY);
        if (setupEmail != null && !setupEmail.isBlank()) {
            return setupEmail;
        }
        SessionUser loginUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (loginUser != null && !loginUser.isHasPassword()) {
            session.setAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY, loginUser.getEmail());
            return loginUser.getEmail();
        }
        return null;
    }

    private VerificationPurpose parsePurpose(String purposeStr) {
        try {
            return VerificationPurpose.valueOf(purposeStr.trim().toUpperCase());
        } catch (Exception e) {
            return VerificationPurpose.SIGNUP;
        }
    }
}
