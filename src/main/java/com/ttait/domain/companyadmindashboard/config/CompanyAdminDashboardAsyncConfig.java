package com.ttait.domain.companyadmindashboard.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CompanyAdminDashboardAsyncConfig {

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
