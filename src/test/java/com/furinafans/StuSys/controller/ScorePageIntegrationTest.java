package com.furinafans.stusys.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 使用配置中的真实 MySQL 和随机 HTTP 端口验证成绩分页。
 * 运行：mvn -Dstusys.score.integration=true -Dtest=ScorePageIntegrationTest test
 * 需要至少两名现有学生及两门课程；只插入并清理带本轮唯一标识的临时成绩。
 */
@Tag("integration")
@DisplayName("Score page 真实数据库集成测试")
@EnabledIfSystemProperty(named = "stusys.score.integration", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl"
})
class ScorePageIntegrationTest {

    private static final List<String> FILTER_FIELDS = List.of("academicYear", "term", "examType", "courseId");
    private static final String SELECT_SCORES = """
            SELECT id, score, course_id, student_id, academic_year, term, exam_type
            FROM scores ORDER BY id
            """;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper json;

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String marker = "st" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private final List<String> examTypes = List.of(marker + "-mid", marker + "-end", marker + "-ret");
    private List<ScoreRow> originalRows;
    private List<ScoreRow> expectedRows;
    private List<Long> fixtureIds = List.of();
    private long primaryStudent;
    private long secondaryStudent;
    private boolean fixtureInsertStarted;
    private ScoreRow sample;

    @BeforeAll
    void prepareFixture() {
        List<Long> students = jdbc.queryForList("SELECT id FROM students ORDER BY id LIMIT 2", Long.class);
        List<Long> courses = jdbc.queryForList("SELECT id FROM courses ORDER BY id LIMIT 2", Long.class);
        assumeTrue(students.size() == 2 && courses.size() == 2, "真实数据库需要至少两名学生和两门课程");
        primaryStudent = students.get(0);
        secondaryStudent = students.get(1);
        originalRows = readScores();
        assertEquals(0L, countFixtures(), "临时数据标识不得与现有数据冲突");

        List<Object[]> values = new ArrayList<>();
        int ordinal = 0;
        for (long student : List.of(primaryStudent, secondaryStudent)) {
            int yearCount = student == primaryStudent ? 12 : 3;
            for (int offset = 0; offset < yearCount; offset++) {
                String year = (2031 + offset) + "-" + (2032 + offset);
                for (String term : List.of("1", "2")) {
                    for (long course : courses) {
                        for (String exam : examTypes) {
                            BigDecimal score = ordinal % 29 == 0 ? null
                                    : ordinal == 1 ? new BigDecimal("0.00")
                                    : ordinal == 2 ? new BigDecimal("100.00")
                                    : BigDecimal.valueOf((ordinal * 37L) % 10000, 2);
                            values.add(new Object[] { score, course, student, year, term, exam });
                            ordinal++;
                        }
                    }
                }
            }
        }
        // 交错插入两名学生的记录，使 ID 顺序与唯一索引、筛选字段的顺序不同。
        Collections.shuffle(values, new Random(928));
        fixtureInsertStarted = true;
        // HTTP 请求使用另一条连接，因此测试数据需提交，不能只留在测试方法的事务里。
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> jdbc.batchUpdate("""
                INSERT INTO scores (score, course_id, student_id, academic_year, term, exam_type)
                VALUES (?, ?, ?, ?, ?, ?)
                """, values));
        expectedRows = readScores();
        List<ScoreRow> fixtures = expectedRows.stream().filter(this::isFixture).toList();
        fixtureIds = fixtures.stream().map(ScoreRow::id).toList();
        assertEquals(180, fixtureIds.size(), "本轮应插入 180 条临时成绩");
        sample = fixtures.stream().filter(row -> row.studentId() == primaryStudent).findFirst().orElseThrow();
    }

    @AfterAll
    void cleanFixture() {
        if (!fixtureInsertStarted) {
            return;
        }
        // 优先按唯一标识和捕获的 ID 双重定位；准备阶段失败时仍可按预先校验过的标识清理。
        List<Object> parameters = markerParameters();
        String idRestriction = "";
        if (!fixtureIds.isEmpty()) {
            idRestriction = " AND id IN (" + String.join(",", Collections.nCopies(fixtureIds.size(), "?")) + ")";
            parameters.addAll(fixtureIds);
        }
        jdbc.update("DELETE FROM scores WHERE " + markerPredicate() + idRestriction, parameters.toArray());
        assertEquals(0L, countFixtures(), "临时数据必须全部清理");
        // 只核对原有记录，不重置自增序列，也不覆盖测试期间可能发生的外部修改。
        assertEquals(originalRows, readScores(), "清理后数据库成绩应与测试前完全一致");
    }

    @ParameterizedTest(name = "主学生 pageSize={0}：逐页排序且无重复遗漏")
    @ValueSource(ints = { 1, 2, 3, 7, 10, 16, 25, 50, 72, 100 })
    void shouldPreserveAscendingOrderAcrossPages(int pageSize) throws Exception {
        assertSequence(primaryQuery(), pageSize, false);
    }

    @ParameterizedTest(name = "第二名学生 pageSize={0}：仅返回本人的成绩")
    @ValueSource(ints = { 1, 7, 100 })
    void shouldIsolateStudentsAcrossPages(int pageSize) throws Exception {
        assertSequence(Map.of("studentId", Long.toString(secondaryStudent)), pageSize, false);
    }

    @ParameterizedTest(name = "筛选组合 {0}：检查所有数据页")
    @MethodSource("filterMasks")
    void shouldPageEveryOptionalFilterCombination(int mask) throws Exception {
        Map<String, String> query = primaryQuery();
        Map<String, String> selectedFilters = Map.of(
                "academicYear", sample.academicYear(), "term", sample.term(),
                "examType", sample.examType(), "courseId", Long.toString(sample.courseId()));
        for (int bit = 0; bit < FILTER_FIELDS.size(); bit++) {
            if ((mask & (1 << bit)) != 0) {
                String field = FILTER_FIELDS.get(bit);
                query.put(field, selectedFilters.get(field));
            }
        }
        assertSequence(query, 7, false);
    }

    static IntStream filterMasks() {
        return IntStream.range(1, 16);
    }

    @Test
    void shouldKeepOrderWhenPagesAreRequestedInReverse() throws Exception {
        assertSequence(primaryQuery(), 7, true);
    }

    @Test
    void shouldReturnIdenticalPagesOnRepeatedRequests() throws Exception {
        List<ScoreRow> rows = matchingRows(primaryQuery());
        int lastPage = (rows.size() + 6) / 7;
        for (int page : List.of(1, (lastPage + 1) / 2, lastPage)) {
            Map<String, String> query = pageQuery(primaryQuery(), page, 7);
            List<Long> firstIds = assertPage(query, rows);
            assertEquals(firstIds, assertPage(query, rows));
            assertEquals(firstIds, assertPage(query, rows));
        }
    }

    @Test
    void shouldApplyDefaultPagination() throws Exception {
        assertPage(primaryQuery(), matchingRows(primaryQuery()));
    }

    @Test
    void shouldReturnEmptyPageForMaximumLongPageNumber() throws Exception {
        assertPage(pageQuery(primaryQuery(), Long.MAX_VALUE, 3), matchingRows(primaryQuery()));
    }

    @Test
    void shouldIgnoreEmptyAndBlankOptionalFilters() throws Exception {
        Map<String, String> query = primaryQuery();
        query.put("academicYear", "");
        query.put("term", "   ");
        query.put("examType", "");
        query.put("courseId", "");
        assertSequence(query, 7, false);
    }

    @ParameterizedTest
    @ValueSource(strings = { "academicYear", "term", "examType", "courseId" })
    void shouldReturnEmptyPageForUnmatchedFilter(String field) throws Exception {
        Map<String, String> query = primaryQuery();
        String value = field.equals("courseId")
                ? Long.toString(expectedRows.stream().mapToLong(ScoreRow::courseId).max().orElse(0) + 1)
                : "unmatched-range-test";
        query.put(field, value);
        assertPage(query, List.of());
    }

    @Test
    void shouldReturnEmptyPageForAbsentStudent() throws Exception {
        long absentStudent = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM students", Long.class);
        assertPage(Map.of("studentId", Long.toString(absentStudent)), List.of());
    }

    @Test
    void shouldTreatSqlMetacharactersAsFilterData() throws Exception {
        Map<String, String> query = primaryQuery();
        query.put("academicYear", "' OR 1=1 --");
        assertPage(query, List.of());
    }

    @Test
    void shouldRejectMissingStudentId() throws Exception {
        assertEquals(400, request(Map.of()).path("code").asInt());
    }

    @ParameterizedTest(name = "非法参数 {0}={1} 返回业务 code=400")
    @MethodSource("invalidParameters")
    void shouldRejectInvalidParameters(String field, String value) throws Exception {
        Map<String, String> query = primaryQuery();
        query.put(field, value);
        assertEquals(400, request(query).path("code").asInt());
    }

    static Stream<Arguments> invalidParameters() {
        List<Arguments> arguments = new ArrayList<>();
        for (String field : List.of("studentId", "pageNum", "pageSize", "courseId")) {
            arguments.add(Arguments.of(field, "abc"));
            arguments.add(Arguments.of(field, "9223372036854775808"));
        }
        for (String field : List.of("studentId", "pageNum", "pageSize")) {
            arguments.add(Arguments.of(field, ""));
        }
        for (String field : List.of("pageNum", "pageSize")) {
            arguments.add(Arguments.of(field, "0"));
            arguments.add(Arguments.of(field, "-1"));
        }
        arguments.add(Arguments.of("pageSize", "101"));
        return arguments.stream();
    }

    private void assertSequence(Map<String, String> baseQuery, int pageSize, boolean reverse) throws Exception {
        List<ScoreRow> rows = matchingRows(baseQuery);
        int pages = (rows.size() + pageSize - 1) / pageSize;
        List<Integer> pageNumbers = new ArrayList<>(IntStream.rangeClosed(1, pages).boxed().toList());
        if (reverse) {
            Collections.reverse(pageNumbers);
        }
        Map<Integer, List<Long>> visitedPages = new TreeMap<>();
        for (int page : pageNumbers) {
            visitedPages.put(page, assertPage(pageQuery(baseQuery, page, pageSize), rows));
        }
        assertTrue(assertPage(pageQuery(baseQuery, pages + 1L, pageSize), rows).isEmpty());
        List<Long> allIds = visitedPages.values().stream().flatMap(List::stream).toList();
        assertEquals(rows.stream().map(ScoreRow::id).toList(), allIds, "拼接后的各页必须与数据库 ID 升序序列一致");
        assertEquals(allIds.size(), allIds.stream().distinct().count(), "跨页不得出现重复记录");
    }

    private List<Long> assertPage(Map<String, String> query, List<ScoreRow> rows) throws Exception {
        long current = Long.parseLong(query.getOrDefault("pageNum", "1"));
        int size = Integer.parseInt(query.getOrDefault("pageSize", "10"));
        int pages = (rows.size() + size - 1) / size;
        // 超大页码先判断是否超过末页，避免测试自身计算偏移量时溢出。
        List<ScoreRow> expectedPage = current > pages ? List.of()
                : rows.subList((int) ((current - 1) * size), (int) Math.min(current * size, rows.size()));
        JsonNode response = request(query);
        assertEquals(200, response.path("code").asInt(), query.toString());
        JsonNode page = response.path("data");
        assertEquals(rows.size(), page.path("total").asLong());
        assertEquals(current, page.path("current").asLong());
        assertEquals(size, page.path("size").asInt());
        assertEquals(pages, page.path("pages").asInt());
        JsonNode records = page.path("records");
        assertTrue(records.isArray());
        assertEquals(expectedPage.size(), records.size());
        List<Long> ids = new ArrayList<>();
        for (int index = 0; index < expectedPage.size(); index++) {
            ScoreRow expected = expectedPage.get(index);
            JsonNode actual = records.get(index);
            ids.add(actual.path("id").asLong());
            assertEquals(expected.id(), actual.path("id").asLong());
            assertEquals(expected.studentId(), actual.path("studentId").asLong());
            assertEquals(expected.courseId(), actual.path("courseId").asLong());
            assertEquals(expected.academicYear(), actual.path("academicYear").asString());
            assertEquals(expected.term(), actual.path("term").asString());
            assertEquals(expected.examType(), actual.path("examType").asString());
            JsonNode score = actual.get("score");
            assertNotNull(score, "score 字段必须返回，包括 NULL 分数");
            if (expected.score() == null) {
                assertTrue(score.isNull());
            } else {
                assertEquals(0, expected.score().compareTo(score.decimalValue()), "分数值必须与数据库一致");
            }
        }
        assertEquals(expectedPage.stream().map(ScoreRow::id).toList(), ids, "页内记录必须按唯一 ID 升序排列");
        return ids;
    }

    private JsonNode request(Map<String, String> query) throws Exception {
        String parameters = query.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .reduce((left, right) -> left + "&" + right).orElse("");
        URI uri = URI.create("http://localhost:" + port + "/scores" + (parameters.isEmpty() ? "" : "?" + parameters));
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode(), "当前接口使用响应体的业务 code 表示参数错误");
        return json.readTree(response.body());
    }

    private List<ScoreRow> matchingRows(Map<String, String> query) {
        long student = Long.parseLong(query.get("studentId"));
        return expectedRows.stream().filter(row -> row.studentId() == student)
                .filter(row -> matches(query, "academicYear", row.academicYear()))
                .filter(row -> matches(query, "term", row.term()))
                .filter(row -> matches(query, "examType", row.examType()))
                .filter(row -> matches(query, "courseId", Long.toString(row.courseId())))
                .toList();
    }

    private boolean matches(Map<String, String> query, String field, String value) {
        String filter = query.get(field);
        return filter == null || filter.isBlank() || filter.equals(value);
    }

    private Map<String, String> primaryQuery() {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("studentId", Long.toString(primaryStudent));
        return query;
    }

    private Map<String, String> pageQuery(Map<String, String> base, long page, int size) {
        Map<String, String> query = new LinkedHashMap<>(base);
        query.put("pageNum", Long.toString(page));
        query.put("pageSize", Integer.toString(size));
        return query;
    }

    private List<ScoreRow> readScores() {
        return jdbc.query(SELECT_SCORES, (row, number) -> new ScoreRow(row.getLong("id"), row.getBigDecimal("score"),
                row.getLong("course_id"), row.getLong("student_id"), row.getString("academic_year"),
                row.getString("term"), row.getString("exam_type")));
    }

    private boolean isFixture(ScoreRow row) {
        return examTypes.contains(row.examType())
                && (row.studentId() == primaryStudent || row.studentId() == secondaryStudent);
    }

    private String markerPredicate() {
        return "exam_type IN (?, ?, ?) AND student_id IN (?, ?)";
    }

    private List<Object> markerParameters() {
        List<Object> parameters = new ArrayList<>(examTypes);
        parameters.add(primaryStudent);
        parameters.add(secondaryStudent);
        return parameters;
    }

    private long countFixtures() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM scores WHERE " + markerPredicate(), Long.class,
                markerParameters().toArray());
    }

    private record ScoreRow(long id, BigDecimal score, long courseId, long studentId,
            String academicYear, String term, String examType) {
    }
}
