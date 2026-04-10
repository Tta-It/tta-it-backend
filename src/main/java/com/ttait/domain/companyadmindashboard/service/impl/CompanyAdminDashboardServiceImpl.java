package com.ttait.domain.companyadmindashboard.service.impl;

import com.ttait.domain.companyadmindashboard.dto.request.CompanyAdminDashboardSearchRequest;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDetailResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardRewardCriteriaResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardSummaryResponse;
import com.ttait.domain.companyadmindashboard.mapper.CompanyAdminDashboardMapper;
import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardService;
import com.ttait.global.exception.BusinessException;
import com.ttait.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기업 관리자 대시보드 응답을 조립하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyAdminDashboardServiceImpl implements CompanyAdminDashboardService {

    private final CompanyAdminDashboardMapper companyAdminDashboardMapper;

    @Override
    public CompanyAdminDashboardResponse getDashboard(Long organizationId, CompanyAdminDashboardSearchRequest request) {
        validateOrganizationId(organizationId);

        SearchCriteria criteria = resolveCriteria(request);
        CompanyAdminDashboardSummaryResponse summary = companyAdminDashboardMapper.findSummary(
                organizationId,
                criteria.startDate(),
                criteria.endDate(),
                criteria.rewardTargetPercent(),
                criteria.minimumMonthlyUsageCount()
        );
        List<CompanyAdminDashboardEmployeeUsageResponse> employeeUsages =
                companyAdminDashboardMapper.findEmployeeUsages(
                        organizationId,
                        criteria.startDate(),
                        criteria.endDate(),
                        criteria.rewardTargetPercent(),
                        criteria.minimumMonthlyUsageCount(),
                        criteria.rewardOnly()
                );

        return CompanyAdminDashboardResponse.builder()
                .summary(summary != null ? summary : emptySummary())
                .rewardCriteria(CompanyAdminDashboardRewardCriteriaResponse.builder()
                        .targetMonth(criteria.targetMonth().toString())
                        .rewardTargetPercent(criteria.rewardTargetPercent())
                        .minimumMonthlyUsageCount(criteria.minimumMonthlyUsageCount())
                        .rewardOnly(criteria.rewardOnly())
                        .build())
                .employeeUsages(employeeUsages)
                .build();
    }

    @Override
    public CompanyAdminDashboardEmployeeDetailResponse getEmployeeDetail(
            Long organizationId,
            Long employeeId,
            CompanyAdminDashboardSearchRequest request
    ) {
        validateOrganizationId(organizationId);

        SearchCriteria criteria = resolveCriteria(request);
        CompanyAdminDashboardEmployeeDetailResponse detail = companyAdminDashboardMapper.findEmployeeDetail(
                organizationId,
                employeeId,
                criteria.startDate(),
                criteria.endDate(),
                criteria.rewardTargetPercent(),
                criteria.minimumMonthlyUsageCount()
        );

        if (detail == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        detail.setDailyUsages(companyAdminDashboardMapper.findEmployeeDailyUsages(
                organizationId,
                employeeId,
                criteria.startDate(),
                criteria.endDate()
        ));
        return detail;
    }

    private void validateOrganizationId(Long organizationId) {
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }
    }

    private SearchCriteria resolveCriteria(CompanyAdminDashboardSearchRequest request) {
        YearMonth targetMonth = parseTargetMonth(request.getTargetMonth());
        int rewardTargetPercent = request.getRewardTargetPercent() != null ? request.getRewardTargetPercent() : 10;
        int minimumMonthlyUsageCount = request.getMinimumMonthlyUsageCount() != null
                ? request.getMinimumMonthlyUsageCount() : 15;
        boolean rewardOnly = Boolean.TRUE.equals(request.getRewardOnly());

        if (rewardTargetPercent <= 0 || rewardTargetPercent > 100 || minimumMonthlyUsageCount < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return new SearchCriteria(
                targetMonth,
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth(),
                rewardTargetPercent,
                minimumMonthlyUsageCount,
                rewardOnly
        );
    }

    private YearMonth parseTargetMonth(String rawTargetMonth) {
        if (rawTargetMonth == null || rawTargetMonth.isBlank()) {
            return YearMonth.from(LocalDate.now().minusYears(1));
        }

        try {
            return YearMonth.parse(rawTargetMonth);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private CompanyAdminDashboardSummaryResponse emptySummary() {
        return CompanyAdminDashboardSummaryResponse.builder()
                .totalUsageCount(0L)
                .totalTravelDistance(java.math.BigDecimal.ZERO)
                .totalCarbonReduction(java.math.BigDecimal.ZERO)
                .rewardTargetEmployeeCount(0)
                .build();
    }

    private record SearchCriteria(
            YearMonth targetMonth,
            LocalDate startDate,
            LocalDate endDate,
            int rewardTargetPercent,
            int minimumMonthlyUsageCount,
            boolean rewardOnly
    ) {
    }
}
