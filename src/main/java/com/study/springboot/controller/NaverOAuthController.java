package com.study.springboot.controller;

import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.study.springboot.domain.Member;
import com.study.springboot.dto.NaverUserProfile;
import com.study.springboot.dto.SessionUser;
import com.study.springboot.service.EmailVerificationService;
import com.study.springboot.service.MemberService;
import com.study.springboot.service.NaverOAuthService;

@Controller
@RequestMapping("/oauth/naver")
public class NaverOAuthController {

    private final NaverOAuthService naverOAuthService;
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;

    public NaverOAuthController(
            NaverOAuthService naverOAuthService,
            MemberService memberService,
            EmailVerificationService emailVerificationService) {
        this.naverOAuthService = naverOAuthService;
        this.memberService = memberService;
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/login")
    public String startNaverLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        if (naverOAuthService.isRealOAuthConfigured()) {
            String state = UUID.randomUUID().toString();
            session.setAttribute(SessionUser.NAVER_STATE_KEY, state);
            return "redirect:" + naverOAuthService.buildAuthorizeUrl(state);
        }

        if (naverOAuthService.isMockEnabled()) {
            return "redirect:/oauth/naver/mock";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "네이버 간편로그인 설정(client-id, client-secret)이 되어 있지 않습니다.");
        return "redirect:/login";
    }

    @GetMapping("/callback")
    public String naverCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String savedState = (String) session.getAttribute(SessionUser.NAVER_STATE_KEY);
        session.removeAttribute(SessionUser.NAVER_STATE_KEY);

        if (error != null || code == null || savedState == null || !savedState.equals(state)) {
            redirectAttributes.addFlashAttribute("errorMessage", "네이버 간편로그인이 취소되었거나 인증 상태가 유효하지 않습니다.");
            return "redirect:/login";
        }

        try {
            NaverUserProfile profile = naverOAuthService.fetchUserProfile(code, state);
            return handleNaverProfile(profile, session);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "네이버 간편로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/terms")
    public String naverTermsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        NaverUserProfile pending = (NaverUserProfile) session.getAttribute(SessionUser.PENDING_NAVER_PROFILE_KEY);
        if (pending == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "약관 동의 대상 네이버 인증 정보가 없습니다. 다시 로그인해 주세요.");
            return "redirect:/login";
        }
        model.addAttribute("naverProfile", pending);
        return "naver-terms";
    }

    @PostMapping("/terms")
    public String naverTermsSubmit(
            @RequestParam(name = "agreeService", defaultValue = "false") boolean agreeService,
            @RequestParam(name = "agreePrivacy", defaultValue = "false") boolean agreePrivacy,
            @RequestParam(name = "note", defaultValue = "회원 가입했음.") String note,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        NaverUserProfile pending = (NaverUserProfile) session.getAttribute(SessionUser.PENDING_NAVER_PROFILE_KEY);
        if (pending == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었습니다. 네이버 간편로그인을 다시 시도해 주세요.");
            return "redirect:/login";
        }

        if (!agreeService || !agreePrivacy) {
            model.addAttribute("errorMessage", "필수 약관(서비스 이용약관, 개인정보 수집 및 이용 동의)에 모두 동의하셔야 가입할 수 있습니다.");
            model.addAttribute("naverProfile", pending);
            model.addAttribute("enteredNote", note);
            return "naver-terms";
        }

        Member member = memberService.registerNaverUserWithTerms(pending, note);
        session.removeAttribute(SessionUser.PENDING_NAVER_PROFILE_KEY);
        session.removeAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY);
        session.setAttribute(SessionUser.SESSION_KEY, SessionUser.from(member, "NAVER"));
        redirectAttributes.addFlashAttribute("successMessage", "약관 동의 및 네이버 간편 회원가입(USER)이 완료되었습니다!");
        return "redirect:/welcome";
    }

    @PostMapping("/terms/cancel")
    public String cancelNaverTerms(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(SessionUser.PENDING_NAVER_PROFILE_KEY);
        redirectAttributes.addFlashAttribute("infoMessage", "네이버 간편 회원가입이 취소되었습니다.");
        return "redirect:/login";
    }

    @GetMapping("/mock")
    public String mockNaverLoginPage(RedirectAttributes redirectAttributes) {
        if (!naverOAuthService.isMockEnabled()) {
            redirectAttributes.addFlashAttribute("errorMessage", "테스트용 네이버 간편로그인이 비활성화되어 있습니다.");
            return "redirect:/login";
        }
        return "naver-mock";
    }

    @PostMapping("/mock")
    public String mockNaverLoginSubmit(
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "name", defaultValue = "") String name,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!naverOAuthService.isMockEnabled()) {
            redirectAttributes.addFlashAttribute("errorMessage", "테스트용 네이버 간편로그인이 비활성화되어 있습니다.");
            return "redirect:/login";
        }

        String normalizedEmail = Member.normalizeEmail(email);
        if (!emailVerificationService.isValidEmailFormat(normalizedEmail)) {
            model.addAttribute("errorMessage", "올바른 이메일 형식을 입력해 주세요.");
            model.addAttribute("enteredEmail", email);
            model.addAttribute("enteredName", name);
            return "naver-mock";
        }

        String displayName = (name == null || name.isBlank()) ? normalizedEmail.split("@")[0] : name.trim();
        String mockNaverId = "mock-naver-" + normalizedEmail;

        return handleNaverProfile(new NaverUserProfile(mockNaverId, normalizedEmail, displayName), session);
    }

    private String handleNaverProfile(NaverUserProfile profile, HttpSession session) {
        Optional<Member> existingOpt = memberService.findAndLinkExistingNaverMember(profile);
        if (existingOpt.isPresent()) {
            session.removeAttribute(SessionUser.PENDING_NAVER_PROFILE_KEY);
            session.removeAttribute(SessionUser.SETUP_PASSWORD_EMAIL_KEY);
            session.setAttribute(SessionUser.SESSION_KEY, SessionUser.from(existingOpt.get(), "NAVER"));
            return "redirect:/welcome";
        }

        session.setAttribute(SessionUser.PENDING_NAVER_PROFILE_KEY, profile);
        return "redirect:/oauth/naver/terms";
    }
}
