package com.study.springboot.dto;

import java.io.Serializable;

public record NaverUserProfile(String naverId, String email, String name, String profileImage) implements Serializable {

    public NaverUserProfile(String naverId, String email, String name) {
        this(naverId, email, name, null);
    }

    public String getNaverId() {
        return naverId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
