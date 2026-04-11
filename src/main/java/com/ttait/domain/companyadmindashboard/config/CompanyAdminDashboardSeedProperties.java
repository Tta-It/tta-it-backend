package com.ttait.domain.companyadmindashboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.company-dashboard-seed")
public class CompanyAdminDashboardSeedProperties {

    /**
     * 승인된 기업 기준 샘플 데이터를 앱 시작 시 생성할지 여부.
     */
    private boolean enabled;
}
