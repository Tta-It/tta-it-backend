package com.ttait.domain.dataimport.service.impl;

import com.ttait.domain.dataimport.config.CsvImportProperties;
import com.ttait.domain.dataimport.service.CsvImportService;
import com.ttait.domain.station.domain.Station;
import com.ttait.domain.station.mapper.StationMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportServiceImpl implements CsvImportService {

    private static final int BATCH_SIZE = 500;
    private static final long MINIMUM_COMPLETE_STATION_COUNT = 2000;
    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.KOREA);

    private static final String HEADER_STATION_CODE = "\uB300\uC5EC\uC18C\uBC88\uD638";
    private static final String STATION_SHEET_NAME = "\uB300\uC5EC\uC18C\uD604\uD669";

    private final CsvImportProperties csvImportProperties;
    private final StationMapper stationMapper;

    @Override
    public void importAll() {
        if (csvImportProperties.isStationEnabled()) {
            importStationsIfNeeded();
        }
    }

    private void importStationsIfNeeded() {
        long existingStationCount = stationMapper.countAll();
        if (existingStationCount >= MINIMUM_COMPLETE_STATION_COUNT) {
            log.info("Station data already has {} rows. Skipping station import.", existingStationCount);
            return;
        }

        if (existingStationCount > 0) {
            stationMapper.deleteAll();
            log.info("Station data has only {} rows. Re-importing from source file.", existingStationCount);
        }

        Path stationFile = resolveStationSource(csvImportProperties.getStationFilePath());
        importStations(stationFile, Charset.forName(csvImportProperties.getStationCharset()));
    }

    private void importStations(Path stationFile, Charset charset) {
        String fileName = stationFile.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            importStationsFromWorkbook(stationFile);
            return;
        }
        importStationsFromCsv(stationFile, charset);
    }

    private void importStationsFromWorkbook(Path stationFile) {
        int importedCount = 0;
        int skippedCount = 0;
        List<Station> batch = new ArrayList<>(BATCH_SIZE);

        try (InputStream inputStream = Files.newInputStream(stationFile);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = resolveStationSheet(workbook);
            for (Row row : sheet) {
                Station station = toStation(row);
                if (station == null) {
                    skippedCount++;
                    continue;
                }

                batch.add(station);
                if (batch.size() >= BATCH_SIZE) {
                    stationMapper.insertAll(batch);
                    importedCount += batch.size();
                    batch.clear();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read station workbook: " + stationFile, e);
        }

        if (!batch.isEmpty()) {
            stationMapper.insertAll(batch);
            importedCount += batch.size();
        }

        log.info("Imported {} stations from {}. skipped={}", importedCount, stationFile, skippedCount);
    }

    private void importStationsFromCsv(Path stationFile, Charset charset) {
        int importedCount = 0;
        List<Station> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = Files.newBufferedReader(stationFile, charset);
             CSVParser parser = csvFormat().parse(reader)) {

            HeaderAccessor headers = HeaderAccessor.from(parser);
            for (CSVRecord record : parser) {
                batch.add(Station.builder()
                        .stationCode(normalizeStationCode(headers.getRequired(record, HEADER_STATION_CODE)))
                        .stationName(headers.getRequired(record,
                                "\uBCF4\uAD00\uC18C(\uB300\uC5EC\uC18C)\uBA85",
                                "\uB300\uC5EC\uC18C\uBA85"))
                        .districtName(headers.getOptional(record, "\uC790\uCE58\uAD6C"))
                        .detailAddress(headers.getOptional(record, "\uC0C1\uC138\uC8FC\uC18C", "\uC8FC\uC18C"))
                        .latitude(parseDecimal(headers.getOptional(record, "\uC704\uB3C4")))
                        .longitude(parseDecimal(headers.getOptional(record, "\uACBD\uB3C4")))
                        .build());

                if (batch.size() >= BATCH_SIZE) {
                    stationMapper.insertAll(batch);
                    importedCount += batch.size();
                    batch.clear();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read station CSV: " + stationFile, e);
        }

        if (!batch.isEmpty()) {
            stationMapper.insertAll(batch);
            importedCount += batch.size();
        }

        log.info("Imported {} stations from {}.", importedCount, stationFile);
    }

    private Sheet resolveStationSheet(Workbook workbook) {
        Sheet stationSheet = workbook.getSheet(STATION_SHEET_NAME);
        if (stationSheet != null) {
            return stationSheet;
        }

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().contains(STATION_SHEET_NAME)) {
                return sheet;
            }
        }

        throw new IllegalStateException("Station sheet not found: " + STATION_SHEET_NAME);
    }

    private Station toStation(Row row) {
        String stationCode = normalizeStationCodeOrNull(cellText(row, 0));
        if (stationCode == null) {
            return null;
        }

        String stationName = normalizeBlank(cellText(row, 1));
        String districtName = normalizeBlank(cellText(row, 2));
        String detailAddress = normalizeBlank(cellText(row, 3));
        if (stationName == null || districtName == null || detailAddress == null) {
            return null;
        }

        return Station.builder()
                .stationCode(stationCode)
                .stationName(stationName)
                .districtName(districtName)
                .detailAddress(detailAddress)
                .latitude(parseDecimal(cellText(row, 4)))
                .longitude(parseDecimal(cellText(row, 5)))
                .build();
    }

    private String cellText(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        return normalizeBlank(DATA_FORMATTER.formatCellValue(cell));
    }

    private Path resolveStationSource(String pathValue) {
        if (!StringUtils.hasText(pathValue)) {
            throw new IllegalStateException("Station import path is not configured.");
        }

        Path path = Paths.get(pathValue).toAbsolutePath().normalize();
        if (Files.isRegularFile(path)) {
            return path;
        }

        if (Files.isDirectory(path)) {
            try (var paths = Files.list(path)) {
                return paths
                        .filter(Files::isRegularFile)
                        .filter(candidate -> {
                            String fileName = candidate.getFileName().toString().toLowerCase(Locale.ROOT);
                            return fileName.endsWith(".xlsx") || fileName.endsWith(".xls") || fileName.endsWith(".csv");
                        })
                        .sorted(Comparator.comparing(candidate -> candidate.getFileName().toString()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Station source file not found: " + path));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to list station source files: " + path, e);
            }
        }

        throw new IllegalStateException("Station import path does not exist: " + path);
    }

    private CSVFormat csvFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();
    }

    private String normalizeStationCode(String rawValue) {
        String normalized = normalizeStationCodeOrNull(rawValue);
        if (normalized == null) {
            throw new IllegalStateException("Station code is required.");
        }
        return normalized;
    }

    private String normalizeStationCodeOrNull(String rawValue) {
        String normalized = normalizeBlank(rawValue);
        if (normalized == null) {
            return null;
        }

        String digitsOnly = normalized.replaceAll("[^0-9]", "");
        if (!StringUtils.hasText(digitsOnly)) {
            return null;
        }

        return digitsOnly.replaceFirst("^0+(?!$)", "");
    }

    private String normalizeBlank(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }
        String value = rawValue.replace('\u00A0', ' ').strip();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal parseDecimal(String rawValue) {
        String normalized = normalizeNumberText(rawValue);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid decimal value: " + rawValue, e);
        }
    }

    private String normalizeNumberText(String rawValue) {
        String normalized = normalizeBlank(rawValue);
        if (normalized == null) {
            return null;
        }
        return normalized.replace(",", "");
    }

    private static class HeaderAccessor {

        private final Map<String, String> headers;

        private HeaderAccessor(Map<String, String> headers) {
            this.headers = headers;
        }

        static HeaderAccessor from(CSVParser parser) {
            Map<String, String> normalizedHeaders = new HashMap<>();
            for (String header : parser.getHeaderMap().keySet()) {
                if (Objects.nonNull(header)) {
                    normalizedHeaders.putIfAbsent(normalizeHeader(header), header);
                }
            }
            return new HeaderAccessor(normalizedHeaders);
        }

        String getRequired(CSVRecord record, String... candidates) {
            String value = getOptional(record, candidates);
            if (!StringUtils.hasText(value)) {
                throw new IllegalStateException("Required CSV column is missing: " + String.join(", ", candidates));
            }
            return value;
        }

        String getOptional(CSVRecord record, String... candidates) {
            for (String candidate : candidates) {
                String originalHeader = headers.get(normalizeHeader(candidate));
                if (originalHeader != null && record.isMapped(originalHeader)) {
                    return record.get(originalHeader);
                }
            }
            return null;
        }

        private static String normalizeHeader(String header) {
            return header.replace("\uFEFF", "")
                    .replaceAll("\\s+", "")
                    .toLowerCase(Locale.ROOT);
        }
    }
}
