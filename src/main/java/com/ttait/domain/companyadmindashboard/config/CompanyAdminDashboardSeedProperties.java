package com.ttait.domain.companyadmindashboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.company-dashboard-seed")
public class CompanyAdminDashboardSeedProperties {

    private boolean enabled;
}
