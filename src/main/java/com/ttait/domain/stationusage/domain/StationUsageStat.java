package com.ttait.domain.stationusage.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StationUsageStat {

    private Long id;
    private LocalDate statDate;
    private String stationCode;
    private String rentalType;
    private String gender;
    private String ageGroup;
    private Integer usageCount;
    private BigDecimal exerciseAmount;
    private BigDecimal carbonAmount;
    private BigDecimal travelDistance;
    private BigDecimal usageDurationMinutes;

    @Builder
    public StationUsageStat(Long id, LocalDate statDate, String stationCode, String rentalType, String gender,
                            String ageGroup, Integer usageCount, BigDecimal exerciseAmount,
                            BigDecimal carbonAmount, BigDecimal travelDistance, BigDecimal usageDurationMinutes) {
        this.id = id;
        this.statDate = statDate;
        this.stationCode = stationCode;
        this.rentalType = rentalType;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.usageCount = usageCount;
        this.exerciseAmount = exerciseAmount;
        this.carbonAmount = carbonAmount;
        this.travelDistance = travelDistance;
        this.usageDurationMinutes = usageDurationMinutes;
    }
}
