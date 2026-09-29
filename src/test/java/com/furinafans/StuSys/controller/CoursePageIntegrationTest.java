package com.furinafans.stusys.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 对已经启动的服务发送真实 HTTP 请求，用配置中的 MySQL 原生查询核对课程分页。
 * 运行：mvn "-Dstusys.course.integration=true" "-Dtest=CoursePageIntegrationTest" test
 * 默认使用配置中的服务端口，可用 stusys.course.base-url 指定服务根地址。
 * 本测试提交 31 门唯一标识的临时课程，并在结束时按 ID、名称和代码共同定位清理。
 */
@Tag("integration")
@DisplayName("Course page 已启动服务与真实 MySQL 集成测试")
@EnabledIfSystemProperty(named = "stusys.course.integration", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl"
})
class CoursePageIntegrationTest {

    private static final String SELECT_COURSES = "SELECT id, name, code FROM courses";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private Environment environment;

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String marker = "ct" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private List<CourseRow> originalRows;
    private List<CourseRow> fixtureRows = List.of();
    private String coursesUrl;
    private int requestCount;

    @BeforeAll
    void prepareFixture() throws Exception {
        String defaultBaseUrl = "http://localhost:" + environment.getProperty("server.port", "8080")
                + environment.getProperty("server.servlet.context-path", "");
        coursesUrl = environment.getProperty("stusys.course.base-url", defaultBaseUrl).replaceAll("/+$", "")
                + "/courses";
        originalRows = readCourses(Map.of());
        assertFalse(originalRows.isEmpty(), "真实数据测试需要至少一门原有课程");
        // 写入前先核对 HTTP 和 JDBC 的完整数据集合，确认当前服务使用相同数据库。
        assertSequence(Map.of(), 100, originalRows, false);

        List<CourseSeed> seeds = new ArrayList<>();
        for (int index = 0; index < 24; index++) {
            String ordinal = String.format(Locale.ROOT, "%02d", index);
            String name = switch (index % 3) {
                case 0 -> marker + "数学" + ordinal;
                case 1 -> "数学" + marker + ordinal;
                default -> "数学" + ordinal + marker;
            };
            seeds.add(new CourseSeed(name, marker + "C" + ordinal));
        }
        seeds.add(new CourseSeed(marker + "O'Reilly", marker + "QUOTE"));
        seeds.add(new CourseSeed(marker + "-AB", marker + "DASH"));
        seeds.add(new CourseSeed(marker + "_AB", marker + "UNDER"));
        seeds.add(new CourseSeed(marker + "课程".repeat(5), marker + "LEN20"));
        String otherMarker = "dx" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        for (int index = 0; index < 3; index++) {
            seeds.add(new CourseSeed("干扰课程" + otherMarker + index, marker + "OTHER" + index));
        }

        // HTTP 使用独立连接，临时课程必须提交；事务内校验失败则整批回滚。
        fixtureRows = new TransactionTemplate(transactionManager).execute(status -> {
            for (CourseSeed seed : seeds) {
                assertEquals(0L, jdbc.queryForObject(
                        "SELECT COUNT(*) FROM courses WHERE name = ? OR code = ?", Long.class,
                        seed.name(), seed.code()), "临时课程不得与原有名称或代码冲突");
            }
            jdbc.batchUpdate("INSERT INTO courses (name, code) VALUES (?, ?)",
                    seeds.stream().map(seed -> new Object[] { seed.name(), seed.code() }).toList());
            String placeholders = String.join(",", Collections.nCopies(seeds.size(), "?"));
            List<CourseRow> inserted = jdbc.query(SELECT_COURSES + " WHERE code IN (" + placeholders
                            + ") ORDER BY id",
                    (row, number) -> new CourseRow(row.getLong("id"), row.getString("name"), row.getString("code")),
                    seeds.stream().map(CourseSeed::code).toArray());
            assertEquals(seeds.size(), inserted.size());
            assertEquals(seeds.stream().collect(Collectors.toMap(CourseSeed::code, CourseSeed::name)),
                    inserted.stream().collect(Collectors.toMap(CourseRow::code, CourseRow::name)));
            return inserted;
        });
        assertNotNull(fixtureRows);
        assertEquals(31, fixtureRows.size());
        System.out.printf("[Course page] endpoint=%s, originalRows=%d, fixtureRows=%d%n",
                coursesUrl, originalRows.size(), fixtureRows.size());
    }

    @AfterAll
    void cleanFixture() {
        if (fixtureRows == null || fixtureRows.isEmpty()) {
            return;
        }
        // 仅删除已捕获的本轮课程，不删除关联记录，也不重置数据库自增序列。
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            for (CourseRow row : fixtureRows) {
                assertEquals(1, jdbc.update("DELETE FROM courses WHERE id = ? AND name = ? AND code = ?",
                        row.id(), row.name(), row.code()), "每门临时课程必须准确清理");
            }
        });
        assertEquals(originalRows, readCourses(Map.of()), "清理后原有课程的 ID、名称、代码必须完全保持一致");
        System.out.printf("[Course page] HTTP requests=%d, removedFixtures=%d, originalRowsPreserved=%d%n",
                requestCount, fixtureRows.size(), originalRows.size());
    }

    @Test
    @DisplayName("不传参数时默认第 1 页、每页 10 条")
    void shouldApplyDefaultPagination() throws Exception {
        assertPage(Map.of(), readCourses(Map.of()));
    }

    @ParameterizedTest(name = "模糊筛选 pageSize={0}：全部页面无重复或遗漏")
    @ValueSource(ints = { 1, 2, 3, 7, 10, 16, 25, 100 })
    void shouldPageAllMatchingCourses(int pageSize) throws Exception {
        Map<String, String> query = Map.of("name", marker);
        List<CourseRow> rows = readCourses(query);
        assertEquals(28, rows.size(), "仅 28 门临时课程的名称包含筛选标识");
        assertSequence(query, pageSize, rows, false);
    }

    @Test
    @DisplayName("无筛选时同时覆盖原有课程和临时课程")
    void shouldPageOriginalAndFixtureCoursesTogether() throws Exception {
        List<CourseRow> rows = readCourses(Map.of());
        assertEquals(originalRows.size() + fixtureRows.size(), rows.size());
        assertSequence(Map.of(), 7, rows, false);
    }

    @ParameterizedTest(name = "名称匹配：{0}")
    @MethodSource("nameFilters")
    void shouldMatchNameAgainstRealSql(String description, String name) throws Exception {
        Map<String, String> query = Map.of("name", name);
        List<CourseRow> rows = readCourses(query);
        assertFalse(rows.isEmpty(), description + "必须匹配到真实数据");
        assertSequence(query, 3, rows, false);
    }

    Stream<Arguments> nameFilters() {
        return Stream.of(
                Arguments.of("关键字在名称开头", marker + "数学"),
                Arguments.of("关键字在名称中间", "数学" + marker),
                Arguments.of("关键字在名称末尾", "02" + marker),
                Arguments.of("中文子串", "数学"),
                Arguments.of("含单引号的名称", marker + "O'Reilly"),
                Arguments.of("单引号子串", "O'Reilly"),
                Arguments.of("名称恰好 20 个字符", marker + "课程".repeat(5)));
    }

    @Test
    @DisplayName("使用数据库中的原有课程名称及子串查询")
    void shouldQueryExistingCourseNames() throws Exception {
        for (CourseRow row : originalRows) {
            for (String name : List.of(row.name(), row.name().substring(0, Math.min(2, row.name().length())))) {
                Map<String, String> query = Map.of("name", name);
                List<CourseRow> expected = readCourses(query);
                assertTrue(expected.contains(row), "原有课程必须出现在其名称的匹配结果中");
                assertSequence(query, 7, expected, false);
            }
        }
    }

    @ParameterizedTest(name = "空白 name=[{0}] 不添加名称筛选")
    @ValueSource(strings = { "", " ", "   ", "\t", "　" })
    void shouldIgnoreEmptyAndBlankNames(String name) throws Exception {
        assertSequence(Map.of("name", name), 7, readCourses(Map.of()), false);
    }

    @Test
    @DisplayName("没有匹配的名称返回空分页")
    void shouldReturnEmptyPageForUnmatchedName() throws Exception {
        Map<String, String> query = Map.of("name", marker + "missing");
        assertEquals(List.of(), readCourses(query));
        assertPage(query, List.of());
    }

    @ParameterizedTest(name = "SQL 特殊字符作为查询数据：{0}")
    @ValueSource(strings = { "' OR 1=1 --", "x' OR '1'='1", "'; DROP TABLE x--" })
    void shouldTreatSqlMetacharactersAsData(String name) throws Exception {
        Map<String, String> query = Map.of("name", name);
        assertEquals(List.of(), readCourses(query));
        assertPage(query, List.of());
    }

    @ParameterizedTest(name = "LIKE 通配符及数据库排序规则：{0}")
    @MethodSource("sqlLikeFilters")
    void shouldFollowDatabaseLikeSemantics(String name) throws Exception {
        // 当前 LIKE 保留 %、_ 通配符；大小写匹配由 MySQL 字段的排序规则决定。
        Map<String, String> query = Map.of("name", name);
        assertSequence(query, 7, readCourses(query), false);
    }

    Stream<String> sqlLikeFilters() {
        return Stream.of("%", "_", marker + "%", marker + "_AB", marker.toUpperCase(Locale.ROOT));
    }

    @Test
    @DisplayName("倒序请求各页时仍没有重复或遗漏")
    void shouldSupportReversePageRequests() throws Exception {
        Map<String, String> query = Map.of("name", marker);
        assertSequence(query, 7, readCourses(query), true);
    }

    @Test
    @DisplayName("同一页重复请求返回相同记录")
    void shouldReturnIdenticalPagesOnRepeatedRequests() throws Exception {
        Map<String, String> base = Map.of("name", marker);
        List<CourseRow> rows = readCourses(base);
        for (int page : List.of(1, 2, 4)) {
            Map<String, String> query = pageQuery(base, page, 7);
            List<CourseRow> first = assertPage(query, rows);
            assertEquals(first, assertPage(query, rows));
        }
    }

    @Test
    @DisplayName("Long 最大页码返回空页且保留总数")
    void shouldHandleMaximumPageNumber() throws Exception {
        Map<String, String> base = Map.of("name", marker);
        assertPage(pageQuery(base, Long.MAX_VALUE, 3), readCourses(base));
    }

    @ParameterizedTest(name = "非法参数 {0}=[{1}] 返回业务 code=400")
    @MethodSource("invalidParameters")
    void shouldRejectInvalidParameters(String field, String value) throws Exception {
        Map<String, String> query = new LinkedHashMap<>(Map.of("name", marker));
        query.put(field, value);
        JsonNode response = request(query);
        assertEquals(400, response.path("code").asInt(), query + " -> " + response);
        assertFalse(response.path("msg").asString().isBlank(), "参数校验失败应返回具体原因");
    }

    static Stream<Arguments> invalidParameters() {
        List<Arguments> arguments = new ArrayList<>();
        for (String field : List.of("pageNum", "pageSize")) {
            for (String value : List.of("0", "-1", "", "abc", "1.5", "9223372036854775808")) {
                arguments.add(Arguments.of(field, value));
            }
        }
        for (String value : List.of("101", "201", Long.toString(Long.MAX_VALUE))) {
            arguments.add(Arguments.of("pageSize", value));
        }
        arguments.add(Arguments.of("name", "课".repeat(21)));
        arguments.add(Arguments.of("name", " ".repeat(21)));
        return arguments.stream();
    }

    private void assertSequence(Map<String, String> base, int size, List<CourseRow> expected, boolean reverse)
            throws Exception {
        int pages = (expected.size() + size - 1) / size;
        List<Integer> pageNumbers = new ArrayList<>(IntStream.rangeClosed(1, pages).boxed().toList());
        if (reverse) {
            Collections.reverse(pageNumbers);
        }
        List<CourseRow> actual = new ArrayList<>();
        for (int page : pageNumbers) {
            actual.addAll(assertPage(pageQuery(base, page, size), expected));
        }
        assertTrue(assertPage(pageQuery(base, pages + 1L, size), expected).isEmpty());
        assertEquals(expected.size(), actual.size(), "分页记录总数必须与原生 SQL 一致");
        assertEquals(actual.size(), actual.stream().map(CourseRow::id).distinct().count(), "跨页不得重复");
        // Course 接口未声明排序规则，按 ID 集合和完整字段核对，不假定返回顺序。
        assertEquals(expected.stream().collect(Collectors.toMap(CourseRow::id, Function.identity())),
                actual.stream().collect(Collectors.toMap(CourseRow::id, Function.identity())), "跨页不得遗漏或混入其他课程");
    }

    private List<CourseRow> assertPage(Map<String, String> query, List<CourseRow> expected) throws Exception {
        long current = Long.parseLong(query.getOrDefault("pageNum", "1"));
        int size = Integer.parseInt(query.getOrDefault("pageSize", "10"));
        int pages = (expected.size() + size - 1) / size;
        // 先比较末页，避免测试自身计算超大页码的偏移量时溢出。
        int count = current > pages ? 0 : (int) Math.min(size, expected.size() - (current - 1) * size);
        JsonNode response = request(query);
        assertEquals(200, response.path("code").asInt(), query + " -> " + response);
        JsonNode page = response.path("data");
        assertEquals(expected.size(), page.path("total").asLong(), query.toString());
        assertEquals(current, page.path("current").asLong());
        assertEquals(size, page.path("size").asInt());
        assertEquals(pages, page.path("pages").asInt());
        JsonNode records = page.path("records");
        assertTrue(records.isArray());
        assertEquals(count, records.size());
        Map<Long, CourseRow> expectedById = expected.stream()
                .collect(Collectors.toMap(CourseRow::id, Function.identity()));
        List<CourseRow> actual = new ArrayList<>();
        for (JsonNode record : records) {
            CourseRow row = new CourseRow(record.path("id").asLong(), record.path("name").asString(),
                    record.path("code").asString());
            assertEquals(expectedById.get(row.id()), row, "响应中的 ID、名称、代码必须与真实数据库一致");
            actual.add(row);
        }
        assertEquals(actual.size(), actual.stream().map(CourseRow::id).distinct().count(), "页内不得重复");
        return actual;
    }

    private List<CourseRow> readCourses(Map<String, String> query) {
        String name = query.get("name");
        boolean filtered = name != null && !name.isBlank();
        // 使用 JDBC 参数化 SQL 作为独立预期值，保留数据库原生 LIKE 和排序规则语义。
        return jdbc.query(SELECT_COURSES + (filtered ? " WHERE name LIKE ?" : "") + " ORDER BY id",
                (row, number) -> new CourseRow(row.getLong("id"), row.getString("name"), row.getString("code")),
                filtered ? new Object[] { "%" + name + "%" } : new Object[0]);
    }

    private JsonNode request(Map<String, String> query) throws Exception {
        String parameters = query.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        URI uri = URI.create(coursesUrl + (parameters.isEmpty() ? "" : "?" + parameters));
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        requestCount++;
        assertEquals(200, response.statusCode(), "接口使用响应体的业务 code 表示校验结果：" + response.body());
        return json.readTree(response.body());
    }

    private Map<String, String> pageQuery(Map<String, String> base, long page, int size) {
        Map<String, String> query = new LinkedHashMap<>(base);
        query.put("pageNum", Long.toString(page));
        query.put("pageSize", Integer.toString(size));
        return query;
    }

    private record CourseSeed(String name, String code) {
    }

    private record CourseRow(long id, String name, String code) {
    }
}
