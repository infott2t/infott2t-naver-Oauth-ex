package com.study.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.springboot.domain.Member;
import com.study.springboot.domain.Role;
import com.study.springboot.domain.VerificationPurpose;
import com.study.springboot.dto.LoginResult;
import com.study.springboot.dto.NaverUserProfile;
import com.study.springboot.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private static final int MIN_PASSWORD_LENGTH = 4;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional(readOnly = true)
    public LoginResult loginWithEmail(String rawEmail, String rawPassword) {
        String email = Member.normalizeEmail(rawEmail);
        Optional<Member> memberOpt = memberRepository.findByEmail(email);

        if (memberOpt.isEmpty()) {
            return LoginResult.userNotFound();
        }

        Member member = memberOpt.get();
        if (!member.hasPassword()) {
            return LoginResult.passwordNotSet(member);
        }

        if (rawPassword == null || rawPassword.isBlank()
                || !passwordEncoder.matches(rawPassword, member.getPassword())) {
            return LoginResult.invalidPassword();
        }

        return LoginResult.success(member);
    }

    public Member registerUser(String rawEmail, String name, String password, String passwordConfirm) {
        return registerUser(rawEmail, name, password, passwordConfirm, "회원 가입했음.");
    }

    public Member registerUser(String rawEmail, String name, String password, String passwordConfirm, String note) {
        String email = Member.normalizeEmail(rawEmail);

        if (!emailVerificationService.isValidEmailFormat(email)) {
            throw new IllegalArgumentException("올바른 이메일 주소를 입력해 주세요.");
        }

        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다. 로그인 페이지에서 로그인해 주세요.");
        }

        if (!emailVerificationService.isVerified(email, VerificationPurpose.SIGNUP)) {
            throw new IllegalArgumentException("이메일 6자리 인증을 먼저 완료해 주세요.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해 주세요.");
        }

        validateNewPassword(password, passwordConfirm);

        Member member = Member.createLocalUser(
                email,
                passwordEncoder.encode(password),
                name.trim(),
                Role.USER,
                note
        );

        Member saved = memberRepository.save(member);
        emailVerificationService.consumeVerification(email, VerificationPurpose.SIGNUP);
        return saved;
    }

    public Member updateNote(Long memberId, String note) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        member.updateNote(note);
        return memberRepository.save(member);
    }

    public Member setupInitialPassword(String rawEmail, String password, String passwordConfirm) {
        String email = Member.normalizeEmail(rawEmail);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원을 찾을 수 없습니다."));

        if (member.hasPassword()) {
            throw new IllegalArgumentException("이미 암호가 설정된 계정입니다. 로그인 페이지에서 로그인해 주세요.");
        }

        if (!emailVerificationService.isVerified(email, VerificationPurpose.PASSWORD_SETUP)) {
            throw new IllegalArgumentException("이메일 6자리 인증을 먼저 완료해 주세요.");
        }

        validateNewPassword(password, passwordConfirm);

        member.assignPassword(passwordEncoder.encode(password));
        Member saved = memberRepository.save(member);
        emailVerificationService.consumeVerification(email, VerificationPurpose.PASSWORD_SETUP);
        return saved;
    }

    public Optional<Member> findAndLinkExistingNaverMember(NaverUserProfile profile) {
        if (profile == null || profile.email() == null || profile.email().isBlank()) {
            throw new IllegalArgumentException("네이버 계정에서 이메일 정보를 가져오지 못했습니다.");
        }

        String normalizedEmail = Member.normalizeEmail(profile.email());

        Optional<Member> byNaverId = (profile.naverId() != null && !profile.naverId().isBlank())
                ? memberRepository.findByNaverId(profile.naverId())
                : Optional.empty();

        if (byNaverId.isPresent()) {
            Member member = byNaverId.get();
            member.updateNameIfBlank(profile.name());
            member.updateProfileImageIfPresent(profile.profileImage());
            return Optional.of(memberRepository.save(member));
        }

        Optional<Member> byEmail = memberRepository.findByEmail(normalizedEmail);
        if (byEmail.isPresent()) {
            Member member = byEmail.get();
            boolean shouldUpdateNaverId = !member.isNaverLinked()
                    || (member.getNaverId().startsWith("mock-naver-")
                    && profile.naverId() != null
                    && !profile.naverId().startsWith("mock-naver-"));
            if (shouldUpdateNaverId && profile.naverId() != null && !profile.naverId().isBlank()) {
                member.linkNaverId(profile.naverId());
            }
            member.updateNameIfBlank(profile.name());
            member.updateProfileImageIfPresent(profile.profileImage());
            return Optional.of(memberRepository.save(member));
        }

        return Optional.empty();
    }

    public Member registerNaverUserWithTerms(NaverUserProfile profile, String note) {
        Optional<Member> existing = findAndLinkExistingNaverMember(profile);
        if (existing.isPresent()) {
            return existing.get();
        }
        String normalizedEmail = Member.normalizeEmail(profile.email());
        Member newMember = Member.createNaverUser(
                normalizedEmail,
                profile.name(),
                profile.naverId(),
                note,
                profile.profileImage()
        );
        return memberRepository.save(newMember);
    }

    public Member loginOrRegisterWithNaver(NaverUserProfile profile) {
        return findAndLinkExistingNaverMember(profile)
                .orElseGet(() -> registerNaverUserWithTerms(profile, "회원 가입했음."));
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String rawEmail) {
        return memberRepository.findByEmail(Member.normalizeEmail(rawEmail));
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Member> findAllMembers() {
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    private void validateNewPassword(String password, String passwordConfirm) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("암호는 최소 " + MIN_PASSWORD_LENGTH + "자 이상 입력해 주세요.");
        }
        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("입력한 두 암호가 일치하지 않습니다. 다시 확인해 주세요.");
        }
    }
}
