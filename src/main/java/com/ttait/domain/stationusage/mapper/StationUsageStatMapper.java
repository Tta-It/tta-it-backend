package com.ttait.domain.stationusage.mapper;

import com.ttait.domain.stationusage.domain.StationUsageStat;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StationUsageStatMapper {

    int deleteAll();

    void restartSequence();

    List<Long> findNextIds(@Param("count") int count);

    int insertAll(@Param("usageStats") List<StationUsageStat> usageStats);
}
