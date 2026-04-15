# TtaIt Backend

서울시 따릉이 기업 협약 기반 ESG 실천 플랫폼용 Spring Boot 백엔드입니다.

## 데모 & 자료

| 자료 | 링크 |
|------|------|
| 시연 영상 (YouTube) | [https://www.youtube.com/watch?v=RWAEyA6tT4o](https://www.youtube.com/watch?v=RWAEyA6tT4o) |
| 발표 자료 (PPT) | [Google Drive](https://drive.google.com/drive/folders/1gHs9FTiL03fy9pMPki6E0Ime2h-rydtf?usp=sharing) |

## 팀 구성 및 역할 분담

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/heejeongJ">
        <img src="https://github.com/heejeongJ.png" width="120" alt="주희정"/><br/>
        <b>주희정</b><br/>
        <sub>@heejeongJ</sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/tmdals1207">
        <img src="https://github.com/tmdals1207.png" width="120" alt="홍승민"/><br/>
        <b>홍승민</b><br/>
        <sub>@tmdals1207</sub>
      </a>
    </td>
  </tr>
  <tr>
    <td valign="top">
      • 아키텍처 설계 (패키지 · 레이어 구조)<br/>
      • 공통 응답 · 예외 처리 표준화<br/>
      • 인증 / 회원 (JWT · 로그인 · 로그아웃 · 탈퇴)<br/>
      • 협약 신청 · 심사 (제출 · 재신청 · 승인 · 반려)<br/>
      • 파일 업로드 / 저장소<br/>
      • WebSocket + STOMP 실시간 알림<br/>
      • AOP 공통 로깅 · i18n 인프라<br/>
      • HTTP 테스트 시나리오 작성<br/>
    </td>
    <td valign="top">
      • 관리자 / 기업 대시보드 API<br/>
      • 통계 집계 쿼리 · DTO 매핑<br/>
      • 임직원 이용 통계 스키마<br/>
      • CSV 대여소 자동 적재<br/>
      • 협약 승인 후 시드 데이터 생성<br/>
      • 인덱스 · projection · 트랜잭션 최적화
    </td>
  </tr>
</table>

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.3.5 |
| Security | Spring Security + JWT (JJWT) | JJWT 0.12.6 |
| ORM | MyBatis | 3.0.3 |
| Database | Oracle Database (ojdbc11) | - |
| Infra | Docker Compose | - |
| Real-time | Spring WebSocket + STOMP | - |
| AOP | Spring AOP | - |
| File | Apache POI (Excel), Commons CSV | POI 5.2.5, CSV 1.10.0 |
| Util | Lombok | - |
| Build | Gradle | - |

## 주요 기능

### 인증 / 사용자
- JWT 기반 로그인 인증 (Access Token 4시간)
- 관리자(ADMIN) / 기업 관리자(COMPANY_ADMIN) 역할 구분
- 회원가입, 로그인, 로그아웃, 회원 탈퇴
- 내 정보 조회 (소속 기업 정보 포함)

### 협약 관리
- 기업 관리자: 협약 신청 제출 (파일 첨부), 조회, 반려 후 재신청
- 관리자: 신청 목록/상세 조회, 승인/반려 처리, 첨부파일 다운로드
- 협약 상태 흐름: `DRAFT → PENDING → ACTIVE / REJECTED`

### 대시보드
- 관리자 대시보드: 전체 통계, 대기 신청, 이용 추이, 지역별 분석
- 기업 관리자 대시보드: 임직원 이용 현황, 리워드 기준, 개별 상세 조회

### 실시간 알림
- WebSocket + STOMP 기반 실시간 알림
- 협약 신청 / 승인 / 반려 이벤트 발행 및 WebSocket 푸시

### 데이터 적재
- CSV 기반 대여소 마스터 데이터, 이용 통계 자동 적재 (CommandLineRunner, feature flag)
- 임직원 이용 통계 시드 데이터 생성

### 공통 / 인프라
- AOP 기반 Controller/Service 공통 로깅
- 공통 응답 포맷 (`ApiResponse<T>`) / 전역 예외 처리 (`ErrorCode`)
- 로컬 파일 저장소 (UUID 기반 파일명, 메타데이터 관리)
- Oracle 스키마 초기화 + 관리자 시드

## 패키지 구조

```text
com.ttait
├── global
│   ├── common         : BaseEntity (타임스탬프 추적)
│   ├── config         : MyBatis, Web, MessageSource 설정
│   ├── exception      : BusinessException, ErrorCode, GlobalExceptionHandler
│   ├── file           : FileStorageService (로컬 파일 저장)
│   ├── logging        : AOP 기반 Controller/Service 로깅 Aspect
│   ├── response       : ApiResponse<T> (공통 응답 래퍼)
│   ├── security       : JWT 필터, 인증, UserDetailsService
│   └── websocket      : WebSocket 설정, STOMP 인증 인터셉터
│
├── domain
│   ├── auth           : 로그인, 회원가입, 로그아웃, 탈퇴, 토큰 발급
│   ├── user           : 사용자 계정, 역할, 내 정보 조회
│   ├── organization   : 기업 엔티티 관리
│   ├── application    : 협약 신청 제출/심사, 첨부파일
│   ├── employee       : 기업 임직원 관리
│   ├── employeeusage  : 임직원 일별 이용 통계
│   ├── station        : 따릉이 대여소 마스터 데이터
│   ├── admindashboard : 관리자 대시보드 (전체 통계/분석)
│   ├── companyadmindashboard : 기업 관리자 대시보드 (임직원 현황)
│   ├── notification   : 실시간 알림 (이벤트, 리스너, WebSocket 발행)
│   └── dataimport     : CSV 데이터 적재 (대여소, 이용 통계)
```

## 실행 전 준비

### 1. 환경변수 설정

`.env.example`을 복사해서 `.env` 파일을 만들고 값을 채워넣으세요.

```bash
cp .env.example .env
```

`.env`는 gitignored 되어 있어 커밋되지 않습니다. 필요한 값은 `.env.example`의 주석을 참고하세요.

### 2. Oracle DB 기동 (docker compose)

```bash
docker compose up -d
```

- 초기 스키마는 [`docker/oracle/init/01-schema.sh`](docker/oracle/init/01-schema.sh) 에서 생성됩니다.
- 관리자 시드는 [`docker/oracle/init/02-seed.sh`](docker/oracle/init/02-seed.sh) 에서 적재됩니다.
- 자격증명은 docker-compose 가 `.env` 에서 자동으로 읽어옵니다.
- 윈도우 환경에서는 init shell script 줄바꿈이 `CRLF` 로 바뀌면 초기화가 실패할 수 있습니다. 이 저장소는 `.gitattributes` 로 `*.sh` 를 `LF` 로 고정합니다.
- Oracle 초기화 스크립트는 DB 볼륨이 비어 있을 때 한 번만 실행됩니다. 첫 기동에 실패했다면 `docker compose down -v` 후 다시 `docker compose up -d` 로 올려야 테이블 생성이 재시도됩니다.
- 테이블이 생기지 않았다면 `docker logs ttait-oracle` 로 `/bin/bash^M`, `sqlplus` 로그인 실패, init script 에러를 먼저 확인하세요.

### Windows 사용자 체크리스트

1. 최신 코드를 pull 받은 뒤 `.env.example` 을 `.env` 로 복사합니다.

   ```powershell
   Copy-Item .env.example .env
   ```

2. `.env` 에 값을 채웁니다.

3. 이전에 DB를 한 번이라도 띄웠거나, 테이블 생성에 실패한 적이 있으면 볼륨까지 삭제 후 다시 실행합니다.

   ```powershell
   docker compose down -v
   docker compose up -d
   ```

5. 테이블이 생성되지 않으면 아래 로그를 먼저 확인합니다.

   ```powershell
   docker logs ttait-oracle
   ```

6. 로그에 `/bin/bash^M` 가 보이면 줄바꿈 문제일 가능성이 큽니다. 최신 코드를 다시 받고, 필요하면 저장소를 새로 clone 한 뒤 다시 시도합니다.

7. DB가 정상 기동된 뒤 애플리케이션을 실행합니다.

   ```powershell
   .\gradlew.bat bootRun
   ```

### 3. 애플리케이션 기동

```bash
./gradlew bootRun
```

Spring Boot 는 `spring.config.import` 설정을 통해 프로젝트 루트의 `.env` 파일을 자동으로 읽어옵니다.

IntelliJ 에서 실행할 경우: Run Configuration → Environment variables → EnvFile 플러그인 또는 직접 env var 로드를 사용하세요.

## API 목록

### 인증 (`/api/v1/auth`)

| Method | Endpoint | 권한 | 설명 |
|--------|----------|------|------|
| POST | `/signup/admin` | 공개 | 관리자 회원가입 |
| POST | `/signup/company-admin` | 공개 | 기업 관리자 회원가입 + 기업 등록 |
| POST | `/login` | 공개 | 로그인 (JWT 발급) |
| POST | `/logout` | 인증 | 로그아웃 |
| POST | `/withdraw` | 인증 | 회원 탈퇴 (비밀번호 확인) |

### 사용자 (`/api/v1/users`)

| Method | Endpoint | 권한 | 설명 |
|--------|----------|------|------|
| GET | `/me` | 인증 | 내 정보 + 소속 기업 조회 |

### 기업 관리자 - 협약 신청 (`/api/v1/company-admin/applications`)

| Method | Endpoint | 권한 | 설명 |
|--------|----------|------|------|
| POST | `/` | COMPANY_ADMIN | 협약 신청 제출 (파일 첨부) |
| POST | `/resubmit` | COMPANY_ADMIN | 반려 후 재신청 |
| GET | `/me` | COMPANY_ADMIN | 내 신청 현황 조회 |

### 관리자 - 협약 심사 (`/api/v1/admin/applications`)

| Method | Endpoint | 권한 | 설명 |
|--------|----------|------|------|
| GET | `/` | ADMIN | 신청 목록 (검색, 상태/날짜 필터, 페이징) |
| GET | `/{organizationId}` | ADMIN | 신청 상세 + 첨부파일 |
| GET | `/{organizationId}/files/{fileId}/download` | ADMIN | 첨부파일 다운로드 |
| POST | `/{organizationId}/approve` | ADMIN | 협약 승인 |
| POST | `/{organizationId}/reject` | ADMIN | 협약 반려 (사유 입력) |

### 관리자 대시보드 (`/api/v1/admin/dashboard`)

| Method | Endpoint | 권한 | 설명 |
|--------|----------|------|------|
| GET | `/` | ADMIN | 전체 통계, 대기 신청, 이용 추이, 지역 분석 |

### 기업 관리자 대시보드 (`/api/v1/company-admin/dashboard`)

| Method | Endpoint | 권한 | 설명 |
|--------|----------|------|------|
| GET | `/` | COMPANY_ADMIN | 기업 통계, 임직원 이용 현황, 리워드 기준 |
| GET | `/employees/{employeeId}` | COMPANY_ADMIN | 임직원 개별 이용 상세 |

## 기본 관리자 계정

`docker/oracle/init/02-seed.sh` 에 BCrypt 해시로 관리자 계정이 미리 적재됩니다.
로컬 개발용 기본 비밀번호는 개발자 온보딩 문서를 참고하세요.

> **배포 전 필수**: 운영 환경에서는 시드 파일의 BCrypt 해시를 새로 생성한 값으로 교체하거나, 시드를 비활성화하고 별도 절차로 초기 관리자 계정을 생성해야 합니다.

## HTTP 클라이언트 테스트

IntelliJ HTTP Client 파일이 `http/` 디렉터리에 포함되어 있습니다.

| 파일 | 설명 |
|------|------|
| `auth.http` | 회원가입, 로그인 시나리오 |
| `logout.http` | 로그아웃 테스트 |
| `withdraw.http` | 회원 탈퇴 테스트 |
| `company-admin-application.http` | 기업 협약 신청 제출/조회 |
| `company-admin-resubmit.http` | 반려 후 재신청 |
| `admin-application.http` | 관리자 협약 심사 (목록/상세/승인/반려/파일) |
| `admin-dashboard.http` | 관리자 대시보드 조회 |
| `company-admin-dashboard.http` | 기업 관리자 대시보드 조회 |
| `ws-test.html` | WebSocket 알림 브라우저 테스트 |

```bash
cp http/http-client.private.env.json.example http/http-client.private.env.json
```
