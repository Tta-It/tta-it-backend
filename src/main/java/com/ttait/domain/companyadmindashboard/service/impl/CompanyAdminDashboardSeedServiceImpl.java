package com.ttait.domain.companyadmindashboard.service.impl;

import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardSeedService;
import com.ttait.domain.employee.domain.Employee;
import com.ttait.domain.employee.mapper.EmployeeMapper;
import com.ttait.domain.employeeusage.domain.EmployeeUsageStat;
import com.ttait.domain.employeeusage.mapper.EmployeeUsageStatMapper;
import com.ttait.domain.organization.domain.Organization;
import com.ttait.domain.organization.mapper.OrganizationMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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

    private static final LocalDate DATA_BASE_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate DATA_BASE_END_DATE = LocalDate.of(2025, 12, 31);
    private static final int INSERT_BATCH_SIZE = 500;
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
            seedEmployeesIfNeeded(organization);
            seedEmployeeUsagesIfNeeded(organization);
        }
    }

    private void seedEmployeesIfNeeded(Organization organization) {
        if (employeeMapper.countByOrganizationId(organization.getId()) > 0) {
            return;
        }

        // 전체 직원이 모두 참여하진 않으므로 일부 인원만 사업 참여 임직원으로 생성한다.
        int participantCount = calculateParticipantCount(organization.getEmployeeCount());
        List<Employee> employees = new ArrayList<>(participantCount);

        for (int index = 1; index <= participantCount; index++) {
            String employeeNo = String.format("ORG%03d-EMP%03d", organization.getId(), index);
            employees.add(Employee.builder()
                    .organizationId(organization.getId())
                    .employeeNo(employeeNo)
                    .name(generateEmployeeName())
                    .email("employee-" + organization.getId() + "-" + index + "@ttait.local")
                    .department(pickRandom(DEPARTMENTS))
                    .position(pickRandom(POSITIONS))
                    .employmentStatus("ACTIVE")
                    .build());
        }

        employeeMapper.insertAll(employees);
        log.info("기업 관리자 대시보드용 임직원 데이터를 생성했습니다. organizationId={}, count={}",
                organization.getId(), participantCount);
    }

    private void seedEmployeeUsagesIfNeeded(Organization organization) {
        if (employeeUsageStatMapper.countByOrganizationId(organization.getId()) > 0) {
            return;
        }

        if (organization.getApprovedAt() == null) {
            return;
        }

        // 실제 협약일보다 1년 앞당긴 날짜를 2025년 사용 데이터의 시작 기준으로 본다.
        LocalDate usageStartDate = organization.getApprovedAt().toLocalDate().minusYears(1);
        if (usageStartDate.isBefore(DATA_BASE_START_DATE)) {
            usageStartDate = DATA_BASE_START_DATE;
        }
        if (usageStartDate.isAfter(DATA_BASE_END_DATE)) {
            return;
        }

        List<Employee> employees = employeeMapper.findByOrganizationId(organization.getId());
        if (employees.isEmpty()) {
            return;
        }

        List<EmployeeUsageStat> buffer = new ArrayList<>(INSERT_BATCH_SIZE);
        for (Employee employee : employees) {
            generateEmployeeUsageStats(organization.getId(), employee, usageStartDate, buffer);
            flushUsageBufferIfNeeded(buffer);
        }
        flushUsageBuffer(buffer);

        log.info("기업 관리자 대시보드용 이용 데이터를 생성했습니다. organizationId={}, employeeCount={}, usageStartDate={}",
                organization.getId(), employees.size(), usageStartDate);
    }

    private int calculateParticipantCount(Integer employeeCount) {
        int totalEmployeeCount = employeeCount != null && employeeCount > 0 ? employeeCount : 20;
        if (totalEmployeeCount == 1) {
            return 1;
        }

        // 참여 임직원은 회사 전체 인원의 일부만 되도록 25%~55% 범위에서 정한다.
        double ratio = ThreadLocalRandom.current().nextDouble(0.25, 0.55);
        int participantCount = (int) Math.round(totalEmployeeCount * ratio);
        participantCount = Math.max(1, participantCount);
        participantCount = Math.min(participantCount, totalEmployeeCount - 1);
        return participantCount;
    }

    private void generateEmployeeUsageStats(
            Long organizationId,
            Employee employee,
            LocalDate usageStartDate,
            List<EmployeeUsageStat> buffer
    ) {
        long totalDays = ChronoUnit.DAYS.between(usageStartDate, DATA_BASE_END_DATE) + 1;
        // 협약 직후 데이터가 많지 않다는 느낌을 주기 위해 일자별 생성 확률을 낮게 잡는다.
        double participationRate = ThreadLocalRandom.current().nextDouble(0.08, 0.17);

        for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
            LocalDate usageDate = usageStartDate.plusDays(dayOffset);
            if (ThreadLocalRandom.current().nextDouble() > participationRate) {
                continue;
            }

            // 이용한 날에는 1~3회 정도만 탄 것으로 보고 거리/탄소량도 함께 계산한다.
            int usageCount = ThreadLocalRandom.current().nextInt(1, 4);
            BigDecimal distancePerRide = decimalBetween(2.6, 5.8);
            BigDecimal totalDistance = distancePerRide.multiply(BigDecimal.valueOf(usageCount))
                    .setScale(1, RoundingMode.HALF_UP);
            BigDecimal carbonAmount = totalDistance.multiply(BigDecimal.valueOf(0.239))
                    .setScale(1, RoundingMode.HALF_UP);
            BigDecimal durationMinutes = totalDistance.multiply(decimalBetween(3.8, 5.2))
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
        }
    }

    private void flushUsageBufferIfNeeded(List<EmployeeUsageStat> buffer) {
        if (buffer.size() >= INSERT_BATCH_SIZE) {
            flushUsageBuffer(buffer);
        }
    }

    private void flushUsageBuffer(List<EmployeeUsageStat> buffer) {
        if (buffer.isEmpty()) {
            return;
        }

        employeeUsageStatMapper.insertAll(buffer);
        buffer.clear();
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
