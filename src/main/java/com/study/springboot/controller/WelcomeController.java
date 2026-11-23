package com.study.springboot.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.study.springboot.domain.Member;
import com.study.springboot.dto.SessionUser;
import com.study.springboot.service.MemberService;

@Controller
public class WelcomeController {

    private final MemberService memberService;

    public WelcomeController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/welcome")
    public String welcomePage(HttpSession session, Model model) {
        SessionUser loginUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (loginUser == null) {
            return "redirect:/login";
        }

        Optional<Member> latestOpt = memberService.findById(loginUser.getId());
        if (latestOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        Member member = latestOpt.get();
        SessionUser refreshed = SessionUser.from(member, loginUser.getLoginMethod());
        session.setAttribute(SessionUser.SESSION_KEY, refreshed);

        model.addAttribute("loginUser", refreshed);
        model.addAttribute("member", member);
        return "welcome";
    }

    @PostMapping("/welcome/note")
    public String updateNote(
            @RequestParam(name = "note", defaultValue = "") String note,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        SessionUser loginUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (loginUser == null) {
            return "redirect:/login";
        }

        Member updated = memberService.updateNote(loginUser.getId(), note);
        session.setAttribute(SessionUser.SESSION_KEY, SessionUser.from(updated, loginUser.getLoginMethod()));
        redirectAttributes.addFlashAttribute("successMessage", "참고 정보가 저장되었습니다. 간편로그인/일반로그인 모두 동일하게 표시됩니다.");
        return "redirect:/welcome";
    }

    @GetMapping("/admin/members")
    public String adminMembersPage(HttpSession session, Model model) {
        SessionUser loginUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("members", memberService.findAllMembers());
        return "admin-members";
    }
}
