package com.study.springboot.dto;

import com.study.springboot.domain.Member;

public class LoginResult {

    public enum Status {
        SUCCESS,
        USER_NOT_FOUND,
        PASSWORD_NOT_SET,
        INVALID_PASSWORD
    }

    private final Status status;
    private final Member member;

    private LoginResult(Status status, Member member) {
        this.status = status;
        this.member = member;
    }

    public static LoginResult success(Member member) {
        return new LoginResult(Status.SUCCESS, member);
    }

    public static LoginResult userNotFound() {
        return new LoginResult(Status.USER_NOT_FOUND, null);
    }

    public static LoginResult passwordNotSet(Member member) {
        return new LoginResult(Status.PASSWORD_NOT_SET, member);
    }

    public static LoginResult invalidPassword() {
        return new LoginResult(Status.INVALID_PASSWORD, null);
    }

    public Status getStatus() {
        return status;
    }

    public Member getMember() {
        return member;
    }
}
