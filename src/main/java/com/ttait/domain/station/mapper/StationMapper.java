package com.ttait.domain.station.mapper;

import com.ttait.domain.station.domain.Station;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StationMapper {

    long countAll();

    int deleteAll();

    int insertAll(@Param("stations") List<Station> stations);
}
