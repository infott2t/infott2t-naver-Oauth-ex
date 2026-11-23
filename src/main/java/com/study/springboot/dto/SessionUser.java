package com.study.springboot.dto;

import java.io.Serializable;

import com.study.springboot.domain.Member;
import com.study.springboot.domain.Role;

public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SESSION_KEY = "LOGIN_USER";
    public static final String SETUP_PASSWORD_EMAIL_KEY = "SETUP_PASSWORD_EMAIL";
    public static final String NAVER_STATE_KEY = "NAVER_OAUTH_STATE";
    public static final String PENDING_NAVER_PROFILE_KEY = "PENDING_NAVER_PROFILE";

    private final Long id;
    private final String email;
    private final String name;
    private final Role role;
    private final boolean hasPassword;
    private final boolean naverLinked;
    private final String loginMethod;
    private final String note;
    private final String profileImage;

    public SessionUser(Long id, String email, String name, Role role,
                       boolean hasPassword, boolean naverLinked, String loginMethod,
                       String note, String profileImage) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.hasPassword = hasPassword;
        this.naverLinked = naverLinked;
        this.loginMethod = loginMethod;
        this.note = note;
        this.profileImage = profileImage;
    }

    public static SessionUser from(Member member, String loginMethod) {
        return new SessionUser(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.hasPassword(),
                member.isNaverLinked(),
                loginMethod,
                member.getNote(),
                member.getProfileImage()
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public boolean isNaverLinked() {
        return naverLinked;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public String getLoginMethod() {
        return loginMethod;
    }

    public String getNote() {
        return note;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
