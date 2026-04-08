#!/bin/bash
set -euo pipefail

sqlplus -S "${APP_USER}/${APP_USER_PASSWORD}@//localhost:1521/FREEPDB1" <<'EOSQL'
WHENEVER SQLERROR EXIT SQL.SQLCODE

INSERT INTO t_user (
    id,
    login_id,
    password,
    name,
    email,
    role,
    status,
    created_at,
    updated_at
) VALUES (
    user_seq.NEXTVAL,
    'admin',
    '$2y$10$m6YAFnmLDzO3BM6vBpQWXOWS9PzNFrKt1N3WUSc0eHkX1PlE5Nrp.',
    '시스템 관리자',
    'admin@ttait.com',
    'ADMIN',
    'ACTIVE',
    SYSTIMESTAMP,
    SYSTIMESTAMP
);
COMMIT;

EXIT;
EOSQL
