package com.ttait.domain.dataimport.runner;

import com.ttait.domain.dataimport.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.csv-import", name = "enabled", havingValue = "true")
public class CsvImportRunner implements ApplicationRunner {

    private final CsvImportService csvImportService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("설정된 CSV 적재를 시작합니다");
        csvImportService.importAll();
        log.info("CSV 적재가 완료되었습니다");
    }
}
