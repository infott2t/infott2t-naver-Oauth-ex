package com.study.springboot.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(unique = true, length = 120)
    private String naverId;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(length = 500)
    private String note;

    @Column(length = 500)
    private String profileImage;

    @Column
    private LocalDateTime termsAgreedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Member() {
    }

    public static Member createLocalUser(String email, String encodedPassword, String name, Role role) {
        return createLocalUser(email, encodedPassword, name, role, "회원 가입했음.");
    }

    public static Member createLocalUser(String email, String encodedPassword, String name, Role role, String note) {
        Member member = new Member();
        member.email = normalizeEmail(email);
        member.password = encodedPassword;
        member.name = name;
        member.role = role != null ? role : Role.USER;
        member.emailVerified = true;
        member.note = (note != null && !note.isBlank()) ? note.trim() : "회원 가입했음.";
        return member;
    }

    public static Member createNaverUser(String email, String name, String naverId) {
        return createNaverUser(email, name, naverId, "회원 가입했음.");
    }

    public static Member createNaverUser(String email, String name, String naverId, String note) {
        return createNaverUser(email, name, naverId, note, null);
    }

    public static Member createNaverUser(String email, String name, String naverId, String note, String profileImage) {
        Member member = new Member();
        member.email = normalizeEmail(email);
        member.password = null;
        member.name = (name != null && !name.isBlank()) ? name : email.split("@")[0];
        member.role = Role.USER;
        member.naverId = naverId;
        member.emailVerified = true;
        member.note = (note != null && !note.isBlank()) ? note.trim() : "회원 가입했음.";
        member.profileImage = (profileImage != null && !profileImage.isBlank()) ? profileImage.trim() : null;
        member.termsAgreedAt = LocalDateTime.now();
        return member;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasPassword() {
        return this.password != null && !this.password.isBlank();
    }

    public boolean isNaverLinked() {
        return this.naverId != null && !this.naverId.isBlank();
    }

    public void assignPassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void linkNaverId(String naverId) {
        this.naverId = naverId;
        this.emailVerified = true;
    }

    public void updateNameIfBlank(String newName) {
        if ((this.name == null || this.name.isBlank()) && newName != null && !newName.isBlank()) {
            this.name = newName;
        }
    }

    public void updateNote(String note) {
        this.note = (note != null) ? note.trim() : "";
    }

    public void updateProfileImageIfPresent(String profileImage) {
        if (profileImage != null && !profileImage.isBlank()) {
            this.profileImage = profileImage.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String getNaverId() {
        return naverId;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getNote() {
        return note;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public LocalDateTime getTermsAgreedAt() {
        return termsAgreedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
