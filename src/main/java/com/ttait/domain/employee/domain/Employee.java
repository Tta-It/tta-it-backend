package com.ttait.domain.employee.domain;

import com.ttait.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기업이 사업 참여 대상으로 등록한 임직원 정보를 담는 도메인 객체.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    private Long id;
    private Long organizationId;
    private Long userId;
    private String employeeNo;
    private String name;
    private String email;
    private String department;
    private String position;
    private String employmentStatus;
}
