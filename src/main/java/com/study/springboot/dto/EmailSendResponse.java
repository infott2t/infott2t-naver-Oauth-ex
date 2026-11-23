package com.study.springboot.dto;

public record EmailSendResponse(boolean success, String message, String devCode, long remainingSeconds) {

    public EmailSendResponse(boolean success, String message, String devCode) {
        this(success, message, devCode, 0L);
    }
}
