package com.study.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.study.springboot.domain.Member;
import com.study.springboot.domain.Role;
import com.study.springboot.dto.SessionUser;
import com.study.springboot.repository.EmailVerificationRepository;
import com.study.springboot.repository.MemberRepository;

@SpringBootTest
class AuthFlowIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerificationRepository verificationRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        verificationRepository.deleteAll();
        memberRepository.findAll().stream()
                .filter(m -> m.getRole() != Role.ADMIN)
                .forEach(memberRepository::delete);
    }

    @Test
    void existingEndpointsRemainIntact() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Jsp in Gradle"));

        mockMvc.perform(get("/test1"))
                .andExpect(status().isOk())
                .andExpect(view().name("test1"));

        mockMvc.perform(get("/test2"))
                .andExpect(status().isOk())
                .andExpect(view().name("sub/test2"));
    }

    @Test
    void naverQuickLoginAndThenPasswordSetupAndDualLoginFlow() throws Exception {
        String naverEmail = "naver-only@naver.com";

        // 1. 미가입 사용자가 네이버 간편로그인 시도 -> 약관 동의 페이지(/oauth/naver/terms)로 이동
        MockHttpSession naverSession1 = new MockHttpSession();
        mockMvc.perform(post("/oauth/naver/mock")
                        .session(naverSession1)
                        .param("email", naverEmail)
                        .param("name", "네이버회원"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth/naver/terms"));

        assertThat(memberRepository.findByEmail(naverEmail)).isEmpty();

        // 1-2. 약관 페이지 조회 확인
        mockMvc.perform(get("/oauth/naver/terms").session(naverSession1))
                .andExpect(status().isOk())
                .andExpect(view().name("naver-terms"))
                .andExpect(model().attributeExists("naverProfile"));

        // 1-3. 필수 약관 미동의 시 가입 차단
        mockMvc.perform(post("/oauth/naver/terms")
                        .session(naverSession1)
                        .param("agreeService", "true")
                        .param("agreePrivacy", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("naver-terms"))
                .andExpect(model().attributeExists("errorMessage"));

        assertThat(memberRepository.findByEmail(naverEmail)).isEmpty();

        // 1-4. 필수 약관 모두 동의 시 가입 완료 (암호 없음, USER) -> /welcome 이동
        mockMvc.perform(post("/oauth/naver/terms")
                        .session(naverSession1)
                        .param("agreeService", "true")
                        .param("agreePrivacy", "true")
                        .param("note", "회원 가입했음."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        Member saved = memberRepository.findByEmail(naverEmail).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.hasPassword()).isFalse();
        assertThat(saved.isNaverLinked()).isTrue();
        assertThat(saved.getTermsAgreedAt()).isNotNull();
        assertThat(saved.getNote()).isEqualTo("회원 가입했음.");

        // 2. 암호 없는 회원이 다시 네이버 간편로그인 버튼으로 로그인 -> 암호 입력 없이 바로 welcome
        MockHttpSession naverSession2 = new MockHttpSession();
        mockMvc.perform(post("/oauth/naver/mock")
                        .session(naverSession2)
                        .param("email", naverEmail)
                        .param("name", "네이버회원"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        mockMvc.perform(get("/welcome").session(naverSession2))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"));

        // 3. 암호 없는 회원이 이메일 로그인 페이지에서 로그인 시도 -> /password/setup으로 이동
        MockHttpSession emailSession = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(emailSession)
                        .param("email", naverEmail)
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/password/setup"));

        // 4. 최초 암호 설정 전 6자리 이메일 인증 발송 및 검증
        MvcResult sendResult = mockMvc.perform(post("/api/email/send-code")
                        .session(emailSession)
                        .param("email", naverEmail)
                        .param("purpose", "PASSWORD_SETUP"))
                .andExpect(status().isOk())
                .andReturn();

        String devCode = extractDevCode(sendResult.getResponse().getContentAsString());
        assertThat(devCode).matches("\\d{6}");

        mockMvc.perform(post("/api/email/verify-code")
                        .session(emailSession)
                        .param("email", naverEmail)
                        .param("code", devCode)
                        .param("purpose", "PASSWORD_SETUP"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":true")));

        // 5. 두 암호가 불일치하면 암호 생성 거부
        mockMvc.perform(post("/password/setup")
                        .session(emailSession)
                        .param("password", "secret1234")
                        .param("passwordConfirm", "different99"))
                .andExpect(status().isOk())
                .andExpect(view().name("password-setup"))
                .andExpect(model().attributeExists("errorMessage"));

        // 6. 두 암호가 일치하면 암호 생성 후 로그인되어 /welcome 이동
        mockMvc.perform(post("/password/setup")
                        .session(emailSession)
                        .param("password", "secret1234")
                        .param("passwordConfirm", "secret1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        Member updated = memberRepository.findByEmail(naverEmail).orElseThrow();
        assertThat(updated.hasPassword()).isTrue();
        assertThat(emailSession.getAttribute(SessionUser.SESSION_KEY)).isNotNull();

        // 7. 암호 생성 후: 이메일+암호 로그인 성공 & 참고 수정 후 저장
        MockHttpSession emailLoginAfter = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(emailLoginAfter)
                        .param("email", naverEmail)
                        .param("password", "secret1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        mockMvc.perform(post("/welcome/note")
                        .session(emailLoginAfter)
                        .param("note", "회원 가입했음."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        SessionUser emailSessionUser = (SessionUser) emailLoginAfter.getAttribute(SessionUser.SESSION_KEY);
        assertThat(emailSessionUser.getNote()).isEqualTo("회원 가입했음.");

        // 8. 암호 생성 후에도 네이버 간편로그인 역시 성공하며, 완전히 동일한 회원(ID/참고)으로 조회됨
        MockHttpSession naverLoginAfter = new MockHttpSession();
        mockMvc.perform(post("/oauth/naver/mock")
                        .session(naverLoginAfter)
                        .param("email", naverEmail)
                        .param("name", "네이버회원"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        SessionUser naverSessionUser = (SessionUser) naverLoginAfter.getAttribute(SessionUser.SESSION_KEY);
        assertThat(naverSessionUser.getId()).isEqualTo(emailSessionUser.getId());
        assertThat(naverSessionUser.getNote()).isEqualTo("회원 가입했음.");
    }

    @Test
    void nonNaverUserSignupWith6DigitEmailVerificationAndRoleCheck() throws Exception {
        String gmail = "newuser@gmail.com";

        // 1. 미가입 사용자 로그인 시도 -> 회원가입 안내
        mockMvc.perform(post("/login")
                        .param("email", gmail)
                        .param("password", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("showSignupHint", true));

        // 2. 이메일 인증 없이 가입 시도 -> 실패
        mockMvc.perform(post("/signup")
                        .param("email", gmail)
                        .param("name", "홍길동")
                        .param("password", "pass1234")
                        .param("passwordConfirm", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("errorMessage"));

        // 3. 6자리 인증번호 발송
        MvcResult sendResult = mockMvc.perform(post("/api/email/send-code")
                        .param("email", gmail)
                        .param("purpose", "SIGNUP"))
                .andExpect(status().isOk())
                .andReturn();

        String devCode = extractDevCode(sendResult.getResponse().getContentAsString());
        assertThat(devCode).matches("\\d{6}");

        // 4. 잘못된 6자리 번호 검증 실패
        String wrongCode = devCode.equals("000000") ? "111111" : "000000";
        mockMvc.perform(post("/api/email/verify-code")
                        .param("email", gmail)
                        .param("code", wrongCode)
                        .param("purpose", "SIGNUP"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")));

        // 5. 올바른 6자리 번호 검증 성공
        mockMvc.perform(post("/api/email/verify-code")
                        .param("email", gmail)
                        .param("code", devCode)
                        .param("purpose", "SIGNUP"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":true")));

        // 6. 회원가입 완료 -> 기본 권한 USER
        mockMvc.perform(post("/signup")
                        .param("email", gmail)
                        .param("name", "홍길동")
                        .param("password", "pass1234")
                        .param("passwordConfirm", "pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Member user = memberRepository.findByEmail(gmail).orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEmailVerified()).isTrue();

        // 7. 일반 사용자 로그인 -> /welcome 성공, /admin/members 는 403 차단
        MockHttpSession userSession = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(userSession)
                        .param("email", gmail)
                        .param("password", "pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        mockMvc.perform(get("/admin/members").session(userSession))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/WEB-INF/views/access-denied.jsp"));

        // 8. ADMIN 로그인 -> /admin/members 접근 허용
        MockHttpSession adminSession = new MockHttpSession();
        mockMvc.perform(post("/login")
                        .session(adminSession)
                        .param("email", "admin@study.com")
                        .param("password", "admin1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        mockMvc.perform(get("/admin/members").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-members"));
    }

    @Test
    void unauthenticatedWelcomeRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void resendVerificationCodeBlockedFor5Minutes() throws Exception {
        String email = "cooldown@gmail.com";

        // 1차 발송 성공 -> remainingSeconds=300
        mockMvc.perform(post("/api/email/send-code")
                        .param("email", email)
                        .param("purpose", "SIGNUP"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":true")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"remainingSeconds\":300")));

        // 5분 이내 즉시 재발송 시도 -> 차단(success=false) 및 5분 뒤 재발송 가능 안내
        MvcResult resendResult = mockMvc.perform(post("/api/email/send-code")
                        .param("email", email)
                        .param("purpose", "SIGNUP"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"success\":false")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("5분 뒤에 다시 발송 가능")))
                .andReturn();

        String body = resendResult.getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"remainingSeconds\"\\s*:\\s*(\\d+)").matcher(body);
        assertThat(m.find()).isTrue();
        assertThat(Long.parseLong(m.group(1))).isGreaterThan(0L).isLessThanOrEqualTo(300L);
    }

    private String extractDevCode(String json) {
        Matcher matcher = Pattern.compile("\"devCode\"\\s*:\\s*\"(\\d{6})\"").matcher(json);
        assertThat(matcher.find()).as("devCode should exist in JSON: " + json).isTrue();
        return matcher.group(1);
    }
}
