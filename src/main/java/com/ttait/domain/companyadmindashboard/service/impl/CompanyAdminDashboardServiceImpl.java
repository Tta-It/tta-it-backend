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
 * 기업 관리자 대시보드 응답을 조립하는 서비스입니다.
 * 조회 월 기준의 임직원 이용 요약과 차트용 월별 집계를 함께 구성합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyAdminDashboardServiceImpl implements CompanyAdminDashboardService {

    private final CompanyAdminDashboardMapper companyAdminDashboardMapper;

    /**
     * 선택 월 기준의 기업 관리자 대시보드 데이터를 구성합니다.
     * 임직원별 월간 이용 요약과 최근 5개월 월별 전체 이용 횟수를 함께 내려줍니다.
     */
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

    /**
     * 선택한 임직원의 월간 이용 요약, 일자별 이용내역, 가장 최근 이용내역을 조회합니다.
     */
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

    /**
     * 로그인 사용자에 연결된 기업 ID가 있는지 확인합니다.
     */
    private void validateOrganizationId(Long organizationId) {
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }
    }

    /**
     * 요청 월을 월 시작일/종료일과 월별 차트 시작일로 변환합니다.
     */
    private SearchCriteria resolveCriteria(CompanyAdminDashboardSearchRequest request) {
        String rawTargetMonth = request != null ? request.getTargetMonth() : null;
        YearMonth targetMonth = parseTargetMonth(rawTargetMonth);

        return new SearchCriteria(
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth(),
                targetMonth.minusMonths(4).atDay(1)
        );
    }

    /**
     * targetMonth 파라미터를 파싱하고, 없으면 현재 월을 기본값으로 사용합니다.
     */
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

    /**
     * 집계 결과가 없을 때 화면에서 null 처리 없이 사용할 기본 요약값을 만듭니다.
     */
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
