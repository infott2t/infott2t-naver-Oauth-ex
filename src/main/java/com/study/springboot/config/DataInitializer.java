package com.study.springboot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.study.springboot.domain.Member;
import com.study.springboot.domain.Role;
import com.study.springboot.repository.MemberRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String adminEmail = "admin@study.com";
        if (!memberRepository.existsByEmail(adminEmail)) {
            Member admin = Member.createLocalUser(
                    adminEmail,
                    passwordEncoder.encode("admin1234"),
                    "관리자",
                    Role.ADMIN,
                    "시스템 관리자 계정"
            );
            memberRepository.save(admin);
            log.info("[초기 관리자 계정 생성] email={}, role=ADMIN", adminEmail);
        }
    }
}
