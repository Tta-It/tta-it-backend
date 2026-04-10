package com.ttait.domain.dataimport.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.csv-import")
public class CsvImportProperties {

    private boolean enabled;
    private boolean stationEnabled = true;
    private boolean usageEnabled = false;
    private String stationFilePath;
    private String usageDirPath;
    private String stationCharset = "UTF-8";
    private String usageCharset = "UTF-8";
}
