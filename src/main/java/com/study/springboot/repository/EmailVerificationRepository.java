package com.study.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.springboot.domain.EmailVerification;
import com.study.springboot.domain.VerificationPurpose;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailAndPurposeAndConsumedFalseOrderByCreatedAtDesc(
            String email, VerificationPurpose purpose);
}
