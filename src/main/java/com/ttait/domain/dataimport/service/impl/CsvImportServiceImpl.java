package com.ttait.domain.dataimport.service.impl;

import com.ttait.domain.dataimport.config.CsvImportProperties;
import com.ttait.domain.dataimport.service.CsvImportService;
import com.ttait.domain.station.domain.Station;
import com.ttait.domain.station.mapper.StationMapper;
import com.ttait.domain.stationusage.domain.StationUsageStat;
import com.ttait.domain.stationusage.mapper.StationUsageStatMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.KOREA);

    private static final String HEADER_STATION_CODE = "\uB300\uC5EC\uC18C\uBC88\uD638";
    private static final String HEADER_STAT_DATE = "\uB300\uC5EC\uC77C\uC790";
    private static final String HEADER_RENTAL_TYPE = "\uB300\uC5EC\uAD6C\uBD84";
    private static final String HEADER_RENTAL_TYPE_CODE = "\uB300\uC5EC\uAD6C\uBD84\uCF54\uB4DC";
    private static final String HEADER_GENDER = "\uC131\uBCC4";
    private static final String HEADER_AGE_GROUP = "\uC5F0\uB839\uB300";
    private static final String HEADER_STATION_NAME = "\uB300\uC5EC\uC18C";
    private static final String HEADER_USAGE_COUNT = "\uC774\uC6A9\uAC74\uC218";
    private static final String HEADER_EXERCISE_AMOUNT = "\uC6B4\uB3D9\uB7C9";
    private static final String HEADER_CARBON_AMOUNT = "\uD0C4\uC18C\uB7C9";
    private static final String HEADER_TRAVEL_DISTANCE = "\uC774\uB3D9\uAC70\uB9AC";
    private static final String HEADER_TRAVEL_DISTANCE_M = "\uC774\uB3D9\uAC70\uB9AC(M)";
    private static final String HEADER_USAGE_DURATION = "\uC774\uC6A9\uC2DC\uAC04";
    private static final String HEADER_USAGE_DURATION_MINUTES = "\uC774\uC6A9\uC2DC\uAC04(\uBD84)";

    private static final String STATION_SHEET_NAME = "\uB300\uC5EC\uC18C\uD604\uD669";

    private final CsvImportProperties csvImportProperties;
    private final StationMapper stationMapper;
    private final StationUsageStatMapper stationUsageStatMapper;

    @Override
    public void importAll() {
        if (csvImportProperties.isStationEnabled()) {
            importStationsIfNeeded();
        }

        if (csvImportProperties.isUsageEnabled()) {
            importUsageStatsFromFiles();
        }
    }

    private void importStationsIfNeeded() {
        long existingStationCount = stationMapper.countAll();
        if (existingStationCount > 0) {
            log.info("대여소 데이터가 이미 {}건 존재하여 자동 적재를 건너뜁니다", existingStationCount);
            return;
        }

        Path stationFile = resolveStationSource(csvImportProperties.getStationFilePath());
        importStations(stationFile, Charset.forName(csvImportProperties.getStationCharset()));
    }

    private void importUsageStatsFromFiles() {
        Path usageDir = requireExistingDirectory(csvImportProperties.getUsageDirPath(), "usage directory");
        List<Path> usageFiles = listUsageFiles(usageDir);

        stationUsageStatMapper.deleteAll();
        stationUsageStatMapper.restartSequence();
        importUsageStats(usageFiles, Charset.forName(csvImportProperties.getUsageCharset()),
                new HashSet<>(stationMapper.findAllStationCodes()));
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
            throw new IllegalStateException("대여소 엑셀 파일을 읽는 중 오류가 발생했습니다: " + stationFile, e);
        }

        if (!batch.isEmpty()) {
            stationMapper.insertAll(batch);
            importedCount += batch.size();
        }

        log.info("대여소 {}건을 적재했습니다. 파일: {}, 건너뛴 행 수: {}", importedCount, stationFile, skippedCount);
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
                        .stationName(headers.getRequired(record, "\uBCF4\uAD00\uC18C(\uB300\uC5EC\uC18C)\uBA85", "\uB300\uC5EC\uC18C\uBA85"))
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
            throw new IllegalStateException("대여소 CSV 파일을 읽는 중 오류가 발생했습니다: " + stationFile, e);
        }

        if (!batch.isEmpty()) {
            stationMapper.insertAll(batch);
            importedCount += batch.size();
        }

        log.info("대여소 {}건을 적재했습니다. 파일: {}", importedCount, stationFile);
    }

    private void importUsageStats(List<Path> usageFiles, Charset charset, Set<String> knownStationCodes) {
        int totalImported = 0;

        for (Path usageFile : usageFiles) {
            int importedCount = 0;
            List<StationUsageStat> batch = new ArrayList<>(BATCH_SIZE);
            Map<String, Station> missingStations = new LinkedHashMap<>();

            try (BufferedReader reader = Files.newBufferedReader(usageFile, charset);
                 CSVParser parser = csvFormat().parse(reader)) {

                HeaderAccessor headers = HeaderAccessor.from(parser);
                for (CSVRecord record : parser) {
                    String stationCode = normalizeStationCode(headers.getRequired(record, HEADER_STATION_CODE));
                    String usageStationName = normalizeBlank(headers.getOptional(record, HEADER_STATION_NAME));
                    collectMissingStation(knownStationCodes, missingStations, stationCode, usageStationName);

                    batch.add(StationUsageStat.builder()
                            .statDate(parseDate(headers.getRequired(record, HEADER_STAT_DATE)))
                            .stationCode(stationCode)
                            .rentalType(headers.getOptional(record, HEADER_RENTAL_TYPE, HEADER_RENTAL_TYPE_CODE))
                            .gender(normalizeBlank(headers.getOptional(record, HEADER_GENDER)))
                            .ageGroup(normalizeBlank(headers.getOptional(record, HEADER_AGE_GROUP)))
                            .usageCount(parseInteger(headers.getRequired(record, HEADER_USAGE_COUNT)))
                            .exerciseAmount(parseDecimal(headers.getOptional(record, HEADER_EXERCISE_AMOUNT)))
                            .carbonAmount(parseDecimal(headers.getOptional(record, HEADER_CARBON_AMOUNT)))
                            .travelDistance(parseDecimal(headers.getOptional(record, HEADER_TRAVEL_DISTANCE, HEADER_TRAVEL_DISTANCE_M)))
                            .usageDurationMinutes(parseDecimal(headers.getOptional(record, HEADER_USAGE_DURATION, HEADER_USAGE_DURATION_MINUTES)))
                            .build());

                    if (batch.size() >= BATCH_SIZE) {
                        insertMissingStationsIfPresent(missingStations, knownStationCodes);
                        assignUsageIds(batch);
                        stationUsageStatMapper.insertAll(batch);
                        importedCount += batch.size();
                        batch.clear();
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("이용 통계 CSV 파일을 읽는 중 오류가 발생했습니다: " + usageFile, e);
            }

            if (!batch.isEmpty()) {
                insertMissingStationsIfPresent(missingStations, knownStationCodes);
                assignUsageIds(batch);
                stationUsageStatMapper.insertAll(batch);
                importedCount += batch.size();
            }

            totalImported += importedCount;
            log.info("이용 통계 {}건을 적재했습니다. 파일: {}", importedCount, usageFile.getFileName());
        }

        log.info("이용 통계 총 {}건을 {}개 파일에서 적재했습니다", totalImported, usageFiles.size());
    }

    private void collectMissingStation(Set<String> knownStationCodes, Map<String, Station> missingStations,
                                       String stationCode, String usageStationName) {
        if (knownStationCodes.contains(stationCode) || missingStations.containsKey(stationCode)) {
            return;
        }

        missingStations.put(stationCode, Station.builder()
                .stationCode(stationCode)
                .stationName(usageStationName != null ? usageStationName : "미확인 대여소 " + stationCode)
                .build());
    }

    private void insertMissingStationsIfPresent(Map<String, Station> missingStations, Set<String> knownStationCodes) {
        if (missingStations.isEmpty()) {
            return;
        }

        List<Station> stations = new ArrayList<>(missingStations.values());
        stationMapper.insertAll(stations);
        knownStationCodes.addAll(missingStations.keySet());
        log.warn("이용 통계에만 존재하는 대여소 {}건을 임시 대여소로 추가했습니다", stations.size());
        missingStations.clear();
    }

    private void assignUsageIds(List<StationUsageStat> usageStats) {
        List<Long> ids = stationUsageStatMapper.findNextIds(usageStats.size());
        if (ids.size() != usageStats.size()) {
            throw new IllegalStateException("이용 통계 시퀀스 ID를 할당하지 못했습니다");
        }

        for (int i = 0; i < usageStats.size(); i++) {
            usageStats.get(i).setId(ids.get(i));
        }
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

        throw new IllegalStateException("대여소 시트를 찾지 못했습니다: " + STATION_SHEET_NAME);
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

    private Path requireExistingFile(String pathValue, String label) {
        if (!StringUtils.hasText(pathValue)) {
            throw new IllegalStateException("CSV 적재용 " + label + " 경로가 설정되지 않았습니다");
        }
        Path path = Paths.get(pathValue).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("CSV 적재용 " + label + " 파일이 존재하지 않습니다: " + path);
        }
        return path;
    }

    private Path resolveStationSource(String pathValue) {
        if (!StringUtils.hasText(pathValue)) {
            throw new IllegalStateException("CSV 적재용 대여소 경로가 설정되지 않았습니다");
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
                        .orElseThrow(() -> new IllegalStateException("대여소 원본 파일을 찾지 못했습니다: " + path));
            } catch (IOException e) {
                throw new IllegalStateException("대여소 원본 파일 목록을 조회하는 중 오류가 발생했습니다: " + path, e);
            }
        }

        throw new IllegalStateException("CSV 적재용 대여소 경로가 존재하지 않습니다: " + path);
    }

    private Path requireExistingDirectory(String pathValue, String label) {
        if (!StringUtils.hasText(pathValue)) {
            throw new IllegalStateException("CSV 적재용 " + label + " 경로가 설정되지 않았습니다");
        }
        Path path = Paths.get(pathValue).toAbsolutePath().normalize();
        if (!Files.isDirectory(path)) {
            throw new IllegalStateException("CSV 적재용 " + label + " 디렉터리가 존재하지 않습니다: " + path);
        }
        return path;
    }

    private List<Path> listUsageFiles(Path usageDir) {
        try (var paths = Files.list(usageDir)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
            if (files.isEmpty()) {
                throw new IllegalStateException("이용 통계 CSV 파일을 찾지 못했습니다: " + usageDir);
            }
            return files;
        } catch (IOException e) {
            throw new IllegalStateException("이용 통계 CSV 파일 목록을 조회하는 중 오류가 발생했습니다: " + usageDir, e);
        }
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
            throw new IllegalStateException("대여소 번호는 필수입니다");
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

    private LocalDate parseDate(String rawValue) {
        try {
            return LocalDate.parse(rawValue.strip(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("날짜 값이 올바르지 않습니다: " + rawValue, e);
        }
    }

    private Integer parseInteger(String rawValue) {
        String normalized = normalizeNumberText(rawValue);
        if (normalized == null) {
            return 0;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("정수 값이 올바르지 않습니다: " + rawValue, e);
        }
    }

    private BigDecimal parseDecimal(String rawValue) {
        String normalized = normalizeNumberText(rawValue);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("소수 값이 올바르지 않습니다: " + rawValue, e);
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
                throw new IllegalStateException("필수 CSV 컬럼이 누락되었습니다: " + String.join(", ", candidates));
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
