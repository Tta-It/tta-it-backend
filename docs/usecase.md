# 유스케이스 다이어그램

## 전체 시스템

```mermaid
flowchart LR
    subgraph Actors
        CA["👤 기업 관리자\n(COMPANY_ADMIN)"]
        AD["👤 관리자\n(ADMIN)"]
        SYS["⚙️ 시스템"]
    end

    subgraph AUTH["🔐 인증"]
        UC1(회원가입)
        UC2(로그인)
        UC3(로그아웃)
        UC4(회원 탈퇴)
        UC5(내 정보 조회)
    end

    subgraph APP_CA["📋 협약 신청"]
        UC6(협약 신청 제출)
        UC6_1(파일 첨부)
        UC7(신청 현황 조회)
        UC8(반려 후 재신청)
    end

    subgraph APP_AD["📑 협약 심사"]
        UC9(신청 목록 조회)
        UC9_1(검색 / 필터)
        UC10(신청 상세 조회)
        UC11(첨부파일 다운로드)
        UC12(협약 승인)
        UC13(협약 반려)
    end

    subgraph DASH["📊 대시보드"]
        UC14(관리자 대시보드 조회)
        UC15(기업 대시보드 조회)
        UC16(임직원 상세 조회)
    end

    subgraph NOTI["🔔 알림"]
        UC17(실시간 알림 발송)
    end

    %% 기업 관리자 연결
    CA --> UC1
    CA --> UC2
    CA --> UC3
    CA --> UC4
    CA --> UC5
    CA --> UC6
    CA --> UC7
    CA --> UC8
    CA --> UC15
    CA --> UC16

    %% 관리자 연결
    AD --> UC2
    AD --> UC3
    AD --> UC5
    AD --> UC9
    AD --> UC10
    AD --> UC11
    AD --> UC12
    AD --> UC13
    AD --> UC14

    %% include 관계
    UC6 -.->|include| UC6_1
    UC9 -.->|include| UC9_1

    %% 시스템 연결
    UC6 -.->|trigger| UC17
    UC12 -.->|trigger| UC17
    UC13 -.->|trigger| UC17
    SYS --> UC17

    %% 스타일
    style AUTH fill:#E8F5E9,stroke:#49D187,color:#333
    style APP_CA fill:#E3F2FD,stroke:#4BACD6,color:#333
    style APP_AD fill:#FFF3E0,stroke:#F4B183,color:#333
    style DASH fill:#F3E5F5,stroke:#A78BDB,color:#333
    style NOTI fill:#FFF8E1,stroke:#FFCA28,color:#333
```

## 액터 설명

| 액터 | 역할 | 주요 기능 |
|------|------|-----------|
| 기업 관리자 | 기업 대표로 협약 신청 및 임직원 관리 | 협약 신청/재신청, 기업 대시보드, 임직원 현황 |
| 관리자 | 시스템 전체 관리 | 협약 심사(승인/반려), 관리자 대시보드 |
| 시스템 | 자동 처리 | 실시간 알림 발송 (WebSocket) |

## 협약 상태 흐름

```mermaid
stateDiagram-v2
    [*] --> DRAFT: 기업 관리자 가입
    DRAFT --> PENDING: 협약 신청 제출
    PENDING --> ACTIVE: 관리자 승인
    PENDING --> REJECTED: 관리자 반려
    REJECTED --> PENDING: 재신청
    ACTIVE --> [*]
```
