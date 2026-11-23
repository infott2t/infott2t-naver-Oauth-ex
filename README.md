# infott2t-naver-Oauth-ex

Spring Boot + JSP 기반의 **네이버 간편로그인(OAuth 2.0) & 이메일 6자리 인증 로그인/회원가입** 예제 프로젝트입니다.

> ✨ **Vibe Coding with Antigravity**
> 이 프로젝트는 **Google Antigravity(안티그래비티)**를 활용한 **바이브코딩(Vibe Coding)**으로 설계·구현·테스트되었습니다.

---

## 주요 기능

- **네이버 간편로그인 (OAuth 2.0)**
  - 실제 네이버 OAuth 2.0 연동(`/oauth/naver/login` → `/oauth/naver/callback`) 및 키 없이 테스트할 수 있는 로컬 Mock 로그인 지원
  - 최초 네이버 로그인 시 필수 약관 동의(`/oauth/naver/terms`) 후 `USER` 권한으로 회원 가입
- **이메일 6자리 인증번호 발송 (SMTP)**
  - 네이버 메일 SMTP(SSL 465) 및 Gmail SMTP(STARTTLS 587) 지원
  - 5분 유효시간 및 재발송 쿨다운 체크, 개발용 Mock 모드(`app.mail.mock-enabled=true`) 지원
- **통합 계정 및 비밀번호 설정**
  - 일반 이메일 회원가입(`/signup`) 및 로그인(`/login`)
  - 네이버 간편로그인으로 가입한 계정이 일반 이메일 로그인을 시도할 경우, 이메일 6자리 인증 후 최초 비밀번호를 설정(`/password/setup`)하여 두 방식 모두 동일 계정으로 로그인 가능
- **권한 관리 및 회원 조회**
  - `USER` / `ADMIN` 권한 분리 (`AuthInterceptor`)
  - 관리자 전용 회원 목록 페이지(`/admin/members`)
- **H2 파일 데이터베이스**
  - 별도 DB 설치 없이 로컬 파일(`./data/springbootdb`)로 데이터 영속성 유지 및 `/h2-console` 지원

---

## 기술 스택

- **Java 17**
- **Spring Boot 4.2.0** (Spring Web MVC, Spring Data JPA, Spring Mail, Spring Security Crypto)
- **View**: JSP / JSTL (Embedded Tomcat Jasper)
- **Database**: H2 Database
- **Build**: Gradle

---

## 시작하기 (실행 전 필수 설정)

보안을 위해 실제 SMTP 비밀번호와 네이버 OAuth 키가 담긴 `src/main/resources/application.properties` 파일은 `.gitignore`로 제외되어 있습니다. 프로젝트를 실행하기 전에 아래 순서대로 설정 파일을 준비해 주세요.

### 1. 설정 파일 복사

`src/main/resources/application.properties.example` 파일을 같은 위치에 `application.properties` 이름으로 복사합니다.

```bash
# macOS / Linux
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Windows (PowerShell)
Copy-Item src\main\resources\application.properties.example src\main\resources\application.properties
```

### 2. 민감 정보 입력

복사한 `src/main/resources/application.properties` 파일을 열고, 아래 항목에 본인의 실제 값을 입력합니다.

| 설정 키 | 설명 | 발급 / 확인 위치 |
| :--- | :--- | :--- |
| `spring.mail.username` | 발신용 네이버 이메일 주소 | 본인 네이버 계정 (`example@naver.com`) |
| `spring.mail.password` | 네이버 비밀번호 또는 앱 비밀번호 | 네이버 메일 `POP3/IMAP` 허용 + 2단계 인증 시 `보안설정 > 애플리케이션 비밀번호` 발급 |
| `app.mail.from` | 발신자 이메일 주소 | 일반적으로 `spring.mail.username`과 동일 |
| `app.naver.client-id` | 네이버 OAuth Client ID | [네이버 개발자센터](https://developers.naver.com/) > Application > 애플리케이션 등록 |
| `app.naver.client-secret` | 네이버 OAuth Client Secret | [네이버 개발자센터](https://developers.naver.com/) > 내 애플리케이션 > 개요 |

> **네이버 개발자센터 애플리케이션 설정 참고**
> - **사용 API**: 네이버 로그인 (회원이름, 이메일 주소 등 필요 항목 선택)
> - **서비스 URL**: `http://localhost:8080`
> - **Callback URL**: `http://localhost:8080/oauth/naver/callback`

> **키/SMTP 설정 없이 빠르게 테스트하고 싶다면?**
> `application.properties`에서 아래 두 옵션을 `true`로 변경하면 외부 API·메일 연동 없이 Mock 모드로 전체 흐름을 체험할 수 있습니다.
> ```properties
> app.mail.mock-enabled=true
> app.naver.mock-enabled=true
> ```

---

## 실행 및 테스트 방법

### 애플리케이션 실행

```bash
# Windows (PowerShell / CMD)
.\gradlew.bat bootRun

# macOS / Linux
./gradlew bootRun
```

실행 후 브라우저에서 `http://localhost:8080/login` 에 접속합니다.

### 기본 테스트 관리자 계정

애플리케이션 최초 기동 시 `DataInitializer`가 아래 테스트용 관리자 계정을 자동 생성합니다.
- **이메일**: `admin@study.com`
- **비밀번호**: `admin1234`
- **권한**: `ADMIN` (`/admin/members` 접근 가능)

### 테스트 실행

```bash
# Windows
.\gradlew.bat test

# macOS / Linux
./gradlew test
```

---

## 프로젝트 주요 경로

- `/login` : 로그인 (일반 이메일 로그인 + 네이버 간편로그인)
- `/signup` : 이메일 6자리 인증 기반 회원가입
- `/oauth/naver/login` : 네이버 OAuth 로그인 시작
- `/oauth/naver/callback` : 네이버 OAuth 콜백 처리
- `/oauth/naver/terms` : 네이버 간편가입 최초 약관 동의
- `/password/setup` : 간편로그인 계정의 최초 비밀번호 설정
- `/welcome` : 로그인 후 마이페이지 (참고 정보 수정)
- `/admin/members` : 관리자 전용 전체 회원 조회
- `/h2-console` : H2 데이터베이스 웹 콘솔

---

## 라이선스

이 프로젝트는 [MIT License](LICENSE)를 따릅니다.
