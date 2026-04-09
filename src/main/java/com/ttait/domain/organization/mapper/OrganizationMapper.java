package com.ttait.domain.organization.mapper;

import com.ttait.domain.organization.domain.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrganizationMapper {

    Organization findById(@Param("id") Long id);

    Organization findByBusinessNumber(@Param("businessNumber") String businessNumber);

    int insert(Organization organization);
}
