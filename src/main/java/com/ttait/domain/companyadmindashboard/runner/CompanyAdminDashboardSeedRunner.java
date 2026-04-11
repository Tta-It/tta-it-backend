package com.ttait.domain.companyadmindashboard.runner;

import com.ttait.domain.companyadmindashboard.config.CompanyAdminDashboardSeedProperties;
import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 개발 환경에서 기업 관리자 대시보드 샘플 데이터를 생성하는 러너.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyAdminDashboardSeedRunner implements ApplicationRunner {

    private final CompanyAdminDashboardSeedProperties properties;
    private final CompanyAdminDashboardSeedService companyAdminDashboardSeedService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        log.info("기업 관리자 대시보드용 샘플 데이터 생성을 시작합니다.");
        companyAdminDashboardSeedService.seedIfNeeded();
        log.info("기업 관리자 대시보드용 샘플 데이터 생성이 완료되었습니다.");
    }
}
