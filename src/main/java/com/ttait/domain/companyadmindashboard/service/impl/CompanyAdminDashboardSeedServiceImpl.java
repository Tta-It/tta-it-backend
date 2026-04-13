package com.ttait.domain.companyadmindashboard.service.impl;

import com.ttait.domain.companyadmindashboard.service.CompanyAdminDashboardSeedService;
import com.ttait.domain.employee.domain.Employee;
import com.ttait.domain.employee.mapper.EmployeeMapper;
import com.ttait.domain.employeeusage.mapper.EmployeeUsageStatMapper;
import com.ttait.domain.organization.domain.AgreementStatus;
import com.ttait.domain.organization.domain.Organization;
import com.ttait.domain.organization.mapper.OrganizationMapper;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 승인된 기업의 임직원과 이용내역 시드 데이터를 생성하는 서비스입니다.
 * 승인 이벤트 이후 백그라운드에서 실행되며, 이미 생성된 기업은 중복 생성하지 않습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyAdminDashboardSeedServiceImpl implements CompanyAdminDashboardSeedService {

    private static final int EMPLOYEE_INSERT_BATCH_SIZE = 50;
    private static final String[] SEED_LAST_NAMES = {
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"
    };
    private static final String[] SEED_NAME_SYLLABLES = {
            "민", "서", "지", "현", "준", "윤", "하", "도", "유", "연",
            "수", "아", "진", "우", "영", "원", "호", "예", "채", "린"
    };
    private static final String[] SEED_DEPARTMENTS = {
            "개발팀", "인사팀", "기획팀", "운영팀", "마케팅팀", "디자인팀", "영업팀", "전략팀"
    };
    private static final String[] SEED_POSITIONS = {
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
        List<Employee> employees = new ArrayList<>(missingCount);
        for (int offset = 0; offset < missingCount; offset++) {
            int employeeIndex = nextIndex + offset;
            employees.add(createSeedEmployee(organization.getId(), employeeIndex));
        }

        assignEmployeeIds(employees);
        insertEmployeesInBatches(employees);
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

    private void insertEmployeesInBatches(List<Employee> employees) {
        for (int start = 0; start < employees.size(); start += EMPLOYEE_INSERT_BATCH_SIZE) {
            int end = Math.min(start + EMPLOYEE_INSERT_BATCH_SIZE, employees.size());
            employeeMapper.insertAll(employees.subList(start, end));
        }
    }

    private Employee createSeedEmployee(Long organizationId, int employeeIndex) {
        return Employee.builder()
                .organizationId(organizationId)
                .userId(null)
                .employeeNo(String.format("ORG%03d-EMP%05d", organizationId, employeeIndex))
                .name(generateEmployeeName(organizationId, employeeIndex))
                .email("employee-" + organizationId + "-" + employeeIndex + "@ttait.local")
                .department(pickByIndex(SEED_DEPARTMENTS, employeeIndex))
                .position(pickByIndex(SEED_POSITIONS, employeeIndex))
                .employmentStatus("ACTIVE")
                .build();
    }

    private LocalDate resolveUsageEndDate() {
        return LocalDate.now().minusDays(1);
    }

    private void assignEmployeeIds(List<Employee> employees) {
        List<Long> ids = employeeMapper.findNextIds(employees.size());
        for (int index = 0; index < employees.size(); index++) {
            employees.get(index).setId(ids.get(index));
        }
    }

    private String generateEmployeeName(Long organizationId, int employeeIndex) {
        int sequence = Math.floorMod(Long.hashCode(organizationId) * 997 + employeeIndex, Integer.MAX_VALUE);
        int lastNameIndex = Math.floorMod(sequence, SEED_LAST_NAMES.length);
        int givenNameSequence = sequence / SEED_LAST_NAMES.length;
        int firstSyllableIndex = Math.floorMod(givenNameSequence, SEED_NAME_SYLLABLES.length);
        int secondSyllableIndex = Math.floorMod(
                givenNameSequence / SEED_NAME_SYLLABLES.length,
                SEED_NAME_SYLLABLES.length - 1
        );
        if (secondSyllableIndex >= firstSyllableIndex) {
            secondSyllableIndex++;
        }

        return SEED_LAST_NAMES[lastNameIndex]
                + SEED_NAME_SYLLABLES[firstSyllableIndex]
                + SEED_NAME_SYLLABLES[secondSyllableIndex];
    }

    private String pickByIndex(String[] values, int index) {
        return values[Math.floorMod(index - 1, values.length)];
    }
}
