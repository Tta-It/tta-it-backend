package com.ttait.domain.companyadmindashboard.service.impl;

import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardSeedService;
import com.ttait.domain.employee.domain.Employee;
import com.ttait.domain.employee.mapper.EmployeeMapper;
import com.ttait.domain.employeeusage.domain.EmployeeUsageStat;
import com.ttait.domain.employeeusage.mapper.EmployeeUsageStatMapper;
import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import com.ttait.domain.organization.mapper.OrganizationMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 승인된 기업 기준으로 기업 관리자 대시보드용 임직원/이용 데이터를 생성한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyAdminDashboardSeedServiceImpl implements CompanyAdminDashboardSeedService {

    private static final int EMPLOYEE_INSERT_BATCH_SIZE = 50;
    private static final int USAGE_INSERT_BATCH_SIZE = 100;
    private static final BigDecimal CARBON_REDUCTION_PER_KM = BigDecimal.valueOf(0.239);
    private static final String[] LAST_NAMES = {
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"
    };
    private static final String[] FIRST_NAMES = {
            "민수", "서준", "지우", "예진", "하은", "도윤", "지훈", "수빈", "현우", "나연",
            "성민", "다은", "유진", "지민", "태윤", "은서", "혜원", "준호", "소연", "민지"
    };
    private static final String[] DEPARTMENTS = {
            "개발팀", "인사팀", "기획팀", "운영팀", "마케팅팀", "디자인팀", "영업팀", "전략팀"
    };
    private static final String[] POSITIONS = {
            "사원", "주임", "대리", "과장", "차장"
    };

    private final OrganizationMapper organizationMapper;
    private final EmployeeMapper employeeMapper;
    private final EmployeeUsageStatMapper employeeUsageStatMapper;

    @Override
    @Transactional
    public void seedIfNeeded() {
        List<Organization> approvedOrganizations = organizationMapper.findApprovedOrganizations();
        if (approvedOrganizations.isEmpty()) {
            log.info("승인된 기업이 없어 기업 관리자 대시보드용 샘플 데이터를 생성하지 않습니다.");
            return;
        }

        for (Organization organization : approvedOrganizations) {
            seedOrganizationIfNeeded(organization);
        }
    }

    @Override
    @Transactional
    public void seedIfNeeded(Long organizationId) {
        Organization organization = organizationMapper.findById(organizationId);
        if (organization == null || organization.getAgreementStatus() != AgreementStatus.ACTIVE) {
            log.warn("Skip company dashboard seed. organizationId={}, reason=not-active", organizationId);
            return;
        }

        seedOrganizationIfNeeded(organization);
    }

    private void seedOrganizationIfNeeded(Organization organization) {
        seedEmployeesIfNeeded(organization);
        seedEmployeeUsagesIfNeeded(organization);
    }

    private void seedEmployeesIfNeeded(Organization organization) {
        int existingEmployeeCount = (int) employeeMapper.countByOrganizationId(organization.getId());
        int participantCount = calculateParticipantCount(organization);
        if (existingEmployeeCount >= participantCount) {
            return;
        }

        // 전체 직원이 모두 참여하진 않으므로 일부 인원만 사업 참여 임직원으로 생성한다.
        int missingCount = participantCount - existingEmployeeCount;
        int nextIndex = existingEmployeeCount + 1;
        employeeMapper.insertSeedEmployees(organization.getId(), nextIndex, missingCount);
        log.info("기업 관리자 대시보드용 임직원 데이터를 생성했습니다. organizationId={}, addedCount={}, totalTargetCount={}",
                organization.getId(), missingCount, participantCount);
    }

    private void seedEmployeeUsagesIfNeeded(Organization organization) {
        if (organization.getApprovedAt() == null) {
            return;
        }

        LocalDate usageEndDate = resolveUsageEndDate();

        // approvedAt은 시연용 협약 시작일로 저장되어 사용 데이터 시작 기준이 된다.
        LocalDate usageStartDate = organization.getApprovedAt().toLocalDate();
        if (usageStartDate.isAfter(usageEndDate)) {
            return;
        }

        long employeeCount = employeeMapper.countByOrganizationId(organization.getId());
        if (employeeCount == 0) {
            return;
        }

        int dayCount = (int) ChronoUnit.DAYS.between(usageStartDate, usageEndDate) + 1;
        int insertedUsageCount = employeeUsageStatMapper.insertSeedUsageStats(
                organization.getId(),
                usageStartDate,
                dayCount
        );

        log.info("기업 관리자 대시보드용 이용 데이터를 생성했습니다. organizationId={}, employeeCount={}, insertedUsageCount={}, usageStartDate={}, usageEndDate={}",
                organization.getId(), employeeCount, insertedUsageCount, usageStartDate, usageEndDate);
    }

    private int calculateParticipantCount(Organization organization) {
        Integer employeeCount = organization.getEmployeeCount();
        int totalEmployeeCount = employeeCount != null && employeeCount > 0 ? employeeCount : 20;
        if (totalEmployeeCount == 1) {
            return 1;
        }

        // 참여 임직원은 회사 전체 인원의 일부만 되도록 25%~55% 범위에서 정한다.
        int seed = organization.getId() != null ? organization.getId().intValue() : totalEmployeeCount;
        double ratio = 0.25 + (Math.floorMod(seed, 31) / 100.0);
        int participantCount = (int) Math.round(totalEmployeeCount * ratio);
        participantCount = Math.max(1, participantCount);
        participantCount = Math.min(participantCount, totalEmployeeCount - 1);
        return participantCount;
    }

    private void generateEmployeeUsageStats(
            Long organizationId,
            Employee employee,
            LocalDate usageStartDate,
            LocalDate usageEndDate,
            List<EmployeeUsageStat> buffer
    ) {
        long totalDays = ChronoUnit.DAYS.between(usageStartDate, usageEndDate) + 1;
        double weekdayCommuteRate = ThreadLocalRandom.current().nextDouble(0.18, 0.34);
        double weekendLeisureRate = ThreadLocalRandom.current().nextDouble(0.28, 0.48);

        for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
            LocalDate usageDate = usageStartDate.plusDays(dayOffset);
            boolean weekend = isWeekend(usageDate);
            double usageRate = weekend ? weekendLeisureRate : weekdayCommuteRate;
            if (ThreadLocalRandom.current().nextDouble() > usageRate) {
                continue;
            }

            int usageCount = weekend ? weekendUsageCount() : weekdayCommuteUsageCount();
            BigDecimal distancePerRide = weekend ? decimalBetween(3.5, 8.0) : decimalBetween(2.0, 4.8);
            BigDecimal totalDistance = distancePerRide.multiply(BigDecimal.valueOf(usageCount))
                    .setScale(1, RoundingMode.HALF_UP);
            BigDecimal carbonAmount = totalDistance.multiply(CARBON_REDUCTION_PER_KM)
                    .setScale(1, RoundingMode.HALF_UP);
            BigDecimal durationMinutes = totalDistance
                    .multiply(weekend ? decimalBetween(4.2, 6.5) : decimalBetween(3.8, 5.2))
                    .setScale(1, RoundingMode.HALF_UP);

            buffer.add(EmployeeUsageStat.builder()
                    .organizationId(organizationId)
                    .employeeId(employee.getId())
                    .usageDate(usageDate)
                    .usageCount(usageCount)
                    .travelDistance(totalDistance)
                    .carbonAmount(carbonAmount)
                    .usageDurationMinutes(durationMinutes)
                    .createdAt(LocalDateTime.now())
                    .build());
            flushUsageBufferIfNeeded(buffer);
        }
    }

    private void insertEmployeesInBatches(List<Employee> employees) {
        for (int start = 0; start < employees.size(); start += EMPLOYEE_INSERT_BATCH_SIZE) {
            int end = Math.min(start + EMPLOYEE_INSERT_BATCH_SIZE, employees.size());
            employeeMapper.insertAll(employees.subList(start, end));
        }
    }

    private LocalDate resolveUsageEndDate() {
        return LocalDate.now().minusDays(1);
    }

    private boolean isWeekend(LocalDate usageDate) {
        DayOfWeek dayOfWeek = usageDate.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private int weekdayCommuteUsageCount() {
        double randomValue = ThreadLocalRandom.current().nextDouble();
        if (randomValue < 0.70) {
            return 2;
        }
        if (randomValue < 0.93) {
            return 1;
        }
        return 3;
    }

    private int weekendUsageCount() {
        double randomValue = ThreadLocalRandom.current().nextDouble();
        if (randomValue < 0.50) {
            return 2;
        }
        if (randomValue < 0.85) {
            return 3;
        }
        return 4;
    }

    private void flushUsageBufferIfNeeded(List<EmployeeUsageStat> buffer) {
        if (buffer.size() >= USAGE_INSERT_BATCH_SIZE) {
            flushUsageBuffer(buffer);
        }
    }

    private void flushUsageBuffer(List<EmployeeUsageStat> buffer) {
        if (buffer.isEmpty()) {
            return;
        }

        assignEmployeeUsageIds(buffer);
        employeeUsageStatMapper.insertAll(buffer);
        buffer.clear();
    }

    private void assignEmployeeIds(List<Employee> employees) {
        List<Long> ids = employeeMapper.findNextIds(employees.size());
        for (int index = 0; index < employees.size(); index++) {
            employees.get(index).setId(ids.get(index));
        }
    }

    private void assignEmployeeUsageIds(List<EmployeeUsageStat> usages) {
        List<Long> ids = employeeUsageStatMapper.findNextIds(usages.size());
        for (int index = 0; index < usages.size(); index++) {
            usages.get(index).setId(ids.get(index));
        }
    }

    private String generateEmployeeName() {
        return pickRandom(LAST_NAMES) + pickRandom(FIRST_NAMES);
    }

    private String pickRandom(String[] values) {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private BigDecimal decimalBetween(double min, double max) {
        double randomValue = ThreadLocalRandom.current().nextDouble(min, max);
        return BigDecimal.valueOf(randomValue).setScale(2, RoundingMode.HALF_UP);
    }
}
