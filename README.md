# TtaIt Backend

서울시 따릉이 기업 협약 기반 ESG 실천 플랫폼용 Spring Boot 백엔드입니다.

## 기술 스택

- Java 17
- Spring Boot 3.3
- Spring Security + JWT
- Spring WebSocket (협약 신청, 승인 처리 실시간 알림)
- Spring AOP (접근 로그 용도)
- MyBatis
- Oracle Database
- Docker Compose

## 현재 포함된 기능

- 관리자 / 기업 관리자 권한 구분
- JWT 기반 로그인 인증
- 관리자 회원가입 (`/auth/signup/admin`)
- 기업 관리자 회원가입 (`/auth/signup/company-admin`)
- 내 정보 조회 (`/users/me`)
- 공통 응답 포맷 / 전역 예외 처리
- Oracle 스키마 초기화 + 관리자 시드

## 패키지 구조

```text
com.ttait
|- global          : 공통 설정, 예외, 응답, 시큐리티
|- domain.auth     : 로그인, 회원가입, 토큰 발급
|- domain.user     : 사용자 계정/권한/내 정보 조회
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

## 기본 API

- `POST /api/v1/auth/signup/admin`
- `POST /api/v1/auth/signup/company-admin`
- `POST /api/v1/auth/login`
- `GET  /api/v1/users/me`

## 기본 관리자 계정

`docker/oracle/init/02-seed.sh` 에 BCrypt 해시로 관리자 계정이 미리 적재됩니다.
로컬 개발용 기본 비밀번호는 개발자 온보딩 문서를 참고하세요.

> **배포 전 필수**: 운영 환경에서는 시드 파일의 BCrypt 해시를 새로 생성한 값으로 교체하거나, 시드를 비활성화하고 별도 절차로 초기 관리자 계정을 생성해야 합니다.

## HTTP 클라이언트 테스트

IntelliJ HTTP Client 파일이 `http/` 디렉터리에 포함되어 있습니다.

- `http/auth.http` — 인증 플로우 테스트 시나리오
- `http/http-client.env.json` — 공개 환경변수 (commit)
- `http/http-client.private.env.json.example` — 비밀 환경변수 템플릿 (복사해서 실제 값 채우기)

```bash
cp http/http-client.private.env.json.example http/http-client.private.env.json
```
