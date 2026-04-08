# TtaIt ERD

TtaIt 백엔드 초기 기능 범위를 기준으로 정리한 ERD 초안입니다.

## Mermaid ERD

```mermaid
erDiagram
    T_ORGANIZATION ||--o{ T_USER : "has"
    T_ORGANIZATION ||--o{ T_EMPLOYEE : "owns"
    T_USER ||--o{ T_NOTIFICATION : "receives"
    T_USER ||--o{ T_APPLICATION_FILE : "uploads"
    T_USER ||--o| T_DASHBOARD : "views"
    T_USER o|--|| T_EMPLOYEE : "mapped_to"

    T_ORGANIZATION {
        long id PK
        string organization_name
        string business_number UK
        string industry_type
        int employee_count
        string address
        string contact_name
        string contact_email
        string contact_phone
        string agreement_status
        datetime approved_at
        datetime created_at
        datetime updated_at
    }

    T_USER {
        long id PK
        long organization_id FK
        string login_id UK
        string password
        string name
        string email UK
        string phone
        string role
        string status
        datetime last_login_at
        datetime created_at
        datetime updated_at
    }

    T_EMPLOYEE {
        long id PK
        long organization_id FK
        long user_id FK
        string employee_no
        string name
        string email UK
        string department
        string position
        string employment_status
        datetime created_at
        datetime updated_at
    }

    T_STATION_USAGE_STAT {
        long id PK
        date stat_date
        string region_name
        string station_name
        string station_code
        int ride_count
        int return_count
        int commute_usage_count
        datetime created_at
    }

    T_DASHBOARD {
        long id PK
        long user_id FK
        int total_ride_count
        int total_commute_count
        int carbon_reduction
        datetime last_updated_at
        datetime created_at
    }

    T_NOTIFICATION {
        long id PK
        long receiver_user_id FK
        string type
        string title
        string content
        long target_id
        boolean is_read
        datetime read_at
        datetime created_at
    }

    T_APPLICATION_FILE {
        long id PK
        long user_id FK
        string original_file_name
        string stored_file_name
        string file_path
        long file_size
        string content_type
        datetime created_at
    }
```

## 메모

- `T_USER`는 관리자, 기업 관리자, 기업 사용자를 모두 담는 공통 계정 테이블입니다.
- `T_ORGANIZATION`은 협약 기업 마스터입니다. 기업 관리자가 회원가입 시점에는 `organization_id = NULL`, 협약 신청이 승인되면 연결됩니다.
- `T_STATION_USAGE_STAT`은 CSV 적재 기반 통계 데이터 저장용이며, 대시보드 차트/집계에 사용됩니다.
- `T_DASHBOARD`는 사용자 개인(또는 기업) 대시보드 요약 지표를 저장합니다. (누적 이용량, 탄소 절감량 등)
- `T_APPLICATION_FILE`은 기업 협약 신청 첨부 파일을 `user_id` 기준으로 저장합니다.
- 협약 신청(`t_partnership_application`) / 접근 로그(`t_access_log`) 테이블은 요구사항 구현 단계에서 추가 예정입니다.
