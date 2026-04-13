package com.ttait.domain.companyadmindashboard.service.impl;

import com.ttait.domain.companyadmindashboard.dto.request.CompanyAdminDashboardSearchRequest;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDetailResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardEmployeeDailyUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardMonthlyUsageResponse;
import com.ttait.domain.companyadmindashboard.dto.response.CompanyAdminDashboardResponse;
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
                criteria.endDate()
        );
        List<CompanyAdminDashboardEmployeeUsageResponse> employeeUsages =
                companyAdminDashboardMapper.findEmployeeUsages(
                        organizationId,
                        criteria.startDate(),
                        criteria.endDate()
                );
        List<CompanyAdminDashboardMonthlyUsageResponse> monthlyUsages =
                companyAdminDashboardMapper.findMonthlyUsages(
                        organizationId,
                        criteria.monthlyStartDate(),
                        criteria.endDate()
                );

        return CompanyAdminDashboardResponse.builder()
                .summary(summary != null ? summary : emptySummary())
                .monthlyUsages(monthlyUsages)
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
                criteria.endDate()
        );

        if (detail == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        CompanyAdminDashboardEmployeeDailyUsageResponse latestUsage =
                companyAdminDashboardMapper.findLatestEmployeeUsage(organizationId, employeeId);
        detail.setLatestUsage(latestUsage);
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
        String rawTargetMonth = request != null ? request.getTargetMonth() : null;
        YearMonth targetMonth = parseTargetMonth(rawTargetMonth);

        return new SearchCriteria(
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth(),
                targetMonth.minusMonths(4).atDay(1)
        );
    }

    private YearMonth parseTargetMonth(String rawTargetMonth) {
        if (rawTargetMonth == null || rawTargetMonth.isBlank()) {
            return YearMonth.from(LocalDate.now());
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
                .build();
    }

    private record SearchCriteria(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate monthlyStartDate
    ) {
    }
}
