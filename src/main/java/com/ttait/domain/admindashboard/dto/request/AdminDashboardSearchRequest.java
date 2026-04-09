package com.ttait.domain.admindashboard.dto.request;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboardSearchRequest {

    private LocalDate from;
    private LocalDate to;
}
