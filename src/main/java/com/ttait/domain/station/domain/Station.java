package com.ttait.domain.station.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Station {

    private String stationCode;
    private String stationName;
    private String districtName;
    private String detailAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @Builder
    public Station(String stationCode, String stationName, String districtName, String detailAddress,
                   BigDecimal latitude, BigDecimal longitude) {
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.districtName = districtName;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
