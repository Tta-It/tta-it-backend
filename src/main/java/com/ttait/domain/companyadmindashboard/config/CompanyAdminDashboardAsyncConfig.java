package com.ttait.domain.companyadmindashboard.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 협약 승인 이후 실행되는 기업 대시보드 시드 작업 전용 비동기 실행 설정입니다.
 * 대량 insert 작업이 공용 비동기 스레드를 과도하게 점유하지 않도록 별도 스레드 풀로 제한합니다.
 */
@Configuration
public class CompanyAdminDashboardAsyncConfig {

    /**
     * 임직원 및 임직원 이용내역 시드 생성에 사용할 전용 executor를 생성합니다.
     */
    @Bean(name = "companyDashboardSeedExecutor")
    public Executor companyDashboardSeedExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("company-dashboard-seed-");
        executor.initialize();
        return executor;
    }
}
